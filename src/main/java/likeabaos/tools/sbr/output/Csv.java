package likeabaos.tools.sbr.output;

import java.io.File;
import java.sql.ResultSet;

public class Csv implements IOutput {
    private ResultSet resultset;
    
    @Override
    public void setResultSet(ResultSet resultset) {
	this.resultset = resultset;
    }

    @Override
    public File save() {
	if (this.resultset == null)
	    throw new IllegalStateException("The resultset is not set, cannot do anything.");
	
	System.out.println("Outputing CSV");
    }
}
