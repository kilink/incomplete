package net.kilink.incomplete;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CompletableFuturesTests {

    private Executor executor = Executors.newCachedThreadPool();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testAllOf() {
        CompletableFuture<String> cfa = CompletableFuture.supplyAsync(() -> "a", executor);
        CompletableFuture<String> cfb = CompletableFuture.supplyAsync(() -> "b", executor);
        CompletableFuture<String> cfc = CompletableFuture.supplyAsync(() -> "c", executor);
        CompletableFuture<List<CharSequence>> resultFuture = CompletableFutures.allOf(cfa, cfb, cfc);
        List<CharSequence> result = resultFuture.join();
        assertEquals(Arrays.asList("a", "b", "c"), result);
    }

    @Test
    public void testAllOf_Failure() {
        CompletableFuture<String> cfa = CompletableFuture.supplyAsync(() -> "a", executor);
        CompletableFuture<String> cfb = failedFuture(new RuntimeException("b"));
        CompletableFuture<List<CharSequence>> resultFuture = CompletableFutures.allOf(cfa, cfb);
        thrown.expectCause(CoreMatchers.instanceOf(RuntimeException.class));
        thrown.expectMessage("b");
        resultFuture.join();
    }

    @Test
    public void testFirstSuccessful() {
        CompletableFuture<String> cfa = new CompletableFuture<>();
        CompletableFuture<String> cfb = failedFuture(new RuntimeException("!!"));
        CompletableFuture<String> cfc = CompletableFuture.completedFuture("c");

        CompletableFuture<CharSequence> result = CompletableFutures.firstSuccessful(cfa, cfb, cfc);
        assertEquals("c", result.join());
        assertTrue(cfa.isCancelled());
        assertTrue(cfb.isCompletedExceptionally());
    }

    @Test
    public void testFirstSuccessful_Failure() {
        CompletableFuture<String> cfa = failedFuture(new RuntimeException("a"));
        CompletableFuture<String> cfb = failedFuture(new RuntimeException("b"));
        CompletableFuture<String> cfc = failedFuture(new RuntimeException("c"));
        CompletableFuture<CharSequence> result = CompletableFutures.firstSuccessful(cfa, cfb, cfc);
        thrown.expectCause(CoreMatchers.instanceOf(RuntimeException.class));
        thrown.expectMessage("c");
        result.join();
    }

    private static <T> CompletableFuture<T> failedFuture(Throwable t) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(t);
        return future;
    }
}
