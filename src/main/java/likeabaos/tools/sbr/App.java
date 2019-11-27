package likeabaos.tools.sbr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import likeabaos.tools.sbr.config.ReportConfig;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "SQL-Based Reporter",
        mixinStandardHelpOptions = true,
        versionProvider = likeabaos.tools.sbr.App.VersionProvider.class,
        description = "Can be used to generate scheduled reports using SQL(s)")
public class App implements Callable<Integer> {
    private static final Logger LOG = LogManager.getLogger();

    public static void main(String[] args) throws SQLException {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }

    @Parameters(index = "0", description = "The JDBC connection string for target database")
    private String db_connection_string;

    @Parameters(index = "1", description = "The database account username")
    private String db_username;

    @Parameters(index = "2", description = "The database account password")
    private String db_password;

    @Parameters(index = "3", description = "The full path to report definition file")
    private File report_definition_file;

    @Option(names = { "-c", "--config" },
            description = "Full path to the alternate configuration directory. Default is the directory this app started at.")
    private File config_dir = new File(".");

    @Option(names = { "-d", "--debug" }, description = "Turn on debug mode.")
    private boolean debug_on = false;

    @Option(names = { "-m", "--mail" },
            description = "Account name and password for sending email if needed. Format account:password.")
    private String email_credentials = ":";

    public String getConnectionString() {
        return this.db_connection_string;
    }

    public String getUsername() {
        return this.db_username;
    }

    public String getPassword() {
        return this.db_password;
    }

    public File getReportDefinitionFile() {
        return this.report_definition_file;
    }

    public File getConfigDir() {
        return this.config_dir;
    }

    public boolean isDebugOn() {
        return this.debug_on;
    }

    public String getEmailCredentials() {
        return this.email_credentials;
    }

    public Integer call() {
        LOG.info("Program started");
        StopWatch watch = new StopWatch();

        try {
            watch.start();
            if (this.isDebugOn()) {
                Configurator.setRootLevel(Level.TRACE);
                printParameters(this);
            }
            Database db = new Database(this.getConnectionString(), this.getUsername(), this.getPassword());
            ReportConfig config = ReportConfig.fromFile(this.getReportDefinitionFile());
            Authenticator auth = App.getMailAuthenticator(this.getEmailCredentials());
            Reporter rpt = new Reporter(db, config, auth, this.getConfigDir());
            rpt.run();
        } catch (Exception e) {
            LOG.catching(e);
            return -1;
        } finally {
            watch.stop();
        }

        LOG.info("Proram completed in: {}", watch.toString());
        return 0;
    }

    public static void printParameters(App app) {
        LOG.debug("Connection String: {}", app.getConnectionString());
        LOG.debug("Database Username: {}", app.getUsername());
        LOG.debug("Database password: {}", StringUtils.isBlank(app.getPassword()) ? "***Blank***" : "***Hidden***");
        LOG.debug("Report Definition: {}", app.getReportDefinitionFile().getAbsolutePath());
        LOG.debug("Report Config Dir: {}", app.getConfigDir().getAbsolutePath());
        LOG.debug("Is Debug On?: {}", app.isDebugOn());
        LOG.debug("Email Credentials: {}", (app.getEmailCredentials().length() <= 1) ? "***Blank***" : "***Hidden***");
    }

    public static Authenticator getMailAuthenticator(String creds) {
        String[] myCreds = StringUtils.split(creds, ":", 2);
        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myCreds[0], myCreds[1]);
            }
        };
        return auth;
    }

    public static Properties loadProperties(File file) throws FileNotFoundException, IOException {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(file)) {
            props.load(input);
        }
        return props;
    }

    public static class VersionProvider implements IVersionProvider {
        @Override
        public String[] getVersion() throws Exception {
            try (InputStream input = this.getClass().getClassLoader().getResourceAsStream("build.properties")) {
                Properties prop = new Properties();
                prop.load(input);
                return new String[] { prop.getProperty("app_version") };
            }
        }
    }
}
