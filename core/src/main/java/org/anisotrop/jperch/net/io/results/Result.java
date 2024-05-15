package org.anisotrop.jperch.net.io.results;

/**
 * {
 *   "cpu_util_total": 70.8398537643007,
 *   "cpu_util_user": 1.6839845516597816,
 *   "cpu_util_system": 69.1558692126409,
 *   "sender_has_retransmits": 1,
 *   "congestion_used": "cubic",
 *   "streams": [
 *     {
 *       "id": 1,
 *       "bytes": 419430400,
 *       "retransmits": 0,
 *       "jitter": 0,
 *       "errors": 0,
 *       "packets": 0,
 *       "start_time": 0,
 *       "end_time": 5.005081
 *     },
 *     {
 *       "id": 3,
 *       "bytes": 420741120,
 *       "retransmits": 0,
 *       "jitter": 0,
 *       "errors": 0,
 *       "packets": 0,
 *       "start_time": 0,
 *       "end_time": 5.005101
 *     },
 *     {
 *       "id": 4,
 *       "bytes": 420741120,
 *       "retransmits": 0,
 *       "jitter": 0,
 *       "errors": 0,
 *       "packets": 0,
 *       "start_time": 0,
 *       "end_time": 5.005106
 *     },
 *     {
 *       "id": 5,
 *       "bytes": 420741120,
 *       "retransmits": 0,
 *       "jitter": 0,
 *       "errors": 0,
 *       "packets": 0,
 *       "start_time": 0,
 *       "end_time": 5.00511
 *     }
 *   ]
 * }
 */
public class Result {
    private double cpu_util_total;
    private double cpu_util_user;
    private double cpu_util_system;
    private int sender_has_retransmits;
    private String congestion_used =  "cubic";

    Stream[] streams;

    public Result(int streamCount) {
        streams = new Stream[streamCount];
        for (int i = 0; i< streamCount; i++) {
            streams[i] = new Stream();
            streams[i].setId(i);
        }
    }

    public Stream getStream(int i) {
        return streams[i];
    }

    public double getCpu_util_total() {
        return cpu_util_total;
    }

    public void setCpu_util_total(double cpu_util_total) {
        this.cpu_util_total = cpu_util_total;
    }

    public double getCpu_util_user() {
        return cpu_util_user;
    }

    public void setCpu_util_user(double cpu_util_user) {
        this.cpu_util_user = cpu_util_user;
    }

    public double getCpu_util_system() {
        return cpu_util_system;
    }

    public void setCpu_util_system(double cpu_util_system) {
        this.cpu_util_system = cpu_util_system;
    }

    public int getSender_has_retransmits() {
        return sender_has_retransmits;
    }

    public void setSender_has_retransmits(int sender_has_retransmits) {
        this.sender_has_retransmits = sender_has_retransmits;
    }

    public String getCongestion_used() {
        return congestion_used;
    }

    public void setCongestion_used(String congestion_used) {
        this.congestion_used = congestion_used;
    }

    public Stream[] getStreams() {
        return streams;
    }

}
