package com.martin;

import com.google.common.collect.Sets;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final Set<String> THREAD_NAMES = Sets.newConcurrentHashSet();

    public static void main( String[] args ) throws ExecutionException, InterruptedException
    {
        // New threads are created if no thread is available, if there is idle thread then it is reused
        ExecutorService executorService = Executors.newCachedThreadPool();

        RestTemplate restTemplate = new RestTemplate();

        long start = System.currentTimeMillis();

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < 100; i++)
        {
            // Asynchronous
            Future<?> future = executorService.submit(() -> callSlowEndpoint(restTemplate));

            futures.add(future);
        }

        for (Future<?> future : futures)
        {
            future.get();
        }

        long end = System.currentTimeMillis();

        System.out.println("Calls took " + (end - start) + " milliseconds to finish.");
        System.out.println(THREAD_NAMES.size() + " threads were used.");

        executorService.shutdown();
    }

    private static String callSlowEndpoint(RestTemplate restTemplate)
    {
        // Blocking background thread
        ResponseEntity<String> responseEntity = restTemplate.getForEntity("http://localhost:8080/slow", String.class);

        String threadName = Thread.currentThread().getName();

        THREAD_NAMES.add(threadName);

        return responseEntity.getBody();
    }
}
