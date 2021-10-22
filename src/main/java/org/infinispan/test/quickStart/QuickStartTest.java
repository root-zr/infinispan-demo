package org.infinispan.test.quickStart;

import org.infinispan.Cache;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.VersioningScheme;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.context.Flag;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.transaction.LockingMode;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.transaction.lookup.GenericTransactionManagerLookup;
import org.infinispan.test.util.LoggingListener;


import javax.transaction.*;
import java.util.UUID;

public class QuickStartTest {
    public static void main(String[] args) throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {

        // Setup up a clustered cache manager
        GlobalConfigurationBuilder global = GlobalConfigurationBuilder.defaultClusteredBuilder();

        // Initialize the cache manager
        DefaultCacheManager cacheManager = new DefaultCacheManager(global.build());
        // Create a replicated synchronous configuration
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.transaction()
                .lockingMode(LockingMode.OPTIMISTIC)
                .autoCommit(true)
                .completedTxTimeout(60000)
                .transactionMode(TransactionMode.NON_TRANSACTIONAL)
                .useSynchronization(false)
                .notifications(true)
                .reaperWakeUpInterval(30000)
                .cacheStopTimeout(30000)
                .transactionManagerLookup(new GenericTransactionManagerLookup())
                .recovery()
                .enabled(false)
                .recoveryInfoCacheName("__recoveryInfoCacheName__");


        builder.clustering().cacheMode(CacheMode.REPL_SYNC);
        Configuration cacheConfig = builder.build();

        // Create a cache
        Cache<String, String> cache = cacheManager.administration()
                .withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
                .getOrCreateCache("cache", cacheConfig);

        //equivalent with calling TransactionManagerLookup.getTransactionManager();
        TransactionManager transactionManager = cache.getAdvancedCache().getTransactionManager();


        // Store the current node address in some random keys
        for(int i=0; i < 10; i++) {

            transactionManager.begin();
            cache.put(UUID.randomUUID().toString(), cacheManager.getNodeAddress());
            transactionManager.commit();

        }

        // Display the current cache contents for the whole cluster
        cache.entrySet().forEach(entry -> System.out.printf("%s = %s\n", entry.getKey(), entry.getValue()));
        // Display the current cache contents for this node
        cache.getAdvancedCache().withFlags(Flag.SKIP_REMOTE_LOOKUP)
                .entrySet().forEach(entry -> System.out.printf("%s = %s\n", entry.getKey(), entry.getValue()));
        // Stop the cache manager and release all resources
        cacheManager.stop();

    }

}
