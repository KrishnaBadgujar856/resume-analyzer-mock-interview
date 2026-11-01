package com.resumeanalyzer.dto.resume;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateResumeRequest {

    @NotBlank(message = "Resume title is required")
    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    private String title;

    private String targetRole;

    public UpdateResumeRequest() {}

    public UpdateResumeRequest(String title, String targetRole) {
        this.title = title;
        this.targetRole = targetRole;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }
}
