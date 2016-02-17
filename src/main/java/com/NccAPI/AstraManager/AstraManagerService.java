package com.NccAPI.AstraManager;

import com.NccAstraManager.*;

import java.nio.channels.Channel;
import java.util.ArrayList;

public interface AstraManagerService {
    public AstraManagerImpl.RunData runTransponder(Integer id);

    public ArrayList<Integer> createAstraServer(Long serverIP,
                                           String serverSecret,
                                           Long serverLocalAddress,
                                           String serverComment,
                                           String serverName);

    public ArrayList<ServerData> getAstraServers();

    public ArrayList<Integer> updateAstraServer(Integer id,
                                           Long serverIP,
                                           String serverSecret,
                                           Long serverLocalAddress,
                                           String serverComment,
                                           String serverName);

    public ArrayList<Integer> deleteAstraServer(Integer id);

    public ArrayList<Integer> createAstraAdapter(Integer adapterDevice,
                                            Integer adapterType,
                                            Integer serverId,
                                            String adapterComment);

    public ArrayList<AdapterData> getAstraAdapters();

    public ArrayList<AdapterType> getAstraAdapterTypes();
    public ArrayList<AdapterData> getAstraAdaptersByServerId(Integer id);

    public ArrayList<Integer> updateAstraAdapter(Integer id,
                                            Integer adapterDevice,
                                            Integer adapterType,
                                            Integer serverId,
                                            String adapterComment);

    public ArrayList<Integer> deleteAstraAdapter(Integer id);

    public ArrayList<Integer> createAstraTransponder(String transponderName,
                                                     Integer transponderFreq,
                                                     String transponderPolarity,
                                                     String transponderFEC,
                                                     Integer transponderSymbolrate,
                                                     String transponderType,
                                                     Integer adapterId,
                                                     String transponderLNB,
                                                     String transponderSat);

    public ArrayList<TransponderData> getAstraTransponders();

    public ArrayList<Integer> updateAstraTransponder(Integer id,
                                                     String transponderName,
                                                     Integer transponderFreq,
                                                     String transponderPolarity,
                                                     String transponderFEC,
                                                     Integer transponderSymbolrate,
                                                     String transponderType,
                                                     Integer adapterId,
                                                     String transponderLNB,
                                                     String transponderSat);

    public ArrayList<Integer> deleteAstraTransponder(Integer id);

    public ArrayList<Integer> createAstraCam(String camServer,
                                             Integer camPort,
                                             String camUser,
                                             String camPassword,
                                             String camName,
                                             String camKey);

    public ArrayList<CamData> getAstraCams();

    public ArrayList<Integer> updateAstraCam(Integer id,
                                             String camServer,
                                             Integer camPort,
                                             String camUser,
                                             String camPassword,
                                             String camName,
                                             String camKey);

    public ArrayList<Integer> deleteAstraCam(Integer id);

}
