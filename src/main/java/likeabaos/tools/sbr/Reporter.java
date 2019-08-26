package likeabaos.tools.sbr;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

	    List<List<String>> data = new ArrayList<List<String>>();
	    try (Connection conn = this.db.connect(); Statement stmt = conn.createStatement()) {
		String finalSql = part.getSql();
		log.debug("SQL to run:{}{}", System.lineSeparator(), finalSql);
		
		ResultSet rs = stmt.executeQuery(finalSql);
		ResultSetMetaData metaData = rs.getMetaData();
		while (rs.next()) {
		    List<String> row = new ArrayList<String>();
		    for (int col = 1; col <= metaData.getColumnCount(); col++) {
			row.add(rs.getString(col));
		    }
		    data.add(row);
		}
		part.setResult(data);
	    } catch (Exception e) {
		log.error("Found error while running report");
		log.catching(e);
		continue;
	    }
	}
	log.traceExit();
    }
}
