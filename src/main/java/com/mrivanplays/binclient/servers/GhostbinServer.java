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

import com.mrivanplays.binclient.paste.GhostbinPaste;
import com.mrivanplays.binclient.paste.impl.GhostbinPasteImpl;
import com.mrivanplays.binclient.request.RestRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Represents a bin server, ran under the ghostbin package
 */
public final class GhostbinServer
{

    private OkHttpClient client;
    private String defaultExpiryTime;
    private String baseUrl;
    private final String userAgent;

    public GhostbinServer(String defaultExpiryTime)
    {
        this(defaultExpiryTime, "https://paste.menudocs.org/");
    }

    public GhostbinServer(String defaultExpiryTime, String baseUrl)
    {
        this(new OkHttpClient(), defaultExpiryTime, baseUrl);
    }

    public GhostbinServer(ExecutorService executor, String defaultExpiryTime)
    {
        this(executor, defaultExpiryTime, "https://paste.menudocs.org/");
    }

    public GhostbinServer(ExecutorService executor, String defaultExpiryTime, String baseUrl)
    {
        this(new OkHttpClient.Builder().dispatcher(new Dispatcher(executor)).build(), defaultExpiryTime, baseUrl);
    }

    public GhostbinServer(OkHttpClient client, String defaultExpiryTime, String baseUrl)
    {
        this.client = client;
        this.userAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:15.0) Gecko/20100101 Firefox/15.0.1";
        this.defaultExpiryTime = defaultExpiryTime;
        this.baseUrl = baseUrl;
    }

    /**
     * Creates a new paste
     *
     * @param code code
     * @param language language id
     * @return rest request
     */
    public RestRequest<String> createPaste(String code, String language)
    {
        return createPaste(code, language, defaultExpiryTime);
    }

    /**
     * Creates a new paste
     *
     * @param code code
     * @param language language id
     * @param expiryTime time after this bin will expire
     * @return rest request
     */
    public RestRequest<String> createPaste(String code, String language, String expiryTime)
    {
        Map<String, String> postBody = new HashMap<>();
        postBody.put("lang", language);
        postBody.put("text", code);
        postBody.put("expire", expiryTime);

        Request request = new Request.Builder()
                .url(baseUrl + "paste/new")
                .addHeader("User-Agent", userAgent)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(RequestBody.create(MediaType.get("application/x-www-form-urlencoded"), createFormBody(postBody).getBytes()))
                .build();
        return new RestRequest<>(request, client, (response) -> response.request().url().url()
                .toString().replace(baseUrl + "paste/", "").replace("/", ""));
    }

    /**
     * Retrieves the paste with the specified id
     *
     * @param id paste id
     * @return rest request
     */
    public RestRequest<GhostbinPaste> retrievePaste(String id)
    {
        Request request = new Request.Builder()
                .url(baseUrl + "paste/" + id + ".json")
                .addHeader("User-Agent", userAgent)
                .get()
                .build();

        return new RestRequest<>(request, client, (response) -> {
            if (response.code() == 404)
            {
                throw new IllegalArgumentException("Bin with id '" + id + "' does not exist.");
            }
            if (response.code() != 200)
            {
                throw new RuntimeException("(THIS IS NOT A BUG) Status code not 200 ; server not responding? (THIS IS NOT A BUG)");
            }
            JSONObject object = new JSONObject(new JSONTokener(response.body().byteStream()));
            String binId = object.getString("id");
            JSONObject languageObject = object.getJSONObject("language");

            return new GhostbinPasteImpl(
                    binId,
                    object.getString("body"),
                    baseUrl + "paste/" + binId,
                    new GhostbinPaste.Language(languageObject.getString("name"), languageObject.getString("id")),
                    object.getString("expiration"),
                    object.getBoolean("encrypted")
                    );
        });
    }

    private String createFormBody(Map<String, String> fields)
    {
        StringBuilder builder = new StringBuilder();

        try
        {
            for (Map.Entry<String, String> entry : fields.entrySet())
            {
                builder.append('&')
                        .append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                        .append('=')
                        .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
        }
        catch (UnsupportedEncodingException ignored)
        {
            return "";
        }

        return builder.toString().substring(1);
    }
}
