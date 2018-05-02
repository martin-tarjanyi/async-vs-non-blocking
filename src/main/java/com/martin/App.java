package com.martin;

import com.google.common.collect.Sets;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class App
{
    private static final int NUMBER_OF_CONCURRENT_REQUESTS = 300;

    private static final Set<String> THREAD_NAMES = Sets.newConcurrentHashSet();

    private static final CountDownLatch COUNT_DOWN_LATCH = new CountDownLatch(NUMBER_OF_CONCURRENT_REQUESTS);

    public static void main(String[] args) throws Exception
    {
        AsyncRestTemplate asyncRestTemplate = createAsyncRestTemplate();

        System.out.println("Started executing " + NUMBER_OF_CONCURRENT_REQUESTS + " requests...");
        long start = System.currentTimeMillis();

        for (int i = 0; i < NUMBER_OF_CONCURRENT_REQUESTS; i++)
        {
            callSlowEndpoint(asyncRestTemplate);
        }

        COUNT_DOWN_LATCH.await();

        long end = System.currentTimeMillis();

        System.out.println("Calls took " + (end - start) + " milliseconds to finish.");
        System.out.println(THREAD_NAMES.size() + " threads were used: " + THREAD_NAMES);
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
        // non-blocking
        asyncRestTemplate.getForEntity("http://localhost:8080/slow", String.class)
                         .addCallback(response -> handleSuccess(), e -> handleError(e));
    }

    private static void handleSuccess()
    {
        COUNT_DOWN_LATCH.countDown();

        String threadName = Thread.currentThread().getName();

        THREAD_NAMES.add(threadName);
    }

    private static void handleError(Throwable throwable)
    {
        COUNT_DOWN_LATCH.countDown();

        throwable.printStackTrace();
    }
}
