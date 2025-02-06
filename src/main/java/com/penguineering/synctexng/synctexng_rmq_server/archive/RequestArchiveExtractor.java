package com.penguineering.synctexng.synctexng_rmq_server.archive;

import com.penguineering.synctexng.synctexng_rmq_server.WorkdirPathOperatorBase;
import lombok.Getter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Getter
public class RequestArchiveExtractor extends WorkdirPathOperatorBase {
    private Path rootTexFile = null;

    public RequestArchiveExtractor(Path workDir) {
        super(workDir);
    }

    public List<Path> unpack(byte[] data) throws IOException {
        List<Path> files = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data), StandardCharsets.UTF_8)) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                Path file = getWorkDir().resolve(zipEntry.getName());
                files.add(file);

                if (Objects.isNull(rootTexFile) && file.toString().endsWith(".tex"))
                    rootTexFile = file;

                Files.createDirectories(file.getParent());
                Files.copy(zis, file);

                zis.closeEntry();
            }
        }
        return files;
    }
}
