package com.penguineering.synctexng.synctexng_rmq_server.archive;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Strategy to determine the main LaTeX file from a set of files.
 * This strategy chooses the file with the same name as the target file.
 */
public class NamedFileStrategy implements MainLatexFileStrategy {
    private final Path targetPath;
    private Path chosenPath = null;

    public NamedFileStrategy(Path targetPath) {
        this.targetPath = Objects.requireNonNull(targetPath, "targetPath must not be null");
    }

    @Override
    public void acceptPath(Path path) {
        if (path.getFileName().equals(targetPath.getFileName()))
            chosenPath = path;
    }

    @Override
    public Path getChosenPath() {
        return chosenPath;
    }
}
