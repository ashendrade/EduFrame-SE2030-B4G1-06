package com.eduframepackage.eduframe.dto;

import java.util.List;

/**
 * Response object indicating schedule availability and listing overlapping events if any.
 */
public class ConflictCheckDTO {

    private boolean conflict;
    private String message;
    private List<AnnouncementDTO> conflictingEvents;

    public ConflictCheckDTO() {
    }

    public ConflictCheckDTO(boolean conflict, String message, List<AnnouncementDTO> conflictingEvents) {
        this.conflict = conflict;
        this.message = message;
        this.conflictingEvents = conflictingEvents;
    }

    public boolean isConflict() {
        return conflict;
    }

    public void setConflict(boolean conflict) {
        this.conflict = conflict;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<AnnouncementDTO> getConflictingEvents() {
        return conflictingEvents;
    }

    public void setConflictingEvents(List<AnnouncementDTO> conflictingEvents) {
        this.conflictingEvents = conflictingEvents;
    }
}
