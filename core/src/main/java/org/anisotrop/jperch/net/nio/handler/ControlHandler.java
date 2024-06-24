package org.anisotrop.jperch.net.nio.handler;

import org.anisotrop.jperch.net.nio.ControlStreamState;

import java.nio.channels.SocketChannel;

public class ControlHandler implements IHandler{

    private final SocketChannel client;
    public static final int COOKIE_SIZE = 37;
    public static final int IN_BUFFER_SIZE = 1024;
    public static final int PARAM_EXCHANGE = 0x09;
    public static final int CREATE_STREAMS = 0x0a;
    public static final int TEST_START = 0x01;
    public static final int TEST_RUNNING = 0x02;
    public static final int TEST_END = 0x04;
    public static final int EXCHANGE_RESULTS = 0x0d;
    public static final int DISPLAY_RESULTS = 0x0e;

    ControlStreamState controlStreamState = ControlStreamState.READING_COOKIE;

    public ControlHandler(SocketChannel client) {
        this.client = client;
    }
}
