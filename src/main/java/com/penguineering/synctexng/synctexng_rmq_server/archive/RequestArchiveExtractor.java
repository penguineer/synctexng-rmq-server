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
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Getter
public class RequestArchiveExtractor extends WorkDirSupplied {
    private Path rootTexFile = null;

    public RequestArchiveExtractor(WorkDir workDir) {
        super(workDir);
    }

    public List<Path> unpack(byte[] data) throws IOException {
        List<Path> files = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data), StandardCharsets.UTF_8)) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                Path file = getWorkPath().resolve(zipEntry.getName());
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
