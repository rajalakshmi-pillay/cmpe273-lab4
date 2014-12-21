package edu.sjsu.cmpe.cache.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * Distributed cache service
 * 
 */
public class DistributedCacheService implements CacheServiceInterface {
  //  private final String cacheServerUrl;
    public int putSuccessCount = 0;
    public int getSuccessCount = 0;
    public ArrayList<String> getResults;

    public DistributedCacheService() {
        this.putSuccessCount = 0;
        this.getSuccessCount = 0;
        getResults = new ArrayList<String>();
    }

    /**
     * @see edu.sjsu.cmpe.cache.client.CacheServiceInterface#get(long)
     */
    @Override
    public ArrayList<String> get(long key) {

        String[] values = new String[3];
        values[0] = asyncGet(key, "http://localhost:3000");
        values[1] = asyncGet(key, "http://localhost:3001");
        values[2] = asyncGet(key, "http://localhost:3002");


        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
       // System.out.println(" Values from Get "+getResults.get(0) + " "+ getResults.get(1) + " "+getResults.get(2));
        ArrayList<String> returnValues = getResults;
        getResults.clear();
        return returnValues;
    }

    /**
     * @see edu.sjsu.cmpe.cache.client.CacheServiceInterface# put(long,
     *      java.lang.String)
     */
    @Override
    public void put(long key, String value, boolean updateAll, String url) {



        if(updateAll){
            System.out.println("in PUT . key, value "+key + " "+value);
            asyncPut(key,value,"http://localhost:3000");
            asyncPut(key,value,"http://localhost:3001");
            asyncPut(key,value,"http://localhost:3002");
        }
        else{
            if(url != null)
                asyncPut(key,value,url);
        }


        //after this timer should start in client.java


    }

    @Override
    public void delete(long key) {

        asyncDelete(key,"http://localhost:3000");
        asyncDelete(key,"http://localhost:3001");
        asyncDelete(key,"http://localhost:3002");


    }


    private void asyncPut(long key, final String value, final String cacheUrl){
        Future<HttpResponse<JsonNode>> response = Unirest.put(cacheUrl + "/cache/{key}/{value}")
                .header("accept", "application/json")
                .routeParam("key", Long.toString(key))
                .routeParam("value", value)
                .asJsonAsync(new Callback<JsonNode>() {

                    public void failed(UnirestException e) {
                        System.out.println("The request has failed");
                        putSuccessCount--;
                    }

                    public void completed(HttpResponse<JsonNode> response) {

                        System.out.println("PUT COMPLETED in cache "+cacheUrl + " inserted "+value);
                        putSuccessCount++;
                        System.out.println("Put count "+putSuccessCount);

                    }

                    public void cancelled() {
                        System.out.println("The request has been cancelled");
                        putSuccessCount--;
                    }

                });
    }

    private String asyncGet(long key, final String cacheUrl){
        final String[] val = new String[3];
        Future<HttpResponse<JsonNode>> response = Unirest.get(cacheUrl + "/cache/{key}")
                .header("accept", "application/json")
                .routeParam("key", Long.toString(key))
                .asJsonAsync(new Callback<JsonNode>() {

                    public void failed(UnirestException e) {
                        System.out.println("Get request has failed");
                        getSuccessCount--;
                        val[0] = null;
                    }

                    public void completed(HttpResponse<JsonNode> response) {

                        getSuccessCount++;
                        val[0] = response.getBody().getObject().getString("value");
                        System.out.println("Get request success in "+cacheUrl + " value "+val[0]);
                        getResults.add(val[0]);
                    }

                    public void cancelled() {
                        System.out.println("The request has been cancelled");
                        getSuccessCount--;
                        val[0] = null;
                    }

                });

        return val[0];
    }

    private void asyncDelete(long key, String cacheUrl){

        Future<HttpResponse<JsonNode>> response = Unirest
                .delete(cacheUrl + "/cache/{key}")
                .routeParam("key", Long.toString(key))
                .asJsonAsync(new Callback<JsonNode>() {

                    public void failed(UnirestException e) {
                        System.out.println("The request has failed");
                        putSuccessCount--;
                    }

                    public void completed(HttpResponse<JsonNode> response) {

                        putSuccessCount++;

                    }

                    public void cancelled() {
                        System.out.println("The request has been cancelled");
                        putSuccessCount--;
                    }

                });
    }
}


