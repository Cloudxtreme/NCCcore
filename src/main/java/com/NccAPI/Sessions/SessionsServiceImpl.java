package com.NccAPI.Sessions;

import com.NccSessions.NccSessions;
import com.NccSessions.NccSessionsException;
import com.NccSessions.SessionData;

import java.util.ArrayList;

public class SessionsServiceImpl implements SessionsService {

    public ArrayList<SessionData> getSessions(){
        try {
            return new NccSessions().getSessions();
        } catch (NccSessionsException e) {
            e.printStackTrace();
        }
        return null;
    }
}
