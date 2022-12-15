// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Scheduler {

  private ScheduledExecutorService threadPool;
  private HashMap<String, Runnable> runnables = new HashMap<String, Runnable>();
  private HashMap<String, ScheduledFuture<?>> statuses = new HashMap<String, ScheduledFuture<?>>();

  public Scheduler() {
    this.threadPool = Executors.newScheduledThreadPool(4);
  }

  public void runOnce(Runnable runnable) {
    this.threadPool.submit(runnable);
  }

  /**
   * Schedule a Runnable
   *
   * @param runnable - The Runnable or lambda to schedule
   * @param period - Period in milliseconds between runs
   */
  public void scheduleRepeating(String name, Runnable runnable, long period) {
    this.runnables.put(name, runnable);
    this.statuses.put(
        name, this.threadPool.scheduleAtFixedRate(runnable, period, period, TimeUnit.MILLISECONDS));
  }

  public boolean runScheduledNow(String name) {
    Runnable runnable = this.runnables.get(name);

    if (runnable != null) {
      this.threadPool.execute(runnable);
      return true;
    }

    return false;
  }

  /** Shutdown the thread pool and all of its tasks */
  public void shutdown() {
    threadPool.shutdown();

    try {
      threadPool.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
    }
  }

  public Set<String> getRunnableNames() {
    return this.runnables.keySet();
  }

  public boolean isRunnableAlive(String name) throws NoSuchRunnableException {
    ScheduledFuture<?> future = statuses.get(name);

    if (future == null)
      throw new NoSuchRunnableException(
          "No runnable with name '" + name + "' has been scheduled yet");

    return !statuses.get(name).isDone();
  }

  public class NoSuchRunnableException extends Exception {
    private static final long serialVersionUID = -6509265497680687398L;

    public NoSuchRunnableException(String message) {
      super(message);
    }
  }
}
