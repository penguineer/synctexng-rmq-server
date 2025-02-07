package com.penguineering.synctexng.synctexng_rmq_server.rmq;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JobResult(
        JobStatus status,
        String pdfPath,
        int passes,
        List<String> processingErrors,
        List<String> latexWarnings,
        List<String> latexErrors
) {
}
