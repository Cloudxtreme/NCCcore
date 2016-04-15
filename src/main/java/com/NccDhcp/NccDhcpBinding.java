package com.NccDhcp;

import com.NccSystem.SQL.NccQuery;
import com.NccSystem.SQL.NccQueryException;
import com.sun.rowset.CachedRowSetImpl;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;

public class NccDhcpBinding {

    private static Logger logger = Logger.getLogger(NccDhcpServer.class);
    private NccQuery query;

    public NccDhcpBinding() {
        try {
            query = new NccQuery();
        } catch (NccQueryException e) {
            e.printStackTrace();
        }
    }

    public NccDhcpBindData getBinding(String remoteID, String clientMAC, Long relayAgent) {

        CachedRowSetImpl rs;
        String whereMAC = "";

        if (clientMAC != null) {
            whereMAC = "clientMAC='" + clientMAC + "' AND ";
        }

        try {
            rs = query.selectQuery("SELECT id, uid, remoteID, clientMAC, relayAgent FROM ncc_dhcp_user_binding WHERE " +
                    "remoteID='" + remoteID + "' AND " +
                    whereMAC +
                    "relayAgent=" + relayAgent);

            if (rs != null) {
                try {
                    if (rs.next()) {
                        NccDhcpBindData bindData = new NccDhcpBindData();

                        bindData.id = rs.getInt("id");
                        bindData.uid = rs.getInt("uid");
                        bindData.remoteID = rs.getString("remoteID");
                        bindData.clientMAC = rs.getString("clientMAC");
                        bindData.relayAgent = rs.getLong("relayAgent");

                        return bindData;
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

    public void setUnbinded(String remoteID, String clientMAC, Long relayAgent) {

        ArrayList<Integer> ids;
        Long lastSeen = System.currentTimeMillis() / 1000L;

        try {
            ids = query.updateQuery("UPDATE ncc_dhcp_unbinded SET lastSeen=" + lastSeen + ", clientMAC='" + clientMAC + "' WHERE " +
                    "remoteID='" + remoteID + "' AND " +
                    "relayAgent=" + relayAgent);

            if (ids == null) ids = query.updateQuery("INSERT INTO ncc_dhcp_unbinded (" +
                    "lastSeen, " +
                    "remoteID, " +
                    "clientMAC, " +
                    "relayAgent) VALUES (" +
                    lastSeen + ", " +
                    "'" + remoteID + "', " +
                    "'" + clientMAC + "', " +
                    relayAgent + ")");

        } catch (NccQueryException e) {
            e.printStackTrace();
        }
    }
}
