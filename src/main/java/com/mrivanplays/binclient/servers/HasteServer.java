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
package com.mrivanplays.binclient.servers;

import com.mrivanplays.binclient.paste.Paste;
import com.mrivanplays.binclient.paste.impl.PasteImpl;
import com.mrivanplays.binclient.request.RequestException;
import com.mrivanplays.binclient.request.RestRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Represents a bin server, ran under the hastebin package
 */
public final class HasteServer
{
    private OkHttpClient client;
    private String baseUrl;
    private final String userAgent;

    public HasteServer()
    {
        this("https://hasteb.in/");
    }

    public HasteServer(String baseUrl)
    {
        this(new OkHttpClient(), baseUrl);
    }

    public HasteServer(ExecutorService executor)
    {
        this(executor, "https://hasteb.in/");
    }

    public HasteServer(ExecutorService executor, String baseUrl)
    {
        this(new OkHttpClient.Builder().dispatcher(new Dispatcher(executor)).build(), baseUrl);
    }

    public HasteServer(OkHttpClient client, String baseUrl)
    {
        this.client = client;
        this.userAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:15.0) Gecko/20100101 Firefox/15.0.1";
        this.baseUrl = baseUrl;
    }

    /**
     * Creates a new paste
     *
     * @param code code
     * @return rest request
     */
    public RestRequest<String> createPaste(String code)
    {
        Request request = new Request.Builder()
                .url(baseUrl + "documents")
                .addHeader("User-Agent", userAgent)
                .addHeader("Content-Type", "text")
                .post(RequestBody.create(MediaType.parse("text/plain"), code))
                .build();
        return new RestRequest<>(request, client, (response) ->
        {
            JSONObject object = new JSONObject(new JSONTokener(response.body().byteStream()));
            return object.getString("key");
        });
    }

    /**
     * Retrieves the paste with the specified id
     *
     * @param id id
     * @return rest request
     */
    public RestRequest<Paste> retrievePaste(String id)
    {
        Request request = new Request.Builder()
                .url(baseUrl + "raw/" + id)
                .addHeader("User-Agent", userAgent)
                .get()
                .build();
        return new RestRequest<>(request, client, (response) ->
        {
            if (response.code() == 404)
            {
                throw new IllegalArgumentException("Bin with id '" + id + "' does not exist.");
            }
            if (response.code() != 200)
            {
                throw new RuntimeException("(THIS IS NOT A BUG) Status code not 200 ; server not responding? (THIS IS NOT A BUG)");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream())))
            {
                StringBuilder codeBuilder = new StringBuilder();
                reader.lines().forEach(line -> codeBuilder.append(line).append("\n"));
                return new PasteImpl(id, codeBuilder.toString(), baseUrl + id);
            }
            catch (IOException e)
            {
                throw new RequestException("Error occurred while trying to retrieve a haste server paste", e);
            }
        });
    }
}
