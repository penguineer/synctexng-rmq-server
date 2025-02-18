package com.penguineering.synctexng.synctexng_rmq_server.archive;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Strategy to determine the main LaTeX file from a set of files.
 * This strategy chooses the first result of the given strategies that is not null, in the order of the provided strategies.
 */
public class LayeredMainFileStrategy implements MainLatexFileStrategy {
    private final List<MainLatexFileStrategy> strategies;

    public LayeredMainFileStrategy(List<MainLatexFileStrategy> strategies) {
        this.strategies = Objects.requireNonNull(strategies, "strategies must not be null");
    }

    @Override
    public void acceptPath(Path path) {
        strategies.forEach(strategy -> strategy.acceptPath(path));
    }

    @Override
    public Path getChosenPath() {
        return strategies.stream()
                .map(MainLatexFileStrategy::getChosenPath)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}