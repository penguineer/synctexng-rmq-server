package com.penguineering.synctexng.synctexng_rmq_server.latex;

import com.penguineering.synctexng.synctexng_rmq_server.WorkdirPathOperatorBase;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LatexLogWrangler extends WorkdirPathOperatorBase {
    public static final String LATEX_WARNING_PREFIX = "LaTeX Warning:";
    public static final String LATEX_ERROR_PREFIX = "! LaTeX Error:";

    private final Path nameRoot;

    public LatexLogWrangler(Path workDir, Path nameRoot) {
        super(workDir);
        this.nameRoot = nameRoot;
    }

    public Path renderLogPath() {
        return getWorkDir().resolve(nameRoot + ".log");
    }

    public Path renderPassLogPath(int pass) {
        return nameRoot.resolveSibling(nameRoot.getFileName().toString() + ".log." + pass);
    }

    public Path copyPassLog(int pass) throws IOException {
        var targetPath = renderPassLogPath(pass);
        Files.copy(renderLogPath(), targetPath);

        return targetPath;
    }

    public LatexLogSummary analyzeLatestLog() throws IOException {
        return filterLogByPrefixes(renderLogPath(), List.of(LATEX_WARNING_PREFIX, LATEX_ERROR_PREFIX));
    }

    public LatexLogSummary filterLogByPrefixes(Path log, List<String> prefixes) throws IOException {
        try (Stream<String> lines = Files.lines(log, StandardCharsets.ISO_8859_1)) {
            var entries = lines
                    // Filter lines by prefixes and group by prefix
                    .map(line -> prefixes.stream()
                            .filter(line::startsWith)
                            .findFirst()
                            // also remove prefixes from lines
                            .map(prefix -> Map.entry(prefix, line.substring(prefix.length())))
                            .orElse(null))
                    .filter(Objects::nonNull)
                    // add to result Map
                    .collect(Collectors.groupingBy(
                            Map.Entry::getKey,
                            Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
            return new LatexLogSummary(
                    entries.get(LATEX_WARNING_PREFIX),
                    entries.get(LATEX_ERROR_PREFIX));
        } catch (IOException e) {
            throw new IOException("Failed to read log file", e);
        }
    }
}
