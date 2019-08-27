package likeabaos.tools.sbr;

import java.io.File;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private final Logger log = LogManager.getLogger();

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

    @Parameters(index = "3", description = "The full path to report configuration file")
    private File config_file;

    @Option(names = { "-d", "--debug" }, description = "Turn on debug and all logging levels")
    private boolean debug_on = false;

    public String getConnectionString() {
	return this.db_connection_string;
    }

    public String getUsername() {
	return this.db_username;
    }

    public String getPassword() {
	return this.db_password;
    }

    public File getConfigFile() {
	return this.config_file;
    }

    public boolean isDebugOn() {
	return this.debug_on;
    }

    public Integer call() {
	log.info("Program started");
	try {
	    Database db = new Database(this.db_connection_string, this.db_username, this.db_username);
	    File configFile = ReportConfig.locateConfigFile(this.config_file, new File("."));
	    ReportConfig config = ReportConfig.fromFile(configFile);
	    Reporter rpt = new Reporter(db, config);
	    rpt.run();
	    new Sender(rpt).send();
	} catch (Exception e) {
	    log.catching(e);
	    return -1;
	}
	log.info("Proram completed");
	return 0;
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
