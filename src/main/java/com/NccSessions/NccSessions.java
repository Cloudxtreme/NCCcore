package com.NccSessions;

import com.NccPools.PoolData;
import com.NccPools.NccPools;
import com.NccSystem.NccUtils;
import com.NccSystem.SQL.NccQuery;
import com.NccSystem.SQL.NccQueryException;
import com.sun.rowset.CachedRowSetImpl;
import org.apache.commons.lang.StringEscapeUtils;

import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

public class NccSessions {

    private NccQuery query;

    public NccSessions() throws NccSessionsException {
        try {
            query = new NccQuery();
        } catch (NccQueryException e) {
            throw new NccSessionsException("SQL error: " + e.getMessage());
        }
    }

    private SessionData fillSessionData(CachedRowSetImpl rs) {
        if (rs != null) {
            SessionData sessionData = new SessionData();

            try {
                sessionData.id = rs.getInt("id");
                sessionData.sessionId = rs.getString("sessionId");
                sessionData.startTime = rs.getLong("startTime");
                sessionData.acctInputOctets = rs.getInt("acctInputOctets");
                sessionData.acctOutputOctets = rs.getInt("acctOutputOctets");
                sessionData.nasId = rs.getInt("nasId");
                sessionData.framedIP = rs.getLong("framedIP");
                sessionData.framedMAC = rs.getString("framedMAC");
                sessionData.framedAgentId = rs.getLong("framedAgentId");
                sessionData.framedCircuitId = rs.getString("framedCircuitId");
                sessionData.framedRemoteId = rs.getString("framedRemoteId");
                sessionData.userId = rs.getInt("userId");
                sessionData.lastAlive = rs.getLong("lastAlive");
                sessionData.sessionDuration = rs.getLong("sessionDuration");

                return sessionData;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean isAllocated(ArrayList<SessionData> sessions, Long ip) {
        if (sessions != null) {
            for (SessionData session : sessions) {
                if (Objects.equals(session.framedIP, ip)) return true;
            }
        }
        return false;
    }

    public Long getIPFromPool(ArrayList<PoolData> pools) {
        //ArrayList<PoolData> pools = new NccPools().getPools();
        ArrayList<SessionData> sessions = new ArrayList<>();

        try {
            sessions = getSessions();
        } catch (NccSessionsException e) {
            e.printStackTrace();
            return null;
        }

        if (pools != null) {
            for (PoolData pool : pools) {
                if (pool != null) if (pool.poolStatus == 1) {
                    for (Long ip = pool.poolStart; ip <= pool.poolEnd; ip++) {
                        if (!isAllocated(sessions, ip)) return ip;
                    }
                }
            }
        }

        return null;
    }

//    public SessionData getSession(String sessionID) throws NccSessionsException {
//
//        CachedRowSetImpl rs;
//
//        try {
//            rs = query.selectQuery("SELECT " +
//                    "acct_session_id, " +
//                    "UNIX_TIMESTAMP(started) AS started, " +
//                    "acct_input_octets, " +
//                    "acct_output_octets, " +
//                    "nas_id, " +
//                    "framed_ip_address, " +
//                    "uid, " +
//                    "lupdated, " +
//                    "acct_session_time FROM dv_calls WHERE acct_session_id='" + StringEscapeUtils.escapeSql(sessionID) + "'");
//
//        } catch (NccQueryException e) {
//            e.printStackTrace();
//            throw new NccSessionsException("getSession error: " + e.getMessage());
//        }
//
//        if (rs != null) {
//            try {
//                if (rs.next()) {
//                    SessionData sessionData = new SessionData();
//
//                    sessionData.id = 0;
//                    sessionData.sessionId = rs.getString("acct_session_id");
//                    sessionData.startTime = rs.getLong("started");
//                    sessionData.acctInputOctets = rs.getInt("acct_input_octets");
//                    sessionData.acctOutputOctets = rs.getInt("acct_output_octets");
//                    sessionData.nasId = rs.getInt("nas_id");
//                    sessionData.framedIP = rs.getLong("framed_ip_address");
//                    sessionData.userId = rs.getInt("uid");
//                    sessionData.lastAlive = rs.getLong("lupdated");
//                    sessionData.sessionDuration = rs.getLong("acct_session_time");
//
//                    return sessionData;
//                }
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return null;
//    }

    public SessionData getSession(String sessionID) throws NccSessionsException {

        CachedRowSetImpl rs;

        try {
            rs = query.selectQuery("SELECT id, " +
                    "sessionId, " +
                    "startTime, " +
                    "acctInputOctets, " +
                    "acctOutputOctets, " +
                    "nasId, " +
                    "framedIP, " +
                    "framedMAC, " +
                    "framedAgentId, " +
                    "framedCircuitId, " +
                    "framedRemoteId, " +
                    "userId, " +
                    "lastAlive, " +
                    "sessionDuration FROM nccSessions WHERE sessionId='" + StringEscapeUtils.escapeSql(sessionID) + "'");

        } catch (NccQueryException e) {
            e.printStackTrace();
            throw new NccSessionsException("getSession error: " + e.getMessage());
        }

        if (rs != null) {
            try {
                if (rs.next()) {
                    SessionData sessionData = fillSessionData(rs);
                    return sessionData;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public SessionData getSession(Integer uid) throws NccSessionsException {

        CachedRowSetImpl rs;

        try {
            rs = query.selectQuery("SELECT id, " +
                    "sessionId, " +
                    "startTime, " +
                    "acctInputOctets, " +
                    "acctOutputOctets, " +
                    "nasId, " +
                    "framedIP, " +
                    "framedMAC, " +
                    "framedAgentId, " +
                    "framedCircuitId, " +
                    "framedRemoteId, " +
                    "userId, " +
                    "lastAlive, " +
                    "sessionDuration FROM nccSessions WHERE userId=" + uid);

        } catch (NccQueryException e) {
            e.printStackTrace();
            throw new NccSessionsException("getSession error: " + e.getMessage());
        }

        if (rs != null) {
            try {
                if (rs.next()) {
                    SessionData sessionData = fillSessionData(rs);
                    return sessionData;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public ArrayList<SessionData> getSessions() throws NccSessionsException {

        CachedRowSetImpl rs;

        try {
            rs = query.selectQuery("SELECT id, " +
                    "sessionId, " +
                    "startTime, " +
                    "acctInputOctets, " +
                    "acctOutputOctets, " +
                    "nasId, " +
                    "framedIP, " +
                    "framedMAC, " +
                    "framedAgentId, " +
                    "framedCircuitId, " +
                    "framedRemoteId, " +
                    "userId, " +
                    "lastAlive, " +
                    "sessionDuration FROM nccSessions");

        } catch (NccQueryException e) {
            e.printStackTrace();
            throw new NccSessionsException("Cached query error: " + e.getMessage());
        }

        if (rs != null) {
            try {
                ArrayList<SessionData> sessions = new ArrayList<>();

                while (rs.next()) {
                    SessionData sessionData = fillSessionData(rs);
                    sessions.add(sessionData);
                }

                return sessions;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public ArrayList<Integer> startSession(SessionData sessionData) {

        try {
            NccQuery query = new NccQuery();

            ArrayList<Integer> ids = query.updateQuery("INSERT INTO nccSessions (" +
                    "sessionId, " +
                    "startTime, " +
                    "acctInputOctets, " +
                    "acctOutputOctets, " +
                    "nasId, " +
                    "framedIP, " +
                    "framedMAC, " +
                    "userId, " +
                    "lastAlive, " +
                    "sessionDuration, " +
                    "framedAgentId, " +
                    "framedCircuitId, " +
                    "framedRemoteId" +
                    ") VALUES (" +
                    "'" + sessionData.sessionId + "', " +
                    sessionData.startTime + ", " +
                    sessionData.acctInputOctets + ", " +
                    sessionData.acctOutputOctets + ", " +
                    sessionData.nasId + ", " +
                    sessionData.framedIP + ", " +
                    "'" + sessionData.framedMAC + "', " +
                    sessionData.userId + ", " +
                    sessionData.lastAlive + ", " +
                    sessionData.sessionDuration + ", " +
                    sessionData.framedAgentId + ", " +
                    "'" + sessionData.framedCircuitId + "', " +
                    "'" + sessionData.framedRemoteId + "'" +
                    ")");

            return ids;

        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Integer> stopSession(SessionData sessionData) {

        try {
            NccQuery query = new NccQuery();

            ArrayList<Integer> idsUpdate = query.updateQuery("INSERT INTO nccSessionsLog (" +
                    "userId, " +
                    "startTime, " +
                    "stopTime, " +
                    "acctInputOctets, " +
                    "acctOutputOctets, " +
                    "terminateCause, " +
                    "nasId, " +
                    "framedIP, " +
                    "framedMAC, " +
                    "framedAgentId, " +
                    "framedCircuitId, " +
                    "framedRemoteId, " +
                    "lastAlive, " +
                    "sessionDuration, " +
                    "sessionId) VALUES (" +
                    sessionData.userId + ", " +
                    sessionData.startTime + ", " +
                    "UNIX_TIMESTAMP(), " +
                    sessionData.acctInputOctets + ", " +
                    sessionData.acctOutputOctets + ", " +
                    sessionData.terminateCause + ", " +
                    sessionData.nasId + ", " +
                    sessionData.framedIP + ", " +
                    "'" + sessionData.framedMAC + "', " +
                    sessionData.framedAgentId + ", " +
                    "'" + sessionData.framedCircuitId + "', " +
                    "'" + sessionData.framedRemoteId + "', " +
                    sessionData.lastAlive + ", " +
                    sessionData.sessionDuration + ", " +
                    "'" + sessionData.sessionId + "')");

            ArrayList<Integer> idsDelete = query.updateQuery("DELETE FROM nccSessions WHERE id=" + sessionData.id);

            return idsUpdate;
        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Integer> updateSession(SessionData sessionData) {

        try {
            NccQuery query = new NccQuery();

            ArrayList<Integer> idsUpdate = query.updateQuery("UPDATE nccSessions SET " +
                    "acctInputOctets=" + sessionData.acctInputOctets + ", " +
                    "acctOutputOctets=" + sessionData.acctOutputOctets + ", " +
                    "lastAlive=" + sessionData.lastAlive + ", " +
                    "sessionDuration=" + sessionData.sessionDuration + " " +
                    "WHERE id=" + sessionData.id);

            return idsUpdate;
        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }

    public SessionData getSessionFromLog(String sessionID) {

        try {
            NccQuery query = new NccQuery();

            CachedRowSetImpl rs;

            rs = query.selectQuery("SELECT id, " +
                    "userId, " +
                    "startTime, " +
                    "stopTime, " +
                    "acctInputOctets, " +
                    "acctOutputOctets, " +
                    "terminateCause, " +
                    "nasId, " +
                    "framedIP, " +
                    "framedMAC, " +
                    "framedAgentId, " +
                    "framedCircuitId, " +
                    "framedRemoteId, " +
                    "lastAlive, " +
                    "sessionDuration, " +
                    "sessionId FROM nccSessionsLog WHERE sessionId='" + StringEscapeUtils.escapeSql(sessionID) + "'");

            if (rs != null) {
                try {
                    if (rs.next()) {
                        return fillSessionData(rs);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Integer> resumeSession(SessionData sessionData) {

        try {
            NccQuery query = new NccQuery();
            ArrayList<Integer> idsStart;

            SessionData resumeSession = getSessionFromLog(sessionData.sessionId);

            if (resumeSession == null) {
                idsStart = startSession(sessionData);
            } else {
                ArrayList<Integer> idsDelete = query.updateQuery("DELETE FROM nccSessionsLog WHERE id=" + sessionData.id);
                idsStart = startSession(sessionData);
            }

            return idsStart;
        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }
}
