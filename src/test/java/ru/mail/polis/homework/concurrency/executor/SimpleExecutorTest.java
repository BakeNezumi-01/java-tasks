package ru.mail.polis.homework.concurrency.executor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class SimpleExecutorTest {


    public static final int SLEEP_TIME = 3000;
    public static final int DELTA_TIME = SLEEP_TIME / 4;
    private SimpleExecutor executor;
    private volatile Exception exception;


    @Before
    public void setUp() {
        executor = new SimpleExecutor(5);
        exception = null;
    }

    @After
    public void tearDown() throws Exception {
        executor.shutdown();
        if (exception != null) {
            throw exception;
        }
    }

    @Test
    public void noExecutionTest() {
        assertEquals(0, executor.getLiveThreadsCount());
    }

    @Test
    public void oneExecutionTest() {
        executor.execute(() -> System.out.println("oneExecutionTest"));
        assertEquals(1, executor.getLiveThreadsCount());
    }

    @Test
    public void oneThreadTest() throws InterruptedException {
        execute(1);
        assertEquals(1, executor.getLiveThreadsCount());
    }

    @Test
    public void twoThreadTest() throws InterruptedException {
        execute(2);
        assertEquals(2, executor.getLiveThreadsCount());
    }

    @Test
    public void fiveThreadTest() throws InterruptedException {
        execute(5);
        assertEquals(5, executor.getLiveThreadsCount());
    }

    @Test
    public void sevenThreadTest() throws InterruptedException {
        execute(7);
        assertEquals(5, executor.getLiveThreadsCount());
    }

    private void execute(int threadsCount) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            CountDownLatch start = new CountDownLatch(threadsCount);
            CountDownLatch finish = new CountDownLatch(threadsCount);
            for (int j = 0; j < threadsCount; j++) {
                executor.execute(new Worker(start, finish));
            }
            start.await(10, TimeUnit.SECONDS);
            long measureTime = System.currentTimeMillis();
            finish.await(10, TimeUnit.SECONDS);
            if (threadsCount <= 5) {
                assertEquals(SLEEP_TIME, System.currentTimeMillis() - measureTime, DELTA_TIME);
            } else {
                assertEquals(2 * SLEEP_TIME, System.currentTimeMillis() - measureTime, 2 * DELTA_TIME);
            }
        }
    }

    private class Worker implements Runnable {

        private final CountDownLatch start;
        private final CountDownLatch finish;

        private Worker(CountDownLatch start, CountDownLatch finish) {
            this.start = start;
            this.finish = finish;
        }

        @Override
        public void run() {
            start.countDown();
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                exception = e;
            }
            finish.countDown();
        }
    }

}