package com.NccPools;

import com.NccSystem.SQL.NccQuery;
import com.NccSystem.SQL.NccQueryException;
import com.sun.rowset.CachedRowSetImpl;

import java.sql.SQLException;
import java.util.ArrayList;

public class NccPools {

    public ArrayList<PoolData> getPools(){
        ArrayList<PoolData> pools = new ArrayList<>();

        try {
            CachedRowSetImpl rs = new NccQuery().selectQuery("SELECT id, poolStart, poolEnd, poolStatus, poolComments, poolName FROM nccPools");

            try {
                while (rs.next()){
                    PoolData poolData = new PoolData();

                    poolData.id = rs.getInt("id");
                    poolData.poolStart = rs.getLong("poolStart");
                    poolData.poolEnd = rs.getLong("poolEnd");
                    poolData.poolStatus = rs.getInt("poolStatus");
                    poolData.poolComments = rs.getString("poolComments");
                    poolData.poolName = rs.getString("poolName");

                    pools.add(poolData);
                }

                return pools;
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }

    public PoolData getPool(Integer poolId){

        try {
            CachedRowSetImpl rs = new NccQuery().selectQuery("SELECT * FROM nccPools WHERE id="+poolId);

            try {
                if(rs.next()){
                    PoolData poolData = new PoolData();

                    poolData.id = rs.getInt("id");
                    poolData.poolStart = rs.getLong("poolStart");
                    poolData.poolEnd = rs.getLong("poolEnd");
                    poolData.poolStatus = rs.getInt("poolStatus");
                    poolData.poolComments = rs.getString("poolComments");
                    poolData.poolName = rs.getString("poolName");

                    return poolData;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }
}
