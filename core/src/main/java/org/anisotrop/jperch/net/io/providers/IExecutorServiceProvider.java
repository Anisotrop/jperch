package org.anisotrop.jperch.net.io.providers;

import java.util.concurrent.ExecutorService;

public interface IExecutorServiceProvider {
    ExecutorService getExecutorService(boolean useVirtualThreads, int parallelStreams);
}
