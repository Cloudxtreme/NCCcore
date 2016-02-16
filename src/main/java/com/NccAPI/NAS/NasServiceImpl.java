package com.NccAPI.NAS;

import com.NccNAS.NccNAS;
import com.NccNAS.NccNasData;
import com.NccNAS.NccNasException;

public class NasServiceImpl implements NasService {

    public NccNasData getNAS(Integer id){
        NccNasData nasData = null;
        try {
            nasData = new NccNAS().getNAS(id);
        } catch (NccNasException e) {

            return null;
        }

        return nasData;
    }
}
