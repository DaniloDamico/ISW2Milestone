import entities.Bug;
import entities.JavaFile;
import entities.Release;
import exceptions.DeletedFileException;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GitUser;

import java.io.IOException;
import java.util.*;

public class MetricsManager {
    private final CSVManager csvManager;
    private ArrayList<Bug> bugs;
    private static final String header = "Version,File Name,LOC,LOC_touched,NR,NFix,NAuth,LOC_added,MAX_LOC_added,AVG_LOC_added,Churn,MAX_Churn,AVG_Churn,ChgSetSize,MAX_ChgSet,AVG_ChgSet,Buggy";

    public MetricsManager(String projName){
        csvManager = new CSVManager(projName);
    }

    //metrics don't stack between releases
    public void buildDataset(ArrayList<Release> releases, ArrayList<Bug> bugs) throws IOException {

        this.bugs = new ArrayList<>(bugs);
        csvManager.writeLine(header);
        ArrayList<JavaFile> files = new ArrayList<>();

        ArrayList<Release> firstHalf = new ArrayList<>(releases.subList(0, releases.size()/2));

        for (Release release : firstHalf) {
            System.out.println("Analyzing release " + release.getVersionNumber());
            addReleaseToFiles(release, files);

            for (JavaFile jf: files) {

                if(jf.getFileHistory().get(release) == null || jf.isDeleted())
                    continue;

                updateFile(release, jf);

                String line = "\n";
                line += release.getVersionNumber() + ", " + jf.getFilename()+ ", ";

                try {
                    for(GHCommit.File f:jf.getFileHistory().get(release)){
                        if(f.getStatus().equals("removed")){
                            jf.setDeleted(true);
                            throw new DeletedFileException();
                        }
                    }

                    line += jf.getLoc() + ", ";
                    line += jf.getLocTouched() + ", ";
                    line += jf.getNr() + ", ";
                    line += jf.getNfix() + ", ";
                    line += jf.getAuthors().size() + ", ";
                    line += jf.getLocAdded() + ", ";
                    line += jf.getMaxLocAdded() + ", ";
                    line += jf.getAvgLocAdded() + ", ";
                    line += jf.getChurn() + ", ";
                    line += jf.getMaxChurn() + ", ";
                    line += jf.getAvgChurn() + ", ";
                    line += jf.getChangeSet().size() + ", ";
                    line += jf.getMaxChangeSetSize() + ", ";
                    line += jf.getAvgChangeSetSize() + ", ";

                    boolean buggy = computeBuggy(release.getVersionNumber(), jf.getFilename(), bugs);
                    if (buggy)
                        line += "yes";
                    else
                        line += "no";

                    csvManager.writeLine(line);
                } catch (Exception e){
                    e.printStackTrace();
                }
                catch (DeletedFileException e) {
                    jf.setDeleted(true);
                }
            }
        }

        csvManager.close();
    }

    private void addReleaseToFiles(Release release, ArrayList<JavaFile> files) throws IOException {
        //prende tutti i file nella release e li mette nei files
        for(GHCommit c:release.getCommits()){
            for(GHCommit.File f:c.getFiles()) {
                if (f.getFileName().contains(".java") && !f.getFileName().contains("/test")) {
                    if (f.getStatus().equals("added")) {
                        files.add(createJavaFile(release, f, c));
                    } else {
                        try {
                            addCommitAndFile(release, Objects.requireNonNull(findByName(f, files)), f, c);
                        } catch (NullPointerException e) {
                            files.add(createJavaFile(release, f, c));
                        }
                    }
                }
            }
        }
    }

    private JavaFile createJavaFile(Release r, GHCommit.File f, GHCommit c) {
        JavaFile jf = new JavaFile();
        jf.setFilename(f.getFileName());

        addCommitAndFile(r, jf, f, c);

        return jf;
    }

    private void addCommitAndFile(Release r, JavaFile jf, GHCommit.File f, GHCommit c) {
        ArrayList<GHCommit.File> releaseFiles = jf.getFileHistory().get(r);
        try {
            releaseFiles.add(f);
        } catch (NullPointerException e){
            releaseFiles = new ArrayList<>();
            releaseFiles.add(f);
        }
        jf.putInFileHistory(r, releaseFiles);

        ArrayList<GHCommit> releaseCommits = jf.getCommitsHistory().get(r);
        try{
            releaseCommits.add(c);
        } catch (NullPointerException e) {
            releaseCommits = new ArrayList<>();
            releaseCommits.add(c);
        }
        jf.putInCommitsHistory(r, releaseCommits);
    }


    private JavaFile findByName(GHCommit.File f, ArrayList<JavaFile> files) {
        for(JavaFile jf:files){
            if(jf.getFilename().equals(f.getFileName()))
                return jf;
            if(jf.getFilename().equals(f.getPreviousFilename())){
                jf.setFilename(f.getFileName());
                return jf;
            }
        }
        return null;
    }
    private boolean computeBuggy(int versionNumber, String filename, ArrayList<Bug> bugs) {
        for (Bug bug : bugs) {

            int iv = bug.getInjectedVersion().getVersionNumber();
            int fv = bug.getFixedVersion().getVersionNumber();
            if (versionNumber < fv && versionNumber >= iv) {
                if(bug.getBuggyFileNames().contains(filename))
                    return true;
            }
        }
        return false;
    }

    //non cumula i valori tra le release
    private int computeNfix(int releaseNumber, JavaFile jf, ArrayList<Bug> bugs) throws IOException {
        int nfix = jf.getNfix();
        for (Bug bug : bugs) {
            int fv = bug.getFixedVersion().getVersionNumber();

            if(releaseNumber==fv){
                if(bug.getCommits().size()==0)  //se non ci sono commit non ci sono fix
                    continue;
                List<GHCommit.File> fixFiles = bug.getCommits().get(bug.getCommits().size()-1).getFiles();
                for(GHCommit.File f:fixFiles){
                    if(f.getFileName().equals(jf.getFilename())){
                        nfix++;
                        break;
                    }
                }
            }
        }
        return nfix;
    }

    private void updateFile(Release release, JavaFile jf) throws IOException {

        int loc = jf.getLoc();
        int locAdded = jf.getLocAdded();
        int maxLocAdded = jf.getMaxLocAdded();
        int locTouched = jf.getLocTouched();
        int churn = jf.getChurn();
        int maxChurn = jf.getMaxChurn();
        for (GHCommit.File f : jf.getFileHistory().get(release)) {
            loc += f.getLinesAdded() - f.getLinesDeleted();
            locAdded += f.getLinesAdded();
            if(f.getLinesAdded()>maxLocAdded){
                maxLocAdded=f.getLinesAdded();
            }
            locTouched += f.getLinesAdded() - f.getLinesDeleted() + f.getLinesChanged(); //TODO check
            churn+= f.getLinesAdded() + f.getLinesDeleted();
            if((f.getLinesAdded() + f.getLinesDeleted())>maxChurn){
                maxChurn=churn;
            }
        }

        int nr = computeNr(release, jf);
        double avglocadded = 0;
        double avgChurn = 0;
        if(nr!=0){
            avglocadded = locAdded/(double)nr;
            avgChurn = churn/(double)nr;
        }


        jf.setLoc(loc);
        jf.setLocAdded(locAdded);
        jf.setMaxLocAdded(maxLocAdded);
        jf.setAvgLocAdded(avglocadded);
        jf.setLocTouched(locTouched);
        jf.setNr(nr);;
        jf.setChurn(churn);
        jf.setMaxChurn(maxChurn);
        jf.setAvgChurn(avgChurn);
        jf.setAuthors(computeAuthors(release, jf));

        Set<GHCommit.File> changeSet = jf.getChangeSet();
        int maxChangeSet = jf.getMaxChangeSetSize();
        int changeAvgNumerator = 0;
        double avgChangeSet = 0;
        for (GHCommit c : jf.getCommitsHistory().get(release)) {
            changeSet.addAll(c.getFiles());
            if(maxChangeSet< c.getFiles().size()){
                maxChangeSet = c.getFiles().size();
            }
            changeAvgNumerator+= c.getFiles().size();
        }

        if (nr!=0)
            avgChangeSet = changeAvgNumerator/(double)nr;

        jf.setChangeSet(changeSet);
        jf.setMaxChangeSetSize(maxChangeSet);
        jf.setAvgChangeSetSize(avgChangeSet);
        jf.setNfix(computeNfix(release.getVersionNumber(), jf, bugs));

    }

    private int computeNr(Release release, JavaFile jf){
        int nr = jf.getNr();
        int currentReleaseNR = jf.getFileHistory().get(release).size();

        nr += currentReleaseNR;
        return nr;
    }

    private Set<GitUser> computeAuthors(Release release, JavaFile jf) throws IOException {
        Set<GitUser> authors = jf.getAuthors();
        for (GHCommit c : jf.getCommitsHistory().get(release)) {
            GitUser author = c.getCommitShortInfo().getAuthor();
            authors.add(author);
        }
        return authors;
    }
}
