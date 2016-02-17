package com;

import com.NccAPI.NccAPI;
import com.NccDhcp.NccDhcp;
import com.NccRadius.NccRadius;
import com.NccSystem.SQL.NccSQLPool;
import org.apache.commons.configuration.*;
import org.apache.log4j.*;
import org.apache.log4j.jmx.LoggerDynamicMBean;

import java.io.IOException;
import java.sql.*;
import java.util.concurrent.TimeUnit;

// TODO: 15.01.2016 override class RadiusServer to serve BindException exceptions

public class Ncc {

    private static NccRadius nccRadius;
    private static NccAPI nccAPI;
    private static NccDhcp nccDhcp;
    public static NccSQLPool sqlPool;
    private static Logger logger = Logger.getRootLogger();
    private static String logLevel = "DEBUG";
    private static String logFile = "NCC.log";

    public static void main(String[] args) throws InterruptedException, SQLException, IOException {

        boolean moduleRadius = true, moduleDHCP = true;

        logger.setLevel(Level.toLevel(logLevel));

        String dbHost, dbDbname, dbUser, dbPassword;
        String connectString;
        Integer dbPort;

        CompositeConfiguration config = new CompositeConfiguration();
        String current = new java.io.File(".").getCanonicalPath();

        System.out.println("Current dir: " + current);

        try {
            config.addConfiguration(new SystemConfiguration());
            config.addConfiguration(new PropertiesConfiguration("config.properties"));

            logLevel = config.getString("log.level");
            logFile = config.getString("log.file");

            moduleRadius = Boolean.valueOf(config.getString("module.radius"));
            moduleDHCP = Boolean.valueOf(config.getString("module.dhcp"));

            logger.setLevel(Level.toLevel(logLevel));

            FileAppender fileAppender = new FileAppender();
            fileAppender.setName("NccFileLogger");
            fileAppender.setFile(logFile);
            fileAppender.setLayout(new PatternLayout("%d{ISO8601} [%-5p] %m%n"));
            fileAppender.setAppend(true);
            fileAppender.activateOptions();

            Logger.getRootLogger().addAppender(fileAppender);

            logger.info("NCC system loading...");

            dbHost = config.getString("db.host");
            dbPort = config.getInt("db.port");
            dbUser = config.getString("db.user");
            dbPassword = config.getString("db.password");
            dbDbname = config.getString("db.dbname");

            logger.debug("Got SQL config");

            logger.info("Init SQL pool: " + dbUser + "@" + dbHost);

            connectString = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbDbname + "?useUnicode=yes&characterEncoding=UTF-8";
            sqlPool = new NccSQLPool(connectString, dbUser, dbPassword);

            logger.info("SQL pool initialized");

        } catch (ConfigurationException ce) {
            logger.fatal("Config file missing");
            System.out.println("Config file missing in " + current);
            System.exit(-1);
        }


        if (moduleRadius) {
            logger.info("Starting Radius");
            nccRadius = new NccRadius();
            nccRadius.start(true, true);
        }

        if (moduleDHCP) {
            logger.info("Starting DHCP");
            nccDhcp = new NccDhcp();
            nccDhcp.start();
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                logger.info("Stopping NCC server...");
                nccRadius.stop();
                nccAPI.stop();
                sqlPool.close();
            }
        });

        logger.info("Starting API");
        nccAPI = new NccAPI();
        nccAPI.start();
    }
}
