package likeabaos.tools.sbr.output;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.FileUtils;

public class CSV extends BaseOutput {
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
}
