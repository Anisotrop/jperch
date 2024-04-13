package org.anisotrop.jperch.lib21;

import org.anisotrop.jperch.net.io.providers.IThreadWaitProvider;

public class SpinWaitProvider implements IThreadWaitProvider {
    @Override
    public void wait(int milliseconds) {
        Thread.onSpinWait();
    }
}
