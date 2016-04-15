package com.NccDhcp;

import com.NccSystem.SQL.NccQuery;
import com.NccSystem.SQL.NccQueryException;
import com.sun.rowset.CachedRowSetImpl;

import java.sql.SQLException;

public class NccDhcpPools {

    private NccQuery query;

    public NccDhcpPools(){
        try {
            query = new NccQuery();
        } catch (NccQueryException e) {
            e.printStackTrace();
        }
    }

    public NccDhcpPoolData getPool(Integer id) {

        CachedRowSetImpl rs;

        try {
            rs = query.selectQuery("SELECT id, " +
                    "poolName, " +
                    "poolStart, " +
                    "poolEnd, " +
                    "poolRouter, " +
                    "poolNetmask, " +
                    "poolDNS1, " +
                    "poolDNS2, " +
                    "poolLeaseTime FROM ncc_dhcp_pools WHERE id=" + id);

            if(rs != null){

                try {
                    if(rs.next()){
                        NccDhcpPoolData poolData = new NccDhcpPoolData();

                        poolData.id = rs.getInt("id");
                        poolData.poolName = rs.getString("poolName");
                        poolData.poolStart = rs.getLong("poolStart");
                        poolData.poolEnd = rs.getLong("poolEnd");
                        poolData.poolRouter = rs.getLong("poolRouter");
                        poolData.poolNetmask  = rs.getLong("poolNetmask");
                        poolData.poolDNS1 = rs.getLong("poolDNS1");
                        poolData.poolDNS2 = rs.getLong("poolDNS2");
                        poolData.poolLeaseTime = rs.getInt("poolLeaseTime");

                        return poolData;
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
