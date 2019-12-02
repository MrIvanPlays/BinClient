/*
    Copyright (c) 2019 Ivan Pekov
    Copyright (c) 2019 Contributors

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/
package com.mrivanplays.binclient.request;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Represents a rest request
 *
 * @param <T> type of value, retrieved from that request
 */
public final class RestRequest<T> {

    private Request request;
    private OkHttpClient client;
    private Function<Response, T> finisher;

    public RestRequest(Request request, OkHttpClient client, Function<Response, T> finisher) {
        this.request = request;
        this.client = client;
        this.finisher = finisher;
    }

    /**
     * Calls this request synchronously. This may cause the main thread to freeze for a moment
     *
     * @return direct value after call
     * @throws RequestException if exception caught
     */
    public T sync() {
        return sync(error -> {
            throw new RequestException("An error occurred while trying to process synchronous request: " + request, error);
        });
    }

    /**
     * Calls this request synchronously and provides a exception handler. This may cause the main thread to freeze for a
     * moment.
     *
     * @param onFailure exception handler
     * @return direct value after call or onFailure given value if fail
     */
    public T sync(Function<Throwable, T> onFailure) {
        Call call = client.newCall(request);
        try (Response response = call.execute()) {
            return finisher.apply(response);
        } catch (Throwable error) {
            return onFailure.apply(error);
        }
    }

    /**
     * Calls this request asynchronously
     *
     * @param onSuccess callback with the value if no failure happened
     */
    public void async(Consumer<T> onSuccess) {
        async(onSuccess, Throwable::printStackTrace);
    }

    /**
     * Calls this request asynchronously
     *
     * @param onSuccess callback with the value if no failure happened
     * @param onFailure exception handler
     */
    public void async(Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onFailure.accept(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    onSuccess.accept(finisher.apply(response));
                } catch (Throwable error) {
                    onFailure.accept(error);
                }
            }
        });
    }
}
