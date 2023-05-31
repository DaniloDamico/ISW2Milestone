package controller;

import entities.Bug;
import entities.Release;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final String PROJ1 = "BOOKKEEPER";
    private static final String PROJ2 = "SYNCOPE";

    private static Logger logger;
    public static void main(String[] args) throws IOException, URISyntaxException {
        logger = Logger.getLogger(Main.class.getName());
        createDatasets(List.of(PROJ1, PROJ2));
    }

    public static void createDatasets(List<String> projNames) throws IOException, URISyntaxException {
        for (String projName : projNames) {

            long start = System.currentTimeMillis();
            logger.info(projName);

            logger.info("Retrieving releases. ");
            ArrayList<Release> releases = (ArrayList<Release>) ReleaseManager.getReleases(projName);

            logger.info("Retrieving bugs. ");
            ArrayList<Bug> bugs = (ArrayList<Bug>) BugManager.getBugs(projName, releases);

            logger.info("Building dataset. ");
            MetricsManager metricsManager = new MetricsManager(projName);
            metricsManager.buildDataset(releases, bugs);
            logger.log(Level.INFO, "Time elapsed: {0}min", (System.currentTimeMillis() - start) / (1000.0*60));


        }
    }
}