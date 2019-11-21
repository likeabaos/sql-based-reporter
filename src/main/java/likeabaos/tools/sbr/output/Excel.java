package likeabaos.tools.sbr.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class EXCEL extends BaseOutput {
    private final Logger LOG = LogManager.getLogger();
    
    public EXCEL() {
        super(false);
    }
    
    public EXCEL(boolean deleteOutputOnExit) {
        super(deleteOutputOnExit);
    }

    @Override
    public String determineOutputFilepath() {
        return this.determineBaseFilepath() + new SimpleDateFormat(" yyyyMMdd_HHmmss").format(new Date()) + ".xlsx";
    }

    @Override
    public void save() throws Exception {
        File file = new File(this.determineOutputFilepath());
        FileUtils.makeParentDirs(file);
        LOG.info("Saving Report to: " + file.getPath());

        DecimalFormatSymbols symbols = ((DecimalFormat) DecimalFormat.getInstance()).getDecimalFormatSymbols();
        String cleanNumericRegex = "^\\d*\\" + symbols.getDecimalSeparator() + "?\\d*$";
        try (Reader reader = Files.newBufferedReader(this.getSourceFile().toPath());
                CSVParser csvParser = new CSVParser(reader, CSV.CSV_FORMAT)) {
            Workbook wb = WorkbookFactory.create(true);
            Sheet sheet = wb.createSheet();
            for (CSVRecord csvRecord : csvParser) {
                Row row = sheet.createRow((int) csvParser.getCurrentLineNumber());
                int j = 0;
                for (String csvCell : csvRecord) {
                    Cell cell = row.createCell(j++);
                    String clean = StringUtils.replaceChars(StringUtils.stripToEmpty(csvCell),
                            symbols.getGroupingSeparator(), Character.MIN_VALUE);
                    if (clean.matches(cleanNumericRegex)) {
                        cell.setCellValue(Double.parseDouble(clean));
                    } else {
                        cell.setCellValue(csvCell);
                    }
                }
            }

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                wb.write(outputStream);
            }

            this.setOutputFile(file);
        }
    }

}
