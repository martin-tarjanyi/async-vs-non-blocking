package com.martin;

import com.google.common.collect.Sets;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final int NUMBER_OF_CONCURRENT_REQUESTS = 6000;

    private static final Set<String> THREAD_NAMES = Sets.newConcurrentHashSet();
    private static final Set<String> RESULTS = Collections.synchronizedSet(new HashSet<>());

    public static void main( String[] args ) throws Exception
    {
        AsyncRestTemplate asyncRestTemplate = createAsyncRestTemplate();

        System.out.println("Started");
        long start = System.currentTimeMillis();

        for (int i = 0; i < NUMBER_OF_CONCURRENT_REQUESTS; i++)
        {
            callSlowEndpoint(asyncRestTemplate);
        }


        while (RESULTS.size() != NUMBER_OF_CONCURRENT_REQUESTS)
        {
            // wait for the calls to finish
        }

        long end = System.currentTimeMillis();

        System.out.println("Calls took " + (end - start) + " milliseconds to finish.");
        System.out.println(THREAD_NAMES.size() + " threads were used.");
    }

    private static AsyncRestTemplate createAsyncRestTemplate() throws IOReactorException
    {
        PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(
                new DefaultConnectingIOReactor());

        connectionManager.setMaxTotal(NUMBER_OF_CONCURRENT_REQUESTS);
        connectionManager.setDefaultMaxPerRoute(NUMBER_OF_CONCURRENT_REQUESTS);

        CloseableHttpAsyncClient httpclient = HttpAsyncClientBuilder.create()
                                                                    .setConnectionManager(connectionManager)
                                                                    .build();

        return new AsyncRestTemplate(new HttpComponentsAsyncClientHttpRequestFactory(httpclient));
    }

    private static void callSlowEndpoint(AsyncRestTemplate asyncRestTemplate)
    {
        // non-blocking IO
        asyncRestTemplate.getForEntity("http://localhost:8080/slow", String.class)
                         .addCallback(App::handleSuccess, throwable -> {throwable.printStackTrace(); RESULTS.add("error");});
    }

    private static void handleSuccess(ResponseEntity<String> responseEntity)
    {
        RESULTS.add(responseEntity.getBody());

        String threadName = Thread.currentThread().getName();

        System.out.println(threadName);

        THREAD_NAMES.add(threadName);
    }
}
