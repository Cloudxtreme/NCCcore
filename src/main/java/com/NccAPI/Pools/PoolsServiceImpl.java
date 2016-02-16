package com.NccAPI.Pools;

import com.NccPools.PoolData;
import com.NccPools.NccPools;

import java.util.ArrayList;

public class PoolsServiceImpl implements PoolsService {
    public ArrayList<PoolData> getPools(){
        return new NccPools().getPools();
    }
}
