package ee.alkohol.juks.sirvid.containers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DaoKalenderJDBCSqlite {
    
    public Connection dbConnection;
    public String errorMsg;
    public String jdbcURL;
    
    
    public static enum DbTables {
        
        EVENTS ("events", "id", "event", "more"),
        RUNES ("runes", "dbid", null, "filename");
        
        private String pk;
        private String table;
        private String title;
        private String description;
        
        public String getTable() { return table; }
        public String getPK() { return pk; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        
        DbTables(String table, String pk, String title, String description) {
            this.table = table;
            this.pk = pk;
            this.title = title;
            this.description = description;
        }

    }
    
    public DaoKalenderJDBCSqlite(String jdbcc) {
        
        dbConnection = null;
        errorMsg = null;
        if(jdbcc != null) {
            try {
                jdbcURL = jdbcc;
                Class.forName("org.sqlite.JDBC");
                dbConnection = DriverManager.getConnection(jdbcc);
            }
            catch(Exception e) {
                errorMsg = e.getMessage();
                dbConnection = null;
            }
        }
        
    }
    
    private ResultSet fetchQueryResults(String query) {
        
        Statement statement;
        try {
            statement = dbConnection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            ResultSet rs = statement.executeQuery(query);
            return rs;
        }
        catch(SQLException e) {
            errorMsg = e.getMessage();
        }
        return null;
        
    }
    
    private StringBuilder generateSimpleQuery(int start, int end, DbTables table) {
        StringBuilder query = new StringBuilder("select * from ");
        query.append(table.getTable());
        query.append(" where ");
        if(start >= end) {
            query.append(table.getPK());
            query.append(" = ");
            query.append(start);
        } else {
            query.append(table.getPK());
            query.append(" >= ");
            query.append(start);
            query.append(" and ");
            query.append(table.getPK());
            query.append(" <= ");
            query.append(end);
        }
        return query;
    }
    
    public ResultSet getRange(int start, int end, DbTables table, String where) {
        StringBuilder query = generateSimpleQuery(start, end, table);
        if(where != null) { query.append(where); }
        return fetchQueryResults(query.toString());
    }
    
    public boolean isConnected() {
        return dbConnection != null;
    }
    
}
