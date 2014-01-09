// Xiaoou, Dec,2013
// xli65@usfca.edu

package database;

import static server.Constant.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import server.sevlet.Status;

/**
 * SearchEngineDatabaseHandler handles all database connections for Search Engine.
 * 
 * Only one instance should be active for the entire search engine.
 */
public class SearchEngineDatabaseHandler extends DatabaseConnector {
    private String server;

    private static Logger logger = Logger.getLogger(SearchEngineDatabaseHandler.class);

    private static SearchEngineDatabaseHandler globalDatabaseHandler = null;

    public static SearchEngineDatabaseHandler getGlobalDatabaseHandler() {
        if (globalDatabaseHandler == null) {
            try {
                globalDatabaseHandler = new SearchEngineDatabaseHandler();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return globalDatabaseHandler;
    }

    public SearchEngineDatabaseHandler() throws FileNotFoundException, IOException {
        super();

        if (!testConnection()) {
            logger.fatal("Could not verify database connection.");
            System.exit(-1);
        }

        if (!(cleanUsersTable() && createUsersTable() &&
                cleanHistoriesTable() && createHistoriesTable() &&
                cleanSnippetTable() && createSnippetTable() &&
                cleanVisitedPagesTable() && createVisitedPagesTable() &&
                cleanFavoriateLinksTable() && createFavoriateLinksTable() &&
                cleanFavoriateQueriesTable() && createFavoriateQueriesTable())) {
            logger.warn("Could not clean and creates tables.");
            System.exit(-1);
        }

        // Set a default user
        registerUser("xiaoou", "xiaoou");

        logger.debug("Database handler started.");
    }

    public Status registerUser(String username, String password) {
        // Check new user exist or not
        String sqlString = "SELECT username FROM " + USERS_TABLE_NAME +
                " WHERE username = ?;";
        ArrayList<String> params = new ArrayList<String>();
        params.add(username);
        ResultSet results = exe_sql_query(sqlString, params);
        try {
            if (results.first()) {
                return Status.DUPLICATE_USER;
            }
        } catch (SQLException e) {
            return Status.SQL_EXCEPTION;
        } finally {
            recycleDBResource(results);
        }

        // If not exist, create a new user
        sqlString = "INSERT INTO " + USERS_TABLE_NAME +
                " (username, password)" +
                " VALUES (?, ?);";
        params = new ArrayList<String>();
        params.add(username);
        params.add(password);

        boolean status = exe_sql_statement(sqlString, params);

        if (status) {
            return Status.OK;
        } else {
            return Status.ERROR;
        }
    }

    public boolean userLogin(String username, String password) {
        String sqlString = "SELECT username FROM " + USERS_TABLE_NAME +
                " WHERE username = ? AND password = ?;";
        ArrayList<String> params = new ArrayList<String>();
        params.add(username);
        params.add(password);

        ResultSet results = exe_sql_query(sqlString, params);

        try {
            if (results.first()) {
                updateLoginTime(username);
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            recycleDBResource(results);
        }
    }

    /**
     * @param username
     */
    private void updateLoginTime(String username) {
        String sqlString = "UPDATE " + USERS_TABLE_NAME +
                " SET lastLogin=NOW()" +
                " WHERE username = ?;";
        ArrayList<String> params = new ArrayList<String>();
        params.add(username);

        exe_sql_statement(sqlString, params);
    }

    public boolean changePassword(String username, String newPassword) {
        String sqlString = "UPDATE " + USERS_TABLE_NAME +
                " SET password=?" +
                " WHERE username=?;";
        ArrayList<String> params = new ArrayList<String>();
        params.add(newPassword);
        params.add(username);

        return exe_sql_statement(sqlString, params);
    }

    public boolean checkTableExist(String tableName) {
        String sqlString = "SHOW TABLES LIKE ?;";
        ArrayList<String> params = new ArrayList<String>();
        params.add(tableName);

        ResultSet results = exe_sql_query(sqlString, params);

        try {
            if (results.first()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            recycleDBResource(results);
        }
    }

    public boolean storeQueryHistory(String username, String query) {
        String sqlString = "INSERT INTO " + HISTORIES_TABLE_NAME +
                " (username, query, queryTime)" +
                " VALUES (?, ?, NOW());";

        ArrayList<String> params = new ArrayList<String>();
        params.add(username);
        params.add(query);
        return exe_sql_statement(sqlString, params);
    }

    public ResultSet getQueryHistory(String username) {
        String sqlString = "SELECT * FROM " + HISTORIES_TABLE_NAME +
                " WHERE username = ?" +
                " ORDER BY queryTime DESC;";
        ArrayList<String> params = new ArrayList<String>();
        params.add(username);

        return exe_sql_query(sqlString, params);
    }

    public boolean clearQueryHistory(String username) {
        String sqlString = "DELETE FROM " + HISTORIES_TABLE_NAME +
                " WHERE username = ?;";
        ArrayList<String> params = new ArrayList<String>();
        params.add(username);

        return exe_sql_statement(sqlString, params);
    }

    public boolean storePageSnippet(String url, String snippet) {
        String sqlString = "INSERT INTO " + SNIPPETS_TABLE_NAME +
                " (url, snippet)" +
                " VALUES (?, ?);";

        ArrayList<String> params = new ArrayList<String>();
        params.add(url);
        params.add(snippet);
        return exe_sql_statement(sqlString, params);

    }

    public String getSnippetForUrl(String url) {
        String sqlString = "SELECT * FROM " + SNIPPETS_TABLE_NAME +
                " WHERE url = ?;";
        ArrayList<String> params = new ArrayList<String>();
        params.add(url);

        ResultSet snippet = exe_sql_query(sqlString, params);

        try {
            if (snippet.first()) {
                return snippet.getString("snippet");
            } else {
                return "";
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            recycleDBResource(snippet);
        }
    }

    public boolean storeVisitedPage(String username, String url) {
        String sqlString = "SELECT * FROM " + VISITED_PAGES_TABLE_NAME +
                " WHERE username = ? AND url = ?;";
        ArrayList<String> params = new ArrayList<String>();
        logger.debug("username: " + username);
        logger.debug("url: " + url);
        params.add(username);
        params.add(url);

        ResultSet pages = exe_sql_query(sqlString, params);

        try {
            if (pages != null && pages.first()) {
                int count = pages.getInt("count");

                sqlString = "UPDATE " + VISITED_PAGES_TABLE_NAME +
                        " SET count = ?" +
                        " WHERE username = ? AND url = ?;";
                params = new ArrayList<String>();
                params.add(String.valueOf(count + 1));
                params.add(username);
                params.add(url);

                return exe_sql_statement(sqlString, params);
            } else {
                sqlString = "INSERT INTO " + VISITED_PAGES_TABLE_NAME +
                        " (username, url, count) " +
                        "VALUES (?, ?, 1);";
                params = new ArrayList<String>();
                params.add(username);
                params.add(url);

                return exe_sql_statement(sqlString, params);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            recycleDBResource(pages);
        }
    }

    public ResultSet getVisitedPages(String username) {
        String sqlString = "SELECT * FROM " + VISITED_PAGES_TABLE_NAME +
                " WHERE username = ?" +
                " ORDER BY count DESC;";
        ArrayList<String> params = new ArrayList<String>();
        params.add(username);

        return exe_sql_query(sqlString, params);
    }

    public boolean createFavoriateQueriesTable() {
        String sqlString = "CREATE TABLE " + FAVORIATE_QUERIES_TABLE_NAME +
                " (username VARCHAR(20)," +
                " query VARCHAR(100));";
        return exe_sql_statement(sqlString, null);
    }

    public boolean cleanFavoriateQueriesTable() {
        return exe_sql_statement("DROP TABLE IF EXISTS " + FAVORIATE_QUERIES_TABLE_NAME + ";", null);
    }

    public boolean storeFavoriateQueries(String username, String query) {
        if (!hasFavoriateQueries(username, query)) {
            String sqlString = "INSERT INTO " + FAVORIATE_QUERIES_TABLE_NAME +
                    " (username, query)" +
                    " VALUES (?, ?);";

            ArrayList<String> params = new ArrayList<String>();
            params.add(username);
            params.add(query);
            return exe_sql_statement(sqlString, params);
        }
        return true;
    }

    public ResultSet getFavoriteQueries(String username) {
        String sqlString = "SELECT * FROM " + FAVORIATE_QUERIES_TABLE_NAME +
                " WHERE username = ?;";
        ArrayList<String> params = new ArrayList<String>();
        params.add(username);

        return exe_sql_query(sqlString, params);
    }

    private boolean hasFavoriateQueries(String username, String query) {
        String sqlString = "SELECT * FROM " + FAVORIATE_QUERIES_TABLE_NAME +
                " WHERE username=? AND query=?;";

        ArrayList<String> params = new ArrayList<String>();
        params.add(username);
        params.add(query);
        ResultSet queries = exe_sql_query(sqlString, params);

        try {
            if (queries != null && queries.first()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            recycleDBResource(queries);
        }
    }

    public boolean createFavoriateLinksTable() {
        String sqlString = "CREATE TABLE " + FAVORIATE_LINKS_TABLE_NAME +
                " (username VARCHAR(20)," +
                " url VARCHAR(100));";
        return exe_sql_statement(sqlString, null);
    }

    public boolean cleanFavoriateLinksTable() {
        return exe_sql_statement("DROP TABLE IF EXISTS " + FAVORIATE_LINKS_TABLE_NAME + ";", null);
    }

    public boolean storeFavoriateLinks(String username, String url) {
        if (!hasFavoriateLinks(username, url)) {
            String sqlString = "INSERT INTO " + FAVORIATE_LINKS_TABLE_NAME +
                    " (username, url)" +
                    " VALUES (?, ?);";

            ArrayList<String> params = new ArrayList<String>();
            params.add(username);
            params.add(url);
            return exe_sql_statement(sqlString, params);
        }
        return true;
    }

    private boolean hasFavoriateLinks(String username, String url) {
        String sqlString = "SELECT * FROM " + FAVORIATE_LINKS_TABLE_NAME +
                " WHERE username=? AND url=?;";

        ArrayList<String> params = new ArrayList<String>();
        params.add(username);
        params.add(url);
        ResultSet links = exe_sql_query(sqlString, params);

        try {
            if (links != null && links.first()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            recycleDBResource(links);
        }
    }

    public ResultSet getFavoriteLinks(String username) {
        String sqlString = "SELECT * FROM " + FAVORIATE_LINKS_TABLE_NAME +
                " WHERE username = ?;";
        ArrayList<String> params = new ArrayList<String>();
        params.add(username);

        return exe_sql_query(sqlString, params);
    }

    private boolean exe_sql_statement(String statementString, ArrayList<String> params) {
        PreparedStatement statement = null;
        Connection connection = null;
        try {
            connection = getConnection();

            statement = connection.prepareStatement(statementString);
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    statement.setString(i + 1, params.get(i));
                }
            }

            logger.debug("Executing SQL query: " + statementString);
            statement.execute();

            return true;
        } catch (Exception ex) {
            logger.debug("Exception: " + ex);
            return false;
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    logger.debug("Could not close statement: " + statementString);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    logger.debug("Could not close connection to server: " + server);
                }
            }
        }
    }

    private ResultSet exe_sql_query(String queryString, ArrayList<String> params) {
        PreparedStatement statement = null;
        Connection connection = null;
        try {
            connection = getConnection();

            statement = connection.prepareStatement(queryString);
            for (int i = 0; i < params.size(); i++) {
                statement.setString(i + 1, params.get(i));
            }

            logger.debug("Executing SQL query: " + queryString);
            ResultSet queryResults = statement.executeQuery();
            return queryResults;
        } catch (Exception ex) {
            logger.debug("Exception: " + ex);
            return null;
        }
    }

    private boolean cleanUsersTable() {
        return exe_sql_statement("DROP TABLE IF EXISTS " + USERS_TABLE_NAME + ";", null);
    }

    private boolean cleanHistoriesTable() {
        return exe_sql_statement("DROP TABLE IF EXISTS " + HISTORIES_TABLE_NAME + ";", null);
    }

    private boolean cleanSnippetTable() {
        return exe_sql_statement("DROP TABLE IF EXISTS " + SNIPPETS_TABLE_NAME + ";", null);
    }

    private boolean cleanVisitedPagesTable() {
        return exe_sql_statement("DROP TABLE IF EXISTS " + VISITED_PAGES_TABLE_NAME + ";", null);
    }

    private boolean createUsersTable() {
        String sqlString = "CREATE TABLE " + USERS_TABLE_NAME +
                " (username VARCHAR(20)," +
                " password VARCHAR(20)," +
                " lastLogin DATETIME);";
        return exe_sql_statement(sqlString, null);
    }

    private boolean createHistoriesTable() {
        String sqlString = "CREATE TABLE " + HISTORIES_TABLE_NAME +
                " (username VARCHAR(20)," +
                " query VARCHAR(100)," +
                " queryTime DATETIME);";
        return exe_sql_statement(sqlString, null);
    }

    private boolean createSnippetTable() {
        String sqlString = "CREATE TABLE " + SNIPPETS_TABLE_NAME +
                " (url VARCHAR(200)," +
                " snippet VARCHAR(500));";
        return exe_sql_statement(sqlString, null);
    }

    private boolean createVisitedPagesTable() {
        String sqlString = "CREATE TABLE " + VISITED_PAGES_TABLE_NAME +
                " (username VARCHAR(20)," +
                " url VARCHAR(200)," +
                " count INT);";
        return exe_sql_statement(sqlString, null);
    }

    /**
     * @param currentUser
     * @return
     */
    @SuppressWarnings("deprecation")
    public String getLastLoginTime(String username) {
        String sqlString = "SELECT * FROM " + USERS_TABLE_NAME +
                " WHERE username = ?;";
        ArrayList<String> params = new ArrayList<String>();
        params.add(username);

        ResultSet results = exe_sql_query(sqlString, params);
        try {
            if (results.next() && results.getTime("lastLogin") != null) {
                String timeString = results.getTime("lastLogin").toLocaleString();
                return timeString.substring(12, timeString.length());
            }
        } catch (SQLException e) {
            logger.debug(e);
        }
        return "";
    }

    /**
     * @param username
     * @return 5 queries made by user other than specified user
     */
    public ResultSet getSuggestedQueries(String username) {
        String sqlString = "SELECT * FROM " + HISTORIES_TABLE_NAME +
                " WHERE username != ?" +
                " ORDER BY queryTime DESC LIMIT 5;";
        ArrayList<String> params = new ArrayList<String>();
        params.add(username);

        return exe_sql_query(sqlString, params);
    }

    /**
     * @param results
     * @throws SQLException
     */
    public static void recycleDBResource(ResultSet results) {
        try {
            results.getStatement().getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e);
            logger.debug(e);
        }
    }
}
