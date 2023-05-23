package entities;

import org.kohsuke.github.GHCommit;

import java.util.ArrayList;
import java.util.HashSet;

public class Bug {
    private ArrayList<GHCommit> commits;
    private HashSet<String> buggyFiles = new HashSet<>();

    private Release injectedVersion = null;
    private Release openingVersion;
    private Release fixedVersion;

    public Release getFixedVersion() {
        return fixedVersion;
    }

    public void setFixedVersion(Release fixedVersion) {
        this.fixedVersion = fixedVersion;
    }

    public HashSet<String> getBuggyFileNames() {
        return buggyFiles;
    }

    public void addBuggyFile(String buggyFile) {
        this.buggyFiles.add(buggyFile);
    }

    public void setBuggyFiles(HashSet<String> buggyFiles) {
        this.buggyFiles = buggyFiles;
    }

    public ArrayList<GHCommit> getCommits() {
        return commits;
    }

    public void setCommits(ArrayList<GHCommit> commits) {
        this.commits = commits;
    }

    public Release getInjectedVersion() {
        return injectedVersion;
    }

    public void setInjectedVersion(Release injectedVersion) {
        this.injectedVersion = injectedVersion;
    }

    public Release getOpeningVersion() {
        return openingVersion;
    }

    public void setOpeningVersion(Release openingVersion) {
        this.openingVersion = openingVersion;
    }
}
