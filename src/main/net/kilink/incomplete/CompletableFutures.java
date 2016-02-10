package net.kilink.incomplete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;


public final class CompletableFutures {

    private CompletableFutures() {}

    @SafeVarargs
    public static <T> CompletableFuture<List<T>> allOf(CompletableFuture<? extends T> ...futures) {
        return allOf(Arrays.asList(futures));
    }

    public static <T> CompletableFuture<List<T>> allOf(
            Iterable<? extends CompletableFuture<? extends T>> futures) {
        CompletableFuture<List<T>> result = CompletableFuture.completedFuture(new ArrayList<>());
        for (CompletableFuture<? extends T> future : futures) {
            result = result.thenCombine(future, (list, item) -> {
                list.add(item);
                return list;
            });
        }
        return result;
    }

    @SafeVarargs
    public static <T> CompletableFuture<T> firstSuccessful(CompletableFuture<? extends T> ...futures) {
        return firstSuccessful(Arrays.asList(futures));
    }

    public static <T> CompletableFuture<T> firstSuccessful(
            Collection<? extends CompletableFuture<? extends T>> futures) {
        CompletableFuture<T> result = new CompletableFuture<>();
        AtomicInteger counter = new AtomicInteger(futures.size());

        for (CompletableFuture<? extends T> future : futures) {
            future.whenComplete((res, exc) -> {
                if (!result.isDone()) {
                    int count = counter.decrementAndGet();
                    if (exc == null) {
                        result.complete(res);
                    } else if (count == 0) {
                        result.completeExceptionally(exc);
                    }
                }
            });
        }
        result.thenRun(() -> futures.forEach(future -> future.cancel(true)));
        return result;
    }
}