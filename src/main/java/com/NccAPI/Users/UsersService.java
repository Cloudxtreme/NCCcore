package com.NccAPI.Users;

import com.NccUsers.UserData;

import java.util.ArrayList;

/**
 * Created by seko on 18.01.2016.
 *
 */
public interface UsersService {
    UserData getUser(String login);
    ArrayList<UserData> getUsers();
    ArrayList<Integer> createUser(
            String userLogin,
            String userPassword,
            Integer userStatus,
            Integer userAccount,
            Integer userIP);
}

