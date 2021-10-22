package org.infinispan.test.config;


import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.lock.EmbeddedClusteredLockManagerFactory;
import org.infinispan.lock.api.ClusteredLockManager;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.test.clusteredLock.ClusteredLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@EnableCaching
@EnableAutoConfiguration
public class InfinispanConfig {

    @Autowired
    DefaultCacheManager cacheManager;

    @Autowired
    private org.infinispan.configuration.cache.Configuration lockCache;

    @Bean(name = "lockCache")
    public org.infinispan.configuration.cache.Configuration getLockCache(){
        return new ConfigurationBuilder()
                .locking()
                .lockAcquisitionTimeout(3, TimeUnit.SECONDS)
                .build();
    }

    @Bean
    public DefaultCacheManager embeddedCacheManage(){
        // Setup up a clustered cache manager
        GlobalConfigurationBuilder global = GlobalConfigurationBuilder.defaultClusteredBuilder();
        global.transport()
                .defaultTransport()
                .clusterName("myCluster")
                .addProperty("configurationFile","default-jgroups-tcp.xml");

        DefaultCacheManager cacheManager = new DefaultCacheManager(global.build());

        return cacheManager;
    }

    @Bean
    public Cache<String,Object> getCache(){

        // Create a replicated synchronous configuration
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.persistence()
                .addSingleFileStore()
                .location("src/main/resources/file")
                .maxEntries(5000);


        builder.clustering().cacheMode(CacheMode.REPL_SYNC);
        org.infinispan.configuration.cache.Configuration cacheConfig = builder.build();

        // Create a cache
        return  cacheManager.administration()
                .withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
                .getOrCreateCache("cache", cacheConfig);

    }

    @Bean
    public ClusteredLock getLock(){

        cacheManager.defineConfiguration("newCache",lockCache);
        // Initialize the clustered lock manager from the cache manager
        ClusteredLockManager clm1 = EmbeddedClusteredLockManagerFactory.from(cacheManager);

        // Define a lock. By default this lock is non reentrant
        clm1.defineLock("lock");

        // Get a lock interface from each node
        return (ClusteredLock) clm1.get("lock");
    }

}
