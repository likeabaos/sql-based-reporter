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
        if (this.sql != null)
            return String.join(System.lineSeparator(), this.sql);
        else
            return null;
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
