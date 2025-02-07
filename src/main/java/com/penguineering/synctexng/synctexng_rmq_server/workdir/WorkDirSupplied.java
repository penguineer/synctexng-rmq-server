package com.penguineering.synctexng.synctexng_rmq_server.workdir;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class WorkDirSupplied {
    private final Supplier<Path> workDirSupplier;

    public WorkDirSupplied (WorkDir workDir) {
        Objects.requireNonNull(workDir, "workDir must not be null");
        workDirSupplier = workDir::getPath;
    }

    public Path getWorkPath() {
        return workDirSupplier.get();
    }
}
