package likeabaos.tools.sbr;

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
    private String subject;
    private String from;
    private String to;
    private String cc;
    private String bcc;
    private final Map<Integer, ReportPart> parts = new TreeMap<Integer, ReportPart>();
    private String output = "csv";
    private String outputPath;
    private boolean emailAsPlainText = false;
    private boolean emailAttachment = true;

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
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

    public Map<Integer, ReportPart> getParts() {
	return parts;
    }

    public void addPart(int orderNum, ReportPart part) {
	this.parts.put(orderNum, part);
    }

    public String getOutput() {
	return output;
    }

    public void setOutput(String output) {
	this.output = output;
    }

    public String getOutputPath() {
	return outputPath;
    }

    public void setOutputPath(String outputPath) {
	this.outputPath = outputPath;
    }

    public boolean isEmailAsPlainText() {
	return emailAsPlainText;
    }

    public void setEmailAsPlainText(boolean emailAsPlainText) {
	this.emailAsPlainText = emailAsPlainText;
    }

    public boolean isEmailAttachment() {
	return emailAttachment;
    }

    public void setEmailAttachment(boolean emailAttachment) {
	this.emailAttachment = emailAttachment;
    }

    public static File locateConfigFile(File file) {
	if (file != null && file.isFile()) {
	    log.info("Used provided file: " + file.getName());
	    return file;
	}

	log.info("Looking for most recent json file in the current dir...");
	File theFile = null;
	long lastModified = 0L;
	for (File f : new File(".").listFiles()) {
	    if (f.isFile() && f.getName().toLowerCase().endsWith(".json") && f.lastModified() > lastModified) {
		theFile = f;
		lastModified = f.lastModified();
	    }
	}

	log.info("Found file: " + theFile == null ? "null" : theFile.getName());
	return theFile;
    }

    public static ReportConfig fromFile(File configFile)
	    throws JsonSyntaxException, JsonIOException, FileNotFoundException {
	log.info("Loading configuration from {}", configFile.getAbsolutePath());
	return new Gson().fromJson(new BufferedReader(new FileReader(configFile)), ReportConfig.class);
    }
}