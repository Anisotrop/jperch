package org.anisotrop.jperch.net.io.results;

import java.util.concurrent.atomic.AtomicLong;

/**
 * {
 * "id": 5,
 * "bytes": 420741120,
 * "retransmits": 0,
 * "jitter": 0,
 * "errors": 0,
 * "packets": 0,
 * "start_time": 0,
 * "end_time": 5.00511
 * }
 */
public class Stream {
    private long id;
    private long bytes;
    private long retransmits;
    private long jitter;
    private long errors;
    private long packets;
    private double start_time;
    private double end_time;
    private transient AtomicLong currentBytes = new AtomicLong();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    public AtomicLong getCurrentBytes() {
        return currentBytes;
    }

    public long getRetransmits() {
        return retransmits;
    }

    public void setRetransmits(long retransmits) {
        this.retransmits = retransmits;
    }

    public long getJitter() {
        return jitter;
    }

    public void setJitter(long jitter) {
        this.jitter = jitter;
    }

    public long getErrors() {
        return errors;
    }

    public void setErrors(long errors) {
        this.errors = errors;
    }

    public long getPackets() {
        return packets;
    }

    public void setPackets(long packets) {
        this.packets = packets;
    }

    public double getStart_time() {
        return start_time;
    }

    public void setStart_time(double start_time) {
        this.start_time = start_time;
    }

    public double getEnd_time() {
        return end_time;
    }

    public void setEnd_time(double end_time) {
        this.end_time = end_time;
    }
}
