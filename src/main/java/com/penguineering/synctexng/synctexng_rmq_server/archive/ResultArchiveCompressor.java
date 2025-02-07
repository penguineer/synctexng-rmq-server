package com.penguineering.synctexng.synctexng_rmq_server.archive;

import com.penguineering.synctexng.synctexng_rmq_server.workdir.WorkDir;
import com.penguineering.synctexng.synctexng_rmq_server.workdir.WorkDirSupplied;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResultArchiveCompressor extends WorkDirSupplied {
    private final List<Path> filesToZip = new ArrayList<>();

    private final Map<Path, byte[]> filesToZipInMemory = new HashMap<>();

    public ResultArchiveCompressor(WorkDir workDir) {
        super(workDir);
    }

    public void addFilePath(Path file) {
        filesToZip.add(file);
    }

    public void addFileInMemory(Path relativePath, byte[] content) {
        filesToZipInMemory.put(relativePath, content);
    }

    public byte[] createZipInMemory() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {

            for (Path filePath : this.filesToZip) {
                // Check if the relativePath is within the workDir
                if (!filePath.normalize().startsWith(getWorkPath().normalize()))
                    throw new NoSuchFileException("The file " + filePath + " is not within the work directory.");

                Path relativePath = getWorkPath().relativize(filePath);

                zos.putNextEntry(new ZipEntry(relativePath.toString()));
                Files.copy(filePath, zos);
                zos.closeEntry();
            }

            for (Map.Entry<Path, byte[]> entry : this.filesToZipInMemory.entrySet()) {
                zos.putNextEntry(new ZipEntry(entry.getKey().toString()));
                zos.write(entry.getValue());
                zos.closeEntry();
            }

            zos.finish();
            return baos.toByteArray();
        }
    }

}
