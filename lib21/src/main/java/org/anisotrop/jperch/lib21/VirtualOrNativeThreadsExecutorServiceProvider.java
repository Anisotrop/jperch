package org.anisotrop.jperch.lib21;

import org.anisotrop.jperch.net.io.providers.IExecutorServiceProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VirtualOrNativeThreadsExecutorServiceProvider implements IExecutorServiceProvider {
    @Override
    public ExecutorService getExecutorService(boolean useVirtualThreads, int parallelStreams) {
        if (useVirtualThreads) {
            return Executors.newVirtualThreadPerTaskExecutor();
        }
        return Executors.newFixedThreadPool(parallelStreams);
    }
}
