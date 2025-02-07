package com.penguineering.synctexng.synctexng_rmq_server.workdir;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Represents a temporary work directory.
 */
public class WorkDir implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(WorkDir.class);

    @Getter
    private final Path path;
    private final Callable<Boolean> cleanupAction;

    public WorkDir(Path path, Callable<Boolean> cleanupAction) {
        this.path = path;
        this.cleanupAction = cleanupAction;
    }

    @Override
    public void close() throws IOException {
        try {
            if (!cleanupAction.call())
                logger.warn("Failed to clean up work directory: {}", path);
        } catch (Exception e) {
            logger.error("Failed to clean up work directory: {}", path, e);

            if (e instanceof IOException)
                throw (IOException) e;
            else
                throw new IOException("Failed to clean up work directory: " + path, e);
        }
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
