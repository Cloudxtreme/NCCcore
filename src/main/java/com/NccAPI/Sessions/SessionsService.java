package com.NccAPI.Sessions;

import com.NccSessions.SessionData;

import java.util.ArrayList;

public interface SessionsService {

    public ArrayList<SessionData> getSessions(String apiKey);
    public SessionData getSessionByUID(String apiKey, Integer uid);
    public SessionData getSession(String apiKey, String sessionId);
}
