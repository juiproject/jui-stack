/*******************************************************************************
 * Copyright 2024 Jeremy Buckley
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * <a href= "http://www.apache.org/licenses/LICENSE-2.0">Apache License v2</a>
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.effacy.jui.rpc.client;

import java.util.Map;

import org.gwtproject.http.client.Request;
import org.gwtproject.http.client.RequestBuilder;
import org.gwtproject.http.client.RequestCallback;
import org.gwtproject.http.client.RequestException;
import org.gwtproject.http.client.Response;
import org.gwtproject.http.client.URL;

import com.effacy.jui.rpc.extdirect.client.ExtDirectRemoteMethod.IRequestCallbackListener;

/**
 * Abstract support class for implementations of
 * {@link IRequestCallbackListener}.
 *
 * @author Jeremy Buckley
 */
public class RequestCallbackListener implements IRequestCallbackListener {

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.remote.client.ExtDirectRemoteMethod.IRequestCallbackListener#on200(java.lang.String)
     */
    @Override
    public void on200(String tids) {
        // Nothing.
    }


    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.remote.client.ExtDirectRemoteMethod.IRequestCallbackListener#on200Exception(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void on200Exception(String tids, String message) {
        // Nothing.
    }


    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.remote.client.ExtDirectRemoteMethod.IRequestCallbackListener#onLossOfSession(java.lang.String)
     */
    @Override
    public void onLossOfSession(String tids) {
        // Nothing.
    }


    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.remote.client.ExtDirectRemoteMethod.IRequestCallbackListener#onError(java.lang.String,
     *      int, java.lang.String)
     */
    @Override
    public void onError(String tids, int code, String message) {
        // Nothing.
    }


    /**
     * Sends a message to a given url.
     * 
     * @param url
     *            the URL.
     * @param parameters
     *            any parameters to encapsulate.
     */
    protected void send(String url, Map<String, String> parameters) {
        StringBuilder sb = new StringBuilder (url);
        sb.append ("?");
        for (String k : parameters.keySet ()) {
            String vx = URL.encodeQueryString (parameters.get (k));
            if (sb.length () > 0)
                sb.append ("&");
            sb.append (k).append ("=").append (vx);
        }
        try {
            new RequestBuilder (RequestBuilder.GET, sb.toString ()).sendRequest (null, new RequestCallback () {

                @Override
                public void onResponseReceived(Request request, Response response) {
                    // Nothing.
                }


                @Override
                public void onError(Request request, Throwable exception) {
                    // Nothing.
                }
            });
        } catch (RequestException e) {
            // Nothing.
        }
    }

}
