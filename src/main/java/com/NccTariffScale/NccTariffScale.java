package com.NccTariffScale;

import com.NccPools.NccPools;
import com.NccPools.PoolData;
import com.NccSystem.SQL.NccQuery;
import com.NccSystem.SQL.NccQueryException;
import com.sun.rowset.CachedRowSetImpl;

import java.sql.SQLException;
import java.util.ArrayList;

public class NccTariffScale {

    public TariffData getTariffByUserId(Integer userId) {

        try {
            NccQuery query = new NccQuery();
            CachedRowSetImpl rs;

            rs = query.selectQuery("SELECT * FROM nccViewUserTariff WHERE userId=" + userId);

            if (rs != null) {
                try {
                    if (rs.next()) {
                        TariffData tariffData = new TariffData();

                        tariffData.id = rs.getInt("id");
                        tariffData.tariffName = rs.getString("tariffName");
                        tariffData.tariffCost = rs.getDouble("tariffCost");
                        tariffData.tariffStart = rs.getDate("tariffStart");
                        tariffData.tariffEnd = rs.getDate("tariffEnd");
                        tariffData.tariffStatus = rs.getInt("tariffStatus");

                        return tariffData;
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

    public ArrayList<PoolData> getTariffPools(Integer tariffId) {
        try {
            NccQuery query = new NccQuery();
            CachedRowSetImpl rs;

            rs = query.selectQuery("SELECT * FROM nccViewTariffPools WHERE tariffId=" + tariffId);

            if(rs != null){
                try {
                    ArrayList<PoolData> tariffPools = new ArrayList<>();

                    while (rs.next()){
                        PoolData poolData = new PoolData();

                        poolData.id = rs.getInt("poolId");
                        poolData.poolName = rs.getString("poolName");
                        poolData.poolStart = rs.getLong("poolStart");
                        poolData.poolEnd = rs.getLong("poolEnd");
                        poolData.poolStatus = rs.getInt("poolStatus");
                        poolData.poolComments = rs.getString("poolComments");

                        tariffPools.add(poolData);
                    }

                    return tariffPools;
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
