import entities.Bug;
import entities.JavaFile;
import entities.Release;
import org.kohsuke.github.GHCommit;

import java.io.IOException;
import java.util.*;

public class DatasetManager {
    public static void buildDataset(String projName, ArrayList<Release> releases, ArrayList<Bug> bugs) throws IOException {

        MetricsManager metricsManager = new MetricsManager(projName);
        metricsManager.buildDataset(releases, bugs);
    }


}
