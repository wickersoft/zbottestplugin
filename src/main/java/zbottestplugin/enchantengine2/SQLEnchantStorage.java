/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin.enchantengine2;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Triple;

/**
 *
 * @author Dennis
 */
public class SQLEnchantStorage {

    private static final int MAX_CACHE_EDITS = 1000;
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    private final String username, password, tableName;
    private final String DB_URL;

    private Connection conn = null;
    private PreparedStatement put_stmt;
    private PreparedStatement del_stmt;

    private static final ArrayList<Triple<Integer, String, Integer>> PUT_CACHE = new ArrayList();
    private static final ArrayList<Integer> DEL_CACHE = new ArrayList();

    public SQLEnchantStorage(String hostname, int port, String username, String password, String databaseName, String tableName) {
        this.username = username;
        this.password = password;
        this.tableName = tableName;
        DB_URL = "jdbc:mysql://" + hostname + ":" + port + "/" + databaseName + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    }

    public boolean connect() {
        try {
            if (conn != null && conn.isValid(5000)) {
                return true;
            }
            disconnect();
            //STEP 2: Register JDBC driver
            Class.forName(JDBC_DRIVER);
            //STEP 3: Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, username, password);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(SQLEnchantStorage.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public void disconnect() {
        try {
            if (conn != null) {
                conn.close();
            }
            if (put_stmt != null) {
                put_stmt.close();
            }
            if (del_stmt != null) {
                del_stmt.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLEnchantStorage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void put(int index, String itemstring, int price) {
        putCache(index, itemstring, price);
        flushCache();
    }

    public void putCache(int index, String itemstring, int price) {
        if (itemstring != null) {
            PUT_CACHE.add(Triple.of(index, itemstring, price));
        } else {
            DEL_CACHE.add(index);
        }

        if (PUT_CACHE.size() + DEL_CACHE.size() >= MAX_CACHE_EDITS) {
            flushCache();
        }
    }

    public void flushCache() {
        flushPutCache();
        flushDelCache();
    }

    private void flushPutCache() {
        if (PUT_CACHE.isEmpty()) {
            return;
        }
        System.out.println("SQL: Flushing Put Cache");
        connect();
        StringBuilder sql = new StringBuilder();
        sql.append("REPLACE INTO ").append(tableName).append(" (slot_id, enchants, price) VALUES ");
        sql.append(String.join(",", PUT_CACHE.stream().map((t) -> {
            return new StringBuilder()
                    .append("(").append(t.getLeft()).append(",'").append(t.getMiddle().replace("'", "")).append("',").append(t.getRight()).append(")")
                    .toString();
        }).collect(Collectors.toSet())));

        Statement stmt = null;
        try {
            //STEP 4: Execute a query
            System.out.println("Creating statement...");
            stmt = conn.createStatement();
            stmt.executeQuery(sql.toString()).close(); // Discard results
            stmt.close();
            PUT_CACHE.clear();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
    }

    private void flushDelCache() {
        if (DEL_CACHE.isEmpty()) {
            return;
        }
        System.out.println("SQL: Flushing Del Cache");
        connect();
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ").append(tableName).append(" WHERE slot_id IN (");
        sql.append(String.join(",", DEL_CACHE.stream().map((i) -> {
            return "" + i;
        }).collect(Collectors.toSet())))
                .append(")");

        Statement stmt = null;
        try {
            //STEP 4: Execute a query
            System.out.println("Creating statement...");
            stmt = conn.createStatement();
            stmt.executeUpdate(sql.toString()); // Discard results
            stmt.close();
            DEL_CACHE.clear();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
    }

    public ArrayList<Triple<Integer, String, Integer>> query(String enchantQueryString) {
        ArrayList<Triple<Integer, String, Integer>> results = new ArrayList<>();

        connect();

        String[] args = enchantQueryString.split(" ");
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT slot_id, enchants, price FROM ").append(tableName).append(" WHERE ");
        for (int i = 0; i < args.length; i++) {
            args[i] = "(enchants LIKE '% " + args[i] + "%' OR enchants LIKE '" + args[i] + "%')";
        }
        String where = String.join(" AND ", args);
        sql.append(where);
        sql.append(" ORDER BY price");

        Statement stmt = null;
        try {
            //STEP 4: Execute a query
            System.out.println("Creating statement...");
            stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(sql.toString());

            //STEP 5: Extract data from result set
            while (rs.next()) {
                //Retrieve by column name
                int slot_id = rs.getInt("slot_id");
                String enchants = rs.getString("enchants");
                int price = rs.getInt("price");
                results.add(Triple.of(slot_id, enchants, price));
            }
            rs.close();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se2) {
            }// nothing we can do
        }//end try
        return results;
    }

    public String getItem(int slot_id) {
        connect();
        String item = null;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT enchants FROM " + tableName + " WHERE slot_id=" + slot_id);

            //STEP 5: Extract data from result set
            while (rs.next()) {
                //Retrieve by column name
                item = rs.getString("enchants");
                break;
            }
            rs.close();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se2) {
            }// nothing we can do
        }//end try
        return item;
    }

    public int size() {
        connect();
        int count = -1;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(`slot_id`) FROM " + tableName);

            //STEP 5: Extract data from result set
            while (rs.next()) {
                //Retrieve by column name
                count = rs.getInt("COUNT(`slot_id`)");
                break;
            }
            rs.close();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se2) {
            }// nothing we can do
        }//end try
        return count;
    }
}
