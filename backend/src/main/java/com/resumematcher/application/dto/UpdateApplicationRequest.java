package com.resumematcher.application.dto;

import com.resumematcher.application.entity.ApplicationStatus;
import jakarta.validation.constraints.Size;

public class UpdateApplicationRequest {

    private ApplicationStatus status;

    @Size(max = 5000, message = "Notes cannot exceed 5000 characters")
    private String notes;

    public UpdateApplicationRequest() {}

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
