package likeabaos.tools.sbr;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import javax.mail.Authenticator;
import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import likeabaos.tools.sbr.config.ReportConfig;
import likeabaos.tools.sbr.output.BaseOutput;
import likeabaos.tools.sbr.output.CSV;

public class Reporter {
    private final Logger log = LogManager.getLogger();

    private final Database db;
    private final ReportConfig config;
    private Map<Integer, File> tempResults;
    private Map<Integer, File> outputResults;
    private boolean deleteOutputOnExit = false;
    private Properties emailProps;
    private Authenticator emailAuth;

    public Reporter(Database db, ReportConfig config, Properties props, Authenticator auth) {
        this.db = db;
        this.config = config;
        this.emailProps = props;
        this.emailAuth = auth;
    }

    Map<Integer, File> getTempResults() {
        if (outputResults == null)
            outputResults = new TreeMap<>();
        return outputResults;
    }

    Map<Integer, File> getOutputResults() {
        if (tempResults == null)
            tempResults = new TreeMap<>();
        return tempResults;
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

        if (this.config.getOutputConfig() != null)
            this.save();
        else
            log.debug("No ouptut config");

        if (this.config.getEmailConfig() != null && this.config.getEmailConfig().isEnabled())
            this.send();
        else
            log.debug("No email config or set to disabled");

        log.traceExit();
    }

    void query() throws Exception {
        File outPath = new File(this.config.getOutputConfig().getOutputPath());
        outPath.mkdirs();
        File tempDir = Files.createTempDirectory(outPath.toPath(), "tmp").toFile();
        tempDir.deleteOnExit();

        for (Entry<Integer, ReportPart> item : this.config.getParts().entrySet()) {
            int orderNum = item.getKey();
            ReportPart part = item.getValue();

            log.info("Processing report part# {}: {}", orderNum, part.getHeader());
            if (!part.isEnabled()) {
                log.warn("This report part is NOT enabled. Continue to the next part.");
                continue;
            }

            try (Connection conn = this.db.connect();
                    Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                String finalSql = part.getSql();
                log.debug("SQL to run:{}{}", System.lineSeparator(), finalSql);
                ResultSet rs = stmt.executeQuery(finalSql);

                // Now we have data for each part. However, we don't want to accumulate all data
                // from each part (may crash due to too much data in memory); or keep the
                // connection from database alive too long. Therefore, we want to save these
                // data into temp files so that we can come back and process them later.
                File tempFile = BaseOutput.saveResultToTempFiles(rs, tempDir, part.getHeader());
                this.getTempResults().put(orderNum, tempFile);

            } catch (Exception e) {
                log.error("Found error while running report");
                log.catching(e);
                continue;
            }
        }
    }

    void save() throws Exception {
        for (Entry<Integer, File> entry : this.getTempResults().entrySet()) {
            int orderNum = entry.getKey();
            File tempFile = entry.getValue();

            ReportPart part = this.config.getParts().get(orderNum);
            String className = BaseOutput.getClassName(this.config.getOutputConfig().getOutput());
            log.debug("Output class is {}", className);

            BaseOutput output = (BaseOutput) Class.forName(className).getConstructor(Boolean.TYPE)
                    .newInstance(this.isDeleteOutputOnExit());
            output.setName(part.getHeader());
            output.setCopySource(output instanceof CSV);
            output.setSourceFile(tempFile);
            output.setOutputConfig(this.config.getOutputConfig());
            output.save();
            this.getOutputResults().put(orderNum, output.getOutputFile());
        }
    }

    void send() throws UnsupportedEncodingException, MessagingException {
        Emailer sender = new Emailer(this.emailProps, this.emailAuth);
        sender.send(this.config, this);
    }
}
