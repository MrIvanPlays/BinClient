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

import com.mrivanplays.binclient.paste.IvanBinPaste;
import com.mrivanplays.binclient.paste.impl.IvanBinPasteImpl;
import com.mrivanplays.binclient.request.RestRequest;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Represents a bin server, ran under the <a href="https://github.com/MrIvanPlays/bin.mrivanplays.com/">ivanbin</a> package
 */
public final class IvanBinServer
{

    private OkHttpClient client;
    private final String userAgent;

    public IvanBinServer()
    {
        this(new OkHttpClient());
    }

    public IvanBinServer(ExecutorService executor)
    {
        this(new OkHttpClient.Builder().dispatcher(new Dispatcher(executor)).build());
    }

    public IvanBinServer(OkHttpClient client)
    {
        this.client = client;
        this.userAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:15.0) Gecko/20100101 Firefox/15.0.1";
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
                .url("https://bin.mrivanplays.com/api/create")
                .header("User-Agent", userAgent)
                .header("Content-Type", "text")
                .post(RequestBody.create(MediaType.parse("text/plain"), code)).build();
        return new RestRequest<>(request, client, (response) ->
        {
            JSONObject object = new JSONObject(new JSONTokener(response.body().byteStream()));
            return object.getString("binId");
        });
    }

    /**
     * Retrieves the paste with the specified id
     *
     * @param id id
     * @return rest request
     */
    public RestRequest<IvanBinPaste> retrievePaste(String id)
    {
        Request request = new Request.Builder()
                .url("https://bin.mrivanplays.com/api/info/" + id)
                .header("User-Agent", userAgent)
                .get().build();
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
            JSONObject object = new JSONObject(new JSONTokener(response.body().byteStream()));
            String binId = object.getString("binId");
            OffsetDateTime createdAt = OffsetDateTime.parse(object.getString("createdAt"), DateTimeFormatter.RFC_1123_DATE_TIME);
            OffsetDateTime expiresAt = OffsetDateTime.parse(object.getString("expiresAt"), DateTimeFormatter.RFC_1123_DATE_TIME);
            return new IvanBinPasteImpl(binId, object.getString("body"), "https://bin.mrivanplays.com/" + binId, createdAt, expiresAt);
        });
    }
}
