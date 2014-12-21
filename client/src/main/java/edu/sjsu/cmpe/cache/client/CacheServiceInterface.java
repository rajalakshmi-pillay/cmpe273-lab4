package edu.sjsu.cmpe.cache.client;

import java.util.ArrayList;

/**
 * Cache Service Interface
 * 
 */
public interface CacheServiceInterface {
    public ArrayList<String> get(long key);

    public void put(long key, String value, boolean updateAll, String url);

    public void delete(long key);


}
