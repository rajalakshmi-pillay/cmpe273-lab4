package edu.sjsu.cmpe.cache.client;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.hash.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Client {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Cache Client...");

        //write to distributed cache
        System.out.println("Step 1: writing value a, key 1 to all 3 cache");
        DistributedCacheService cache1 = new DistributedCacheService();
        cache1.put(1, "a", true, null);
        TimeUnit.SECONDS.sleep(30);

        System.out.println("Step 2: After sleeping for 30s");
        //check if put was success in all 3 cache

        System.out.println("Step 3: Checking if PUT success in atleast 2 cache");
        if(cache1.putSuccessCount <2){
            System.out.println("PUT failed. Deleting values from all caches");

            cache1.delete(1);
        }
        else{
            //reset
            cache1.putSuccessCount = 0;
        }


        //read and repair. First bring down server A

        //write value b
        System.out.println("Step 4: writing value b, key 1");
        cache1.put(1, "b", true, null);
        TimeUnit.SECONDS.sleep(30);

        System.out.println("Step 5: after timer for put value b, key 1");
        //bring server A up and read
        ArrayList<String> values = cache1.get(1);
        TimeUnit.SECONDS.sleep(30);

        System.out.println("Step 6: read values "+values.get(0) + " "+values.get(1) + " "+values.get(2));

        String majorityVal = null;
        String repairUrl = null;
        boolean isConsistent = true;

        if(values.get(0).equals(values.get(1))){
            majorityVal = values.get(0);
            if(!(values.get(0).equals(values.get(2)))){
                isConsistent = false;
                repairUrl = "http://localhost:3002";
            }


        }
        else if(values.get(0).equals(values.get(2))){
            majorityVal = values.get(0);
            if(!(values.get(0).equals(values.get(1)))){
                repairUrl = "http://localhost:3001";
                isConsistent = false;
            }

        }
        else if(values.get(1).equals(values.get(2))){
            majorityVal = values.get(1);
            if(!(values.get(1).equals(values.get(0)))){
                repairUrl = "http://localhost:3000";
                isConsistent = false;
            }

        }

        //repair if value is not consistent across 3 cache
        if(!isConsistent)
            cache1.put(1, majorityVal, false, repairUrl);

    }

}
