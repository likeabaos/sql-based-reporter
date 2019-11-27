package likeabaos.tools.sbr.config;

public class ReportPart {
    private String header;
    private String description;
    private String[] sql;
    private boolean enabled = true;

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSql() {
        return String.join(System.lineSeparator(), this.sql);
    }

    public void setSql(String[] sql) {
        this.sql = sql;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
