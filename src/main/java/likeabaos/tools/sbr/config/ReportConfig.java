package likeabaos.tools.sbr.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class ReportConfig {
    private static final Logger log = LogManager.getLogger();
    private String name;
    private String summary;
    private final Map<Integer, ReportPart> parts = new TreeMap<Integer, ReportPart>();
    private EmailConfig emailConfig;
    private OutputConfig outputConfig;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Map<Integer, ReportPart> getParts() {
        return parts;
    }

    public void addPart(int orderNum, ReportPart part) {
        this.parts.put(orderNum, part);
    }

    public EmailConfig getEmailConfig() {
        return emailConfig;
    }

    public void setEmailConfig(EmailConfig emailConfig) {
        this.emailConfig = emailConfig;
    }

    public OutputConfig getOutputConfig() {
        return outputConfig;
    }

    public void setOutputConfig(OutputConfig outputConfig) {
        this.outputConfig = outputConfig;
    }

    public static ReportConfig fromFile(File configFile)
            throws JsonSyntaxException, JsonIOException, FileNotFoundException {
        log.info("Loading configuration from {}", configFile.getAbsolutePath());
        return new Gson().fromJson(new BufferedReader(new FileReader(configFile)), ReportConfig.class);
    }
}
