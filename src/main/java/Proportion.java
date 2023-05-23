import boundaries.JiraBoundary;
import entities.Bug;
import entities.JiraTicket;
import entities.Release;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class Proportion {
    private final String[] projects = {"AVRO", "OPENJPA", "STORM", "ZOOKEEPER", "TAJO"};
    double pColdStart = 0;
    // double pColdStart = 1.7670585166374084;

    public Proportion() throws IOException, URISyntaxException {
        coldStart();
        System.out.println("pColdStart: " + pColdStart);
    }

    private void coldStart() throws IOException, URISyntaxException {
        for(String project:projects){
            pColdStart += computePColdStart(project);
        }
        pColdStart = pColdStart /projects.length;
    }

    private double computePColdStart(String project) throws IOException, URISyntaxException {
        ArrayList<Release> releases = ReleaseManager.getReleases(project);
        ArrayList<JiraTicket> tickets = JiraBoundary.getTickets(project, releases);

        ArrayList<Bug> bugs = BugManager.getValidBugsMin(tickets, releases);

        double pNumerator = 0;
        // we exclude issues that are not post release, this means we exclude defects that have IV=FV
        // for each defect we check the AV consistency, if IV <= OV
        for(int i = 0; i < bugs.size(); i++){
            int iv = bugs.get(i).getInjectedVersion().getVersionNumber();
            int ov = bugs.get(i).getOpeningVersion().getVersionNumber();
            int fv = bugs.get(i).getFixedVersion().getVersionNumber();
            if(iv > ov || ov > fv || iv == fv){
                bugs.remove(i);
                i--;
            } else if( ov==fv){
                //If FV equals OV, then FV − OV is set to one to avoid divide
                //by zero cases.
                pNumerator += fv - iv;
            } else{
                pNumerator += (fv - iv)/(double)(fv-ov);
            }
        }
        return pNumerator / bugs.size();
    }

    // parameter bugs is the list of bugs of the project without an injected version
    public ArrayList<Bug> AddInjectedVersionsMovingWindow(ArrayList<Bug> bugList, ArrayList<Release> releases){
        int movingWindow = (int) Math.max(Math.round(bugList.size()/100.0), 1); // l'1% dei difetti totali

        System.out.println("Inizio calcolo di IV con Proportion Moving window: " + movingWindow);

        for(int i=0; i<bugList.size();i++){
            Bug b = bugList.get(i);
            int ov = b.getOpeningVersion().getVersionNumber();
            int fv = b.getFixedVersion().getVersionNumber();

            if (b.getInjectedVersion()==null){
                double p;
                ArrayList<Bug> bugsToComputeProportionOn = findBugsToComputeProportionOn(bugList, i, movingWindow);
                if(bugsToComputeProportionOn == null)
                    p = pColdStart;
                else{
                    // compute proportion
                    double pNumerator = 0;
                    for(Bug bug:bugsToComputeProportionOn){
                        int currIV = bug.getInjectedVersion().getVersionNumber();
                        int currOV = bug.getOpeningVersion().getVersionNumber();
                        int currFV = bug.getFixedVersion().getVersionNumber();
                        if(currFV == currOV)
                            pNumerator += currFV - currIV;
                        else
                            pNumerator += (currFV - currIV) / (double)(currFV - currOV);
                    }
                    p = pNumerator / (double)movingWindow;
                }
                // P = (FV - IV) / (FV - OV) therefore IV = FV - P * (FV - OV)

                /*For each defect, we computed the IV as IV = (FV − OV ) ∗ P_Moving Window. If
                FV equals OV, then IV equals FV. However, we excluded defects that were not
                post-release. Therefore, we set FV − OV equal to 1 to assure IV is not equal to FV
                 */
                int iv;
                if(fv == ov)
                    iv = (int) Math.max(Math.round(fv - p),1);
                else
                    iv = (int) Math.max(Math.round(fv - (p * (fv - ov))),1);


                b.setInjectedVersion(releases.get(Math.max(iv-1,0)));
                bugList.set(i, b);
            }
        }

        return bugList;
    }

    private ArrayList<Bug> findBugsToComputeProportionOn(ArrayList<Bug> bugs, int i, int movingWindow) {
        // find the movingWindow elements before the current bug to compute proportion on
        ArrayList<Bug> bugsToComputeProportionOn = new ArrayList<>();
        int j = i-1;
        while(bugsToComputeProportionOn.size() < movingWindow && j >= 0){
            // do not use defects injected and fixed in the same version to compute proportion
            if(bugs.get(j).getInjectedVersion().getVersionNumber() < bugs.get(j).getFixedVersion().getVersionNumber())
                bugsToComputeProportionOn.add(bugs.get(j));
            j--;
        }

        if(bugsToComputeProportionOn.size() < movingWindow)
            return null;
        else return bugsToComputeProportionOn;
    }
}