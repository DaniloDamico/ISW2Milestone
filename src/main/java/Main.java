import entities.Bug;
import entities.Release;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String PROJ1 = "BOOKKEEPER";
    private static final String PROJ2 = "SYNCOPE";

    public static void main(String[] args) throws IOException, URISyntaxException {
        createDatasets(List.of(PROJ1));
    }

    public static void createDatasets(List<String> projNames) throws IOException, URISyntaxException {
        for (String projName : projNames) {

            System.out.println(projName);
            long Start = System.currentTimeMillis();
            System.out.print("Retrieving releases. ");
            ArrayList<Release> releases = ReleaseManager.getReleases(projName);
            System.out.println("Time elapsed: " + (System.currentTimeMillis() - Start) / 1000.0 + "s");

            Start = System.currentTimeMillis();
            System.out.print("Retrieving bugs. ");
            ArrayList<Bug> bugs = BugManager.getBugs(projName, releases);
            System.out.println("Time elapsed: " + (System.currentTimeMillis() - Start) / 1000.0 + "s");

            Start = System.currentTimeMillis();
            System.out.println("Building dataset. ");
            DatasetManager.buildDataset(projName, releases, bugs);
            System.out.println("Time elapsed: " + (System.currentTimeMillis() - Start) / 1000.0 + "s");
        }
    }
}