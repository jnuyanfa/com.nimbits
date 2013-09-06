/*
 * Copyright (c) 2013 Nimbits Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.  See the License for the specific language governing permissions and limitations under the License.
 */

package com.nimbits.mobile.server;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.Map;

/**
 * Created by benjamin on 9/2/13.
 */
public class HttpService extends Service {

    public final static String TAG = "HttpService";
    private static final int PORT = 8765;

    private MyHTTPD server;
    private Handler handler = new Handler();

    public HttpService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "Service Started");
//        run(MyHTTPD.class);
        return super.onStartCommand(intent, flags, startId);

    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void run(Class serverClass) {
        try {
            executeInstance((NanoHTTPD) serverClass.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void executeInstance(NanoHTTPD server) {
        try {
            server.start();
        } catch (IOException ioe) {
            Log.v(TAG, "Couldn't start server:\n" + ioe);
            System.exit(-1);
        }

        Log.v(TAG, "Server started, Hit Enter to stop.\n");


        // server.stop();
        // Log.v(TAG, "Server stopped.\n");
    }

    private class MyHTTPD extends NanoHTTPD {


        public MyHTTPD() {
            super(PORT);
        }

        @Override
        public Response serve(String uri, Method method, Map<String, String> headers,
                              Map<String, String> parms, Map<String, String> files) {
            final StringBuilder buf = new StringBuilder();
            for (Map.Entry<String, String> kv : headers.entrySet())
                buf.append(kv.getKey() + " : " + kv.getValue() + "\n");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.v(TAG, buf.toString());
                }
            });

            final String html = "<html><head><head><body><h1>Hello, World</h1></body></html>";
            return new Response(Response.Status.OK, MIME_HTML, html);
        }
    }
}