package com.NccUsers;

import com.NccSystem.SQL.NccQuery;
import com.NccSystem.SQL.NccQueryException;
import com.sun.rowset.CachedRowSetImpl;
import org.apache.commons.lang.StringEscapeUtils;

import java.sql.SQLException;
import java.util.ArrayList;

public class NccUsers {

    private NccQuery query;
    private final String usersQueryFieldset = "id, userLogin, userPassword, userStatus, accountId, userIP";

    public NccUsers() throws NccUsersException {
        try {
            query = new NccQuery();
        } catch (NccQueryException e) {
            e.printStackTrace();
            throw new NccUsersException("CachedQuery SQL error");
        }
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
