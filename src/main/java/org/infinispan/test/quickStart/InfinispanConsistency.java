package org.infinispan.test.quickStart;

import org.infinispan.Cache;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.lock.EmbeddedClusteredLockManagerFactory;
import org.infinispan.lock.api.ClusteredLock;
import org.infinispan.lock.api.ClusteredLockManager;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.transaction.TransactionMode;

import javax.transaction.TransactionManager;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class InfinispanConsistency {

   public static void main(String[] args) throws Exception {
      // Construct a local cache manager
      DefaultCacheManager cacheManager = new DefaultCacheManager();
      // Create a transaction cache config
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.locking().lockAcquisitionTimeout(10, TimeUnit.SECONDS);
      Configuration cacheConfig = builder.build();

      // Create a cache with the config
      Cache<String, String> cache = cacheManager.administration()
              .withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
              .getOrCreateCache("cache", cacheConfig);

      // Initialize the clustered lock manager from the cache manager
      ClusteredLockManager clm1 = EmbeddedClusteredLockManagerFactory.from(cacheManager);

      // Define a lock. By default this lock is non reentrant
      clm1.defineLock("lock");

      // Get a lock interface from each node
      ClusteredLock lock = clm1.get("lock");

      AtomicInteger counter = new AtomicInteger(0);

      // Acquire and release the lock 3 times
      CompletableFuture<Boolean> call1 = lock.tryLock(1, TimeUnit.SECONDS).whenComplete((r, ex) -> {
         if (r) {
            System.out.println("lock is acquired by the call 1");
            lock.unlock().whenComplete((nil, ex2) -> {
               System.out.println("lock is released by the call 1");
               counter.incrementAndGet();
            });
         }
      });

      CompletableFuture<Boolean> call2 = lock.tryLock(1, TimeUnit.SECONDS).whenComplete((r, ex) -> {
         if (r) {
            System.out.println("lock is acquired by the call 2");
            lock.unlock().whenComplete((nil, ex2) -> {
               System.out.println("lock is released by the call 2");
               counter.incrementAndGet();
            });
         }
      });

      CompletableFuture<Boolean> call3 = lock.tryLock(1, TimeUnit.SECONDS).whenComplete((r, ex) -> {
         if (r) {
            System.out.println("lock is acquired by the call 3");
            lock.unlock().whenComplete((nil, ex2) -> {
               System.out.println("lock is released by the call 3");
               counter.incrementAndGet();
            });
         }
      });

      CompletableFuture.allOf(call1, call2, call3).whenComplete((r, ex) -> {
         // Print the value of the counter
         System.out.println("Value of the counter is " + counter.get());

         // Stop the cache manager
         cacheManager.stop();
      });

   }

}
