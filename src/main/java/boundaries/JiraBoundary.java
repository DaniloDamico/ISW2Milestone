package boundaries;

import entities.JiraTicket;
import entities.Release;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class JiraBoundary {

    public static Set<Release> getReleaseSet(String projName) throws IOException, URISyntaxException {

        String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName;
        JSONObject json = readJsonFromUrl(url);
        JSONArray versions = json.getJSONArray("versions");

        Set<Release> releaseSet = new HashSet<>();
        for (Object obj:versions) {
            JSONObject ver = (JSONObject) obj;

            String name = "";
            String id = "";
            if(ver.has("releaseDate")) {
                if (ver.has("name"))
                    name = ver.get("name").toString();
                if (ver.has("id"))
                    id = ver.get("id").toString();

                releaseSet.add(getRelease(ver.get("releaseDate").toString(),name,id));
            }
        }
        return releaseSet;


    }

    private static Release getRelease(String strDate, String name, String id) {
        Release rel = new Release();
        rel.setReleaseDate(LocalDate.parse(strDate));
        rel.setVersionId(Integer.parseInt(id));
        rel.setVersionName(name);

        return rel;
    }

    public static ArrayList<JiraTicket> getTickets(String projName, ArrayList<Release> releases) throws IOException, URISyntaxException {

        ArrayList<JiraTicket> tickets = new ArrayList<>();

        int i=0, j, total;
        JSONArray issues = new JSONArray();
        do {
            j=i+1000;

            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                    + projName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                    + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,fixVersions,versions,created&startAt="
                    + i + "&maxResults=" + j;
            JSONObject json = readJsonFromUrl(url);

            issues.putAll(json.getJSONArray("issues"));

            total = json.getInt("total");
            i=j+1;
        } while(i<total);

        for(Object obj:issues){
            JSONObject issue = (JSONObject) obj;
            JiraTicket ticket = getTicket(issue, releases);
            if(ticket!=null)
                tickets.add(ticket);
        }

        return tickets;
    }

    private static JiraTicket getTicket(JSONObject ticket, ArrayList<Release> releases){
        JiraTicket newTicket = new JiraTicket();

        newTicket.setKey(ticket.get("key").toString());
        newTicket.setCreated(LocalDateTime.parse(ticket.getJSONObject("fields").get("created").toString().substring(0,23)));

        JSONArray versions = ticket.getJSONObject("fields").getJSONArray("versions");

        for(Object v:versions){
            JSONObject ver = (JSONObject)v;

            LocalDate verDate;
            try {
                verDate = LocalDate.parse(ver.get("releaseDate").toString());
            } catch (Exception e){
                continue;
            }

            for(Release r:releases){
                if(r.getReleaseDate().equals(verDate) || r.getVersionName().equals(ver.get("name").toString())){
                    newTicket.addVersion(r);
                }
            }
        }

        try {
            JSONArray fixVersions = ticket.getJSONObject("fields").getJSONArray("fixVersions");
            JSONObject fixVersion = (JSONObject) fixVersions.get(fixVersions.length() - 1);
            LocalDate fixDate = LocalDate.parse(fixVersion.get("releaseDate").toString());
            for (Release r : releases) {
                if (r.getReleaseDate().equals(fixDate) || r.getVersionName().equals(fixVersion.get("name").toString())) {
                    newTicket.setFixVersion(r);
                }
            }
        } catch (Exception e){
            return null;
        }

        return newTicket;
    }

    private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException, URISyntaxException {

        try (InputStream is = new URI(url).toURL().openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
