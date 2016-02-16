package com.NccAPI.UserAccounts;

import com.NccAccounts.AccountData;

import java.util.ArrayList;
import java.util.Date;

public interface AccountsService {
    AccountData getAccount(Integer id);
    ArrayList<AccountData> getAccounts();
    ArrayList<Integer> createAccount(Double accDeposit,
                                     Double accCredit,
                                     String accPerson,
                                     String accAddressCity,
                                     String accAddressStreet,
                                     String accAddressBuild,
                                     String accAddressApt,
                                     Date accRegDate,
                                     String accPersonPassport,
                                     String accPersonPhone,
                                     String accPersonEmail,
                                     String accComments);
}
