package entities;

import org.kohsuke.github.GHCommit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Release {

    private int versionNumber;

    private String versionName;
    private LocalDate releaseDate;

    private final ArrayList<GHCommit> commits = new ArrayList<>();

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public List<GHCommit> getCommits() {
        return commits;
    }

    public void addCommit(GHCommit c) {
        this.commits.add(c);
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }
}
