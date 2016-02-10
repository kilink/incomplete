package net.kilink.incomplete;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class CompletableFutureService<V> implements CompletionService<V> {

    private final Executor executor;
    private final BlockingQueue<CompletableFuture<V>> completionQueue;

    public CompletableFutureService(Executor executor) {
        this(executor, new LinkedBlockingQueue<>());
    }

    public CompletableFutureService(Executor executor,
                                    BlockingQueue<CompletableFuture<V>> completionQueue) {
        this.executor = executor;
        this.completionQueue = completionQueue;
    }

    @Override
    public CompletableFuture<V> submit(Callable<V> task) {
        CompletableFuture<V> future = CompletableFuture.supplyAsync(toSupplier(task), executor);
        return future.whenComplete((res, exc) -> completionQueue.add(future));
    }

    @Override
    public CompletableFuture<V> submit(Runnable task, V result) {
        CompletableFuture<V> future = CompletableFuture.runAsync(task, executor)
                .thenApply(nil -> result);
        return future.whenComplete((res, exc) -> completionQueue.add(future));
    }

    @Override
    public CompletableFuture<V> take() throws InterruptedException {
        return completionQueue.take();
    }

    @Override
    public CompletableFuture<V> poll() {
        return completionQueue.poll();
    }

    @Override
    public CompletableFuture<V> poll(long timeout, TimeUnit unit) throws InterruptedException {
        return completionQueue.poll(timeout, unit);
    }

    private static <V> Supplier<V> toSupplier(Callable<? extends V> callable) {
        return () -> {
            try {
                return callable.call();
            } catch (Exception exc) {
                throw new CompletionException(exc);
            }
        };
    }
}