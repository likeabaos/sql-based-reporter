package likeabaos.tools.sbr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

public class Database {
    private final String connectionString;
    private final String username;
    private final String password;

    public Database(String connectionString, String username, String password) {
	this.connectionString = connectionString;
	this.username = username;
	this.password = password;
    }

    public String getConnectionString() {
	return this.connectionString;
    }

    public String getUsername() {
	return this.username;
    }

    public boolean isReady() {
	return !StringUtils.isAnyBlank(this.connectionString, this.username, this.password);
    }
    
    public Connection connect() throws SQLException {
	if (!this.isReady())
	    throw new IllegalStateException("Connectionis not ready:" + System.lineSeparator() + this.toString());
	return DriverManager.getConnection(this.connectionString, this.username, this.password);
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("Connection String: ").append(this.getConnectionString());
	sb.append("User: ").append(this.getUsername()).append(System.lineSeparator());
	sb.append("Password: ").append(StringUtils.isBlank(this.password) ? "Not Set" : "****")
		.append(System.lineSeparator());
	sb.append("Ready: ").append(this.isReady());
	return sb.toString();
    }
}
