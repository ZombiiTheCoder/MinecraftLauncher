package org.zombii.utils;

import java.io.*;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {
    private static final int BUFFER_SIZE = 4096;

    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdirs(); // Use mkdirs() to create parent directories if necessary
        }
        
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                String filePath = destDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    // If the entry is a file, extracts it
                    extractFile(zipIn, filePath);
                } else {
                    // If the entry is a directory, make the directory
                    File dir = new File(filePath);
                    dir.mkdirs();
                }
                zipIn.closeEntry();
            }
        }
    }

    public static void extractSubDir(URI zipFileUri, String DirInZip, String targetDir) throws IOException {

        Path targetPathDir = new File(targetDir).toPath();
        FileSystem zipFs = FileSystems.newFileSystem(zipFileUri, new HashMap<>());
        Path pathInZip = zipFs.getPath(DirInZip);
        Files.walkFileTree(pathInZip, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
                // Make sure that we conserve the hierachy of files and folders inside the zip
                Path relativePathInZip = pathInZip.relativize(filePath);
                Path targetPath = targetPathDir.resolve(relativePathInZip.toString());
                Files.createDirectories(targetPath.getParent());

                // And extract the file
                Files.copy(filePath, targetPath);

                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            int read;
            byte[] bytesIn = new byte[BUFFER_SIZE];
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }
}
