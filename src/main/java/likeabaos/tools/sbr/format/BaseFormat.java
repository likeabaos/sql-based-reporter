package likeabaos.tools.sbr.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.FileUtils;

import apache.mkt.tools.sbr.config.OutputConfig;

/**
 * TODO: Save separate files (nice to have)
 * 
 * @author bao.quach
 *
 */
public abstract class BaseFormat {
    private static final Logger LOG = LogManager.getLogger();

    private String name;
    private boolean copySource = false;
    private File sourceFile;
    private OutputConfig outputConfig;
    private File outputFile;
    private boolean saveSeparateFile = true;
    private boolean deleteOutputOnExit = false;

    public BaseFormat(boolean deleteOutputOnExit) {
        this.deleteOutputOnExit = deleteOutputOnExit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCopySource() {
        return copySource;
    }

    public void setCopySource(boolean copySource) {
        this.copySource = copySource;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public OutputConfig getOutputConfig() {
        return outputConfig;
    }

    public void setOutputConfig(OutputConfig outputConfig) {
        this.outputConfig = outputConfig;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
        if (this.isDeleteOutputOnExit())
            this.outputFile.deleteOnExit();
    }

    public boolean isDeleteOutputOnExit() {
        return deleteOutputOnExit;
    }

    public boolean isSaveSeparateFile() {
        return saveSeparateFile;
    }

    public void setSaveSeparateFile(boolean saveSeparateFile) {
        this.saveSeparateFile = saveSeparateFile;
    }

    public String determineBaseFilepath() {
        return this.getOutputConfig().getOutputPath() + "/" + this.getName();
    }

    public abstract String determineOutputFilepath();

    public abstract void save() throws Exception;

    public static String getClassName(String className) {
        if (className == null || className.contains("."))
            return className;

        String baseClass = BaseFormat.class.getCanonicalName();
        int pos = baseClass.lastIndexOf(".");
        baseClass = baseClass.substring(0, pos + 1);
        return baseClass + className;
    }

    public static File saveResultToTempFiles(ResultSet rs, File dir, String filename) throws IOException, SQLException {
        String prefix = (StringUtils.length(filename) >= 3) ? filename : "tempOutput";
        File file = File.createTempFile(prefix, ".csv", dir);
        FileUtils.makeParentDirs(file);
        file.deleteOnExit();
        LOG.debug("Saving temp file: {}", file.getAbsolutePath());
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(file), CSV.CSV_FORMAT.withHeader(rs.getMetaData()))) {
            printer.printRecords(rs);
        }
        return file;
    }

    public static boolean isTempFileEmpty(File file, boolean firstRowHeader) throws IOException {
        boolean isEmpty = true;
        int countToBreak = (firstRowHeader) ? 2 : 1;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int count = 0;
            String currentLine = null;
            while ((currentLine = reader.readLine()) != null) {
                if (StringUtils.isAllBlank(currentLine))
                    continue;

                if (++count >= countToBreak) {
                    isEmpty = false;
                    break;
                }
            }
        }
        return isEmpty;
    }
}
