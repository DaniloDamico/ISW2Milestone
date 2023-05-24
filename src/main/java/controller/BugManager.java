package controller;

import boundaries.JiraBoundary;
import entities.Bug;
import entities.JiraTicket;
import entities.Release;
import org.kohsuke.github.GHCommit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BugManager {

    private BugManager() {}

    public static List<Bug> getBugs(String projName, List<Release> releases) throws IOException, URISyntaxException {

        ArrayList<JiraTicket> tickets = (ArrayList<JiraTicket>) JiraBoundary.getTickets(projName, releases);
        ArrayList<Bug> bugs = new ArrayList<>();
        Logger.getLogger("BugManager").log(Level.INFO, "Tickets: {0}", tickets.size());

        for (JiraTicket ticket : tickets) {
            Release openingVersion = computeOpeningVersion(ticket, releases);
            Release fixedVersion = ticket.getFixVersion();

            if (openingVersion == null || fixedVersion == null || openingVersion.getVersionNumber() > fixedVersion.getVersionNumber()) {
                continue;
            }


            Bug currBug = new Bug();

            ArrayList<GHCommit> linkedCommits = new ArrayList<>();
            linkCommitsAndBuggyFiles(releases, ticket, currBug, linkedCommits);
            currBug.setCommits(linkedCommits);

            currBug.setOpeningVersion(openingVersion);
            currBug.setFixedVersion(fixedVersion);

            try {
                ArrayList<Release> affectedVersions = (ArrayList<Release>) ticket.getAffectedVersions();
                affectedVersions.sort(Comparator.comparing(Release::getVersionNumber));
                currBug.setInjectedVersion(ticket.getAffectedVersions().get(0));
            } catch (Exception ignored) {
                //ignore exception
            }

                bugs.add(currBug);
            }

            Proportion proportion = new Proportion();
            Logger.getLogger("BugManager").log(Level.INFO, "Bugs: {0}", + bugs.size());
            return proportion.addInjectedVersionsMovingWindow(bugs, releases);
        }

    private static void linkCommitsAndBuggyFiles(List<Release> releases, JiraTicket ticket, Bug currBug, ArrayList<GHCommit> linkedCommits) throws IOException {
        for (Release r : releases) {
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
    }


    // Bug manager for cold start proportion
    public static List<Bug> getValidBugsMin(List<JiraTicket> tickets, List<Release> releases) {
        ArrayList<Bug> bugs = new ArrayList<>();
        for (JiraTicket ticket : tickets) {
            if (!ticket.getAffectedVersions().isEmpty() && ticket.getAffectedVersions().get(0) != null) {
                Bug currBug = new Bug();

                Release injectedVersion = ticket.getAffectedVersions().get(0);
                Release openingVersion = computeOpeningVersion(ticket, releases);
                Release fixedVersion = ticket.getFixVersion();

                if (openingVersion != null && fixedVersion != null && injectedVersion != null && openingVersion.getVersionNumber() <= fixedVersion.getVersionNumber()) {
                    currBug.setInjectedVersion(injectedVersion);
                    currBug.setOpeningVersion(openingVersion);
                    currBug.setFixedVersion(fixedVersion);

                    bugs.add(currBug);
                }
            }
        }
        return bugs;
    }


    private static Release computeOpeningVersion(JiraTicket ticket, List<Release> releases) {

        for(Release r:releases){
            if(!r.getReleaseDate().isBefore(ticket.getCreated().toLocalDate()))
                return r;
        }
        return null;
    }
}
