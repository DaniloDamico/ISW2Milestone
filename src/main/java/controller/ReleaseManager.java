package controller;

import boundaries.GitHubBoundary;
import boundaries.JiraBoundary;
import entities.Release;
import org.kohsuke.github.GHCommit;
import utils.Conversions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class ReleaseManager {

    private ReleaseManager(){}

    public static List<Release> getReleases(String projName) throws IOException, URISyntaxException {
        Set<Release> releaseSet = JiraBoundary.getReleaseSet(projName);

        ArrayList<Release> releases = new ArrayList<>(releaseSet);
        releases.sort((o1, o2) -> {
            if(o1.getReleaseDate().equals(o2.getReleaseDate()))
                return 0;
            return o1.getReleaseDate().isAfter(o2.getReleaseDate())? 1:-1;
        });

        // Releases are numbered from 1 to n
        for(int i=0; i<releases.size();i++){
            Release currRelease = releases.get(i);
            currRelease.setVersionNumber(i+1);
            releases.set(i, currRelease);
        }

        return sortCommitsIntoReleases(releases, projName);
    }

    private static ArrayList<Release> sortCommitsIntoReleases(ArrayList<Release> releases, String projName) throws IOException {

        Date cutoffDate = Conversions.localDateToDate(releases.get(releases.size()-1).getReleaseDate());
        ArrayList<GHCommit> commits = (ArrayList<GHCommit>) GitHubBoundary.getOrderedCommits(projName, cutoffDate);

        for(GHCommit c:commits){
            for(Release r:releases){
                Date releaseDate = Date.from(r.getReleaseDate().atStartOfDay(ZoneId.systemDefault()).toInstant());

                if(c.getCommitDate().before(releaseDate)){
                    r.addCommit(c);
                    break;
                }
            }
        }
        return releases;
    }
}
