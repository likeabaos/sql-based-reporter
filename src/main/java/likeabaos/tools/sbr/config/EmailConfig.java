package likeabaos.tools.sbr.config;

import java.util.ArrayList;
import java.util.List;

public class EmailConfig {
    private boolean enabled = true;
    private String subject;
    private String from;
    private String to;
    private String cc = "";
    private String bcc = "";
    private List<Integer> attachments;
    private boolean displayTable;
    private boolean displayLink;
    private boolean displayEmptyReport = true;
    private boolean emailWhenNoData = true;
    private int emailRowsLimit = 10;
    private String[] valuesInjectionSQL;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public List<Integer> getAttachments() {
        if (attachments == null) {
            attachments = new ArrayList<>();
        }
        return attachments;
    }

    public void setAttachments(List<Integer> attachments) {
        this.attachments = attachments;
    }

    public boolean isDisplayTable() {
        return displayTable;
    }

    public void setDisplayTable(boolean displayTable) {
        this.displayTable = displayTable;
    }

    public boolean isDisplayLink() {
        return displayLink;
    }

    public void setDisplayLink(boolean displayLink) {
        this.displayLink = displayLink;
    }

    public boolean isDisplayEmptyReport() {
        return displayEmptyReport;
    }

    public void setDisplayEmptyReport(boolean displayEmptyReport) {
        this.displayEmptyReport = displayEmptyReport;
    }

    public boolean isEmailWhenNoData() {
        return emailWhenNoData;
    }

    public void setEmailWhenNoData(boolean emailWhenNoData) {
        this.emailWhenNoData = emailWhenNoData;
    }

    public int getEmailRowsLimit() {
        return emailRowsLimit;
    }

    public void setEmailRowsLimit(int emailRowsLimit) {
        this.emailRowsLimit = emailRowsLimit;
    }

    public String getValuesInjectionSQL() {
        if (this.valuesInjectionSQL != null)
            return String.join(System.lineSeparator(), this.valuesInjectionSQL);
        else
            return null;
    }

    public void setValuesInjectionSQL(String[] sql) {
        this.valuesInjectionSQL = sql;
    }

}
