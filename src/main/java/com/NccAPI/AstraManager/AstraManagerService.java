package com.NccAPI.AstraManager;

import com.NccAstraManager.*;

import java.nio.channels.Channel;
import java.util.ArrayList;

public interface AstraManagerService {
    public AstraManagerImpl.RunData runTransponder(Integer id);

    public ArrayList<ServerData> getAstraServers();

    public ArrayList<Integer> createServer(Long serverIP,
                                           String serverSecret,
                                           Long serverLocalAddress,
                                           String serverComment,
                                           String serverName);

    public ArrayList<Integer> deleteServer(Integer id);

    public ArrayList<Integer> updateServer(Integer id,
                                           Long serverIP,
                                           String serverSecret,
                                           Long serverLocalAddress,
                                           String serverComment,
                                           String serverName);

    public ArrayList<AdapterData> getAstraAdapters();

    public ArrayList<AdapterData> getAstraAdaptersByServerId(Integer id);

    public ArrayList<CamData> getAstraCam();

    public ArrayList<Integer> createAdapter(Integer adapterDevice,
                                            Integer adapterType,
                                            Integer serverId,
                                            String adapterComment);

    public ArrayList<Integer> deleteAdapter(Integer id);

    public ArrayList<Integer> updateAdapter(Integer id,
                                            Integer adapterDevice,
                                            Integer adapterType,
                                            Integer serverId,
                                            String adapterComment);

    public ArrayList<AdapterType> getAstraAdapterTypes();

    public ArrayList<TransponderData> getAstraTransponders();

    public ArrayList<Integer> createAstraTransponder(String transponderName,
                                                     Integer transponderFreq,
                                                     String transponderPolarity,
                                                     String transponderFEC,
                                                     Integer transponderSymbolrate,
                                                     String transponderType,
                                                     Integer adapterId,
                                                     String transponderLNB,
                                                     String transponderSat);

    public ArrayList<Integer> deleteAstraTransponder(Integer id);

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

    public ArrayList<Integer> createAstraCam(String camServer,
                                             Integer camPort,
                                             String camUser,
                                             String camPassword,
                                             String camName,
                                             String camKey);

    public ArrayList<Integer> deleteAstraCam(Integer id);

    public ArrayList<Integer> updateAstraCam(Integer id,
                                             String camServer,
                                             Integer camPort,
                                             String camUser,
                                             String camPassword,
                                             String camName,
                                             String camKey);
}
