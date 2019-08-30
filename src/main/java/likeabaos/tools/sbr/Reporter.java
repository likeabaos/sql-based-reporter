package likeabaos.tools.sbr;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import likeabaos.tools.sbr.output.BaseOutput;
import likeabaos.tools.sbr.output.Helper;

public class Reporter {
    private final Logger log = LogManager.getLogger();

    private final Database db;
    private final ReportConfig config;

    public Reporter(Database db, ReportConfig config) {
	this.db = db;
	this.config = config;
    }

    public void run() {
	log.traceEntry();
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
		String className = Helper.getClassName(this.config.getOutput());
		log.debug("Output class is {}", className);

		Class<?> cls = Class.forName(className);
		BaseOutput output = (BaseOutput) cls.newInstance();
		output.setReportPartId(orderNum);
		output.setReportConfig(this.config);
		output.setResultSet(rs);
		output.save();
	    } catch (Exception e) {
		Result result = new Result();
		result.setErrorMessage(e.getMessage());
		part.setResult(result);

		log.error("Found error while running report");
		log.catching(e);
		continue;
	    }
	}
	log.traceExit();
    }
}
