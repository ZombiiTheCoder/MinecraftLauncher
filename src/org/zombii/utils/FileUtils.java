package org.zombii.utils;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileUtils {

    public FileUtils() {
    }

    public static String read(String filePath) {
        StringBuilder data = new StringBuilder();
        try (Scanner myReader = new Scanner(new File(filePath))) {
            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();
                data.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data.toString();
    }

    public static byte[] readBytes(String filePath) {
        byte[] bytes = new byte[0];
        try {
            bytes = Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
        return bytes;
    }

    public static void remove(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    public static boolean deleteDirectory(File dir) {
        File[] allContents = dir.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return dir.delete();
    }

    public static File[] getFiles(File startDir) {
        List<File> files = new ArrayList<>();
        File[] allContents = startDir.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                if (!file.isDirectory()) {
                    files.add(file);
                }
                files.addAll(Arrays.asList(getFiles(file)));
            }
        }
        return files.toArray(new File[0]);
    }

    public static File[] getFiles(File startDir, String ext) {
        List<File> files = new ArrayList<>();
        File[] allContents = startDir.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                if (!file.isDirectory() && file.toString().contains(ext)) {
                    files.add(file);
                }
                files.addAll(Arrays.asList(getFiles(file, ext)));
            }
        }
        return files.toArray(new File[0]);
    }

    public static void writeFile(String filePath, String contents) {
        try (FileWriter myWriter = new FileWriter(filePath)) {
            myWriter.write(contents);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(String filePath, byte[] contents) {
        try (FileOutputStream myWriter = new FileOutputStream(filePath)) {
            myWriter.write(contents);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
