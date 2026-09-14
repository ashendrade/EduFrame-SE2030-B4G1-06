package com.eduframepackage.eduframe.repository;

import com.eduframepackage.eduframe.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * <h1>UC-05 Submit Support Ticket - Ticket JPA Repository</h1>
 * 
 * <p>
 * Data access abstraction interface for managing {@link Ticket} persistent entities.
 * Provides custom query methods for retrieving tickets by generated ticket code,
 * verifying duplicate ticket submissions, and supporting sequential ticket ID generation.
 * </p>
 * 
 * @author EduFrame Group B4G1-06 (SE2030)
 * @version 1.0
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Retrieves a Ticket by its unique formatted ticket code (e.g., TKT-1001).
     * 
     * @param ticketId the unique formatted ticket code
     * @return an {@link Optional} containing the found Ticket, or empty if not found
     */
    Optional<Ticket> findByTicketId(String ticketId);

    /**
     * Checks if a ticket already exists with the identical subject, description, and status.
     * Used by {@code TicketService} for UC-05 duplicate prevention.
     * 
     * @param subject     the subject title to check
     * @param description the description body to check
     * @param status      the current status (e.g. "Open")
     * @return {@code true} if a matching duplicate ticket exists, {@code false} otherwise
     */
    boolean existsBySubjectIgnoreCaseAndDescriptionIgnoreCaseAndStatus(String subject, String description, String status);

    /**
     * Retrieves the most recently inserted Ticket entity ordered by primary key descending.
     * Used for sequential ticket ID calculation (e.g., TKT-1001 -> TKT-1002).
     * 
     * @return an {@link Optional} containing the latest Ticket entry
     */
    Optional<Ticket> findTopByOrderByIdDesc();

    /**
     * Returns all tickets ordered by creation timestamp descending (newest first).
     * 
     * @return list of tickets
     */
    List<Ticket> findAllByOrderByCreatedAtDesc();

    /**
     * Returns tickets matching a specific status ordered by creation timestamp descending.
     * 
     * @param status ticket status (e.g., "Open", "In Progress", "Resolved", "Closed")
     * @return list of matching tickets
     */
    List<Ticket> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Returns tickets by category ordered by creation timestamp descending.
     * 
     * @param category the ticket category
     * @return list of matching tickets
     */
    List<Ticket> findByCategoryOrderByCreatedAtDesc(String category);

    /**
     * Returns tickets submitted by a specific student ID ordered by creation timestamp descending.
     * 
     * @param studentId the student identifier
     * @return list of student's tickets
     */
    List<Ticket> findByStudentIdOrderByCreatedAtDesc(String studentId);

    /**
     * Counts the number of tickets with a specific status.
     * 
     * @param status the ticket status
     * @return count of matching tickets
     */
    long countByStatus(String status);

    /**
     * Searches tickets across Ticket ID, Subject, Description, Student ID, or Student Email.
     * 
     * @param query the search keyword
     * @return list of matching tickets
     */
    @Query("SELECT t FROM Ticket t WHERE " +
           "LOWER(t.ticketId) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.subject) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(t.studentId IS NOT NULL AND LOWER(t.studentId) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(t.studentEmail IS NOT NULL AND LOWER(t.studentEmail) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY t.createdAt DESC")
    List<Ticket> searchTickets(@Param("query") String query);
}
