package org.anisotrop.jperch.lib8;

import org.anisotrop.jperch.net.io.providers.IThreadWaitProvider;
public class SleepWaitProvider implements IThreadWaitProvider {
    @Override
    public void wait(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
