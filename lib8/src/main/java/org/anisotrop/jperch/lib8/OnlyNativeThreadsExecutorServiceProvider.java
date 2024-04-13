package org.anisotrop.jperch.lib8;

import org.anisotrop.jperch.net.io.providers.IExecutorServiceProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OnlyNativeThreadsExecutorServiceProvider implements IExecutorServiceProvider {
    @Override
    public ExecutorService getExecutorService(boolean useVirtualThreads, int parallelStreams) {
        return Executors.newFixedThreadPool(parallelStreams);
    }
}
