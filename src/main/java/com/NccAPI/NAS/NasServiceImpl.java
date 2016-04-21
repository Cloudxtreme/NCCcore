package com.NccAPI.NAS;

import com.NccAPI.NccAPI;
import com.NccNAS.NccNAS;
import com.NccNAS.NccNasData;
import com.NccNAS.NccNasException;
import com.mysql.management.util.Str;

public class NasServiceImpl implements NasService {

    public NccNasData getNAS(String apiKey, Integer id){
        if (!new NccAPI().checkKey(apiKey)) return null;
        NccNasData nasData = null;
        try {
            nasData = new NccNAS().getNAS(id);
        } catch (NccNasException e) {

            return null;
        }

        return nasData;
    }
}
