package likeabaos.tools.sbr;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

public class TestDatabase {

    @Test(expected = IllegalStateException.class)
    public void testMissingFields() throws SQLException {
	new Database(null, null, null).connect();
    }
    
    @Test
    public void testMakeConnection() throws SQLException {
	Database db = new Database("jdbc:sqlite::memory:", "test", "test");
	try (Connection conn = db.connect()) {
	    assertEquals("SQLite JDBC", conn.getMetaData().getDriverName());
	}
    }

}
