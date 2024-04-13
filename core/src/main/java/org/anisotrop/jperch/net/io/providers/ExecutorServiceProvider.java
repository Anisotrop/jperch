package org.anisotrop.jperch.net.io.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class ExecutorServiceProvider {
    public static List<IExecutorServiceProvider> getProviders() {
        List<IExecutorServiceProvider> services = new ArrayList<>();
        ServiceLoader<IExecutorServiceProvider> loader = ServiceLoader.load(IExecutorServiceProvider.class);
        loader.forEach(services::add);
        return services;
    }
}
