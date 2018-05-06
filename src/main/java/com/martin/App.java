package com.martin;

import com.google.common.collect.Sets;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class App
{
    private static final int NUMBER_OF_CONCURRENT_REQUESTS = 100;

    private static final Set<String> THREAD_NAMES = Sets.newConcurrentHashSet();

    private static final CountDownLatch COUNT_DOWN_LATCH = new CountDownLatch(NUMBER_OF_CONCURRENT_REQUESTS);

    public static void main(String[] args) throws Exception
    {
        CloseableHttpAsyncClient asyncHttpClient = createAsyncAndNonBlockingHttpClient();

        System.out.println("Started executing " + NUMBER_OF_CONCURRENT_REQUESTS + " requests...");

        long start = System.currentTimeMillis();

        for (int i = 0; i < NUMBER_OF_CONCURRENT_REQUESTS; i++)
        {
            callSlowEndpoint(asyncHttpClient);
        }

        COUNT_DOWN_LATCH.await();

        long end = System.currentTimeMillis();

        asyncHttpClient.close();

        System.out.println("Calls took " + (end - start) + " milliseconds to finish.");
        System.out.println(THREAD_NAMES.size() + " threads were used: " + THREAD_NAMES);
    }

    private static CloseableHttpAsyncClient createAsyncAndNonBlockingHttpClient() throws IOReactorException
    {
        PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(
            new DefaultConnectingIOReactor());

        connectionManager.setMaxTotal(NUMBER_OF_CONCURRENT_REQUESTS);
        connectionManager.setDefaultMaxPerRoute(NUMBER_OF_CONCURRENT_REQUESTS);

        CloseableHttpAsyncClient asyncHttpClient = HttpAsyncClientBuilder.create()
                                                                         .setConnectionManager(connectionManager)
                                                                         .build();
        asyncHttpClient.start();

        return asyncHttpClient;
    }

    private static void callSlowEndpoint(CloseableHttpAsyncClient asyncHttpClient)
    {
        // callback hell
        asyncHttpClient.execute(new HttpGet("http://localhost:8080/slow"), (Callback<HttpResponse>) result ->
        {
            THREAD_NAMES.add(Thread.currentThread().getName());

            asyncHttpClient.execute(new HttpGet("http://localhost:8080/slow?" + result.getStatusLine().getStatusCode()), (Callback<HttpResponse>) result2 ->
            {
                THREAD_NAMES.add(Thread.currentThread().getName());

                asyncHttpClient.execute(new HttpGet("http://localhost:8080/slow?" + result2.getStatusLine().getStatusCode()), (Callback<HttpResponse>) result3 ->
                {
                    THREAD_NAMES.add(Thread.currentThread().getName());

                    asyncHttpClient.execute(new HttpGet("http://localhost:8080/slow?" + result3.getStatusLine().getStatusCode()), (Callback<HttpResponse>) result4 ->
                    {
                        COUNT_DOWN_LATCH.countDown();

                        THREAD_NAMES.add(Thread.currentThread().getName());
                    });
                });
            });
        });
    }
}
