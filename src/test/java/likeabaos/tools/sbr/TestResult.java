package likeabaos.tools.sbr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.junit.Test;

public class TestResult {

    @Test
    public void testResult() throws SQLException {
	ResultSetMetaData m = new ResultSetMetaData() {
	    @Override
	    public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	    }

	    @Override
	    public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	    }

	    @Override
	    public boolean isWritable(int column) throws SQLException {
		return false;
	    }

	    @Override
	    public boolean isSigned(int column) throws SQLException {
		return false;
	    }

	    @Override
	    public boolean isSearchable(int column) throws SQLException {
		return false;
	    }

	    @Override
	    public boolean isReadOnly(int column) throws SQLException {
		return false;
	    }

	    @Override
	    public int isNullable(int column) throws SQLException {
		return 0;
	    }

	    @Override
	    public boolean isDefinitelyWritable(int column) throws SQLException {
		return false;
	    }

	    @Override
	    public boolean isCurrency(int column) throws SQLException {
		return false;
	    }

	    @Override
	    public boolean isCaseSensitive(int column) throws SQLException {
		return false;
	    }

	    @Override
	    public boolean isAutoIncrement(int column) throws SQLException {
		return false;
	    }

	    @Override
	    public String getTableName(int column) throws SQLException {
		return null;
	    }

	    @Override
	    public String getSchemaName(int column) throws SQLException {
		return null;
	    }

	    @Override
	    public int getScale(int column) throws SQLException {
		return 0;
	    }

	    @Override
	    public int getPrecision(int column) throws SQLException {
		return 0;
	    }

	    @Override
	    public String getColumnTypeName(int column) throws SQLException {
		return null;
	    }

	    @Override
	    public int getColumnType(int column) throws SQLException {
		return 0;
	    }

	    private String[] columns = { "column_1", "column_2", "column_3" };

	    @Override
	    public String getColumnName(int column) throws SQLException {
		return columns[column - 1];
	    }

	    @Override
	    public String getColumnLabel(int column) throws SQLException {
		return null;
	    }

	    @Override
	    public int getColumnDisplaySize(int column) throws SQLException {
		return 0;
	    }

	    @Override
	    public int getColumnCount() throws SQLException {
		return columns.length;
	    }

	    @Override
	    public String getColumnClassName(int column) throws SQLException {
		return null;
	    }

	    @Override
	    public String getCatalogName(int column) throws SQLException {
		return null;
	    }
	};
	
	Result r = new Result();
	r.createColumns(m);
	assertEquals(3, r.getLastColumn());
	assertEquals("[column_1, column_2, column_3]", r.getColumnNames().toString());
	
	r.add(1, 101);
	assertEquals(1, r.getLastRow());
	assertEquals(101, r.get(1, r.getLastRow()));
	assertNull(r.get(2, r.getLastRow()));
	
	r.add(2, 102);
	assertEquals(1, r.getLastRow());
	assertEquals(102, r.get(2, r.getLastRow()));
	
	r.add(2, 202);
	assertEquals(2, r.getLastRow());
	assertEquals(202, r.get(2, r.getLastRow()));
    }
}
