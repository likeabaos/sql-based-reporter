package likeabaos.tools.sbr;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.mail.Authenticator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import likeabaos.tools.sbr.config.EmailConfig;
import likeabaos.tools.sbr.config.ReportConfig;
import likeabaos.tools.sbr.config.ReportPart;
import likeabaos.tools.sbr.output.BaseOutput;
import likeabaos.tools.sbr.output.CSV;

public class Reporter {
    private final Logger log = LogManager.getLogger();

    private final Database db;
    private final ReportConfig config;
    private final Authenticator emailAuth;
    private final File configDir;
    private Map<Integer, File> tempResults;
    private Map<Integer, File> outputResults;
    private List<Integer> emptyResults;
    private boolean deleteOutputOnExit = false;

    public Reporter(Database db, ReportConfig config, Authenticator auth, File configDir) {
        this.db = db;
        this.config = config;
        this.emailAuth = auth;
        this.configDir = configDir;
    }

    public ReportConfig getConfig() {
        return config;
    }

    public Authenticator getEmailAuth() {
        return emailAuth;
    }

    public File getConfigDir() {
        return configDir;
    }

    public Map<Integer, File> getTempResults() {
        if (outputResults == null)
            outputResults = new TreeMap<>();
        return outputResults;
    }

    public Map<Integer, File> getOutputResults() {
        if (tempResults == null)
            tempResults = new TreeMap<>();
        return tempResults;
    }

    public List<Integer> getEmptyResults() {
        if (emptyResults == null)
            emptyResults = new ArrayList<>();
        return emptyResults;
    }

    public boolean isDeleteOutputOnExit() {
        return deleteOutputOnExit;
    }

    public void setDeleteOutputOnExit(boolean deleteOutputOnExit) {
        this.deleteOutputOnExit = deleteOutputOnExit;
    }

    public void run() throws Exception {
        log.traceEntry();

        this.query();

        if (this.getConfig().getOutputConfig() != null && this.getConfig().getOutputConfig().isEnabled())
            this.save();
        else
            log.debug("No ouptut config");

        if (this.getConfig().getEmailConfig() != null && this.getConfig().getEmailConfig().isEnabled())
            this.send();
        else
            log.debug("No email config or set to disabled");

        log.traceExit();
    }

    void query() throws Exception {
        File tempOutPath = new File("./output");
        tempOutPath.mkdirs();
        File tempDir = Files.createTempDirectory(tempOutPath.toPath(), "tmp").toFile();
        tempDir.deleteOnExit();

        for (Entry<Integer, ReportPart> item : this.getConfig().getParts().entrySet()) {
            int orderNum = item.getKey();
            ReportPart part = item.getValue();

            log.info("Querying report part# {}: {}", orderNum, part.getHeader());
            if (!part.isEnabled()) {
                log.warn("This report part is NOT enabled. Continue to the next part.");
                continue;
            }

            StopWatch watch = new StopWatch();
            watch.start();
            try (Connection conn = this.db.connect();
                    Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                String sql = part.getSql();
                log.debug("SQL to run:{}{}", System.lineSeparator(), sql);
                ResultSet rs = stmt.executeQuery(sql);

                // Now we have data for each part. However, we don't want to accumulate all data
                // from each part (may crash due to too much data in memory); or keep the
                // connection from database alive too long. Therefore, we want to save these
                // data into temp files so that we can come back and process them later.
                watch.split();
                File tempFile = BaseOutput.saveResultToTempFiles(rs, tempDir, part.getHeader());
                this.getTempResults().put(orderNum, tempFile);

                watch.stop();
                log.debug("Query completed in: {}", watch.toSplitString());

                if (BaseOutput.isTempFileEmpty(tempFile, true))
                    this.getEmptyResults().add(orderNum);

            } catch (Exception e) {
                log.error("Found error while running report part #{}", orderNum);
                log.debug("Is throwing exception on error: {}", part.isThrowExceptionOnError());
                if (part.isThrowExceptionOnError())
                    throw e;
                else {
                    log.catching(e);
                    continue;
                }
            }
        }
    }

    void save() throws Exception {
        for (Entry<Integer, File> entry : this.getTempResults().entrySet()) {
            StopWatch watch = new StopWatch();
            watch.start();

            int orderNum = entry.getKey();
            File tempFile = entry.getValue();

            log.debug("Saving file for report part #{}", orderNum);
            ReportPart part = this.getConfig().getParts().get(orderNum);
            String className = BaseOutput.getClassName(this.getConfig().getOutputConfig().getOutput());
            log.debug("Output class is {}", className);

            BaseOutput output = (BaseOutput) Class.forName(className).getConstructor(Boolean.TYPE)
                    .newInstance(this.isDeleteOutputOnExit());
            output.setName(part.getHeader());
            output.setCopySource(output instanceof CSV);
            output.setSourceFile(tempFile);
            output.setOutputConfig(this.getConfig().getOutputConfig());
            output.setSaveSeparateFile(this.getConfig().getOutputConfig().isSaveSeparateFile());
            output.save();
            this.getOutputResults().put(orderNum, output.getOutputFile());

            watch.stop();
            log.debug("Save completed in: {}", watch.toString());
        }
    }

    void send() throws Exception {
        EmailConfig config = this.getConfig().getEmailConfig();

        log.info("Emailing report(s)...");
        if (!config.isEmailWhenNoData() && this.areAllResultsEmpty()) {
            log.info("No email will be sent because emailWhenNoData is turn off and all reports returned empty.");
            return;
        }

        log.debug("Replacing placeholers with values...");
        Map<String, String> valuesInjection = this.queryValuesInjection();
        for (Entry<String, String> item : valuesInjection.entrySet()) {
            String searchKey = "{{" + item.getKey() + "}}";
            for (ReportPart part : this.getConfig().getParts().values()) {
                if (part.isEnabled()) {
                    part.setDescription(StringUtils.replace(part.getDescription(), searchKey, item.getValue()));
                }
            }
            this.getConfig().setName(StringUtils.replace(this.getConfig().getName(), searchKey, item.getValue()));
            this.getConfig().setSummary(StringUtils.replace(this.getConfig().getSummary(), searchKey, item.getValue()));
            config.setSubject(StringUtils.replace(config.getSubject(), searchKey, item.getValue()));
        }

        Emailer sender = new Emailer(this);
        sender.run();
    }

    boolean areAllResultsEmpty() {
        boolean empty = true;
        for (Entry<Integer, ReportPart> item : this.getConfig().getParts().entrySet()) {
            if (item.getValue().isEnabled()) {
                if (!this.getEmptyResults().contains(item.getKey()))
                    empty = false;
            }
        }
        return empty;
    }

    Map<String, String> queryValuesInjection() throws Exception {
        EmailConfig config = this.getConfig().getEmailConfig();
        String sql = config.getValuesInjectionSQL();
        Map<String, String> values = new HashMap<>();
        if (StringUtils.isNotBlank(sql)) {
            try (Connection conn = this.db.connect();
                    Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {

                log.debug("SQL to run:{}{}", System.lineSeparator(), sql);
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        String name = rs.getMetaData().getColumnName(i);
                        String value = (String) rs.getObject(i);
                        values.put(name, value);
                    }
                    // We only use the first row anyway. This is to ensure
                    // the SQL return multiple rows
                    break;
                }
            }
        }
        return values;
    }
}
