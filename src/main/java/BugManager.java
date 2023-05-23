import boundaries.JiraBoundary;
import entities.Bug;
import entities.JiraTicket;
import entities.Release;
import org.kohsuke.github.GHCommit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;

public class BugManager {

    public static ArrayList<Bug> getBugs(String projName, ArrayList<Release> releases) throws IOException, URISyntaxException {

        ArrayList<JiraTicket> tickets = JiraBoundary.getTickets(projName, releases);
        ArrayList<Bug> bugs = new ArrayList<>();
        System.out.println("TICKETS: " + tickets.size());

        for(JiraTicket ticket:tickets){
            Release openingVersion = computeOpeningVersion(ticket, releases);
            Release fixedVersion = ticket.getFixVersion();

            if (openingVersion == null || fixedVersion == null || openingVersion.getVersionNumber() > fixedVersion.getVersionNumber()) {
                continue;
            }


            Bug currBug = new Bug();

            ArrayList<GHCommit> linkedCommits = new ArrayList<>();

            for(Release r:releases) {
                for (GHCommit c : r.getCommits()) {
                    if (c.getCommitShortInfo().getMessage().contains(ticket.getKey())) {
                        linkedCommits.add(c);

                        for (GHCommit.File file : c.getFiles()) {
                            if (file.getFileName().contains(".java") && !file.getFileName().contains("/test")) {
                                currBug.addBuggyFile(file.getFileName());
                            }
                        }
                    }
                }
            }

            currBug.setCommits(linkedCommits);

            currBug.setOpeningVersion(openingVersion);
            currBug.setFixedVersion(fixedVersion);

            try {
                ArrayList<Release> affectedVersions = ticket.getAffectedVersions();
                affectedVersions.sort(Comparator.comparing(Release::getVersionNumber));
                currBug.setInjectedVersion(ticket.getAffectedVersions().get(0));
            } catch (Exception ignored) {}

            bugs.add(currBug);
        }

        Proportion proportion = new Proportion();
        System.out.println("Bugs: " + bugs.size());
        return proportion.AddInjectedVersionsMovingWindow(bugs, releases);
    }

    // Bug manager for cold start proportion
    public static ArrayList<Bug> getValidBugsMin(ArrayList<JiraTicket> tickets, ArrayList<Release> releases) {

        ArrayList<Bug> bugs = new ArrayList<>();
        for(JiraTicket ticket:tickets){
            if(ticket.getAffectedVersions().size() == 0 || ticket.getAffectedVersions().get(0) == null)
                continue;

            Bug currBug = new Bug();

            Release injectedVersion = ticket.getAffectedVersions().get(0);
            Release openingVersion = computeOpeningVersion(ticket, releases);
            Release fixedVersion = ticket.getFixVersion();

            if(openingVersion == null || fixedVersion == null || injectedVersion == null) continue; // il bug è ignorato perchè mancano i dati per analizzarlo
            if(openingVersion.getVersionNumber()>fixedVersion.getVersionNumber()) continue; // il bug è ignorato perchè semanticamente sbagliato

            currBug.setInjectedVersion(injectedVersion);
            currBug.setOpeningVersion(openingVersion);
            currBug.setFixedVersion(fixedVersion);

            bugs.add(currBug);
        }
        return bugs;
    }

    private static Release computeOpeningVersion(JiraTicket ticket, ArrayList<Release> releases) {

        for(Release r:releases){
            if(!r.getReleaseDate().isBefore(ticket.getCreated().toLocalDate()))
                return r;
        }
        return null;
    }
}
