package org.anisotrop.jperch.net.io;

import org.anisotrop.jperch.net.io.handler.ControlHandler;
import org.anisotrop.jperch.net.io.handler.DataHandler;
import org.anisotrop.jperch.net.io.providers.ExecutorServiceProvider;
import org.anisotrop.jperch.net.io.providers.IExecutorServiceProvider;
import org.anisotrop.jperch.net.io.results.Result;
import org.anisotrop.jperch.net.io.timer.MeasurementsTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

class ServerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerController.class);

    public void runTestLoop(boolean useNativeThreads, ServerSocket serverSocket) throws IOException, InterruptedException {
        while (true) {
            serverSocket.setSoTimeout(0); // block waiting for control connection
            ControlHandler controlHandler = startControlHandler(serverSocket);
            serverSocket.setSoTimeout(500); // will not wait for any new connection for more than 500ms
            boolean started = controlHandler.getParametersReceived().await(30, TimeUnit.SECONDS);
            if (started) {
                int parallelStreams = controlHandler.getParallel();
                ExecutorService executor = getExecutorService(useNativeThreads, parallelStreams);
                try {
                    Result testResult = new Result(parallelStreams);
                    controlHandler.setTestResult(testResult);
                    List<DataHandler> dataHandlers = startDataHandlers(serverSocket, parallelStreams, controlHandler, executor, testResult);
                    LOGGER.info("Launching test and protecting from other clients");
                    TestGuard testGuard = new TestGuard(serverSocket);
                    testGuard.start();
                    MeasurementsTimer timer = new MeasurementsTimer(testResult);
                    timer.start();
                    executor.invokeAll(dataHandlers.stream().map(DataHandler::getDataReader).collect(Collectors.toList()));
                    timer.stop();
                    testGuard.stop();
                    LOGGER.info("Workers done");
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } finally {
                    stopTestExecution(executor);
                }
            } else {
                LOGGER.error("Control flow non initialized.");
                throw new IllegalStateException();
            }
        }
    }

    private static List<DataHandler> startDataHandlers(ServerSocket serverSocket, int parallelStreams, ControlHandler controlHandler, ExecutorService executor, Result testResult) throws IOException, InterruptedException, ExecutionException {
        LOGGER.info("Waiting for clients");
        List<Future<Boolean>> dataFutures = new ArrayList<>(parallelStreams);
        List<DataHandler> dataHandlers = new ArrayList<>(parallelStreams);
        for (int i = 0; i < parallelStreams; i++) {
            Socket clientSocket = serverSocket.accept();
            DataHandler dataHandler = new DataHandler(clientSocket, controlHandler.getMustStop(), controlHandler.getDataHandlersStopped(), controlHandler.getTestCookie(), testResult.getStream(i));
            dataFutures.add(executor.submit(dataHandler.getCookieReader()));
            dataHandlers.add(dataHandler);
        }
        // Might have a different test sending cookie. Only considered this test is started after all streams received test cookie.
        LOGGER.info("Waiting for streams initialization");
        boolean streamsReady = false;
        while (!streamsReady) {
            streamsReady = true;
            for (int i = 0; i < parallelStreams; i++) {
                LOGGER.info("Waiting for stream worker #{} to initialize.", i);
                if (!dataFutures.get(i).get()) {
                    LOGGER.warn("Stream worker #{} failed to initialize. Rescheduling", i);
                    streamsReady = false;
                    Socket clientSocket = serverSocket.accept();
                    DataHandler dataHandler = new DataHandler(clientSocket, controlHandler.getMustStop(), controlHandler.getDataHandlersStopped(), controlHandler.getTestCookie(), testResult.getStream(i));
                    dataFutures.set(i, executor.submit(dataHandler.getCookieReader()));
                    dataHandlers.set(i, dataHandler);
                }
            }
        }
        return dataHandlers;
    }


    private void stopTestExecution(ExecutorService executor) throws InterruptedException {
        executor.shutdown();
        if (executor.awaitTermination(5, TimeUnit.SECONDS)) {
            LOGGER.info("Executor terminated.");
        } else {
            LOGGER.error("Executor not ready!");
            executor.shutdownNow();
            if (executor.awaitTermination(5, TimeUnit.SECONDS)) {
                LOGGER.info("Executor terminated.");
            } else {
                throw new RuntimeException("Executor not ready!");
            }
        }
    }

    private ControlHandler startControlHandler(ServerSocket serverSocket) throws IOException {
        Socket clientSocket = serverSocket.accept();
        ControlHandler controlHandler = new ControlHandler(clientSocket);
        Thread control = new Thread(controlHandler);
        control.start();
        // TODO control.setUncaughtExceptionHandler();
        return controlHandler;
    }

    private ExecutorService getExecutorService(boolean useVirtualThreads, int parallelStreams) {
        List<IExecutorServiceProvider> services = ExecutorServiceProvider.getProviders();
        if (services.isEmpty()) {
            throw new IllegalStateException("No provider found for " + IExecutorServiceProvider.class);
        }
        return services.get(0).getExecutorService(useVirtualThreads, parallelStreams);
    }
}
