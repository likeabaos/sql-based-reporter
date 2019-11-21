package likeabaos.tools.sbr.config;

public class OutputConfig {
    private boolean enabled = true;
    private String output = "CSV"; // CSV, EXCEL
    private String outputPath = ".";
    private boolean saveSeparateFile = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public boolean isSaveSeparateFile() {
        return saveSeparateFile;
    }

    public void setSaveSeparateFile(boolean saveSeparateFile) {
        this.saveSeparateFile = saveSeparateFile;
    }

}
