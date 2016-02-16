package com.NccAstraManager;

import com.NccSystem.SQL.NccQuery;
import com.NccSystem.SQL.NccQueryException;
import com.sun.rowset.CachedRowSetImpl;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;

public class NccAstraManager {
    private static Logger logger = Logger.getLogger(NccAstraManager.class);
    private NccQuery query;

    public NccAstraManager() {
        try {
            query = new NccQuery();
        } catch (NccQueryException e) {
            e.printStackTrace();
        }
    }

    private ServerData fillServerData(CachedRowSetImpl rs) {
        ServerData serverData = new ServerData();

        try {
            serverData.id = rs.getInt("id");
            serverData.serverIP = rs.getLong("serverIP");
            serverData.serverSecret = rs.getString("serverSecret");
            serverData.serverLocalAddress = rs.getLong("serverLocalAddress");
            serverData.serverComment = rs.getString("serverComment");
            serverData.serverName = rs.getString("serverName");

            return serverData;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private AdapterData fillAdapterData(CachedRowSetImpl rs) {
        AdapterData adapterData = new AdapterData();

        try {
            adapterData.id = rs.getInt("id");
            adapterData.adapterDevice = rs.getInt("adapterDevice");
            adapterData.adapterType = rs.getInt("adapterType");
            adapterData.serverId = rs.getInt("serverId");
            adapterData.adapterComment = rs.getString("adapterComment");

            return adapterData;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private AdapterType fillAdapterType(CachedRowSetImpl rs) {
        AdapterType adapterType = new AdapterType();

        try {
            adapterType.id = rs.getInt("id");
            adapterType.cardName = rs.getString("cardName");
            adapterType.chipName = rs.getString("chipName");

            return adapterType;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private TransponderData fillTransponderData(CachedRowSetImpl rs) {
        TransponderData transponderData = new TransponderData();

        try {
            transponderData.id = rs.getInt("id");
            transponderData.transponderName = rs.getString("transponderName");
            transponderData.transponderFreq = rs.getInt("transponderFreq");
            transponderData.transponderPolarity = rs.getString("transponderPolarity");
            transponderData.transponderFEC = rs.getString("transponderFEC");
            transponderData.transponderSymbolrate = rs.getInt("transponderSymbolrate");
            transponderData.transponderType = rs.getString("transponderType");
            transponderData.adapterId = rs.getInt("adapterId");
            transponderData.adapterDevice = rs.getInt("adapterDevice");
            transponderData.adapterCard = rs.getString("adapterCard");
            transponderData.adapterChip = rs.getString("adapterChip");
            transponderData.serverId = rs.getInt("serverId");
            transponderData.serverIP = rs.getLong("serverIP");
            transponderData.serverSecret = rs.getString("serverSecret");
            transponderData.serverLocalAddress = rs.getLong("serverLocalAddress");
            transponderData.serverName = rs.getString("serverName");
            transponderData.transponderLNB = rs.getString("transponderLNB");
            transponderData.transponderSat = rs.getString("transponderSat");
            transponderData.transponderStatus = rs.getInt("transponderStatus");

            return transponderData;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private CamData fillCamData(CachedRowSetImpl rs) {
        CamData camData = new CamData();

        try {
            camData.id = rs.getInt("id");
            camData.camName = rs.getString("camName");
            camData.camServer = rs.getString("camServer");
            camData.camPort = rs.getInt("camPort");
            camData.camUser = rs.getString("camUser");
            camData.camPassword = rs.getString("camPassword");
            camData.camKey = rs.getString("camKey");

            return camData;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private ChannelData fillChannelData(CachedRowSetImpl rs) {
        ChannelData channelData = new ChannelData();

        try {
            channelData.channelId = rs.getInt("channelId");
            channelData.channelName = rs.getString("channelName");
            channelData.channelPnr = rs.getInt("channelPnr");
            channelData.transponderId = rs.getInt("transponderId");
            channelData.channelIP = rs.getLong("channelIP");
            channelData.camId = rs.getInt("camId");

            return channelData;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private ChannelData fillChannelDataExtended(CachedRowSetImpl rs) {
        ChannelData channelData = new ChannelData();

        channelData = fillChannelData(rs);

        try {
            channelData.camName = rs.getString("camName");
            channelData.camServer = rs.getLong("camServer");
            channelData.camPort = rs.getInt("camPort");
            channelData.camUser = rs.getString("camUser");
            channelData.camPassword = rs.getString("camPassword");
            channelData.camKey = rs.getString("camKey");

            channelData.transponderName = rs.getString("transponderName");
            channelData.transponderFreq = rs.getInt("transponderFreq");
            channelData.transponderPolarity = rs.getString("transponderPolarity");
            channelData.transponserFEC = rs.getString("transponderFEC");
            channelData.transponderSymbolrate = rs.getInt("transponderSymbolrate");
            channelData.transponderType = rs.getString("transponderType");

            channelData.adapterDevice = rs.getInt("adapterDevice");
            channelData.adapterType = rs.getString("adapterType");

            channelData.serverIP = rs.getLong("serverIP");
            channelData.serverSecret = rs.getString("serverSecret");
            channelData.serverLocalAddress = rs.getLong("serverLocalAddress");

            return channelData;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ServerData getServerById(Integer id) {
        ServerData serverData = new ServerData();

        CachedRowSetImpl rs;

        try {
            rs = query.selectQuery("SELECT * FROM nccIptvServers WHERE id=" + id);

            try {
                if (rs.next()) {
                    return fillServerData(rs);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<ChannelData> getChannelsByTransponder(Integer id) {
        ArrayList<ChannelData> channels = new ArrayList<>();

        CachedRowSetImpl rs;

        try {
            rs = query.selectQuery("SELECT * FROM nccViewAstraManagerChannel WHERE transponderId=" + id);

            try {
                while (rs.next()) {
                    ChannelData channelData = fillChannelDataExtended(rs);

                    if (channelData != null) {
                        channels.add(channelData);
                    }
                }

                return channels;
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<ServerData> getServers() {
        ArrayList<ServerData> servers = new ArrayList<>();
        CachedRowSetImpl rs;

        try {
            rs = query.selectQuery("SELECT * FROM nccIptvServers");

            try {
                while (rs.next()) {
                    ServerData serverData = fillServerData(rs);
                    servers.add(serverData);
                }
                return servers;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<AdapterData> getAdapters() {
        ArrayList<AdapterData> adapters = new ArrayList<>();
        CachedRowSetImpl rs;

        try {
            rs = query.selectQuery("SELECT * FROM nccIptvAdapters GROUP BY serverId, adapterDevice");

            try {
                while (rs.next()) {
                    AdapterData adapterData = fillAdapterData(rs);
                    adapters.add(adapterData);
                }
                return adapters;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<AdapterData> getAdaptersByServerId(Integer id) {
        ArrayList<AdapterData> adapters = new ArrayList<>();
        CachedRowSetImpl rs;

        try {
            rs = query.selectQuery("SELECT * FROM nccIptvAdapters WHERE serverId=" + id);

            try {
                while (rs.next()) {
                    AdapterData adapterData = fillAdapterData(rs);
                    adapters.add(adapterData);
                }
                return adapters;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<AdapterType> getAdapterTypes() {
        ArrayList<AdapterType> adapterTypes = new ArrayList<>();
        CachedRowSetImpl rs;

        try {
            rs = query.selectQuery("SELECT * FROM nccIptvAdapterTypes");

            try {
                while (rs.next()) {
                    AdapterType adapterType = fillAdapterType(rs);
                    adapterTypes.add(adapterType);
                }
                return adapterTypes;
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<TransponderData> getTransponders() {
        ArrayList<TransponderData> transponders = new ArrayList<>();
        CachedRowSetImpl rs;

        try {
            rs = query.selectQuery("SELECT * FROM nccViewAstraTransponders");

            try {
                while (rs.next()) {
                    TransponderData transponderData = fillTransponderData(rs);
                    transponders.add(transponderData);
                }
                return transponders;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<CamData> getCams() {
        ArrayList<CamData> cams = new ArrayList<>();
        CachedRowSetImpl rs;

        try {
            rs = query.selectQuery("SELECT * FROM nccIptvCam");

            try {
                while (rs.next()) {
                    CamData camData = fillCamData(rs);
                    cams.add(camData);
                }
                return cams;
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (NccQueryException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Integer> createServer(ServerData serverData) {

        try {
            ArrayList<Integer> ids = query.updateQuery("INSERT INTO nccIptvServers (" +
                    "serverIP," +
                    "serverSecret," +
                    "serverLocalAddress, " +
                    "serverComment, " +
                    "serverName" +
                    ") VALUES (" +
                    serverData.serverIP + ", " +
                    "'" + serverData.serverSecret + "', " +
                    serverData.serverLocalAddress + ", " +
                    "'" + serverData.serverComment + "', " +
                    "'" + serverData.serverName + "'" +
                    ")");

            return ids;
        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Integer> createAdapter(AdapterData adapterData) {

        try {
            ArrayList<Integer> ids = query.updateQuery("INSERT INTO nccIptvAdapters (" +
                    "adapterDevice," +
                    "adapterType," +
                    "serverId, " +
                    "adapterComment" +
                    ") VALUES (" +
                    adapterData.adapterDevice + ", " +
                    adapterData.adapterType + ", " +
                    adapterData.serverId + ", " +
                    "'" + adapterData.adapterComment + "'" +
                    ")");

            return ids;
        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Integer> createTransponder(TransponderData transponderData) {

        try {
            ArrayList<Integer> ids = query.updateQuery("INSERT INTO nccIptvTransponders (" +
                    "transName," +
                    "transFreq," +
                    "transPolarity," +
                    "transFEC," +
                    "transSymbolrate," +
                    "transType," +
                    "adapterId," +
                    "transLNB," +
                    "transSat," +
                    "transStatus" +
                    ") VALUES (" +
                    "'" + transponderData.transponderName + "', " +
                    transponderData.transponderFreq + ", " +
                    "'" + transponderData.transponderPolarity + "', " +
                    "'" + transponderData.transponderFEC + "', " +
                    transponderData.transponderSymbolrate + ", " +
                    "'" + transponderData.transponderType + "', " +
                    transponderData.adapterId + ", " +
                    "'" + transponderData.transponderLNB + "', " +
                    "'" + transponderData.transponderSat + "', " +
                    "1" +
                    ")");

            return ids;
        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Integer> createCam(CamData camData) {

        try {
            ArrayList<Integer> ids = query.updateQuery("INSERT INTO nccIptvCam (" +
                    "camServer," +
                    "camPort," +
                    "camUser," +
                    "camPassword," +
                    "camName," +
                    "camKey" +
                    ") VALUES (" +
                    "'" + camData.camServer + "', " +
                    camData.camPort + ", " +
                    "'" + camData.camUser + "', " +
                    "'" + camData.camPassword + "', " +
                    "'" + camData.camName + "', " +
                    "'" + camData.camKey + "'" +
                    ")");

            return ids;
        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Integer> deleteServer(Integer id) {
        try {
            ArrayList<Integer> ids = query.updateQuery("DELETE FROM nccIptvServers WHERE id=" + id);

            return ids;
        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Integer> deleteAdapter(Integer id) {
        try {
            ArrayList<Integer> ids = query.updateQuery("DELETE FROM nccIptvAdapters WHERE id=" + id);

            return ids;
        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Integer> deleteTransponder(Integer id) {
        try {
            ArrayList<Integer> ids = query.updateQuery("DELETE FROM nccIptvTransponders WHERE id=" + id);

            return ids;
        } catch (NccQueryException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Integer> deleteCam(Integer id) {

        try {
            ArrayList<Integer> ids = query.updateQuery("DELETE FROM nccIptvCam WHERE id=" + id);

            return ids;
        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Integer> updateServer(ServerData serverData) {

        try {
            ArrayList<Integer> ids = query.updateQuery("UPDATE nccIptvServers SET " +
                    "serverIP=" + serverData.serverIP + ", " +
                    "serverSecret='" + serverData.serverSecret + "', " +
                    "serverLocalAddress=" + serverData.serverLocalAddress + "," +
                    "serverComment='" + serverData.serverComment + "'," +
                    "serverName='" + serverData.serverName + "' " +
                    "WHERE id=" + serverData.id);

            return ids;
        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Integer> updateAdapter(AdapterData adapterData) {

        try {
            ArrayList<Integer> ids = query.updateQuery("UPDATE nccIptvAdapters SET " +
                    "adapterDevice=" + adapterData.adapterDevice + ", " +
                    "adapterType=" + adapterData.adapterType + ", " +
                    "serverId=" + adapterData.serverId + "," +
                    "adapterComment='" + adapterData.adapterComment + "' " +
                    "WHERE id=" + adapterData.id);

            return ids;
        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Integer> updateTransponder(TransponderData transponderData) {

        try {
            ArrayList<Integer> ids = query.updateQuery("UPDATE nccIptvTransponders SET " +
                    "transName='" + transponderData.transponderName + "', " +
                    "transFreq=" + transponderData.transponderFreq + ", " +
                    "transPolarity='" + transponderData.transponderPolarity + "', " +
                    "transFEC='" + transponderData.transponderFEC + "', " +
                    "transSymbolrate=" + transponderData.transponderSymbolrate + ", " +
                    "transType='" + transponderData.transponderType + "', " +
                    "adapterId=" + transponderData.adapterId + ", " +
                    "transLNB='" + transponderData.transponderLNB + "', " +
                    "transSat='" + transponderData.transponderSat + "' " +
                    "WHERE id=" + transponderData.id);

            return ids;
        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Integer> updateCam(CamData camData) {

        try {
            ArrayList<Integer> ids = query.updateQuery("UPDATE nccIptvCam SET " +
                    "camServer='" + camData.camServer + "', " +
                    "camPort=" + camData.camPort + ", " +
                    "camUser='" + camData.camUser + "', " +
                    "camPassword='" + camData.camPassword + "', " +
                    "camName='" + camData.camName + "', " +
                    "camKey='" + camData.camKey + "' " +
                    "WHERE id=" + camData.id);

            return ids;
        } catch (NccQueryException e) {
            e.printStackTrace();
        }

        return null;
    }
}
