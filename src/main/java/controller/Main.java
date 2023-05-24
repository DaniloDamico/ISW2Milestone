package controller;

import entities.Bug;
import entities.Release;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Main {

    private static final String PROJ1 = "BOOKKEEPER";
    private static final String PROJ2 = "SYNCOPE";
    private static final String TIME = "Time elapsed: ";

    private static Logger LOGGER;
    public static void main(String[] args) throws IOException, URISyntaxException {
        LOGGER = Logger.getLogger(Main.class.getName());
        createDatasets(List.of(PROJ1, PROJ2));
    }

    public static void createDatasets(List<String> projNames) throws IOException, URISyntaxException {
        for (String projName : projNames) {

            long start = System.currentTimeMillis();
            LOGGER.info(projName);

            LOGGER.info("Retrieving releases. ");
            ArrayList<Release> releases = (ArrayList<Release>) ReleaseManager.getReleases(projName);

            LOGGER.info("Retrieving bugs. ");
            ArrayList<Bug> bugs = (ArrayList<Bug>) BugManager.getBugs(projName, releases);

            LOGGER.info("Building dataset. ");
            MetricsManager metricsManager = new MetricsManager(projName);
            metricsManager.buildDataset(releases, bugs);
            LOGGER.info(TIME + (System.currentTimeMillis() - start) / (1000.0*60) + "min");
        }
    }
}