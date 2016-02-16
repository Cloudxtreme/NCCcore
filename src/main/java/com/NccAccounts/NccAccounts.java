package com.NccAccounts;

import com.NccSystem.SQL.NccQuery;
import com.NccSystem.SQL.NccQueryException;
import com.sun.rowset.CachedRowSetImpl;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class NccAccounts {

    private NccQuery query;
    private static Logger logger = Logger.getLogger(NccAccounts.class);
    private final String accQueryFieldset = "id, accDeposit, accCredit, accPerson, accAddressCity, accAddressStreet, accAddressBuild, accAddressApt, accRegDate, accPersonPassport, accPersonPhone, accPersonEmail, accComments";

    public NccAccounts() throws NccAccountsException {
        try {
            query = new NccQuery();
        } catch (NccQueryException e) {
            e.printStackTrace();
        }
    }

    public AccountData getAccount(Integer id) throws NccAccountsException {

        CachedRowSetImpl rs;

        try {
            rs = query.selectQuery("SELECT " + accQueryFieldset + " FROM nccUserAccounts WHERE id=" + id);
        } catch (NccQueryException e) {
            throw new NccAccountsException("UserAccount: SQL error: " + e.getMessage());
        }

        if (rs != null) {
            try {
                if (rs.next()) {

                    AccountData accountData = new AccountData();

                    accountData.id = rs.getInt("id");
                    accountData.accDeposit = rs.getDouble("accDeposit");
                    accountData.accCredit = rs.getDouble("accCredit");
                    accountData.accPerson = rs.getString("accPerson");
                    accountData.accAddressCity = rs.getString("accAddressCity");
                    accountData.accAddressStreet = rs.getString("accAddressStreet");
                    accountData.accAddressBuild = rs.getString("accAddressBuild");
                    accountData.accAddressApt = rs.getString("accAddressApt");
                    accountData.accRegDate = rs.getDate("accRegDate");
                    accountData.accPersonPassport = rs.getString("accPersonPassport");
                    accountData.accPersonPhone = rs.getString("accPersonPhone");
                    accountData.accPersonEmail = rs.getString("accPersonEmail");
                    accountData.accComments = rs.getString("accComments");

                    return accountData;
                } else {
                    throw new NccAccountsException("UserAccount not found");
                }
            } catch (SQLException e) {
                throw new NccAccountsException("UserAccount: SQL error: " + e.getMessage());
            }
        } else {
            throw new NccAccountsException("UserAccount not found");
        }
    }

    public ArrayList<AccountData> getAccounts() throws NccAccountsException {

        ArrayList<AccountData> accounts = new ArrayList<>();

        ResultSet rs;

        try {
            rs = query.selectQuery("SELECT " + accQueryFieldset + " FROM nccUserAccounts LIMIT 10");

            try {
                while (rs.next()) {

                    AccountData accountData = new AccountData();

                    accountData.id = rs.getInt("id");
                    accountData.accDeposit = rs.getDouble("accDeposit");
                    accountData.accCredit = rs.getDouble("accCredit");
                    accountData.accPerson = rs.getString("accPerson");
                    accountData.accAddressCity = rs.getString("accAddressCity");
                    accountData.accAddressStreet = rs.getString("accAddressStreet");
                    accountData.accAddressBuild = rs.getString("accAddressBuild");
                    accountData.accAddressApt = rs.getString("accAddressApt");
                    accountData.accRegDate = rs.getDate("accRegDate");
                    accountData.accPersonPassport = rs.getString("accPersonPassport");
                    accountData.accPersonPhone = rs.getString("accPersonPhone");
                    accountData.accPersonEmail = rs.getString("accPersonEmail");
                    accountData.accComments = rs.getString("accComments");

                    accounts.add(accountData);
                }

                return accounts;

            } catch (SQLException e) {
                e.printStackTrace();
            }


        } catch (NccQueryException e) {
            e.printStackTrace();
            throw new NccAccountsException("CachedQuery error: " + e.getMessage());
        }

        return null;
    }

    public ArrayList<Integer> createAccount(AccountData accountData) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String insertQuery = "INSERT INTO nccUserAccounts (" +
                "accDeposit, " +
                "accCredit, " +
                "accPerson, " +
                "accAddressCity, " +
                "accAddressStreet, " +
                "accAddressBuild, " +
                "accAddressApt, " +
                "accRegDate, " +
                "accPersonPassport, " +
                "accPersonPhone, " +
                "accPersonEmail, " +
                "accComments) VALUES (" +
                accountData.accDeposit + ", " +
                accountData.accCredit + ", " +
                "'" + accountData.accPerson + "', " +
                "'" + accountData.accAddressCity + "', " +
                "'" + accountData.accAddressStreet + "', " +
                "'" + accountData.accAddressBuild + "', " +
                "'" + accountData.accAddressApt + "', " +
                "'" + dateFormat.format(accountData.accRegDate) + "', " +
                "'" + accountData.accPersonPassport + "', " +
                "'" + accountData.accPersonPhone + "', " +
                "'" + accountData.accPersonEmail + "', " +
                "'" + accountData.accComments + "')";

        try {
            NccQuery query = new NccQuery();

            return query.updateQuery(insertQuery);

        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }
}
