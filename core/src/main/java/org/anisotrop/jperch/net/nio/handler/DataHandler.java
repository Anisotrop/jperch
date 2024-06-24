package org.anisotrop.jperch.net.nio.handler;

import org.anisotrop.jperch.net.nio.StreamState;

import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataHandler implements IHandler {
    private final SocketChannel client;
    private final AtomicBoolean mustStop;
    private StreamState streamState = StreamState.READING_COOKIE;

    public DataHandler(SocketChannel client, AtomicBoolean mustStop) {
        this.client = client;
        this.mustStop = mustStop;
    }
}
