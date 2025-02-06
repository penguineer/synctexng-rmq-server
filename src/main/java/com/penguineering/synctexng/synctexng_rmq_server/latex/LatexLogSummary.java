package com.penguineering.synctexng.synctexng_rmq_server.latex;

import java.util.List;
import java.util.Objects;

public record LatexLogSummary(
        List<String> warnings,
        List<String> errors
) {
    public LatexLogSummary {
        if (Objects.isNull(warnings))
            warnings = List.of();
        if (Objects.isNull(errors))
            errors = List.of();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean isRepeatPass() {
        return hasWarnings() && !hasErrors();
    }
}
