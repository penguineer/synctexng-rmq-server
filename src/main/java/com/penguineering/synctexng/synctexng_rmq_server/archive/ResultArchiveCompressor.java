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
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResultArchiveCompressor extends WorkDirSupplied {
    private final List<Path> filesToZip = new ArrayList<>();

    public ResultArchiveCompressor(WorkDir workDir) {
        super(workDir);
    }

    public void addFilePath(Path file) {
        filesToZip.add(file);
    }

    public byte[] createZipInMemory() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {

            for (Path filePath : this.filesToZip) {
                // Check if the relativePath is within the workDir
                var a = filePath.normalize();
                var b = getWorkPath().normalize();
                if (!filePath.normalize().startsWith(getWorkPath().normalize()))
                    throw new NoSuchFileException("The file " + filePath + " is not within the work directory.");

                Path relativePath = getWorkPath().relativize(filePath);

                zos.putNextEntry(new ZipEntry(relativePath.toString()));
                Files.copy(filePath, zos);
                zos.closeEntry();
            }

            zos.finish();
            return baos.toByteArray();
        }
    }

}
