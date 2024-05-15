package org.anisotrop.jperch.net.io.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class AbstractHandler {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected void readCookie(InputStream in, byte[] buff, byte[] cookie) throws IOException {
        int n;
        int count = 0;
        while ((n = in.read(buff, 0, DataHandler.COOKIE_SIZE - count)) > -1) {
            count += n;
            if (count < DataHandler.COOKIE_SIZE) {
                System.arraycopy(buff, 0, cookie, count - n, n);
            } else if (count == DataHandler.COOKIE_SIZE) {
                System.arraycopy(buff, 0, cookie, count - n, n);
                logger.info("Received cookie: {}", new String(cookie));
                break;
            } else {
                logger.warn("Should not arrive here");
            }
        }
    }


    boolean validateCookie(byte[] cookie, byte[] testCookie, OutputStream out, InputStream in) throws IOException {
        if (!Arrays.equals(cookie, testCookie)) {
            logger.error("Wrong cookie, replying test is already running.");
            out.write(0xFF);
            out.flush();
            closeStreams(in, out);
            return false;
        }
        return true;
    }

    protected byte[] readNBytes(DataInputStream in, int len) throws IOException {
        byte[] result = new byte[len];
        byte[] buff = new byte[len];
        int n ;
        int count = 0;
        while ((n = in.read(buff, 0, len - count)) > -1) {
            count += n;
            logger.debug("Read: {} bytes, total: {}", n, count);
            if (count <= len) {
                System.arraycopy(buff, 0, result, count - n, n);
                if (count == len) {
                    break;
                }
            } else {
                logger.error("Should not arrive here");
            }
        }
        return result;
    }

    protected void closeStreams(InputStream in, OutputStream out) {
        RuntimeException closeException = new RuntimeException();
        boolean closed = true;
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                closed = false;
                closeException.addSuppressed(e);
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                closed = false;
                closeException.addSuppressed(e);
            }
        }
        if (!closed) {
            throw closeException;
        }
    }
}
