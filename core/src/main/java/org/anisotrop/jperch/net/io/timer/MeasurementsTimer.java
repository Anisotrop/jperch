package org.anisotrop.jperch.net.io.timer;

import org.anisotrop.jperch.net.io.results.Result;
import org.anisotrop.jperch.net.io.results.Stream;

import java.util.concurrent.*;

public class MeasurementsTimer {

    private final Result testResult;

    private final ScheduledExecutorService timerExecutor = Executors.newScheduledThreadPool(1, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread t = new Thread(runnable);
            t.setName("ScheduledMeasurementsTimer");
            return t;
        }
    });

    private int currentSecond = 0;

    public MeasurementsTimer(Result testResult) {
        this.testResult = testResult;
    }

    public void start() {
        timerExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // TODO cancel if test is finished
                System.out.println("Measuring ..." + currentSecond + "-" + ++currentSecond);
                Stream[] testStreams = testResult.getStreams();
                for (Stream s: testStreams) {
                    long bytes = s.getCurrentBytes().getAndSet(0);
                    System.out.print("[" + String.format("%03d", s.getId()) + "]");
                    System.out.print("   ");
                    System.out.print((bytes / 1024) + "KBytes ");
                    System.out.println((bytes * 8) / (1024 * 1024) + "Mbits/sec");
                }
            }
        }, 1,1, TimeUnit.SECONDS);
    }

    public void stop() {
//        timerExecutor.shutdown();
        timerExecutor.shutdownNow();
        try {
            boolean done = timerExecutor.awaitTermination(30, TimeUnit.SECONDS);
            if (!done) {
                throw new RuntimeException("Executor not finished");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
