package com.resumeanalyzer.dto.resume;

import com.resumeanalyzer.entity.SuggestionStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateSuggestionStatusRequest {

    @NotNull(message = "Suggestion status is required")
    private SuggestionStatus status;

    public UpdateSuggestionStatusRequest() {}

    public UpdateSuggestionStatusRequest(SuggestionStatus status) {
        this.status = status;
    }

    public SuggestionStatus getStatus() {
        return status;
    }

    public void setStatus(SuggestionStatus status) {
        this.status = status;
    }
}
