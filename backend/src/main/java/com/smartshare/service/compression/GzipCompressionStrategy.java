package com.smartshare.service.compression;

import com.smartshare.exception.compression.CompressionException;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

@Component
public class GzipCompressionStrategy implements CompressionStrategy {

    private static final int BUFFER_SIZE = 8192;

    @Override
    public CompressionResult compress(InputStream inputStream, String fileName) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("smartshare_compress_", ".gz");
            long originalSize = 0;

            try (OutputStream fileOut = Files.newOutputStream(tempFile);
                 GZIPOutputStream gzipOut = new GZIPOutputStream(fileOut)) {
                
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    gzipOut.write(buffer, 0, bytesRead);
                    originalSize += bytesRead;
                }
            }

            long compressedSize = Files.size(tempFile);

            // Create an InputStream that automatically deletes the temp file when closed
            final Path fileToDelete = tempFile;
            InputStream compressedStream = new FileInputStream(tempFile.toFile()) {
                @Override
                public void close() throws IOException {
                    super.close();
                    Files.deleteIfExists(fileToDelete);
                }
            };

            return CompressionResult.builder()
                    .compressedStream(compressedStream)
                    .originalSize(originalSize)
                    .compressedSize(compressedSize)
                    .compressionFormat(getFormat())
                    .build();

        } catch (Exception e) {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {}
            }
            throw new CompressionException("Failed to gzip file: " + fileName, e);
        }
    }

    @Override
    public String getFormat() {
        return "gzip";
    }
}
