package com.NccAPI.AstraManager;

import com.NccAstraManager.*;

import java.nio.channels.Channel;
import java.util.ArrayList;

public interface AstraManagerService {
    public AstraManagerImpl.RunData runTransponder(String apiKey, Integer id);

    public ArrayList<ServerData> getAstraServers(String apiKey);
    public ArrayList<AdapterData> getAstraAdapters(String apiKey);
    public ArrayList<AdapterType> getAstraAdapterTypes(String apiKey);
    public ArrayList<AdapterData> getAstraAdaptersByServerId(String apiKey, Integer id);
    public ArrayList<TransponderData> getAstraTransponders(String apiKey);
    public ArrayList<CamData> getAstraCams(String apiKey);

    public ArrayList<Integer> createAstraServer(String apiKey,
                                                Long serverIP,
                                                String serverSecret,
                                                Long serverLocalAddress,
                                                String serverComment,
                                                String serverName);

    public ArrayList<Integer> createAstraAdapter(String apiKey,
                                                 Integer adapterDevice,
                                                 Integer adapterType,
                                                 Integer serverId,
                                                 String adapterComment);

    public ArrayList<Integer> createAstraTransponder(String apiKey,
                                                     String transponderName,
                                                     Integer transponderFreq,
                                                     String transponderPolarity,
                                                     String transponderFEC,
                                                     Integer transponderSymbolrate,
                                                     String transponderType,
                                                     Integer adapterId,
                                                     String transponderLNB,
                                                     String transponderSat);

    public ArrayList<Integer> createAstraCam(String apiKey,
                                             String camServer,
                                             Integer camPort,
                                             String camUser,
                                             String camPassword,
                                             String camName,
                                             String camKey);

    public ArrayList<Integer> createAstraChannel(String apiKey,
                                             String channelName,
                                             Integer channelTransponder,
                                             Integer channelPnr,
                                             Integer channelCam,
                                             Long channelIP,
                                             String channelComment);

    public ArrayList<Integer> updateAstraServer(String apiKey,
                                                Integer id,
                                                Long serverIP,
                                                String serverSecret,
                                                Long serverLocalAddress,
                                                String serverComment,
                                                String serverName);


    public ArrayList<Integer> updateAstraAdapter(String apiKey,
                                                 Integer id,
                                                 Integer adapterDevice,
                                                 Integer adapterType,
                                                 Integer serverId,
                                                 String adapterComment);

    public ArrayList<Integer> updateAstraTransponder(String apiKey,
                                                     Integer id,
                                                     String transponderName,
                                                     Integer transponderFreq,
                                                     String transponderPolarity,
                                                     String transponderFEC,
                                                     Integer transponderSymbolrate,
                                                     String transponderType,
                                                     Integer adapterId,
                                                     String transponderLNB,
                                                     String transponderSat);

    public ArrayList<Integer> updateAstraCam(String apiKey,
                                             Integer id,
                                             String camServer,
                                             Integer camPort,
                                             String camUser,
                                             String camPassword,
                                             String camName,
                                             String camKey);

    public ArrayList<Integer> deleteAstraServer(String apiKey, Integer id);
    public ArrayList<Integer> deleteAstraAdapter(String apiKey, Integer id);
    public ArrayList<Integer> deleteAstraTransponder(String apiKey, Integer id);
    public ArrayList<Integer> deleteAstraCam(String apiKey, Integer id);

}
