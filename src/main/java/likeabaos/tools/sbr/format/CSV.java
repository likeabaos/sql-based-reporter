package likeabaos.tools.sbr.format;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import likeabaos.tools.sbr.Emailer;

public class CSV extends BaseFormat {
    private final Logger LOG = LogManager.getLogger();
    public static final CSVFormat CSV_FORMAT = CSVFormat.EXCEL.withQuoteMode(QuoteMode.NON_NUMERIC);

    public CSV() {
        super(false);
    }

    public CSV(boolean deleteOutputOnExit) {
        super(deleteOutputOnExit);
    }

    @Override
    public String determineOutputFilepath() {
        return this.determineBaseFilepath() + new SimpleDateFormat(" yyyyMMdd_HHmmss").format(new Date()) + ".csv";
    }

    @Override
    public void save() throws Exception {
        File file = new File(this.determineOutputFilepath());
        FileUtils.makeParentDirs(file);
        LOG.info("Saving Report to: " + file.getPath());
        if (this.isCopySource()) {
            LOG.debug("Just Copying source file");
            Files.copy(this.getSourceFile().toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            this.setOutputFile(file);
            return;
        }

        throw new IllegalStateException(
                "This is not implemented. Assume source file is CSV format and should be copied to target");
    }

    public String toHtmlTable(int rows) throws IOException {
        try (Reader reader = Files.newBufferedReader(this.getSourceFile().toPath());
                CSVParser csvParser = new CSVParser(reader, CSV.CSV_FORMAT.withFirstRecordAsHeader())) {
            Document html = Jsoup.parseBodyFragment("");
            html.outputSettings(Emailer.getHtmlOutputFormat());
            Element table = html.body().appendElement("table");

            Element hdrRow = table.appendElement("tr");
            for (String name : csvParser.getHeaderNames()) {
                hdrRow.appendElement("th").text(name);
            }

            int rowId = -1;
            int rowProcessed = 0;
            for (CSVRecord csvRecord : csvParser) {
                if (++rowId >= rows && rows > 0)
                    continue;

                rowProcessed++;
                html.append("<tr>");
                Element datRow = table.appendElement("tr");
                for (String value : csvRecord) {
                    datRow.appendElement("td").text(value);
                }
            }

            if (rowProcessed <= rowId) {
                StringBuilder sb = new StringBuilder();
                sb.append("*Showed ").append(rowProcessed).append(" of ").append(rowId + 1).append(" record(s)");
                Element note = html.body().appendElement("div").addClass("table-note");
                note.text(sb.toString());
            }

            html.outputSettings().prettyPrint(false);
            return html.select("body").html();
        }
    }
}
