package org.anisotrop.jperch.net.io.handler;

import org.anisotrop.jperch.net.io.providers.IThreadWaitProvider;
import org.anisotrop.jperch.net.io.providers.ThreadWaitProvider;
import org.anisotrop.jperch.net.io.results.Stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataHandler extends AbstractHandler {
    public static final int COOKIE_SIZE = 37;
    public static final int IN_BUFFER_SIZE = 4 * 1024 * 1024;
    public static final long NANOS_TO_SECONDS = 1_000_000_000;
    private final Socket client;
    private final AtomicBoolean mustStop;
    private final CountDownLatch dataHandlersStopped;

    private final CookieReader cookieReader = new CookieReader();

    private final DataReader dataReader = new DataReader();

    private final byte[] testCookie;
    private final Stream stream;

    private volatile long timeStartReadingCookie;

    static IThreadWaitProvider waitProvider;

    static {
        List<IThreadWaitProvider> providers = ThreadWaitProvider.getProviders();
        if (providers.isEmpty()) {
            throw new IllegalStateException("No provider found for " + IThreadWaitProvider.class);
        }
        waitProvider = providers.get(0);
    }


    public DataHandler(Socket client, AtomicBoolean mustStop, CountDownLatch dataHandlersStopped, byte[] testCookie, Stream stream) {
        this.client = client;
        this.mustStop = mustStop;
        this.dataHandlersStopped = dataHandlersStopped;
        this.testCookie = testCookie;
        this.stream = stream;
    }

    public CookieReader getCookieReader() {
        return cookieReader;
    }

    public DataReader getDataReader() {
        return dataReader;
    }

    public final class CookieReader implements Callable<Boolean> {
        @Override
        public Boolean call() {
            InputStream in = null;
            OutputStream out = null;
            try {
                DataHandler.this.timeStartReadingCookie = System.nanoTime();
                in = client.getInputStream();
                out = client.getOutputStream();
                logger.info("Started reading cookie");
                byte[] buff = new byte[IN_BUFFER_SIZE];
                byte[] cookie = new byte[COOKIE_SIZE];
                readCookie(in, buff, cookie);
                return validateCookie(cookie,testCookie, out, in);
            } catch (IOException e) {
                logger.error("I/O exception while reading cookie.", e);
                closeStreams(in, out);
                throw new RuntimeException(e);
            }
        }
    }


    public final class DataReader implements Callable<Boolean> {
        @Override
        public Boolean call() {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = client.getInputStream();
                out = client.getOutputStream();
                long startTime = System.nanoTime();
                DataHandler.this.stream.setStart_time((double) (startTime - DataHandler.this.timeStartReadingCookie) / NANOS_TO_SECONDS);

                logger.info("Started reading data at: {}s.", DataHandler.this.stream.getStart_time());
                byte[] buff = new byte[IN_BUFFER_SIZE];
                int n;
                long count = 0;
                while (!mustStop.get()) {
                    n = 0;
                    while (in.available() > 0) {
                        if ((n = in.read(buff, 0, IN_BUFFER_SIZE)) > -1) {
                            count += n;
                            DataHandler.this.stream.getCurrentBytes().addAndGet(n);
                        }
                        logger.debug("Read partial payload with {} bytes, total {} bytes", n, count);
                    }
                    if (n == 0 && waitProvider != null) {
                        waitProvider.wait(1);
                    }
                }
                long endTime = System.nanoTime();
                DataHandler.this.stream.setEnd_time((double) (endTime - startTime) / NANOS_TO_SECONDS);
                logger.info("Read payload with {} bytes at {}s.", count, DataHandler.this.stream.getEnd_time());
                dataHandlersStopped.countDown();
            } catch (Exception e) {
                logger.error("Exception while reading data.", e);
                closeStreams(in, out);
                throw new RuntimeException(e);
            }
            return true;
        }
    }

}

