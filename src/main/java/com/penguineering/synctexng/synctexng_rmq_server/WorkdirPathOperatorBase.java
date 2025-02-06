package com.penguineering.synctexng.synctexng_rmq_server;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.file.Path;

@Getter
@AllArgsConstructor
public abstract class WorkdirPathOperatorBase {
    private final Path workDir;
}
