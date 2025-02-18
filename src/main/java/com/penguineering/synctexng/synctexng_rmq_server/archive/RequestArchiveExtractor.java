package com.penguineering.synctexng.synctexng_rmq_server.archive;

import com.penguineering.synctexng.synctexng_rmq_server.workdir.WorkDir;
import com.penguineering.synctexng.synctexng_rmq_server.workdir.WorkDirSupplied;
import lombok.Getter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Getter
public class RequestArchiveExtractor extends WorkDirSupplied {
    private final Consumer<Path> pathObserver;

    public RequestArchiveExtractor(WorkDir workDir,
                                   Consumer<Path> pathObserver) {
        super(workDir);
        this.pathObserver = pathObserver;
    }

    public List<Path> unpack(byte[] data) throws IOException {
        List<Path> files = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data), StandardCharsets.UTF_8)) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                Path file = getWorkPath().resolve(zipEntry.getName());
                files.add(file);

                Path relativeArchivePath = getWorkPath().relativize(file);
                pathObserver.accept(relativeArchivePath);

                Files.createDirectories(file.getParent());
                Files.copy(zis, file);

                zis.closeEntry();
            }
        }
        return files;
    }
}
