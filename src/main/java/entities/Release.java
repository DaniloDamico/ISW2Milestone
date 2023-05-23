package entities;

import org.kohsuke.github.GHCommit;

import java.time.LocalDate;
import java.util.ArrayList;

public class Release {

    private int versionNumber;

    private int versionId;
    private String versionName;
    private LocalDate releaseDate;

    private final ArrayList<GHCommit> commits = new ArrayList<>();

    public int getVersionId() {
        return versionId;
    }

    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }

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

    public ArrayList<GHCommit> getCommits() {
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
