package controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class CSVManager {

    private static final String FOLDER = "output/";
    private static final String DATASET = "dataset.csv";
    private static FileWriter dataset;
    private final String projName;

    private void createFile() {
        try{
            Files.deleteIfExists(Path.of(FOLDER + projName + DATASET));
            new File(FOLDER + projName + DATASET);
        } catch(Exception e){
            Logger.getLogger("CSVManager").warning("File creation failed");
            e.printStackTrace();
        }
    }

    //sostituire con metodo writeLine
    private void initializeWriter(){
        try {
            dataset = new FileWriter(FOLDER + projName + DATASET);
        } catch (IOException e) {
            Logger.getLogger("CSVManager").warning("File initialization failed");
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
            Logger.getLogger("CSVManager").warning("Failed to write line: " + line);
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            dataset.flush();
            dataset.close();
        } catch (IOException e) {
            Logger.getLogger("CSVManager").warning("Failed to close dataset");
            e.printStackTrace();
        }
    }

}


