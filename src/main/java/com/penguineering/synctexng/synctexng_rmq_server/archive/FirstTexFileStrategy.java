package com.penguineering.synctexng.synctexng_rmq_server.archive;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Strategy to determine the main LaTeX file from a set of files.
 * This strategy chooses the first file that ends with ".tex".
 */
public class FirstTexFileStrategy implements MainLatexFileStrategy {
    private Path rootTexFile = null;

    @Override
    public void acceptPath(Path path) {
        if (Objects.isNull(rootTexFile) && path.toString().endsWith(".tex"))
            rootTexFile = path;
    }

    @Override
    public Path getChosenPath() {
        return rootTexFile;
    }
}