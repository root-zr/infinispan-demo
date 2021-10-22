package org.infinispan.test.controller;

import org.infinispan.test.clusteredLock.ClusteredLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.security.util.Cache;

import java.util.Random;

@RestController
public class InfinispanController {
    @Autowired
    Cache<String,Object> cache;

    @Autowired
    ClusteredLock lock;

    @GetMapping("/addToCache")
    public  Cache<String,Object> addToCache(){
        Random random = new Random();
        int num = random.nextInt(10);

        cache.put("key" + num, "value" + num);

        return cache;
    }

    @GetMapping("/readFromCache")
    public  Object readFromCache(){
        Random random = new Random();
        int num = random.nextInt(10);

       Object res = cache.getOrDefault("key" + num, "not found" + num);

       return res;
    }
}
