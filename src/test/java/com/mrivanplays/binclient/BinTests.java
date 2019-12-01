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
package com.mrivanplays.binclient;

import com.mrivanplays.binclient.paste.GhostbinPaste;
import com.mrivanplays.binclient.paste.IvanBinPaste;
import com.mrivanplays.binclient.paste.Paste;
import com.mrivanplays.binclient.paste.SourcebinPaste;
import com.mrivanplays.binclient.servers.GhostbinServer;
import com.mrivanplays.binclient.servers.HasteServer;
import com.mrivanplays.binclient.servers.IvanBinServer;
import com.mrivanplays.binclient.servers.SourcebinServer;
import okhttp3.OkHttpClient;
import org.junit.Test;

public class BinTests
{

    private static final OkHttpClient client;

    static
    {
        client = new OkHttpClient();
    }

    @Test
    public void testHastebin()
    {
        HasteServer hasteServer = new HasteServer(client, "https://hasteb.in/");
        String id = hasteServer.createPaste("<h1>Hello, world!</h1>").sync();
        Paste paste = hasteServer.retrievePaste(id).sync();
        System.out.println(paste.getBody());
        System.out.println(paste.getId());
        System.out.println(paste.getUrl());
    }

    @Test
    public void testGhostbin()
    {
        GhostbinServer ghostbinServer = new GhostbinServer(client, "10m", "https://paste.menudocs.org/");
        String id = ghostbinServer.createPaste("<h1>Hello, world!</h1>", "html").sync();
        GhostbinPaste paste = ghostbinServer.retrievePaste(id).sync();
        System.out.println(paste.getBody());
        System.out.println(paste.getId());
        System.out.println(paste.getUrl());
        System.out.println(paste.getExpiration());
        System.out.println(paste.getLanguage().getId());
        System.out.println(paste.getLanguage().getName());
        System.out.println(paste.isEncrypted());
    }

    @Test
    public void testIvanBin()
    {
        IvanBinServer ivanBinServer = new IvanBinServer(client);
        String id = ivanBinServer.createPaste("System.out.println(\"Hello, world\");").sync();
        IvanBinPaste paste = ivanBinServer.retrievePaste(id).sync();
        System.out.println(paste.getBody());
        System.out.println(paste.getId());
        System.out.println(paste.getUrl());
        System.out.println(paste.getCreatedAt());
        System.out.println(paste.getExpiresAt());
    }

    @Test
    public void testSourcebin()
    {
        SourcebinServer sourcebinServer = new SourcebinServer(client);
        String id = sourcebinServer.createPaste("<h1>Hello, world!</h1>").sync();
        SourcebinPaste paste = sourcebinServer.retrievePaste(id).sync();
        System.out.println(paste.getBody());
        System.out.println(paste.getId());
        System.out.println(paste.getUrl());
        System.out.println(paste.getCreatedAt());
    }
}
