package org.infinispan.clusteredLock;

import org.infinispan.commons.util.Experimental;
import org.infinispan.lock.api.ClusteredLock;
import org.infinispan.lock.configuration.ClusteredLockConfiguration;

import java.util.concurrent.CompletableFuture;

@Experimental
public interface ClusteredLockManager {

    //定义一个具有指定名称和默认 ClusteredLockConfiguration 的锁。它不会覆盖现有的配置。
    boolean defineLock(String name);

    //用指定的名称和 ClusteredLockConfiguration 定义锁。它不会覆盖现有的配置。
    boolean defineLock(String name, ClusteredLockConfiguration configuration);

    //通过名称获取 ClusteredLock。必须在集群中至少调用一次 defineLock。
    ClusteredLock get(String name);

    ClusteredLockConfiguration getConfiguration(String name);

    boolean isDefined(String name);

    CompletableFuture<Boolean> remove(String name);

    //释放或解锁 ClusteredLock
    CompletableFuture<Boolean> forceRelease(String name);
}
