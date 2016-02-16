package com.NccRadius;

import com.NccAccounts.AccountData;
import com.NccAccounts.NccAccounts;
import com.NccAccounts.NccAccountsException;
import com.NccNAS.NccNAS;
import com.NccNAS.NccNasData;
import com.NccNAS.NccNasException;
import com.NccPools.PoolData;
import com.NccSessions.NccSessions;
import com.NccSessions.NccSessionsException;
import com.NccSessions.SessionData;
import com.NccSystem.NccUtils;
import com.NccTariffScale.NccTariffScale;
import com.NccUsers.NccUsers;
import com.NccUsers.NccUsersException;
import com.NccUsers.UserData;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.log4j.Logger;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.AccountingRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusException;
import org.tinyradius.util.RadiusServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

import static com.NccSystem.NccUtils.ip2long;

// TODO: 27.01.2016 acctInputGigawords support
// TODO: 27.01.2016 Concurrent sessions control

public class NccRadius extends RadiusServer {

    private static Logger logger = Logger.getLogger(NccRadius.class);
    private static Integer radAuthPort = 1812;
    private static Integer radAcctPort = 1813;
    private static String radSecret = "";
    private static NccUsers nccUsers;
    private static NccAccounts nccAccounts;

    @Override
    public String getSharedSecret(InetSocketAddress inetSocketAddress) {
        try {
            NccNAS nccNAS = new NccNAS();
            try {
                return nccNAS.getNasSecretByIP(NccUtils.ip2long(inetSocketAddress.getHostString()));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } catch (NccNasException e) {
            e.printStackTrace();
        }

        return UUID.randomUUID().toString();
    }

    @Override
    public String getUserPassword(String s) {
        return null;
    }

    public NccRadius() {
        super();

        logger.info("NccRadius starting");

        CompositeConfiguration config = new CompositeConfiguration();

        try {
            config.addConfiguration(new SystemConfiguration());
            config.addConfiguration(new PropertiesConfiguration("config.properties"));

            radAuthPort = config.getInt("radius.authport");
            radAcctPort = config.getInt("radius.acctport");
            radSecret = config.getString("radius.secret");

        } catch (ConfigurationException ce) {
            logger.fatal("config.properties not found.");
            System.exit(-1);
        }

        try {
            nccUsers = new NccUsers();
        } catch (NccUsersException e) {
            e.printStackTrace();
            logger.fatal(e.getMessage());
            System.exit(-1);
        }

        try {
            nccAccounts = new NccAccounts();
        } catch (NccAccountsException e) {
            e.printStackTrace();
            logger.fatal(e.getMessage());
            System.exit(-1);
        }

        setAuthPort(radAuthPort);
        setAcctPort(radAcctPort);

        start(true, true);
    }

    public RadiusPacket accountingRequestReceived(AccountingRequest accountingRequest, InetSocketAddress client) {

        RadiusPacket radiusPacket = new RadiusPacket();
        Integer packetIdentifier = accountingRequest.getPacketIdentifier();
        Integer packetType = accountingRequest.getPacketType();
        Integer statusType = 0;

        try {
            statusType = accountingRequest.getAcctStatusType();
        } catch (RadiusException e) {
            e.printStackTrace();
        }

        switch (packetType) {
            case RadiusPacket.ACCOUNTING_REQUEST:
                radiusPacket.setPacketType(RadiusPacket.ACCOUNTING_RESPONSE);
                radiusPacket.setPacketIdentifier(packetIdentifier);

                String userLogin = "";
                String sessionID = "";
                String nasPort = "";
                String nasIP = "";
                String nasPortType = "";
                String nasIdentifier = "";
                String framedIP = "";
                String framedMAC = "";
                Integer acctInputOctets;
                Integer acctOutputOctets;
                Integer acctInputGigawords;
                Integer acctOutputGigawords;
                String acctSessionTime = "";
                String callingStation = "";
                String calledStation = "";
                String framedProtocol = "";
                String serviceType = "";
                String eventTimestamp = "";
                String acctAuthentic = "";

                try {
                    userLogin = accountingRequest.getUserName();
                    sessionID = accountingRequest.getAttributeValue("Acct-Session-Id");
                    nasIP = accountingRequest.getAttributeValue("NAS-IP-Address");
                    nasPort = accountingRequest.getAttributeValue("NAS-Port");
                    nasIdentifier = accountingRequest.getAttributeValue("NAS-Identifier");
                    nasPortType = accountingRequest.getAttributeValue("NAS-Port-Type");
                    framedIP = accountingRequest.getAttributeValue("Framed-IP-Address");
                    callingStation = accountingRequest.getAttributeValue("Calling-Station-Id");
                    framedMAC = callingStation;
                    calledStation = accountingRequest.getAttributeValue("Called-Station-Id");
                    framedProtocol = accountingRequest.getAttributeValue("Framed-Protocol");
                    serviceType = accountingRequest.getAttributeValue("Service-Type");
                    eventTimestamp = accountingRequest.getAttributeValue("Event-Timestamp");
                    acctAuthentic = accountingRequest.getAttributeValue("Acct-Authentic");

                } catch (RadiusException e) {
                    e.printStackTrace();
                }

                SessionData sessionData = new SessionData();

                switch (statusType) {
                    case AccountingRequest.ACCT_STATUS_TYPE_START:

                        logger.info("Session start: '" + userLogin + "' sessionId=" + sessionID + " nasIP=" + nasIP + " nasPort=" + nasPort + " framedIP=" + framedIP + " framedMAC=" + framedMAC);

                        try {
                            SessionData checkSession = new NccSessions().getSession(sessionID);
                            if (checkSession != null) {
                                logger.error("Duplicate session: '" + sessionID + "'");
                                break;
                            }
                        } catch (NccSessionsException e) {
                            e.printStackTrace();
                        }

                        // TODO: 21.01.2016 Get nasId from db
                        sessionData.nasId = 1;

                        try {
                            sessionData.framedIP = NccUtils.ip2long(framedIP);
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }
                        sessionData.framedMAC = framedMAC;
                        sessionData.acctInputOctets = 0;
                        sessionData.acctOutputOctets = 0;
                        sessionData.sessionId = sessionID;
                        sessionData.startTime = System.currentTimeMillis() / 1000L;
                        sessionData.lastAlive = sessionData.startTime;
                        sessionData.sessionDuration = 0L;

                        try {
                            UserData userData = new NccUsers().getUser(userLogin);
                            sessionData.userId = userData.id;
                        } catch (NccUsersException e) {
                            e.printStackTrace();
                        }

                        try {
                            new NccSessions().startSession(sessionData);
                        } catch (NccSessionsException e) {
                            e.printStackTrace();
                        }

                        break;
                    case AccountingRequest.ACCT_STATUS_TYPE_STOP:

                        logger.info("Session stop: '" + userLogin + "' sessionId=" + sessionID + " nasIP=" + nasIP + " nasPort=" + nasPort + " framedIP=" + framedIP);

                        acctInputOctets = Integer.parseInt(accountingRequest.getAttributeValue("Acct-Input-Octets"));
                        acctOutputOctets = Integer.parseInt(accountingRequest.getAttributeValue("Acct-Output-Octets"));
                        acctInputGigawords = Integer.parseInt(accountingRequest.getAttributeValue("Acct-Input-Gigawords"));
                        acctOutputGigawords = Integer.parseInt(accountingRequest.getAttributeValue("Acct-Output-Gigawords"));
                        acctSessionTime = accountingRequest.getAttributeValue("Acct-Session-Time");
                        String terminateCause = accountingRequest.getAttributeValue("Acct-Terminate-Cause");

                        try {
                            sessionData = new NccSessions().getSession(sessionID);
                        } catch (NccSessionsException e) {
                            e.printStackTrace();
                        }

                        if (sessionData != null) {
                            sessionData.nasId = 1;

                            try {
                                UserData userData = new NccUsers().getUser(userLogin);
                                sessionData.userId = userData.id;
                            } catch (NccUsersException e) {
                                e.printStackTrace();
                            }


                            switch (terminateCause) {
                                case "User-Request":
                                    sessionData.terminateCause = 1;
                                    break;
                                default:
                                    sessionData.terminateCause = 0;
                                    break;
                            }

                            sessionData.stopTime = System.currentTimeMillis() / 1000L;

                            try {
                                new NccSessions().stopSession(sessionData);
                            } catch (NccSessionsException e) {
                                e.printStackTrace();
                            }
                        } else {
                            logger.error("Session not found: '" + sessionID + "'");

                        }

                        break;
                    case AccountingRequest.ACCT_STATUS_TYPE_INTERIM_UPDATE:

                        logger.info("Session update: '" + userLogin + "' sessionId=" + sessionID + " nasIP=" + nasIP + " nasPort=" + nasPort + " framedIP=" + framedIP);

                        acctInputOctets = Integer.parseInt(accountingRequest.getAttributeValue("Acct-Input-Octets"));
                        acctOutputOctets = Integer.parseInt(accountingRequest.getAttributeValue("Acct-Output-Octets"));
                        acctInputGigawords = Integer.parseInt(accountingRequest.getAttributeValue("Acct-Input-Gigawords"));
                        acctOutputGigawords = Integer.parseInt(accountingRequest.getAttributeValue("Acct-Output-Gigawords"));
                        acctSessionTime = accountingRequest.getAttributeValue("Acct-Session-Time");

                        try {
                            sessionData = new NccSessions().getSession(sessionID);
                        } catch (NccSessionsException e) {
                            System.out.println("getSession(" + sessionID + ")");
                            e.printStackTrace();
                        }

                        if (sessionData != null) {
                            sessionData.acctInputOctets = acctInputOctets;
                            sessionData.acctOutputOctets = acctOutputOctets;
                            sessionData.lastAlive = System.currentTimeMillis() / 1000L;
                            sessionData.sessionDuration = sessionData.lastAlive - sessionData.startTime;

                            try {
                                new NccSessions().updateSession(sessionData);
                            } catch (NccSessionsException e) {
                                e.printStackTrace();
                            }
                        } else {
                            logger.error("Session not found: '" + sessionID + "'");
                            try {
                                SessionData resumeSession = new SessionData();

                                try {
                                    resumeSession.framedIP = ip2long(framedIP);
                                } catch (UnknownHostException e) {
                                    e.printStackTrace();
                                }

                                resumeSession.sessionId = sessionID;
                                resumeSession.acctInputOctets = acctInputOctets;
                                resumeSession.acctOutputOctets = acctOutputOctets;
                                // TODO: 21.01.2016 Get nasId from db
                                resumeSession.nasId = 1;

                                try {
                                    UserData userData = new NccUsers().getUser(userLogin);

                                    if (userData != null) {
                                        resumeSession.userId = userData.id;
                                    }
                                } catch (NccUsersException e) {
                                    e.printStackTrace();
                                }

                                resumeSession.lastAlive = System.currentTimeMillis() / 1000L;
                                resumeSession.sessionDuration = Long.parseLong(acctSessionTime);
                                resumeSession.startTime = resumeSession.lastAlive - resumeSession.sessionDuration;

                                ArrayList<Integer> ids = new NccSessions().resumeSession(resumeSession);
                                if (ids != null) {
                                    logger.info("Session '" + sessionID + "' resumed");
                                }
                            } catch (NccSessionsException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    default:
                        break;
                }

                break;
            default:
                break;
        }

        return radiusPacket;
    }

    public RadiusPacket accessRequestReceived(AccessRequest req, InetSocketAddress addr) {

        long startTime = System.nanoTime();

        String reqUserName = req.getUserName();
        String reqUserPassword = req.getUserPassword();
        Integer reqPacketIdentifier = req.getPacketIdentifier();

        logger.debug("Access-Request '" + reqUserName + "'");

        RadiusPacket radiusPacket = new RadiusPacket();
        Integer packetType = RadiusPacket.ACCESS_REJECT;

        NccNasData nasData = new NccNasData();
        Long nasIP = null;

        try {
            nasIP = NccUtils.ip2long(addr.getHostString());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        try {
            NccNAS nccNAS = new NccNAS();

            nasData = nccNAS.getNasByIP(nasIP);
        } catch (NccNasException e) {
            logger.error("NAS error: " + e.getMessage());
            return radiusPacket;
        }

        try {
            UserData userData = nccUsers.getUser(reqUserName);

            try {
                AccountData accountData = nccAccounts.getAccount(userData.accountId);

                try {
                    if (req.verifyPassword(userData.userPassword)) {
                        if (userData.userStatus > 0) {

                            if (accountData != null) if (accountData.accDeposit > -accountData.accCredit) {
                                logger.info("Login OK: '" + reqUserName + "'");

                                packetType = RadiusPacket.ACCESS_ACCEPT;

                                try {
                                    ArrayList<PoolData> pools;

                                    NccTariffScale tariffScale = new NccTariffScale();

                                    pools = tariffScale.getTariffPools(userData.userTariff);

                                    Long framedIP = new NccSessions().getIPFromPool(pools);

                                    try {
                                        radiusPacket.addAttribute("Framed-IP-Address", NccUtils.long2ip(framedIP));
                                        radiusPacket.addAttribute("Framed-IP-Netmask", "255.255.255.255");
                                        radiusPacket.addAttribute("Acct-Interim-Interval", nasData.nasInterimInterval.toString());
                                    } catch (UnknownHostException e) {
                                        e.printStackTrace();
                                    }
                                } catch (NccSessionsException e) {
                                    logger.info("Login FAIL: no enough IP in pools");
                                }
                            } else {
                                logger.info("Login FAIL: deposit <= -credit");
                            }
                        } else {
                            logger.info("Login FAIL: user disabled");
                        }
                    } else {
                        logger.info("Login FAIL: incorrect userPassword for '" + reqUserName + "': '" + reqUserPassword + "' expected '" + userData.userPassword + "'");
                    }
                } catch (RadiusException re) {
                    re.printStackTrace();
                }
            } catch (NccAccountsException e) {
                logger.error(e.getMessage() + " for userId=" + userData.id);
            }

        } catch (NccUsersException e) {
            logger.info("User not found: '" + reqUserName + "'");
        }

        radiusPacket.setPacketIdentifier(reqPacketIdentifier);
        radiusPacket.setPacketType(packetType);

        logger.debug("Response time: " + new DecimalFormat("#.#########").format((double) (System.nanoTime() - startTime) / 1000000000) + " sec.");

        return radiusPacket;
    }

    public void startServer() {
        start(true, true);
    }
}
