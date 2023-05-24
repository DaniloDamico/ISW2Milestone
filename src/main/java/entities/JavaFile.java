package entities;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GitUser;

import java.util.*;

public class JavaFile {
    private String filename;
    private final Map<Release, ArrayList<GHCommit>> commitsHistory = new HashMap<>();
    private final Map<Release, ArrayList<GHCommit.File>> fileHistory = new HashMap<>();
    private int loc = 0;
    // LOC: lines of code.

    private int locTouched = 0;

    private int nr = 0;
    // NR: number of revisions.
    // Number of revisions
    int nfix = 0;
    // Nfix: number of defect fixes.
    // Number of bug fixes
    private Set<GitUser> authors = new HashSet<>();
    // Nauth: number of authors.
    // Size of the Set.
    private int locAdded = 0;
    // LOC Added: sum over revisions of LOC added.
    private int maxLocAdded = 0;
    // Max LOC Added: maximum over revisions of LOC added.
    private double avgLocAdded = 0;
    // Average LOC Added: average LOC added per revision.
    private int churn = 0;
    // Churn: sum over revisions of added and deleted LOC.
    // Sum over revisions of added – deleted LOC
    private int maxChurn = 0;
    // Max Churn: maximum churn over revisions.
    private double avgChurn = 0;
    // Average Churn: average churn over revisions.
    private Set<GHCommit.File> changeSet = new HashSet<>();
    // Change Set Size: number of files committed together.
    // Size of the Set.
    private int maxChangeSetSize = 0;
    // Max Change Set: maximum change set size over revisions.
    private double avgChangeSetSize = 0;
    // Average Change Set: average change set size over revisions.

    private boolean isDeleted = false;


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Map<Release, ArrayList<GHCommit>> getCommitsHistory() {
        return commitsHistory;
    }

    public void putInCommitsHistory(Release release, List<GHCommit> commits) {
        commitsHistory.put(release, (ArrayList<GHCommit>) commits);
    }

    public Map<Release, ArrayList<GHCommit.File>> getFileHistory() {
        return fileHistory;
    }

    public void putInFileHistory(Release release, List<GHCommit.File> files) {
        fileHistory.put(release, (ArrayList<GHCommit.File>) files);
    }

    public int getLoc() {
        return loc;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    public int getLocTouched() {
        return locTouched;
    }

    public void setLocTouched(int locTouched) {
        this.locTouched = locTouched;
    }

    public Set<GitUser> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<GitUser> authors) {
        this.authors = authors;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public int getNr() {
        return nr;
    }

    public void setNr(int nr) {
        this.nr = nr;
    }

    public int getLocAdded() {
        return locAdded;
    }

    public void setLocAdded(int locAdded) {
        this.locAdded = locAdded;
    }

    public int getMaxLocAdded() {
        return maxLocAdded;
    }

    public void setMaxLocAdded(int maxLocAdded) {
        this.maxLocAdded = maxLocAdded;
    }

    public int getNfix() {
        return nfix;
    }

    public void setNfix(int nfix) {
        this.nfix = nfix;
    }

    public double getAvgLocAdded() {
        return avgLocAdded;
    }

    public void setAvgLocAdded(double avgLocAdded) {
        this.avgLocAdded = avgLocAdded;
    }

    public int getChurn() {
        return churn;
    }

    public void setChurn(int churn) {
        this.churn = churn;
    }

    public int getMaxChurn() {
        return maxChurn;
    }

    public void setMaxChurn(int maxChurn) {
        this.maxChurn = maxChurn;
    }

    public double getAvgChurn() {
        return avgChurn;
    }

    public void setAvgChurn(double avgChurn) {
        this.avgChurn = avgChurn;
    }

    public Set<GHCommit.File> getChangeSet() {
        return changeSet;
    }

    public void setChangeSet(Set<GHCommit.File> changeSet) {
        this.changeSet = changeSet;
    }

    public int getMaxChangeSetSize() {
        return maxChangeSetSize;
    }

    public void setMaxChangeSetSize(int maxChangeSetSize) {
        this.maxChangeSetSize = maxChangeSetSize;
    }

    public double getAvgChangeSetSize() {
        return avgChangeSetSize;
    }

    public void setAvgChangeSetSize(double avgChangeSetSize) {
        this.avgChangeSetSize = avgChangeSetSize;
    }
}
