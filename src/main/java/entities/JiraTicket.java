package entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JiraTicket {
    private final ArrayList<Release> affectedVersions = new ArrayList<>();
    private String key;
    private LocalDateTime created;

    private Release fixVersion;

    public Release getFixVersion() {
        return fixVersion;
    }

    public void setFixVersion(Release fixVersion) {
        this.fixVersion = fixVersion;
    }

    public List<Release> getAffectedVersions() {
        return affectedVersions;
    }

    public void addVersion(Release ver) {
        affectedVersions.add(ver);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }
}
