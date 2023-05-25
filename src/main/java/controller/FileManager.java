package controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileManager {

    private static final String FOLDER = "output/";
    private FileWriter dataset;
    private final String filename;

    private void createFile() {
        try{
            Files.deleteIfExists(Path.of(FOLDER + filename));
            new File(FOLDER + filename);
        } catch(Exception e){
            Logger.getLogger(FileManager.class.getName()).log(Level.WARNING, "File {0} creation failed", filename);
            e.printStackTrace();
        }
    }
    private void initializeWriter(){
        try {
            dataset = new FileWriter(FOLDER + filename);
        } catch (IOException e) {
            Logger.getLogger(FileManager.class.getName()).warning("File initialization failed");
            e.printStackTrace();
        }
    }

    public FileManager(String filename) {
        this.filename = filename;
        createFile();
        initializeWriter();
    }

    public void writeLine(String line){
        try {
            dataset.write(line);
        } catch (IOException e) {
            Logger.getLogger(FileManager.class.getName()).log(Level.WARNING, "Failed to write line: {0}", line);
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            dataset.flush();
            dataset.close();
        } catch (IOException e) {
            Logger.getLogger(FileManager.class.getName()).warning("Failed to close dataset");
            e.printStackTrace();
        }
    }

}


