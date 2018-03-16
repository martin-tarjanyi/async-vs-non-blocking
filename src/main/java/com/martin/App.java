package com.martin;

import com.google.common.collect.Sets;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Hello world!
 */
public class App
{
    private static final int NUMBER_OF_CONCURRENT_REQUESTS = 2000;

    private static final Set<String> THREAD_NAMES = Sets.newConcurrentHashSet();
    private static final Collection<String> RESULTS = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args)
    {
        WebClient webClient = createWebClient();

        System.out.println("Started");
        long start = System.currentTimeMillis();

        for (int i = 0; i < NUMBER_OF_CONCURRENT_REQUESTS; i++)
        {
            callSlowEndpoint(webClient);
        }

        while (RESULTS.size() != NUMBER_OF_CONCURRENT_REQUESTS)
        {
            // wait for the calls to finish
        }

        long end = System.currentTimeMillis();

        System.out.println("Calls took " + (end - start) + " milliseconds to finish.");
        System.out.println(THREAD_NAMES.size() + " threads were used.");
    }

    private static WebClient createWebClient()
    {
        return WebClient.create();
    }

    private static void callSlowEndpoint(WebClient webClient)
    {
        // non-blocking IO
        webClient.get()
                 .uri("http://localhost:8080/slow")
                 .exchange()
                 .flatMap(clientResponse -> clientResponse.bodyToMono(String.class))
                 .subscribe(App::handleSuccess, throwable ->
                 {
                     throwable.printStackTrace();
                     RESULTS.add("error");
                 });
    }

    private static void handleSuccess(String body)
    {
        RESULTS.add(body);

        String threadName = Thread.currentThread()
                                  .getName();

//        System.out.println(threadName);

        THREAD_NAMES.add(threadName);
    }
}
