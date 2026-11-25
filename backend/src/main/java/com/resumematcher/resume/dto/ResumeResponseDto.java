package com.resumematcher.resume.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ResumeResponseDto {
    private UUID id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Boolean isActive;
    private String parsingStatus;
    private String candidateName;
    private String candidateEmail;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public ResumeResponseDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }

    public String getParsingStatus() { return parsingStatus; }
    public void setParsingStatus(String parsingStatus) { this.parsingStatus = parsingStatus; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
