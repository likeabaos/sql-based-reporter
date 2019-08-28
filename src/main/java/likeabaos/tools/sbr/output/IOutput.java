package likeabaos.tools.sbr.output;

import java.io.File;
import java.sql.ResultSet;

public interface IOutput {
    public void setResultSet(ResultSet resultset);
    public File save();
}
