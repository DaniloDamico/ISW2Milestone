package boundaries;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class GitHubBoundary {

    private static ArrayList<GHCommit> commits;
    public static ArrayList<GHCommit> getOrderedCommits(String projName, Date cutoffDate) throws IOException {

        GitHub github = GitHubBuilder.fromPropertyFile().build();
        GHRepository repo = github.getRepository("APACHE/" + projName);

        commits = new ArrayList<>();

        for (GHCommit commit : repo.listCommits()) {
            if (commit.getCommitDate().before(cutoffDate)) {
                commits.add(commit);
            }
        }

        sortCommitsByDate();

        return commits;
    }

    private static void sortCommitsByDate() {

        Comparator<GHCommit> byDate = Comparator.comparing(ghCommit -> {
            try {
                return ghCommit.getCommitDate();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        commits.sort(byDate);
    }

}