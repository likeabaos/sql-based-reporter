package likeabaos.tools.sbr;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
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

	    try (Connection conn = this.db.connect(); Statement stmt = conn.createStatement()) {
		String finalSql = part.getSql();
		log.debug("SQL to run:{}{}", System.lineSeparator(), finalSql);

		ResultSet rs = stmt.executeQuery(finalSql);
		Result result = this.getDataFromResultSet(rs);
		part.setResult(result);
		
		log.info("Returned {} column(s) and {} row(s)", result.getLastColumn(), result.getLastRow());
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

    private Result getDataFromResultSet(ResultSet rs) throws SQLException {
	ResultSetMetaData metaData = rs.getMetaData();
	Result result = new Result();
	result.createColumns(metaData);

	while (rs.next()) {
	    for (int col = 1; col <= metaData.getColumnCount(); col++) {
		int colType = metaData.getColumnType(col);
		if (Result.integerTypes.contains(colType))
		    result.add(col, rs.getInt(col));
		else if (Result.doubleTypes.contains(colType))
		    result.add(col, rs.getDouble(col));
		else
		    result.add(col, rs.getString(col));
	    }
	}
	return result;
    }
}
