package com.martin;

import com.google.common.collect.Sets;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

public class App
{
    private static final int NUMBER_OF_CONCURRENT_REQUESTS = 100;

    private static final Set<String> THREAD_NAMES = Sets.newConcurrentHashSet();

    public static void main(String[] args)
    {
        WebClient webClient = WebClient.create();

        System.out.println("Started executing " + NUMBER_OF_CONCURRENT_REQUESTS + " requests...");

        long start = System.currentTimeMillis();

        Flux.range(1, NUMBER_OF_CONCURRENT_REQUESTS)
            .flatMap(count -> callSlowEndpoint(webClient))
            .doOnEach(response -> THREAD_NAMES.add(Thread.currentThread().getName()))
            .blockLast(); // do NOT block in production code, this is just for demonstration purposes

        long end = System.currentTimeMillis();

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
}
