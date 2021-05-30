/**
 * This file is part of HifumiBot, licensed under the MIT License (MIT)
 * 
 * Copyright (c) 2020 RedPanda4552 (https://github.com/RedPanda4552)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.redpanda4552.HifumiBot;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler
{

    private ScheduledExecutorService threadPool;
    private HashMap<String, Runnable> runnables;

    public Scheduler()
    {
        this.threadPool = Executors.newScheduledThreadPool(4);
        this.runnables = new HashMap<String, Runnable>();
    }

    public void runOnce(Runnable runnable)
    {
        this.threadPool.submit(runnable);
    }

    /**
     * Schedule a Runnable
     * 
     * @param runnable - The Runnable or lambda to schedule
     * @param period   - Period in milliseconds between runs
     */
    public void scheduleRepeating(String name, Runnable runnable, long period)
    {
        this.runnables.put(name, runnable);
        this.threadPool.scheduleAtFixedRate(runnable, period, period, TimeUnit.MILLISECONDS);
    }

    public boolean runScheduledNow(String name)
    {
        Runnable runnable = this.runnables.get(name);

        if (runnable != null)
        {
            this.threadPool.execute(runnable);
            return true;
        }

        return false;
    }

    /**
     * Shutdown the thread pool and all of its tasks
     */
    public void shutdown()
    {
        threadPool.shutdown();

        try
        {
            threadPool.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e)
        {
        }
    }

    public Set<String> getRunnableNames()
    {
        return this.runnables.keySet();
    }
}
