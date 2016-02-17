package com.NccAPI.AstraManager;

import com.Ncc;
import com.NccAstraManager.*;
import com.NccSystem.NccUtils;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class AstraManagerImpl implements AstraManagerService {
    public class Channel {
        public String name;
        public Integer pnr;
        public String ip;
        public String localaddr;
    }

    public class RunData {
        public String serverIP;
        public String serverSecret;
        public String serverLocalAddress;
        public Integer transponderFreq;
        public String transponderPolarity;
        public String transponderType;
        public Integer adapterDevice;
        public String lnbSharing;
        public String camName;
        public String camIP;
        public Integer camPort;
        public String camUser;
        public String camPassword;
        public String camKey;
        public ArrayList<Channel> channels;
    }

    public class RunResult {
        public Integer status;
    }

    public RunData runTransponder(Integer id) {
        NccAstraManager astraManager = new NccAstraManager();
        ArrayList<ChannelData> channelData = astraManager.getChannelsByTransponder(id);
        ArrayList<Channel> channels = new ArrayList<>();
        RunData runData = new RunData();

        for (ChannelData ch : channelData) {
            Channel channel = new Channel();

            runData.transponderFreq = ch.transponderFreq;
            runData.transponderPolarity = ch.transponderPolarity;
            runData.transponderType = ch.transponderType;
            runData.adapterDevice = ch.adapterDevice;
            try {
                runData.camIP = NccUtils.long2ip(ch.camServer);
                runData.serverIP = NccUtils.long2ip(ch.serverIP);
                runData.serverLocalAddress = NccUtils.long2ip(ch.serverLocalAddress);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            runData.camPort = ch.camPort;
            runData.serverSecret = ch.serverSecret;
            runData.lnbSharing = "true";
            runData.camName = ch.camName;
            runData.camUser = ch.camUser;
            runData.camPassword = ch.camPassword;
            runData.camKey = ch.camKey;

            channel.name = ch.channelName;
            try {
                channel.ip = NccUtils.long2ip(ch.channelIP);
                channel.localaddr = NccUtils.long2ip(ch.serverLocalAddress);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            channel.pnr = ch.channelPnr;

            channels.add(channel);
        }

        runData.channels = channels;

        try {
            JsonRpcHttpClient client = new JsonRpcHttpClient(new URL("http://" + runData.serverIP + ":8085"));

            try {
                RunResult result = new RunResult();
                result = client.invoke("runTransponder", new Object[]{
                        runData.transponderFreq,
                        runData.transponderPolarity,
                        runData.adapterDevice,
                        runData.transponderType,
                        runData.lnbSharing,
                        runData.camIP,
                        runData.camName,
                        runData.camPort,
                        runData.camUser,
                        runData.camPassword,
                        runData.camKey,
                        "false",
                        runData.channels
                }, RunResult.class);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return runData;
    }

    public ArrayList<ServerData> getAstraServers(){
        NccAstraManager astraManager = new NccAstraManager();

        return astraManager.getServers();
    }

    public ArrayList<AdapterData> getAstraAdapters(){
        NccAstraManager astraManager = new NccAstraManager();

        return astraManager.getAdapters();
    }

    public ArrayList<AdapterData> getAstraAdaptersByServerId(Integer id){
        NccAstraManager astraManager = new NccAstraManager();

        return astraManager.getAdaptersByServerId(id);
    }

    public ArrayList<AdapterType> getAstraAdapterTypes(){
        NccAstraManager astraManager = new NccAstraManager();

        return astraManager.getAdapterTypes();
    }

    public ArrayList<TransponderData> getAstraTransponders(){
        NccAstraManager astraManager = new NccAstraManager();

        return astraManager.getTransponders();
    }

    public ArrayList<CamData> getAstraCams(){
        NccAstraManager astraManager = new NccAstraManager();

        return astraManager.getCams();
    }

    public ArrayList<Integer> createAstraServer(Long serverIP,
                                           String serverSecret,
                                           Long serverLocalAddress,
                                           String serverComment,
                                           String serverName){

        NccAstraManager astraManager = new NccAstraManager();
        ServerData serverData = new ServerData();

        serverData.serverIP = serverIP;
        serverData.serverSecret = serverSecret;
        serverData.serverLocalAddress = serverLocalAddress;
        serverData.serverComment = serverComment;
        serverData.serverName = serverName;

        return astraManager.createServer(serverData);
    }

    public ArrayList<Integer> createAstraAdapter(Integer adapterDevice,
                                            Integer adapterType,
                                            Integer serverId,
                                            String adapterComment){
        NccAstraManager astraManager = new NccAstraManager();
        AdapterData adapterData = new AdapterData();

        adapterData.adapterDevice = adapterDevice;
        adapterData.adapterType = adapterType;
        adapterData.serverId = serverId;
        adapterData.adapterComment = adapterComment;

        return astraManager.createAdapter(adapterData);
    }

    public ArrayList<Integer> createAstraTransponder(String transponderName,
                                                     Integer transponderFreq,
                                                     String transponderPolarity,
                                                     String transponderFEC,
                                                     Integer transponderSymbolrate,
                                                     String transponderType,
                                                     Integer adapterId,
                                                     String transponderLNB,
                                                     String transponderSat){
        NccAstraManager astraManager = new NccAstraManager();
        TransponderData transponderData = new TransponderData();

        transponderData.transponderName = transponderName;
        transponderData.transponderFreq = transponderFreq;
        transponderData.transponderPolarity = transponderPolarity;
        transponderData.transponderFEC = transponderFEC;
        transponderData.transponderSymbolrate = transponderSymbolrate;
        transponderData.transponderType = transponderType;
        transponderData.adapterId = adapterId;
        transponderData.transponderLNB = transponderLNB;
        transponderData.transponderSat = transponderSat;

        return astraManager.createTransponder(transponderData);
    }

    public ArrayList<Integer> createAstraCam(String camServer,
                                             Integer camPort,
                                             String camUser,
                                             String camPassword,
                                             String camName,
                                             String camKey){

        NccAstraManager astraManager = new NccAstraManager();
        CamData camData = new CamData();

        camData.camServer = camServer;
        camData.camPort = camPort;
        camData.camUser = camUser;
        camData.camPassword = camPassword;
        camData.camName = camName;
        camData.camKey = camKey;

        return astraManager.createCam(camData);
    }

    public ArrayList<Integer> deleteAstraServer(Integer id){
        NccAstraManager astraManager = new NccAstraManager();

        return astraManager.deleteServer(id);
    }

    public ArrayList<Integer> deleteAstraAdapter(Integer id){
        NccAstraManager astraManager = new NccAstraManager();

        return astraManager.deleteAdapter(id);
    }

    public ArrayList<Integer> deleteAstraTransponder(Integer id){
        NccAstraManager astraManager = new NccAstraManager();

        return astraManager.deleteTransponder(id);
    }

    public ArrayList<Integer> deleteAstraCam(Integer id){
        NccAstraManager astraManager = new NccAstraManager();

        return astraManager.deleteCam(id);
    }

    public ArrayList<Integer> updateAstraServer(Integer id, Long serverIP, String serverSecret, Long serverLocalAddress, String serverComment, String serverName){
        NccAstraManager astraManager = new NccAstraManager();

        ServerData serverData = new ServerData();

        serverData.id = id;
        serverData.serverIP = serverIP;
        serverData.serverSecret = serverSecret;
        serverData.serverLocalAddress = serverLocalAddress;
        serverData.serverComment = serverComment;
        serverData.serverName = serverName;

        return astraManager.updateServer(serverData);
    }

    public ArrayList<Integer> updateAstraAdapter(Integer id, Integer adapterDevice, Integer adapterType, Integer serverId, String adapterComment){
        NccAstraManager astraManager = new NccAstraManager();

        AdapterData adapterData = new AdapterData();

        adapterData.id = id;
        adapterData.adapterDevice = adapterDevice;
        adapterData.adapterType = adapterType;
        adapterData.serverId = serverId;
        adapterData.adapterComment = adapterComment;

        return astraManager.updateAdapter(adapterData);
    }

    public ArrayList<Integer> updateAstraTransponder(Integer id, String transponderName, Integer transponderFreq, String transponderPolarity, String transponderFEC, Integer transponderSymbolrate, String transponderType, Integer adapterId, String transponderLNB, String transponderSat){
        NccAstraManager astraManager = new NccAstraManager();

        TransponderData transponderData = new TransponderData();

        transponderData.id = id;
        transponderData.transponderName = transponderName;
        transponderData.transponderFreq = transponderFreq;
        transponderData.transponderPolarity = transponderPolarity;
        transponderData.transponderFEC = transponderFEC;
        transponderData.transponderSymbolrate = transponderSymbolrate;
        transponderData.transponderType = transponderType;
        transponderData.adapterId = adapterId;
        transponderData.transponderLNB = transponderLNB;
        transponderData.transponderSat = transponderSat;

        return astraManager.updateTransponder(transponderData);
    }

    public ArrayList<Integer> updateAstraCam(Integer id,
                                             String camServer,
                                             Integer camPort,
                                             String camUser,
                                             String camPassword,
                                             String camName,
                                             String camKey){

        NccAstraManager astraManager = new NccAstraManager();
        CamData camData = new CamData();

        camData.id = id;
        camData.camServer = camServer;
        camData.camPort = camPort;
        camData.camUser = camUser;
        camData.camPassword = camPassword;
        camData.camName = camName;
        camData.camKey = camKey;

        return astraManager.updateCam(camData);
    }
}
