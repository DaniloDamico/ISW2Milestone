package entities;

import org.kohsuke.github.GHCommit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Bug {
    private ArrayList<GHCommit> commits;
    private final HashSet<String> buggyFiles = new HashSet<>();

    private Release injectedVersion = null;
    private Release openingVersion;
    private Release fixedVersion;

    public Release getFixedVersion() {
        return fixedVersion;
    }

    public void setFixedVersion(Release fixedVersion) {
        this.fixedVersion = fixedVersion;
    }

    public Set<String> getBuggyFileNames() {
        return buggyFiles;
    }

    public void addBuggyFile(String buggyFile) {
        this.buggyFiles.add(buggyFile);
    }

    public List<GHCommit> getCommits() {
        return commits;
    }

    public void setCommits(List<GHCommit> commits) {
        this.commits = (ArrayList<GHCommit>) commits;
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
