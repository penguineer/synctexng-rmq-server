package com.penguineering.synctexng.synctexng_rmq_server.workdir;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;

/**
 * Factory for creating temporary work directories.
 */
@Component
public class WorkDirFactory {
    private static final Logger logger = LoggerFactory.getLogger(WorkDirFactory.class);

    @Value("${synctexng.server.work-dir}")
    private String configuredAppWorkDir;

    /**
     * The work directory for the application. If not configured, a temporary directory will be created.
     */
    @Getter
    private Path appWorkDir = null;

    @PostConstruct
    public void initAppWorkDir() throws IOException {
        appWorkDir = shouldCreateTempDirectory() ? createTempDirectory() : Path.of(configuredAppWorkDir);

        if (Files.notExists(appWorkDir) || !Files.isDirectory(appWorkDir))
            throw new IOException("Work directory is not available: " + appWorkDir);

        logger.info("Work Directory: {}", appWorkDir);
    }

    @PreDestroy
    public void cleanupAppWorkDir() {
        if (Objects.isNull(appWorkDir)) {
            logger.warn("Work Directory is not initialized, nothing to delete");
            return;
        }

        if (!shouldCreateTempDirectory()) {
            logger.info("Work Directory is configured, not deleting: {}", appWorkDir);
            return;
        }

        if (!Files.exists(appWorkDir)) {
            logger.warn("Work Directory does not exist: {}", appWorkDir);
            return;
        }

        // Do not fail if the directory cannot be deleted
        try {
            if (recursiveSafeDelete(appWorkDir))
                logger.info("Temporary Work Directory deleted: {}", appWorkDir);
            else
                logger.error("Failed to delete Work Directory: {}", appWorkDir);
        } catch (IOException e) {
            logger.error("Failed to delete Work Directory {}", appWorkDir, e);
        }
    }

    public WorkDir createWorkDir() throws IOException {
        Path workDir = Files.createTempDirectory(appWorkDir, "process-");
        return new WorkDir(workDir, () -> recursiveSafeDelete(workDir));
    }

    private boolean shouldCreateTempDirectory() {
        return Objects.isNull(configuredAppWorkDir) || configuredAppWorkDir.isBlank();
    }

    private Path createTempDirectory() throws IOException {
        try {
            return Files.createTempDirectory("synctexng-");
        } catch (IOException e) {
            throw new IOException("Failed to create temp directory", e);
        }
    }

    /**
     * Recursively delete a directory and all its contents, but do not fail if any file cannot be deleted.
     *
     * @param directory the directory to delete
     * @throws IOException if the path walk fails
     */
    private boolean recursiveSafeDelete(Path directory) throws IOException {
        try (var paths = Files.walk(directory)) {
            return paths
                    .sorted(Comparator.reverseOrder())
                    .map(this::safeDelete)
                    // Check if all deletions were successful
                    .reduce(Boolean::logicalAnd)
                    .orElse(true);
        }
    }

    /**
     * Delete a file and log any exception that occurs, but to not fail the operation.
     *
     * @param path the file to delete
     */
    private boolean safeDelete(Path path) {
        try {
            Files.delete(path);
            return true;
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", path, e);
            return false;
        }
    }
}
