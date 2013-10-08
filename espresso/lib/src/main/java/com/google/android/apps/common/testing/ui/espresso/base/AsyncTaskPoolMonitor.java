package com.google.android.apps.common.testing.ui.espresso.base;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides a way to monitor AsyncTask's work queue to ensure that there is no work pending
 * or executing (and to allow notification of idleness).
 *
 * This class is based on the assumption that we can get at the ThreadPoolExecutor AsyncTask uses.
 * That is currently possible and easy in Froyo to JB. If it ever becomes impossible, as long as we
 * know the max # of executor threads the AsyncTask framework allows we can still use this
 * interface, just need a different implementation.
 */
class AsyncTaskPoolMonitor {
  private final AtomicReference<Runnable> onIdle = new AtomicReference<Runnable>(null);
  private final ThreadPoolExecutor pool;
  private final AtomicInteger activeBarrierChecks = new AtomicInteger(0);

  AsyncTaskPoolMonitor(ThreadPoolExecutor pool) {
    this.pool = checkNotNull(pool);
  }

  /**
   * Checks if the pool is idle at this moment.
   *
   * @return true if the pool is idle, false otherwise.
   */
  boolean isIdleNow() {
    if (!pool.getQueue().isEmpty()) {
      return false;
    } else {
      int activeCount = pool.getActiveCount();
      if (0 != activeCount) {
        if (onIdle.get() == null) {
          // if there's no idle runnable scheduled and there are still barrier
          // checks running, they are about to exit, ignore them.
          activeCount = activeCount - activeBarrierChecks.get();
        }
      }
      return 0 == activeCount;
    }
  }

  /**
   * Notifies caller once the pool is idle.
   *
   * We check for idle-ness by submitting the max # of tasks the pool will take and blocking
   * the tasks until they are all executing. Then we know there are no other tasks _currently_
   * executing in the pool, we look back at the work queue to see if its backed up, if it is
   * we reenqueue ourselves and try again.
   *
   * Obviously this strategy will fail horribly if 2 parties are doing it at the same time,
   * we prevent recursion here the best we can.
   *
   * @param idleCallback called once the pool is idle.
   */
  void notifyWhenIdle(final Runnable idleCallback) {
    checkNotNull(idleCallback);
    checkState(onIdle.compareAndSet(null, idleCallback), "cannot monitor for idle recursively!");
    monitorForIdle();
  }

  private void monitorForIdle() {
    if (isIdleNow()) {
      onIdle.getAndSet(null).run();
    } else {
      // Submit N tasks that will block until they are all running on the thread pool.
      // at this point we can check the pool's queue and verify that there are no new
      // tasks behind us and deem the queue idle.

      int poolSize = pool.getCorePoolSize();
      final CyclicBarrier idleBarrier = new CyclicBarrier(poolSize,
          new Runnable() {
            @Override
            public void run() {
              if (pool.getQueue().isEmpty()) {
                // no one is behind us, so the queue is idle!
                onIdle.getAndSet(null).run();
              } else {
                // work is waiting behind us, enqueue another block of tasks and
                // hopefully when they're all running, the queue will be empty.
                monitorForIdle();
              }
            }
          });
      final AtomicInteger barrierGeneration = new AtomicInteger(0);
      final BarrierRestarter restarter = new BarrierRestarter(idleBarrier, barrierGeneration);

      for (int i = 0; i < poolSize; i++) {
        pool.execute(new Runnable() {
          @Override
          public void run() {
            while (true) {
              activeBarrierChecks.incrementAndGet();
              int myGeneration = barrierGeneration.get();
              try {
                idleBarrier.await();
                return;
              } catch (InterruptedException ie) {
                // sorry - I cant let you interrupt me!
                restarter.restart(myGeneration);
              } catch (BrokenBarrierException bbe) {
                restarter.restart(myGeneration);
              } finally {
                activeBarrierChecks.decrementAndGet();
              }
            }
          }
        });
      }
    }
  }

  private static class BarrierRestarter {
    private final CyclicBarrier barrier;
    private final AtomicInteger barrierGeneration;
    BarrierRestarter(CyclicBarrier barrier, AtomicInteger barrierGeneration) {
      this.barrier = barrier;
      this.barrierGeneration = barrierGeneration;
    }

    /**
     * restarts the barrier.
     *
     * After the calling this function it is guaranteed that barrier generation has been incremented
     * and the barrier can be awaited on again.
     *
     * @param fromGeneration the generation that encountered the breaking exception.
     */
    synchronized void restart(int fromGeneration) {
      // must be synchronized. T1 could pass the if check, be suspended before calling reset, T2
      // sails thru - and awaits on the barrier again before T1 has awoken and reset it.
      int nextGen = fromGeneration + 1;
      if (barrierGeneration.compareAndSet(fromGeneration, nextGen)) {
        // first time we've seen fromGeneration request a reset. lets reset the barrier.
        barrier.reset();
      } else {
        // some other thread has already reset the barrier - this request is a no op.
      }
    }
  }
}
