package controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CSVManager {

    private static final String FOLDER = "output/";
    private static final String FILENAME = "dataset.csv";
    private static FileWriter dataset;
    private final String projName;

    private void createFile() {
        try{
            Files.deleteIfExists(Path.of(FOLDER + projName + FILENAME));
            new File(FOLDER + projName + FILENAME);
        } catch(Exception e){
            Logger.getLogger(CSVManager.class.getName()).warning("File creation failed");
            e.printStackTrace();
        }
    }
    private void initializeWriter(){
        try {
            dataset = new FileWriter(FOLDER + projName + FILENAME);
        } catch (IOException e) {
            Logger.getLogger(CSVManager.class.getName()).warning("File initialization failed");
            e.printStackTrace();
        }
    }

    public CSVManager(String projName) {
        this.projName = projName;
        createFile();
        initializeWriter();
    }

    public void writeLine(String line){
        try {
            dataset.write(line);
        } catch (IOException e) {
            Logger.getLogger(CSVManager.class.getName()).log(Level.WARNING, "Failed to write line: {0}", line);
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            dataset.flush();
            dataset.close();
        } catch (IOException e) {
            Logger.getLogger(CSVManager.class.getName()).warning("Failed to close dataset");
            e.printStackTrace();
        }
    }

}


