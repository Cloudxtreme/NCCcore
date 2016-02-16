package com.NccSystem.SQL;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class NccSQLPool {

    private BasicDataSource dataSource = new BasicDataSource();

    public NccSQLPool(String connectString, String dbUser, String dbPassword){

        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUsername(dbUser);
        dataSource.setPassword(dbPassword);
        dataSource.setUrl(connectString);
        dataSource.setMaxTotal(100);
    }

    public Connection getConnection() throws SQLException{
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException();
        }
    }

    public void close(){
        try {
            dataSource.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
