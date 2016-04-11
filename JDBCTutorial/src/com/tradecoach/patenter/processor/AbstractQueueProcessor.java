package com.tradecoach.patenter.processor;

import com.google.common.util.concurrent.*;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Alexander Loginov on 3/16/15.
 */
public abstract class AbstractQueueProcessor<I, O> {

    private static final long TERMINATION_AWAIT_TIMEOUT = 60; //in ms
    final Queue<I> inputQueue;
    final Queue<O> outputQueue;
    final ListeningExecutorService executorService;
    private final AtomicInteger successCounter = new AtomicInteger();
    private final AtomicInteger failCounter = new AtomicInteger();
    private volatile boolean isProcessorRunning = false;
    private Thread processingThread = null;


    public AbstractQueueProcessor(final Queue<I> inputQueue, final Queue<O> outputQueue, ExecutorService executorService) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.executorService = MoreExecutors.listeningDecorator(executorService);
    }

    @SuppressWarnings("unchecked")
	public boolean start() {
        if ((this.processingThread != null && this.processingThread.isAlive()) || isProcessorRunning) {
            return false;
        }

        this.processingThread = new Thread(() -> {
            while (isProcessorRunning) {
                //Wait for objects in queue
                I inputTask = null;
                while (inputTask == null && isProcessorRunning) {
                    inputTask = inputQueue.poll();
                }

                if (inputTask != null) {
                    final I finalInputTask = inputTask;
                    ListenableFuture<O> future = executorService.submit(() -> execute(finalInputTask));

                    Futures.addCallback(future, new ProcessorFutureCallback(inputTask));
                }
            }
        });

        this.processingThread.start();
        this.isProcessorRunning = true;
        return true;
    }

    /**
     * Shutdown this processor. Wait for processing tasks, but doesn't add new from a queue
     */
    public void shutdown() {
        isProcessorRunning = false;
        this.executorService.shutdown();

        while (this.executorService.isTerminated()) {
            try {
                this.executorService.awaitTermination(TERMINATION_AWAIT_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                this.executorService.shutdownNow();
            }
        }
    }

    public final boolean isTerminated() {
        return this.executorService.isTerminated() && !this.processingThread.isAlive();
    }

    /**
     * Number of successfully executed tasks
     *
     * @return number of successfully executed tasks
     */
    public final Integer getSuccessCounter() {
        return successCounter.get();
    }

    /**
     * Number of failed tasks
     *
     * @return number of failed tasks
     */
    public final Integer getFailCounter() {
        return failCounter.get();
    }

    public abstract O execute(I input) throws Exception;

    public abstract void onSuccessCallback(O result);

    public abstract void onFailureCallback(I input, Throwable t);

    private class ProcessorFutureCallback<T> implements FutureCallback<O> {

        private final T input;

        private ProcessorFutureCallback(T input) {
            this.input = input;
        }


        @Override
        public void onSuccess(O result) {
            successCounter.getAndIncrement();
            while (!outputQueue.offer(result)) {
                try {
                    Thread.currentThread().sleep(500);
                } catch (InterruptedException ignored) {

                }
            }

            onSuccessCallback(result);
        }

        @Override
        public void onFailure(Throwable t) {
            failCounter.getAndIncrement();

            onFailureCallback((I) input, t); //cast will be ok
        }
    }

    public static ExecutorService newFixedThreadPoolWithQueueSize(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(nThreads, true), new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
