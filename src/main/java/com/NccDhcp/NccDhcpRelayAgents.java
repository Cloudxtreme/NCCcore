package com.NccDhcp;

import com.Ncc;
import com.NccSystem.SQL.NccQuery;
import com.NccSystem.SQL.NccQueryException;
import com.sun.rowset.CachedRowSetImpl;

import java.net.InetAddress;
import java.sql.SQLException;

public class NccDhcpRelayAgents {

    private NccQuery query;

    public NccDhcpRelayAgents() {
        try {
            query = new NccQuery();
        } catch (NccQueryException e) {
            e.printStackTrace();
        }
    }

    public NccDhcpRelayAgentData getAgentByIP(Long ip) {

        CachedRowSetImpl rs;

        try {
            rs = query.selectQuery("SELECT id, " +
                    "agentName, " +
                    "agentIP, " +
                    "agentPool FROM ncc_dhcp_relay_agents WHERE agentIP=" + ip);

            if(rs != null){

                try {
                    if(rs.next()){
                        NccDhcpRelayAgentData agentData = new NccDhcpRelayAgentData();

                        agentData.id = rs.getInt("id");
                        agentData.agentName = rs.getString("agentName");
                        agentData.agentIP = rs.getLong("agentIP");
                        agentData.agentPool = rs.getInt("agentPool");

                        return agentData;
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
}
