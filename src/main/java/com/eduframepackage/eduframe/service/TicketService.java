package com.eduframepackage.eduframe.service;

import com.eduframepackage.eduframe.dto.TicketDTO;
import com.eduframepackage.eduframe.dto.TicketReceiptDTO;
import com.eduframepackage.eduframe.dto.TicketStatsDTO;
import com.eduframepackage.eduframe.dto.TicketStatusUpdateDTO;
import com.eduframepackage.eduframe.model.Ticket;
import com.eduframepackage.eduframe.repository.TicketRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <h1>UC-05 Submit Support Ticket - Service Control Layer</h1>
 * 
 * <p>
 * Business logic service component encapsulating the operational flow of UC-05.
 * Coordinates input validation, duplicate ticket checking, unique Ticket ID sequence generation,
 * default status assignment, physical file attachment storage, ticket lifecycle tracking,
 * Help Desk statistics calculation, and persistence via {@link TicketRepository}.
 * </p>
 * 
 * @author EduFrame Group B4G1-06 (SE2030)
 * @version 2.0
 */
@Service
public class TicketService {

    private static final String TICKET_PREFIX = "TKT-";
    private static final long INITIAL_TICKET_NUMBER = 1001L;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final TicketRepository ticketRepository;
    private final Path uploadStorageLocation;

    @Autowired
    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
        this.uploadStorageLocation = Paths.get("uploads", "tickets").toAbsolutePath().normalize();
    }

    @PostConstruct
    public void initStorage() {
        try {
            Files.createDirectories(this.uploadStorageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload storage location for tickets.", e);
        }
    }

    /**
     * Processes a support ticket submission request (UC-05).
     * 
     * @param dto  the ticket input DTO containing subject, description, and metadata
     * @param file optional uploaded file attachment
     * @return {@link TicketReceiptDTO} digital eTicketReceipt confirming creation
     * @throws IllegalArgumentException if validation fails or input is missing
     * @throws IllegalStateException    if a duplicate ticket is detected
     */
    @Transactional
    public TicketReceiptDTO submitSupportTicket(TicketDTO dto, MultipartFile file) {
        // 1. Validate Input Data
        validateTicketInput(dto);

        // 2. Perform Duplicate Submission Check
        checkForDuplicates(dto.getSubject(), dto.getDescription());

        // 3. Generate Unique Sequential Ticket ID (e.g. TKT-1001)
        String generatedTicketId = generateUniqueTicketId();

        // 4. Extract and Physically Store Optional File Attachment
        String attachmentName = null;
        String attachmentType = null;
        String attachmentPath = null;
        Long attachmentSize = null;

        if (file != null && !file.isEmpty()) {
            attachmentName = StringUtils.cleanPath(file.getOriginalFilename());
            attachmentType = file.getContentType();
            attachmentSize = file.getSize();
            attachmentPath = storeAttachment(generatedTicketId, file);
        }

        // 5. Instantiate Ticket JPA Entity
        Ticket ticket = new Ticket(
                generatedTicketId,
                dto.getSubject().trim(),
                dto.getDescription().trim(),
                dto.getStudentId() != null ? dto.getStudentId().trim() : null,
                dto.getStudentEmail() != null ? dto.getStudentEmail().trim() : null,
                dto.getCategory(),
                dto.getPriority(),
                attachmentName,
                attachmentType,
                attachmentPath,
                attachmentSize
        );
        ticket.setStatus("Open");

        // 6. Persist Entity to Repository
        Ticket savedTicket = ticketRepository.save(ticket);

        // 7. Build and Return eTicketReceipt DTO
        return convertToReceiptDTO(savedTicket, "Your support ticket has been submitted successfully! Please retain your Ticket ID for tracking.");
    }

    /**
     * Stores uploaded attachment safely on local disk under uploads/tickets/{ticketId}/.
     */
    private String storeAttachment(String ticketId, MultipartFile file) {
        try {
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            if (originalFileName.contains("..")) {
                throw new IllegalArgumentException("Invalid file path sequence in file name: " + originalFileName);
            }

            Path ticketDir = this.uploadStorageLocation.resolve(ticketId);
            Files.createDirectories(ticketDir);

            Path targetPath = ticketDir.resolve(originalFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return targetPath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store ticket file attachment: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves stored file attachment as a downloadable Spring Resource.
     * 
     * @param ticketId formatted ticket ID
     * @return {@link Resource} file resource
     */
    public Resource getAttachmentResource(String ticketId) {
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));

        if (ticket.getAttachmentPath() == null || ticket.getAttachmentPath().trim().isEmpty()) {
            throw new IllegalArgumentException("Ticket has no attachment file: " + ticketId);
        }

        try {
            Path filePath = Paths.get(ticket.getAttachmentPath());
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IllegalArgumentException("Attachment file not found or unreadable on server: " + ticket.getAttachmentName());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error resolving attachment file URL", e);
        }
    }

    /**
     * Validates Subject, Description, and optional Email requirements for UC-05.
     * 
     * @param dto ticket DTO to validate
     */
    private void validateTicketInput(TicketDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Ticket data must not be empty.");
        }
        if (dto.getSubject() == null || dto.getSubject().trim().isEmpty()) {
            throw new IllegalArgumentException("Subject is required and cannot be blank.");
        }
        if (dto.getSubject().trim().length() < 5) {
            throw new IllegalArgumentException("Subject must be at least 5 characters long.");
        }
        if (dto.getSubject().trim().length() > 200) {
            throw new IllegalArgumentException("Subject cannot exceed 200 characters.");
        }
        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required and cannot be blank.");
        }
        if (dto.getDescription().trim().length() < 10) {
            throw new IllegalArgumentException("Description must be at least 10 characters long.");
        }
        if (dto.getDescription().trim().length() > 2000) {
            throw new IllegalArgumentException("Description cannot exceed 2000 characters.");
        }
        if (dto.getStudentEmail() != null && !dto.getStudentEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(dto.getStudentEmail().trim()).matches()) {
                throw new IllegalArgumentException("Please provide a valid contact email address.");
            }
        }
    }

    /**
     * Checks if an open ticket with matching subject and description already exists.
     * 
     * @param subject     subject title
     * @param description description text
     */
    private void checkForDuplicates(String subject, String description) {
        boolean duplicateExists = ticketRepository.existsBySubjectIgnoreCaseAndDescriptionIgnoreCaseAndStatus(
                subject.trim(),
                description.trim(),
                "Open"
        );
        if (duplicateExists) {
            throw new IllegalStateException("Duplicate ticket detected! An open support ticket with this identical subject and description has already been submitted.");
        }
    }

    /**
     * Generates a unique sequential ticket code starting at TKT-1001.
     * 
     * @return formatted unique ticket ID string (e.g., TKT-1001)
     */
    public synchronized String generateUniqueTicketId() {
        Optional<Ticket> latestTicket = ticketRepository.findTopByOrderByIdDesc();
        long nextNumber = INITIAL_TICKET_NUMBER;

        if (latestTicket.isPresent()) {
            String lastTicketId = latestTicket.get().getTicketId();
            if (lastTicketId != null && lastTicketId.startsWith(TICKET_PREFIX)) {
                try {
                    long currentNum = Long.parseLong(lastTicketId.substring(TICKET_PREFIX.length()));
                    nextNumber = Math.max(currentNum + 1, INITIAL_TICKET_NUMBER);
                } catch (NumberFormatException e) {
                    nextNumber = ticketRepository.count() + INITIAL_TICKET_NUMBER;
                }
            } else {
                nextNumber = ticketRepository.count() + INITIAL_TICKET_NUMBER;
            }
        }

        return TICKET_PREFIX + nextNumber;
    }

    /**
     * Retrieves ticket receipt details by formatted ticket code.
     * 
     * @param ticketId ticket identifier (e.g. TKT-1001)
     * @return {@link TicketReceiptDTO}
     */
    @Transactional(readOnly = true)
    public TicketReceiptDTO getTicketReceipt(String ticketId) {
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));

        return convertToReceiptDTO(ticket, "Support Ticket details retrieved successfully.");
    }

    /**
     * Updates ticket status, staff resolution response, and priority.
     * 
     * @param ticketId formatted ticket ID
     * @param updateDTO DTO with updated status and remarks
     * @return updated {@link TicketReceiptDTO}
     */
    @Transactional
    public TicketReceiptDTO updateTicketStatus(String ticketId, TicketStatusUpdateDTO updateDTO) {
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));

        if (updateDTO.getStatus() != null && !updateDTO.getStatus().trim().isEmpty()) {
            String status = updateDTO.getStatus().trim();
            if (!status.equalsIgnoreCase("Open") &&
                !status.equalsIgnoreCase("In Progress") &&
                !status.equalsIgnoreCase("Resolved") &&
                !status.equalsIgnoreCase("Closed")) {
                throw new IllegalArgumentException("Invalid status value. Allowed: Open, In Progress, Resolved, Closed.");
            }
            ticket.setStatus(status);
        }

        if (updateDTO.getAdminResponse() != null) {
            ticket.setAdminResponse(updateDTO.getAdminResponse().trim());
        }

        if (updateDTO.getPriority() != null && !updateDTO.getPriority().trim().isEmpty()) {
            ticket.setPriority(updateDTO.getPriority().trim());
        }

        if (updateDTO.getAssignedTo() != null) {
            ticket.setAssignedTo(updateDTO.getAssignedTo().trim());
        }

        ticket.setUpdatedAt(LocalDateTime.now());
        Ticket updatedTicket = ticketRepository.save(ticket);

        return convertToReceiptDTO(updatedTicket, "Ticket status and resolution updated successfully.");
    }

    /**
     * Deletes a ticket by its identifier, also cleaning up any stored attachment file.
     * 
     * @param ticketId formatted ticket ID
     */
    @Transactional
    public void deleteTicket(String ticketId) {
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));

        // Delete physical attachment if exists
        if (ticket.getAttachmentPath() != null) {
            try {
                Path filePath = Paths.get(ticket.getAttachmentPath());
                Files.deleteIfExists(filePath);
                Path parentDir = filePath.getParent();
                if (parentDir != null && Files.isDirectory(parentDir)) {
                    Files.deleteIfExists(parentDir);
                }
            } catch (IOException e) {
                // Log and proceed with entity deletion
                System.err.println("Notice: Could not delete attachment file: " + e.getMessage());
            }
        }

        ticketRepository.delete(ticket);
    }

    /**
     * Lists all support tickets stored in system ordered by newest first.
     * 
     * @return list of all tickets
     */
    @Transactional(readOnly = true)
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Searches and filters tickets by optional query keyword, status, and category.
     * 
     * @param query    optional text search across fields
     * @param status   optional status filter
     * @param category optional category filter
     * @return list of matching tickets
     */
    @Transactional(readOnly = true)
    public List<Ticket> getTicketsFiltered(String query, String status, String category) {
        List<Ticket> tickets;

        if (query != null && !query.trim().isEmpty()) {
            tickets = ticketRepository.searchTickets(query.trim());
        } else {
            tickets = ticketRepository.findAllByOrderByCreatedAtDesc();
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("All")) {
            tickets = tickets.stream()
                    .filter(t -> t.getStatus().equalsIgnoreCase(status.trim()))
                    .collect(Collectors.toList());
        }

        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("All")) {
            tickets = tickets.stream()
                    .filter(t -> t.getCategory() != null && t.getCategory().equalsIgnoreCase(category.trim()))
                    .collect(Collectors.toList());
        }

        return tickets;
    }

    /**
     * Calculates KPI statistics for the Help Desk Console.
     * 
     * @return {@link TicketStatsDTO}
     */
    @Transactional(readOnly = true)
    public TicketStatsDTO getTicketStats() {
        long total = ticketRepository.count();
        long open = ticketRepository.countByStatus("Open");
        long inProgress = ticketRepository.countByStatus("In Progress");
        long resolved = ticketRepository.countByStatus("Resolved");
        long closed = ticketRepository.countByStatus("Closed");

        return new TicketStatsDTO(total, open, inProgress, resolved, closed);
    }

    /**
     * Converts a Ticket entity to a TicketReceiptDTO.
     */
    private TicketReceiptDTO convertToReceiptDTO(Ticket ticket, String message) {
        return new TicketReceiptDTO(
                ticket.getTicketId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getCategory(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getStudentId(),
                ticket.getStudentEmail(),
                ticket.getAttachmentName(),
                ticket.getAttachmentSize(),
                ticket.getAdminResponse(),
                ticket.getAssignedTo(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                message
        );
    }
}
