package likeabaos.tools.sbr.output;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CSV extends BaseOutput {
    private final Logger log = LogManager.getLogger();

    @Override
    public void save() throws Exception {
	String filepath = this.determineOutputFilepath();

	if (filepath == null)
	    throw new IllegalArgumentException("File path cannot be null");
	if (this.getResultSet() == null)
	    throw new IllegalStateException("The resultset is not set, cannot do anything.");

	File file = new File(filepath);
	log.info("Saving CSV to: {}", file.getAbsolutePath());
	this.createParentFolder(file);
	try (CSVPrinter printer = new CSVPrinter(new FileWriter(file),
		CSVFormat.EXCEL.withHeader(this.getResultSet().getMetaData()).withQuoteMode(QuoteMode.NON_NUMERIC))) {
	    printer.printRecords(this.getResultSet());
	}
    }

    @Override
    public String determineOutputFilepath() {
	return this.determineBaseFilepath() + new SimpleDateFormat("_yyyyMMdd_HHmmss").format(new Date()) + ".csv";
    }

}
