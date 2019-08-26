package likeabaos.tools.sbr;

import java.util.List;

public class ReportPart {
    private String header;
    private String description;
    private String sql;
    private boolean enabled = true;
    private List<List<String>> result;

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
	return sql;
    }

    public void setSql(String sql) {
	this.sql = sql;
    }

    public boolean isEnabled() {
	return enabled;
    }

    public void setEnabled(boolean enabled) {
	this.enabled = enabled;
    }
    
    public List<List<String>> getResult() {
	return this.result;
    }
    
    public void setResult(List<List<String>> result) {
	this.result = result;
    }
}
