package org.infinispan.test.Persistent;

import org.infinispan.Cache;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.context.Flag;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.transaction.LockingMode;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.transaction.lookup.GenericTransactionManagerLookup;

import javax.transaction.*;
import java.util.UUID;

public class PersistentTest {
    public static void main(String[] args) throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {

        // Setup up a clustered cache manager
        GlobalConfigurationBuilder global = GlobalConfigurationBuilder.defaultClusteredBuilder();

        // Initialize the cache manager
        DefaultCacheManager cacheManager = new DefaultCacheManager(global.build());
        // Create a replicated synchronous configuration
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.persistence()
                .addSingleFileStore()
                .location("src/main/resources/file")
                .maxEntries(5000);


        builder.clustering().cacheMode(CacheMode.REPL_SYNC);
        Configuration cacheConfig = builder.build();

        // Create a cache
        Cache<String, String> cache = cacheManager.administration()
                .withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
                .getOrCreateCache("cache", cacheConfig);


        // Store the current node address in some random keys
        for(int i=0; i < 10; i++) {
            cache.put( "key" + i , "value" + i );

        }

        // Stop the cache manager and release all resources
        cacheManager.stop();

    }
}
