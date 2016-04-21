package com.NccUsers;

import com.NccSystem.SQL.NccQuery;
import com.NccSystem.SQL.NccQueryException;
import com.mongodb.DB;
import com.sun.rowset.CachedRowSetImpl;
import org.apache.commons.lang.StringEscapeUtils;

import java.sql.SQLException;
import java.util.ArrayList;

public class NccUsers {

    private NccQuery query;
    private final String usersQueryFieldset = "id, userLogin, userPassword, userStatus, accountId, userIP";

    private static String DB_DECODE_KEY = "sab1093582";

    public NccUsers() throws NccUsersException {
        try {
            query = new NccQuery();
        } catch (NccQueryException e) {
            e.printStackTrace();
            throw new NccUsersException("CachedQuery SQL error");
        }
    }

    private UserData fillUserData(CachedRowSetImpl rs) throws NccUsersException {
        if (rs != null) {
            try {
                if (rs.next()) {
                    UserData userData = new UserData();

                    userData.userLogin = rs.getString("id");
                    userData.id = rs.getInt("uid");
                    userData.userPassword = rs.getString("userPassword");
                    userData.userStatus = rs.getBoolean("disable") ? 0 : 1;
                    userData.userIP = rs.getInt("ip");
                    userData.userTariff = rs.getInt("tp_id");
                    userData.userCredit = rs.getFloat("credit");
                    userData.userDeposit = rs.getFloat("deposit");

                    return userData;
                } else {
                    throw new NccUsersException("User not found");
                }
            } catch (SQLException se) {
                throw new NccUsersException("SQL error: " + se.getMessage());
            }
        } else {
            throw new NccUsersException("User not found");
        }
    }

    public UserData getUser(Integer uid) throws NccUsersException {

        CachedRowSetImpl rs;

        try {
            rs = query.selectQuery("SELECT " +
                    "u.id AS userLogin, " +
                    "0 AS accountId, " +
                    "DECODE(u.password, '" + DB_DECODE_KEY + "') AS userPassword, " +
                    "u.credit AS userCredit, " +
                    "u.disable AS userStatus, " +
                    "u.bill_id AS billId, " +
                    "u.uid AS userId, " +
                    "u.gid AS groupId, " +
                    "d.ip AS userIP, " +
                    "d.tp_id AS userTariff, " +
                    "d.cid AS userMAC, " +
                    "b.deposit AS userDeposit " +
                    "FROM users u " +
                    "LEFT JOIN bills b ON b.id=u.bill_id " +
                    "LEFT JOIN dv_main d ON d.uid=u.uid " +
                    "WHERE u.uid=" + uid);
        } catch (NccQueryException e) {
            throw new NccUsersException("SQL error: " + e.getMessage());
        }

        return fillUserData(rs);
    }

    public UserData getUser(String login) throws NccUsersException {

        CachedRowSetImpl rs;

        try {
            rs = query.selectQuery("SELECT * FROM nccUsers WHERE userLogin='" + StringEscapeUtils.escapeSql(login) + "'");
        } catch (NccQueryException e) {
            throw new NccUsersException("SQL error: " + e.getMessage());
        }

        if (rs != null) {
            try {
                if (rs.next()) {
                    UserData userData = new UserData();

                    userData.id = rs.getInt("id");
                    userData.userLogin = login;
                    userData.userPassword = rs.getString("userPassword");
                    userData.userStatus = rs.getInt("userStatus");
                    userData.accountId = rs.getInt("accountId");
                    userData.userIP = rs.getInt("userIP");
                    userData.userTariff = rs.getInt("userTariff");

                    return userData;
                } else {
                    throw new NccUsersException("User not found");
                }
            } catch (SQLException se) {
                throw new NccUsersException("SQL error: " + se.getMessage());
            }
        } else {
            throw new NccUsersException("User not found");
        }
    }

    public ArrayList<UserData> getUsers() throws NccUsersException {

        ArrayList<UserData> users = new ArrayList<>();

        CachedRowSetImpl rs;

        try {
            rs = query.selectQuery("SELECT " + usersQueryFieldset + " FROM nccUsers LIMIT 10");

            try {
                while (rs.next()) {
                    UserData userData = new UserData();

                    userData.id = rs.getInt("id");
                    userData.userLogin = rs.getString("userLogin");
                    userData.userPassword = rs.getString("userPassword");
                    userData.userStatus = rs.getInt("userStatus");
                    userData.accountId = rs.getInt("accountId");
                    userData.userIP = rs.getInt("userIP");

                    users.add(userData);
                }

                return users;

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (NccQueryException e) {
            e.printStackTrace();
            throw new NccUsersException("CachedQuery error: " + e.getMessage());
        }

        return null;
    }

    public ArrayList<Integer> createUser(UserData userData) throws NccUsersException {

        try {
            if (getUser(userData.userLogin) != null) {
                System.out.println("User '" + userData.userLogin + "' exists");
                throw new NccUsersException("User exists");
            }

            String insertQuery = "INSERT INTO nccUsers (" +
                    "userLogin, " +
                    "userPassword, " +
                    "userStatus, " +
                    "accountId, " +
                    "userIP) VALUES (" +
                    "'" + StringEscapeUtils.escapeSql(userData.userLogin) + "', " +
                    "'" + StringEscapeUtils.escapeSql(userData.userPassword) + "', " +
                    userData.userStatus + ", " +
                    userData.accountId + ", " +
                    userData.userIP + ")";

            try {
                NccQuery query = new NccQuery();

                return query.updateQuery(insertQuery);

            } catch (NccQueryException e) {
                e.printStackTrace();
            }

        } catch (NccUsersException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }
}
