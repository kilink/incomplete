package net.kilink.incomplete;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class CompletableFutureServiceTests {

    private Executor executor = Executors.newSingleThreadExecutor();
    private CompletableFutureService<String> service = new CompletableFutureService<>(executor);

    @Test
    public void testSubmit_Callable() throws InterruptedException {
        service.submit(() -> "foo");
        service.submit(() -> "bar");
        service.submit(() -> "baz");
        CompletableFuture<String> cf1 = service.poll();
        CompletableFuture<String> cf2 = service.take();
        CompletableFuture<String> cf3 = service.poll(5, TimeUnit.SECONDS);
        assertEquals("foo", cf1.join());
        assertEquals("bar", cf2.join());
        assertEquals("baz", cf3.join());
        assertNull(service.poll());
    }

    @Test
    public void testSubmit_Runnable() throws InterruptedException {
        service.submit(() -> {}, "foo");
        service.submit(() -> {}, "bar");
        service.submit(() -> {}, "baz");
        CompletableFuture<String> cf1 = service.poll();
        CompletableFuture<String> cf2 = service.take();
        CompletableFuture<String> cf3 = service.poll(5, TimeUnit.SECONDS);
        assertEquals("foo", cf1.join());
        assertEquals("bar", cf2.join());
        assertEquals("baz", cf3.join());
        assertNull(service.poll());
    }

    @Test
    public void testQueue() throws InterruptedException {
        BlockingQueue<CompletableFuture<String>> queue = new ArrayBlockingQueue<>(1);
        service = new CompletableFutureService<>(executor, queue);
        CountDownLatch latch = new CountDownLatch(3);
        service.submit(() -> {latch.countDown(); return "foo";});
        service.submit(() -> {latch.countDown(); return "bar";});
        service.submit(() -> {latch.countDown(); return "baz";});
        latch.await(1, TimeUnit.SECONDS);
        assertEquals("foo", queue.poll().join());
        assertNull(service.poll());
    }
}
