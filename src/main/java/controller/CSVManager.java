package controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CSVManager {

    private static final String FOLDER = "output/";
    private static String filename;
    private static FileWriter dataset;

    private static void createFile() {
        try{
            Files.deleteIfExists(Path.of(FOLDER + filename));
            new File(FOLDER + filename);
        } catch(Exception e){
            System.out.println("File creation failed");
            e.printStackTrace();
        }
    }

    //sostituire con metodo writeLine
    private static void initializeWriter(){
        try {
            dataset = new FileWriter(FOLDER + filename);
        } catch (IOException e) {
            System.out.println("File initialization failed");
            e.printStackTrace();
        }
    }

    public CSVManager(String projName) {
        filename = projName+"dataset.csv";
        createFile();
        initializeWriter();
    }

    public void writeLine(String line){
        try {
            dataset.write(line);
        } catch (IOException e) {
            System.out.println("Failed to write line: " + line);
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            dataset.flush();
            dataset.close();
        } catch (IOException e) {
            System.out.println("Failed to close dataset");
            e.printStackTrace();
        }
    }

}


