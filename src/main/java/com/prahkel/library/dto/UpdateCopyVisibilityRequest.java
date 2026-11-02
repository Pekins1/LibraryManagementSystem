package com.prahkel.library.dto;

import jakarta.validation.constraints.NotNull;
import com.prahkel.library.model.CopyVisibility;

public record UpdateCopyVisibilityRequest(
    @NotNull CopyVisibility visibility
) {
    
}
