package com.eduframepackage.eduframe.controller;

import com.eduframepackage.eduframe.dto.TicketDTO;
import com.eduframepackage.eduframe.dto.TicketReceiptDTO;
import com.eduframepackage.eduframe.dto.TicketStatsDTO;
import com.eduframepackage.eduframe.dto.TicketStatusUpdateDTO;
import com.eduframepackage.eduframe.model.Ticket;
import com.eduframepackage.eduframe.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h1>UC-05 Submit Support Ticket - REST Controller</h1>
 * 
 * <p>
 * REST API Boundary Controller handling client HTTP requests for UC-05 "Submit Support Ticket".
 * Exposes endpoints for ticket creation with file attachment, ticket receipt lookup, 
 * administrative ticket listing, lifecycle updates, stats calculation, and attachment downloads.
 * </p>
 * 
 * <h2>API Endpoint Summary:</h2>
 * <ul>
 *   <li>{@code POST /api/tickets/submit}: Processes support ticket creation (accepts multipart/form-data or JSON).</li>
 *   <li>{@code GET /api/tickets/{ticketId}}: Fetches eTicketReceipt for a specific ticket ID (e.g. TKT-1001).</li>
 *   <li>{@code GET /api/tickets}: Returns submitted support tickets, supporting search, status, and category filters.</li>
 *   <li>{@code PUT /api/tickets/{ticketId}}: Updates ticket status, staff resolution remarks, and priority.</li>
 *   <li>{@code DELETE /api/tickets/{ticketId}}: Removes ticket and any associated file attachment.</li>
 *   <li>{@code GET /api/tickets/{ticketId}/attachment}: Streams file attachment download.</li>
 *   <li>{@code GET /api/tickets/stats}: Returns summary metrics for Help Desk KPI dashboard.</li>
 * </ul>
 * 
 * @author EduFrame Group B4G1-06 (SE2030)
 * @version 2.0
 */
@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;

    @Autowired
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Handles support ticket submission (UC-05).
     * Accepts multipart/form-data containing subject, description, metadata, and optional file attachment.
     * 
     * @param subject      the subject title of the support request
     * @param description  the detailed description body
     * @param studentId    optional student ID
     * @param studentEmail optional student contact email
     * @param category     optional category
     * @param priority     optional priority
     * @param file         optional attached file (max 5MB)
     * @return {@link ResponseEntity} containing {@link TicketReceiptDTO} (eTicketReceipt)
     */
    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submitTicketMultipart(
            @RequestParam("subject") String subject,
            @RequestParam("description") String description,
            @RequestParam(value = "studentId", required = false) String studentId,
            @RequestParam(value = "studentEmail", required = false) String studentEmail,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            TicketDTO dto = new TicketDTO(subject, description, studentId, studentEmail, category, priority);
            TicketReceiptDTO receipt = ticketService.submitSupportTicket(dto, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(receipt);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Validation Error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Duplicate Ticket");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Server Error");
            error.put("message", "An unexpected error occurred while processing your request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Alternative JSON submission endpoint for support ticket creation (without file upload).
     * 
     * @param dto the ticket request DTO payload
     * @return {@link ResponseEntity} containing {@link TicketReceiptDTO}
     */
    @PostMapping(value = "/submit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> submitTicketJson(@RequestBody TicketDTO dto) {
        try {
            TicketReceiptDTO receipt = ticketService.submitSupportTicket(dto, null);
            return ResponseEntity.status(HttpStatus.CREATED).body(receipt);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Validation Error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Duplicate Ticket");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Server Error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Retrieves eTicketReceipt details by ticket identifier (e.g. TKT-1001).
     * 
     * @param ticketId formatted ticket ID
     * @return {@link ResponseEntity} with ticket receipt data
     */
    @GetMapping("/{ticketId}")
    public ResponseEntity<?> getTicketReceipt(@PathVariable("ticketId") String ticketId) {
        try {
            TicketReceiptDTO receipt = ticketService.getTicketReceipt(ticketId);
            return ResponseEntity.ok(receipt);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Not Found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Retrieves tickets with optional query keyword, status, and category filtering.
     * 
     * @param search   optional search query
     * @param status   optional status filter
     * @param category optional category filter
     * @return list of matching {@link Ticket} entities
     */
    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "category", required = false) String category) {
        List<Ticket> tickets = ticketService.getTicketsFiltered(search, status, category);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Updates ticket status, staff resolution remarks, priority, and assignment.
     * 
     * @param ticketId  formatted ticket ID
     * @param updateDTO update payload
     * @return updated {@link TicketReceiptDTO}
     */
    @PutMapping("/{ticketId}")
    public ResponseEntity<?> updateTicketStatus(
            @PathVariable("ticketId") String ticketId,
            @RequestBody TicketStatusUpdateDTO updateDTO) {
        try {
            TicketReceiptDTO updatedReceipt = ticketService.updateTicketStatus(ticketId, updateDTO);
            return ResponseEntity.ok(updatedReceipt);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Update Error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Server Error");
            error.put("message", "Error updating ticket: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Deletes a support ticket by ticket ID.
     * 
     * @param ticketId formatted ticket ID
     * @return deletion confirmation
     */
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<?> deleteTicket(@PathVariable("ticketId") String ticketId) {
        try {
            ticketService.deleteTicket(ticketId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Ticket " + ticketId + " has been successfully deleted.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Not Found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Server Error");
            error.put("message", "Error deleting ticket: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Streams file attachment for download or preview.
     * 
     * @param ticketId formatted ticket ID
     * @return {@link ResponseEntity} streaming resource
     */
    @GetMapping("/{ticketId}/attachment")
    public ResponseEntity<?> downloadAttachment(@PathVariable("ticketId") String ticketId) {
        try {
            Resource resource = ticketService.getAttachmentResource(ticketId);
            TicketReceiptDTO ticket = ticketService.getTicketReceipt(ticketId);

            String contentType = null;
            try {
                contentType = Files.probeContentType(Paths.get(resource.getFile().getAbsolutePath()));
            } catch (IOException ex) {
                contentType = "application/octet-stream";
            }
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + ticket.getAttachmentName() + "\"")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Not Found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Server Error");
            error.put("message", "Could not stream attachment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Retrieves KPI metrics for the Help Desk Console.
     * 
     * @return {@link TicketStatsDTO}
     */
    @GetMapping("/stats")
    public ResponseEntity<TicketStatsDTO> getTicketStats() {
        TicketStatsDTO stats = ticketService.getTicketStats();
        return ResponseEntity.ok(stats);
    }
}
