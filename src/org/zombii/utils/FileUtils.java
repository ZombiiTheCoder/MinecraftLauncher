package org.zombii.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class FileUtils {

    public FileUtils(){

    }

    public static String read(String File) {
        File FileObj = new File(File);
        StringBuilder Data = new StringBuilder();
        try {
            Scanner myReader = new Scanner(FileObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                Data.append(data).append("\n");
            }
            myReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Data.toString();
    }

    public static byte[] readBytes(String file) {
        byte[] bytes = new byte[1];

        try {
            Files.readAllBytes(Paths.get(file));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }

        return bytes;
    }

    public static void remove(String file) {
        File f = new File(file);
        if (f.exists()) {
            f.delete();
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

    public static File[] getFiles(File StartDir) {
        ArrayList<File> Files = new ArrayList<File>();
        File[] allContents = StartDir.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                if (!file.isDirectory()) {
                    Files.add(file);
                }
                Files.addAll(Arrays.asList(getFiles(file)));
            }
        }
        File[] FilesN = new File[Files.size()];
        for (int i = 0; i < FilesN.length; i++) {
            FilesN[i] = Files.get(i);
        }
        return FilesN;
    }

    public static File[] getFiles(File StartDir, String ext) {
        ArrayList<File> Files = new ArrayList<File>();
        File[] allContents = StartDir.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                if (!file.isDirectory() && file.toString().contains(ext)) {
                    Files.add(file);
                }
                Files.addAll(Arrays.asList(getFiles(file, ext)));
            }
        }
        File[] FilesN = new File[Files.size()];
        for (int i = 0; i < FilesN.length; i++) {
            FilesN[i] = Files.get(i);
        }
        return FilesN;
    }

    public static void writeFile(String file, String contents) {
        try {
            FileWriter myWriter = new FileWriter(file);
            myWriter.write(contents);
            myWriter.close();
            // System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            // System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void writeFile(String file, byte[] contents) {
        try {
            FileOutputStream myWriter = new FileOutputStream(file);
            myWriter.write(contents);
            myWriter.close();
            // System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            // System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}