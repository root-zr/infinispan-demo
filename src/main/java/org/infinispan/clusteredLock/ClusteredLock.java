package org.infinispan.clusteredLock;

import org.infinispan.commons.util.Experimental;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Experimental
public interface ClusteredLock { //是实现集群锁的接口。

    // Acquires the lock. If the lock is not available then call blocks until the lock is acquired. Currently,
    // there is no maximum time specified for a lock request to fail, so this could cause thread starvation.
    CompletableFuture<Void> lock();

    //只有在调用时锁是空的时候才获取锁，在这种情况下返回 true。此方法不阻塞(或等待)任何锁获取。
    CompletableFuture<Boolean> tryLock();

    CompletableFuture<Boolean> tryLock(long time, TimeUnit unit);

    CompletableFuture<Void> unlock();

    CompletableFuture<Boolean> isLocked();

    CompletableFuture<Boolean> isLockedByMe();
}
