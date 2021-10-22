package org.infinispan.test.clusteredLock;

import org.infinispan.commons.util.Experimental;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Experimental
public interface ClusteredLock { //是实现集群锁的接口。

    //获取锁。如果锁不可用，则调用阻塞，直到获得锁为止。没有为锁请求失败指定最长时间，因此这可能导致线程饥饿。
    CompletableFuture<Void> lock();

    //只有在调用时锁是空的时候才获取锁，在这种情况下返回 true。此方法不阻塞(或等待)任何锁获取。
    CompletableFuture<Boolean> tryLock();

    //如果锁可用，此方法将立即返回 true。如果锁不可用，那么调用将等待直到设置的时间过去
    CompletableFuture<Boolean> tryLock(long time, TimeUnit unit);

    //释放锁。只有锁的持有者可以释放锁。
    CompletableFuture<Void> unlock();

    CompletableFuture<Boolean> isLocked();

    CompletableFuture<Boolean> isLockedByMe();
}
