package com.NccAPI.Views;

import com.NccSystem.SQL.NccQuery;
import com.NccSystem.SQL.NccQueryException;
import com.sun.rowset.CachedRowSetImpl;

import java.sql.SQLException;
import java.util.ArrayList;

public class ViewsServiceImpl implements ViewsService {

    private static NccQuery query;

    public ViewsServiceImpl() {
        try {
            query = new NccQuery();
        } catch (NccQueryException e) {
            e.printStackTrace();
        }
    }

    // TODO: 01.02.2016 Fix TIMEDIFF in view
    public ArrayList<OnlineData> getOnline() {
        ArrayList<OnlineData> onlineData = new ArrayList<>();

        CachedRowSetImpl rs;

        try {
            rs = query.selectQuery("SELECT * FROM nccViewOnline");

            if (rs != null) {
                try {
                    while (rs.next()) {
                        OnlineData res = new OnlineData();

                        res.sessionId = rs.getString("sessionId");
                        res.startTime = rs.getInt("startTime");
                        res.acctInputOctets = rs.getLong("acctInputOctets");
                        res.acctOutputOctets = rs.getLong("acctOutputOctets");
                        res.framedIP = rs.getLong("framedIP");
                        res.framedMAC = rs.getString("framedMAC");
                        res.userLogin = rs.getString("userLogin");
                        res.accDeposit = rs.getDouble("accDeposit");
                        res.accCredit = rs.getDouble("accCredit");
                        res.startTimeHR = rs.getString("startTimeHR");
                        res.framedIpHR = rs.getString("framedIpHR");
                        res.sessionDuration = rs.getLong("sessionDuration");
                        res.sessionDurationHR = rs.getString("sessionDurationHR");

                        onlineData.add(res);
                    }

                    return onlineData;
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
