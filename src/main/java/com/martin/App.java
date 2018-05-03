package com.martin;

import com.google.common.collect.Sets;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class App
{
    private static final int NUMBER_OF_CONCURRENT_REQUESTS = 100;

    private static final Set<String> THREAD_NAMES = Sets.newConcurrentHashSet();
    private static final CountDownLatch COUNT_DOWN_LATCH = new CountDownLatch(NUMBER_OF_CONCURRENT_REQUESTS);

    public static void main(String[] args) throws InterruptedException
    {
        WebClient webClient = WebClient.create();

        System.out.println("Started executing " + NUMBER_OF_CONCURRENT_REQUESTS + " requests...");

        long start = System.currentTimeMillis();

        Flux.range(1, NUMBER_OF_CONCURRENT_REQUESTS)
            .flatMap(count -> callSlowEndpoint(webClient), NUMBER_OF_CONCURRENT_REQUESTS)
            .subscribe(body -> handleSuccess(), e -> handleError(e));

        COUNT_DOWN_LATCH.await();

        long end = System.currentTimeMillis();

        Thread.sleep(1000);

        System.out.println("Calls took " + (end - start) + " milliseconds to finish.");
        System.out.println(THREAD_NAMES.size() + " threads were used: " + THREAD_NAMES);
    }

    private static Mono<String> callSlowEndpoint(WebClient webClient)
    {
        return webClient.get()
                        .uri("http://localhost:8080/slow")
                        .retrieve()
                        .bodyToMono(String.class);
    }

    private static void handleSuccess()
    {
        COUNT_DOWN_LATCH.countDown();

        THREAD_NAMES.add(Thread.currentThread().getName());
    }

    private static void handleError(Throwable throwable)
    {
        throwable.printStackTrace();

        COUNT_DOWN_LATCH.countDown();

        THREAD_NAMES.add(Thread.currentThread().getName());
    }
}
