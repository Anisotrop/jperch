package org.anisotrop.jperch.net.io.handler;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ControlHandler extends AbstractHandler implements Runnable {

    private static final Gson gson = new Gson();

    public static final int COOKIE_SIZE = 37;
    public static final int IN_BUFFER_SIZE = 1024;
    public static final int PARAM_EXCHANGE = 0x09;
    public static final int CREATE_STREAMS = 0x0a;
    public static final int TEST_START = 0x01;
    public static final int TEST_RUNNING = 0x02;
    public static final int TEST_END = 0x04;
    public static final int EXCHANGE_RESULTS = 0x0d;
    public static final int DISPLAY_RESULTS = 0x0e;
    private final Socket client;

    private int parallel;

    private byte[] testCookie;

    private final CountDownLatch parametersReceived = new CountDownLatch(1);

    private CountDownLatch dataHandlersStopped;

    private final AtomicBoolean mustStop = new AtomicBoolean(false);

    private static final Pattern parallelValue = Pattern.compile("\"parallel\":([0-9]+),");

    public ControlHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        DataInputStream in = null;
        OutputStream out = null;
        try {
            logger.info("Started control handler");
            in = new DataInputStream(client.getInputStream());
            out = client.getOutputStream();
            byte[] buff = new byte[IN_BUFFER_SIZE];
            byte[] cookie = new byte[COOKIE_SIZE];
            readCookie(in, buff, cookie);
            this.testCookie = cookie;
            out.write(PARAM_EXCHANGE);
            out.flush();

            int lenParams = in.readInt();
            logger.info("Params len: {}", lenParams);
            byte[] bParams = readNBytes(in, lenParams);
            String params = new String(bParams);
            logger.info("Params: {}", params);
            Matcher m = parallelValue.matcher(params);
            if (m.find()) {
                System.out.println(m.group(1));
                parallel = Integer.parseInt(m.group(1));
                dataHandlersStopped = new CountDownLatch(parallel);
            }
            parametersReceived.countDown();

            out.write(CREATE_STREAMS);
            out.write(TEST_START);
            out.write(TEST_RUNNING);
            out.flush();

            byte ctrl = in.readByte();
            if (ctrl == TEST_END) {
                mustStop.set(true);
                boolean stopped = dataHandlersStopped.await(30, TimeUnit.SECONDS);
                if (!stopped) {
                    throw new RuntimeException("Not all data handlers stopped from reading. Exiting!");
                }
                out.write(EXCHANGE_RESULTS);
                out.flush();
            }

            int lenResults = in.readInt();
            logger.info("Results len: {}", lenResults);
            byte[] bResults = readNBytes(in, lenResults);
            String results = new String(bResults);
            logger.info("Results: {}", results);

            //gson.toJson(new Result());

            byte[] lenResultsOut = ByteBuffer.allocate(4).putInt(lenResults).array();
            out.write(lenResultsOut);
            out.write(bResults);
            out.flush();

            logger.info("Test is finished");
            out.write(DISPLAY_RESULTS);
            out.flush();
        } catch (Exception e) {
            logger.error("Exception while reading data.", e);
            closeStreams(in, out);
            mustStop.set(true);
        }
    }

    public int getParallel() {
        return parallel;
    }

    public CountDownLatch getParametersReceived() {
        return parametersReceived;
    }

    public AtomicBoolean getMustStop() {
        return mustStop;
    }

    public CountDownLatch getDataHandlersStopped() {
        return dataHandlersStopped;
    }

    public byte[] getTestCookie() {
        return testCookie;
    }
}

