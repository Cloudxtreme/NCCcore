package com.NccNAS;

import com.NccSystem.NccUtils;
import com.NccSystem.SQL.NccQuery;
import com.NccSystem.SQL.NccQueryException;
import com.sun.rowset.CachedRowSetImpl;
import org.apache.log4j.Logger;

import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.UUID;

public class NccNAS {

    private static Logger logger = Logger.getLogger(NccNAS.class);
    private NccQuery query;

    public NccNAS() throws NccNasException {
        try {
            query = new NccQuery();
        } catch (NccQueryException e) {
            e.printStackTrace();
            throw new NccNasException("SQL error: " + e.getMessage());
        }
    }

    private NccNasData fillNasData(CachedRowSetImpl rs){

        NccNasData nasData = new NccNasData();

        try {
            nasData.id = rs.getInt("id");
            nasData.nasName = rs.getString("nasName");
            nasData.nasIP = rs.getLong("nasIP");
            nasData.nasType = rs.getInt("nasType");
            nasData.nasStatus = rs.getInt("nasStatus");
            nasData.nasSecret = rs.getString("nasSecret");
            nasData.nasInterimInterval = rs.getInt("nasInterimInterval");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return nasData;
    }

    public NccNasData getNAS(Integer id) throws NccNasException {
        CachedRowSetImpl rs;

        try {
            rs = query.selectQuery("SELECT * FROM nccNAS WHERE id=" + id);
        } catch (NccQueryException e) {
            e.printStackTrace();
            throw new NccNasException("getNAS: SQL error: " + e.getMessage());
        }

        if (rs != null) {
            try {
                if (rs.next()) {
                    return fillNasData(rs);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new NccNasException("getNAS: SQL error: " + e.getMessage());
            }
        } else {
            throw new NccNasException("getNAS: NAS not found");
        }

        throw new NccNasException("getNAS: NAS not found");
    }

    public NccNasData getNasByIP(Long nasIP) throws NccNasException {
        CachedRowSetImpl rs;

        try {
            rs = query.selectQuery("SELECT * FROM nccNAS WHERE nasIP=" + nasIP);
        } catch (NccQueryException e) {
            e.printStackTrace();
            throw new NccNasException("getNasByIP: SQL error: " + e.getMessage());
        }

        if (rs != null) {
            try {
                if (rs.next()) {
                    return fillNasData(rs);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new NccNasException("getNasByIP: SQL error: " + e.getMessage());
            }
        } else {
            throw new NccNasException("getNasByIP: NAS not found");
        }

        throw new NccNasException("getNasByIP: NAS not found");
    }

    public String getNasSecretByIP(Long nasIP){
        try {
            NccNAS nas = new NccNAS();
            try {

                logger.debug("Getting nasSecret for '" + NccUtils.long2ip(nasIP) + "'");
                NccNasData nasData = nas.getNasByIP(nasIP);

                if (nasData != null) {
                    return nasData.nasSecret;
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } catch (NccNasException e) {
            try {
                logger.error("NAS error for '" + NccUtils.long2ip(nasIP) + "': " + e.getMessage());
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            }
        }

        return null;
    }
}
