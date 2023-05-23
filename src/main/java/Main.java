import controller.BugManager;
import controller.MetricsManager;
import controller.ReleaseManager;
import entities.Bug;
import entities.Release;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String PROJ1 = "BOOKKEEPER";
    //private static final String PROJ2 = "SYNCOPE";
    private static final String TIME = "Time elapsed: ";

    public static void main(String[] args) throws IOException, URISyntaxException {
        createDatasets(List.of(PROJ1));
    }

    public static void createDatasets(List<String> projNames) throws IOException, URISyntaxException {
        for (String projName : projNames) {

            System.out.println(projName);
            long start = System.currentTimeMillis();
            System.out.println("Retrieving releases. ");
            ArrayList<Release> releases = (ArrayList<Release>) ReleaseManager.getReleases(projName);
            System.out.println(TIME + (System.currentTimeMillis() - start) / 1000.0 + "s");

            start = System.currentTimeMillis();
            System.out.println("Retrieving bugs. ");
            ArrayList<Bug> bugs = (ArrayList<Bug>) BugManager.getBugs(projName, releases);
            System.out.println(TIME + (System.currentTimeMillis() - start) / 1000.0 + "s");

            start = System.currentTimeMillis();
            System.out.println("Building dataset. ");
            MetricsManager metricsManager = new MetricsManager(projName);
            metricsManager.buildDataset(releases, bugs);
            System.out.println(TIME + (System.currentTimeMillis() - start) / 1000.0 + "s");
        }
    }
}