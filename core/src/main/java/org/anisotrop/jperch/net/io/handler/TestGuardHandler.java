package org.anisotrop.jperch.net.io.handler;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestGuardHandler extends AbstractHandler implements Runnable {

    private final AtomicBoolean testFinished = new AtomicBoolean(false);

    private final Socket client;

    public TestGuardHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        InputStream in = null;
        OutputStream out = null;
        try {
            logger.info("Started control handler");
            in = client.getInputStream();
            out = client.getOutputStream();
            logger.error("Replying test is already running.");
            out.write(0xFF);
            out.flush();
            closeStreams(in, out);
        } catch (Exception e) {
            logger.error("Exception while reading data.", e);
            closeStreams(in, out);
            throw new RuntimeException(e);
        }
    }
}
