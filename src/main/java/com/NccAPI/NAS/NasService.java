package com.NccAPI.NAS;

import com.NccNAS.NccNasData;

public interface NasService {
    NccNasData getNAS(String apiKey, Integer id);
}
