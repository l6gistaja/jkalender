package ee.alkohol.juks.sirvid.containers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DaoKalenderJDBCSqlite {
    
    public Connection dbConnection;
    public String errorMsg;
    public String jdbcConnect;
    
    public DaoKalenderJDBCSqlite(String jdbcc) {
        
        dbConnection = null;
        errorMsg = null;
        jdbcConnect = jdbcc;
        
        try {
            Class.forName("org.sqlite.JDBC");
            dbConnection = DriverManager.getConnection(jdbcc);
        }
        catch(Exception e) {
            errorMsg = e.getMessage();
        }
        
    }
    
    public ResultSet fetchQueryResults(String query) {
  
        Statement statement;
        try {
            statement = dbConnection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            ResultSet rs = statement.executeQuery(query);
            return rs;
        }
        catch(SQLException e) {
            // TODO Auto-generated catch block
            errorMsg = e.getMessage();
        }
        return null;
        
    }
    
    public ResultSet getAnniversaries() {
        return fetchQueryResults("select * from events where id > 100 and id < 1232");
    }
    
}
