package com.resumeanalyzer.dto.resume;

import jakarta.validation.constraints.NotBlank;

public class CreateVersionRequest {

    private String versionNote;

    @NotBlank(message = "Resume content is required")
    private String contentText;

    public CreateVersionRequest() {}

    public CreateVersionRequest(String versionNote, String contentText) {
        this.versionNote = versionNote;
        this.contentText = contentText;
    }

    public String getVersionNote() {
        return versionNote;
    }

    public void setVersionNote(String versionNote) {
        this.versionNote = versionNote;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }
}
