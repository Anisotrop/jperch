package org.anisotrop.jperch.net.io.providers;

import java.util.concurrent.ExecutorService;

public interface IThreadWaitProvider {
    /**
     * Different implementation of thread waiting.
     * @param milliseconds How much time to wait, not honored in all implementations.
     */
    void wait(int milliseconds);
}
