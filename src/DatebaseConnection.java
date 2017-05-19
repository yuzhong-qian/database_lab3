/**
 * Created by qianyuzhong on 4/27/17.
 */

import java.sql.*;

public class DatebaseConnection {
    public static final String SERVER   = "jdbc:mysql://localhost/";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "luyang";
    public static final String DATABASE = "luyang_test";
    public static final String QUERY    = "SELECT * FROM reviewer LIMIT 10;";

    public static void main(String[] args) {

        Connection con = null;
        Statement stmt = null;
        ResultSet res  = null;
        int numColumns = 0;

        // attempt to connect to db
        try {
            con = connection();
            // initialize a query statement
            stmt = con.createStatement();

            // query db and save results
            res = stmt.executeQuery(QUERY);

            System.out.format("Query executed: '%s'\n\nResults:\n", QUERY);

            // the result set contains metadata
            numColumns = res.getMetaData().getColumnCount();

            // print table header
            for(int i = 1; i <= numColumns; i++) {
                System.out.format("%-20s", res.getMetaData().getColumnName(i));
            }
            System.out.println("\n----------------------------------------------------------------");

            // iterate through results
            while(res.next()) {
                for(int i = 1; i <= numColumns; i++) {
                    System.out.format("%-20s", res.getObject(i));
                }
                System.out.println("");
            }
        } catch (SQLException e ) {          // catch SQL errors
            System.err.format("SQL Error: %s", e.getMessage());
        } catch (Exception e) {              // anything else
            e.printStackTrace();
        } finally {
            // cleanup
            try {
                res.close();
                stmt.close();
                con.close();
                System.out.println("Connection terminated.");
            } catch (Exception e) { /* ignore cleanup errors */ }
        }
    }

    public static Connection connection() {
        Connection con = null;
        // attempt to connect to db
        try {
            // load mysql driver
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            // initialize connection
            con = DriverManager.getConnection(SERVER + DATABASE, USERNAME, PASSWORD);

            System.out.println("Connection established.");
        } catch (SQLException e ) {          // catch SQL errors
            System.err.format("SQL Error: %s", e.getMessage());
        } catch (Exception e) {              // anything else
            e.printStackTrace();
        }
        return con;
    }
}
