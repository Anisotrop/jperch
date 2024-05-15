package org.anisotrop.jperch.net.io;

import org.anisotrop.jperch.net.io.handler.AbstractHandler;
import org.anisotrop.jperch.net.io.handler.TestGuardHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestGuard extends AbstractHandler {

    private final ServerSocket serverSocket;

    private TestGuardHandler testGuardHandler;

    private final AtomicBoolean testFinished = new AtomicBoolean(false);


    public TestGuard(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void start() {
        Thread t = new Thread(() -> {
            while (!testFinished.get()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    testGuardHandler = new TestGuardHandler(clientSocket);
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        logger.info("Started test guard handler");
                        in = clientSocket.getInputStream();
                        out = clientSocket.getOutputStream();
                        logger.error("Replying test is already running.");
                        out.write(0xFF);
                        out.flush();
                        closeStreams(in, out);
                    } catch (Exception e) {
                        logger.error("Exception while reading data.", e);
                        closeStreams(in, out);
                        throw new RuntimeException(e);
                    }
                } catch (SocketTimeoutException ste) {
                    logger.info("No connection received, re-opening socket.");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            logger.info("Test guard stopped.");
        }, "TestGuardThread");
        t.start();
    }

    public void stop() {
        logger.info("Test finished, stopping guard.");
        testFinished.set(true);
    }

}
