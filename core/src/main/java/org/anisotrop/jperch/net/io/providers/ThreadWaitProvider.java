package org.anisotrop.jperch.net.io.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class ThreadWaitProvider {
    public static List<IThreadWaitProvider> getProviders() {
        List<IThreadWaitProvider> services = new ArrayList<>();
        ServiceLoader<IThreadWaitProvider> loader = ServiceLoader.load(IThreadWaitProvider.class);
        loader.forEach(services::add);
        return services;
    }
}
