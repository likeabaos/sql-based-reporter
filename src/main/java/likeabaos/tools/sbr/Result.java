package likeabaos.tools.sbr;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Result {
    // Keys need to be unique. If dup, append counter to make it unique
    private final Map<String, List<Object>> data = new HashMap<String, List<Object>>();

    // To track original column number to unique name (can be changed)
    private final Map<Integer, String> columns = new TreeMap<Integer, String>();

    private int lastRow = 0;
    private int lastColumn = 0;
    private String errorMessage = null;

    public String getUnqiueKeyName(String key) {
	if (!this.data.containsKey(key))
	    return key;
	int counter = 0;
	for (String k : this.data.keySet()) {
	    if (k.startsWith(key))
		counter++;
	}
	return key + "_" + counter;
    }

    public void createColumns(ResultSetMetaData metaData) throws SQLException {
	for (int i = 1; i <= metaData.getColumnCount(); i++) {
	    String name = metaData.getColumnName(i);
	    name = this.getUnqiueKeyName(name);
	    this.columns.put(i, name);
	    this.data.put(name, new ArrayList<Object>());
	    this.lastColumn++;
	}
    }

    List<Object> getColumn(int colNum) {
	String name = this.columns.get(colNum);
	return this.data.get(name);
    }

    public void add(int colNum, Object item) {
	List<Object> column = this.getColumn(colNum);
	column.add(item);
	if (column.size() > this.lastRow)
	    this.lastRow = column.size();
    }

    public List<String> getColumnNames() {
	return new ArrayList<String>(this.columns.values());
    }

    public int getLastRow() {
	return this.lastRow;
    }

    public int getLastColumn() {
	return this.lastColumn;
    }

    public Object get(int col, int row) {
	List<Object> column = this.getColumn(col);
	if (column.size() >= row)
	    return column.get(row - 1);
	else
	    return null;
    }

    public String getErrorMessage() {
	return this.errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
	this.errorMessage = errorMessage;
    }

    /*
     * #############################################################################
     * Since this is a report, we generally do not need accurate data type as long
     * as the values are represented properly to the user. Technically, we can set
     * everything as String. However, for an excel file or csv, having some simple
     * type is helpful such as numbers, or float, etc...
     * 
     * WITH THE ABOVE THOUGHT, we are going to overly simplify the mapping from JDBC
     * into our data types.
     * 
     * This will be a balance between data usefulness and code complexity
     * #############################################################################
     */
    public static List<Integer> integerTypes = Arrays.asList(Types.NUMERIC, Types.DECIMAL, Types.BIT, Types.TINYINT,
	    Types.SMALLINT, Types.INTEGER, Types.BIGINT);

    public static List<Integer> doubleTypes = Arrays.asList(Types.REAL, Types.FLOAT, Types.DOUBLE);

    // Everything else is String.... this is the simplify part :)
}
