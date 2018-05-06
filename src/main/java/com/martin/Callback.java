package com.martin;

import org.apache.http.concurrent.FutureCallback;

public interface Callback<T> extends FutureCallback<T>
{
    @Override
    default void failed(Exception ex)
    {
        throw new RuntimeException(ex);
    }

    @Override
    default void cancelled()
    {
        throw new IllegalStateException("Unexpected cancellation.");
    }
}
