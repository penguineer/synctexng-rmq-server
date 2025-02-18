package com.penguineering.synctexng.synctexng_rmq_server.archive;

import java.nio.file.Path;

/**
 * Strategy to determine the main LaTeX file from a set of files.
 */
public interface MainLatexFileStrategy {
    /**
     * Accept a path to be considered for the main LaTeX file.
     *
     * @param paths the path to consider
     */
    void acceptPath(Path paths);

    /**
     * Get the chosen path.
     *
     * @return the chosen path
     */
    Path getChosenPath();
}