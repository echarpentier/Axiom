package fr.pfgen.axiom.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.pfgen.axiom.server.utils.MD5Password;
import fr.pfgen.axiom.server.utils.SQLUtils;
import fr.pfgen.axiom.shared.records.UserRecord;

public class UsersTable {

    public static List<UserRecord> getUsers(ConnectionPool pool, Integer startRow, Integer endRow, String sortBy, Map<String, String> filterCriteria) {
        List<UserRecord> list = new ArrayList<UserRecord>();
        String query = "SELECT * FROM users ";
        String critToString = "WHERE ";

        //filter criterias
        if (filterCriteria != null && filterCriteria.size() > 0) {
            List<String> critList = new ArrayList<String>();
            if (filterCriteria.containsKey("appID")) {
                critList.add("app_id=" + filterCriteria.get("appID"));
            } else if (filterCriteria.containsKey("status")) {
                critList.add("app_status=\"" + filterCriteria.get("app_status") + "\"");
            }

            for (Iterator<String> i = critList.iterator(); i.hasNext();) {
                String condition = i.next();
                critToString = critToString.concat(condition + " AND ");
            }
            if (critToString.endsWith(" AND ")) {
                critToString = critToString.replaceAll("\\sAND\\s$", " ");
            }
            query = query.concat(critToString);
        }

        PreparedStatement psUsers = null;
        ResultSet rsUsers = null;
        Connection con = null;

        try {
            con = pool.getConnection();
            psUsers = con.prepareStatement(query);
            rsUsers = psUsers.executeQuery();
            while (rsUsers.next()) {
                UserRecord record = new UserRecord();
                record.setUserID(rsUsers.getInt("user_id"));
                record.setFirstname(rsUsers.getString("firstname"));
                record.setLastname(rsUsers.getString("lastname"));
                record.setEmail(rsUsers.getString("email"));
                record.setOffice_number(rsUsers.getString("office_number"));
                record.setTeam(rsUsers.getString("team_id"));
                record.setAppID(rsUsers.getString("app_id"));
                record.setAppPw(rsUsers.getString("app_passwd"));
                record.setStatus(rsUsers.getString("app_status"));
                list.add(record);
            }
            psUsers.close();
            rsUsers.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            SQLUtils.safeClose(rsUsers);
            SQLUtils.safeClose(psUsers);
            pool.close(con);
        }

        return list;
    }

    public static UserRecord getUser(ConnectionPool pool, String userName, String password) {

        String query = "SELECT * FROM users WHERE app_id=?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = null;

        UserRecord user = new UserRecord();
        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            ps.setString(1, userName);
            rs = ps.executeQuery();
            if (rs.next()) {
                do {
                    if (MD5Password.testPassword(password, rs.getString("app_passwd"))) {
                        user.setLoginText("User authenticated !\nWelcome " + rs.getString("firstname") + " " + rs.getString("lastname"));
                        user.setUserID(rs.getInt("user_id"));
                        user.setFirstname(rs.getString("firstname"));
                        user.setLastname(rs.getString("lastname"));
                        user.setEmail(rs.getString("email"));
                        user.setOffice_number(rs.getString("office_number"));
                        user.setAppID(rs.getString("app_id"));
                        user.setAppPw(rs.getString("app_passwd"));
                        user.setStatus(rs.getString("app_status"));
                    } else {
                        user.setLoginText("Incorrect password for user \"" + userName + "\"");
                    }
                } while (rs.next());
            } else {
                user.setLoginText("Incorrect username");
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(ps);
            pool.close(con);
        }
        return user;
    }

    public static UserRecord addUser(ConnectionPool pool, UserRecord record) {
        Connection con = null;
        PreparedStatement psAddUser = null;
        ResultSet rsk = null;
        String addUserSql = "INSERT IGNORE INTO users SET firstname=?,lastname=?,email=?,office_number=?,team_id=?,app_id=?,app_passwd=md5(?),app_status=?";

        try {
            con = pool.getConnection();
            psAddUser = con.prepareStatement(addUserSql, Statement.RETURN_GENERATED_KEYS);
            psAddUser.setString(1, record.getFirstname());
            psAddUser.setString(2, record.getLastname());
            psAddUser.setString(3, record.getEmail());
            if (record.getOffice_number() != null) {
                psAddUser.setString(4, record.getOffice_number());
            } else {
                psAddUser.setNull(4, Types.VARCHAR);
            }
            if (record.getTeam() != null) {
                psAddUser.setString(5, record.getTeam());
            } else {
                psAddUser.setNull(5, Types.INTEGER);
            }
            psAddUser.setString(6, record.getAppID());
            psAddUser.setString(7, record.getAppPw());
            psAddUser.setString(8, record.getStatus());

            psAddUser.executeUpdate();
            rsk = psAddUser.getGeneratedKeys();
            if (rsk.next()) {
                record.setUserID(rsk.getInt(1));
            } else {
                record = null;
            }
            rsk.close();
            psAddUser.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            SQLUtils.safeClose(rsk);
            SQLUtils.safeClose(psAddUser);
            pool.close(con);
        }
        return record;
    }

    public static String getUserNameFromId(ConnectionPool pool, int id) {
        String query = "SELECT firstname,lastname FROM users WHERE user_id=?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = null;

        String user = "";
        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                do {
                    user = rs.getString("firstname") + " " + rs.getString("lastname");
                } while (rs.next());
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(ps);
            pool.close(con);
        }
        return user;
    }
}
