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
package com.effacy.jui.rpc.extdirect.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.gwtproject.http.client.Request;
import org.gwtproject.http.client.RequestBuilder;
import org.gwtproject.http.client.RequestCallback;
import org.gwtproject.http.client.RequestException;
import org.gwtproject.http.client.Response;
import org.gwtproject.http.client.URL;
import org.gwtproject.json.client.JSONArray;
import org.gwtproject.json.client.JSONObject;
import org.gwtproject.json.client.JSONParser;
import org.gwtproject.timer.client.Timer;
import org.gwtproject.user.window.client.Cookies;

import com.effacy.jui.json.client.Serializer;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.rpc.client.ErrorMessage;
import com.effacy.jui.rpc.client.IRemoteMethod;
import com.effacy.jui.rpc.client.IRemoteMethodCallback;
import com.effacy.jui.rpc.client.IRemoteMethodFactory;
import com.effacy.jui.rpc.client.IRemoteMethodInjectable;
import com.effacy.jui.rpc.client.IRequestExecution;
import com.effacy.jui.rpc.client.RemoteMethod;
import com.effacy.jui.rpc.client.RemoteResponse;
import com.effacy.jui.rpc.client.RemoteResponseType;

import elemental2.dom.DomGlobal;

/**
 * A remote method implementation that uses the Ext.Direct (see
 * <a href="http://www.sencha.com/products/js/direct.php">Sencha</a> for
 * details). protocol.
 * <p>
 * Each direct method needs to specify a router URL, action reference, method
 * name and an argument length. Calls to invoke the method are translated to XHR
 * requested appropriately constructed to adhere to the Ext.Direct protocol.
 * <p>
 * The data structure that is returned by the remote method MUST be translatable
 * to a {@link RemoteResponse}. This is not part of Ext.Direct but is a small
 * extension (actually, an additional layer on top of the Ext.Direct transport)
 * that enables error states to be returned in a meaningful manner to be handled
 * by the client (as opposed to exception handling).
 * <p>
 * An additional extension is the specification of a login URL. If this is
 * supplied, then whenever the XHR response returns a 403 (not found) the
 * implementation will assume that there has been a loss of session. It will the
 * present a login dialog to the user and the credentials they supply will be
 * passed to the login URL under the <code>j_username</code> and
 * <code>j_password</code> HTTP request parameters. The login URL will either
 * return a text message to be displayed to the user OR <code>"OK"</code> in
 * which case the method will re-try the transaction.
 * <p>
 * Some benefits of this approach include batching of requests to the server for
 * performance, improved application error handing (as described above) and
 * session recovery (as described above).
 * <p>
 * This supports cross site request forgery by way of double submit cookies.
 * This simply return in the request the value in the cookie
 * {@link #CSRF_COOKIE}.
 * 
 * @author Jeremy Buckley
 * @author Steve Baker
 * @author Marcus Manning
 */
public class ExtDirectRemoteMethod<T> extends RemoteMethod<T> implements IRemoteMethodInjectable<T> {

    /**
     * Configuration for the method.
     */
    public static class Config {

        /**
         * The cookie that should contain the CSRF token.
         */
        private String csrfTokenCookie = "CSRFTOKENID";

        /**
         * See {@link #logLevel(LogLevel)}.
         */
        protected LogLevel logLevel = LogLevel.NONE;

        /**
         * See {@link #requestContributor(Consumer)}.
         */
        protected Consumer<RequestBuilder> requestContributor = null;

        /**
         * An option logging translator to use.
         */
        protected ILogTranslator logTranslator = null;

        /**
         * Hook to listen to responses.
         */
        protected IRequestCallbackListener responseListener = null;

        /**
         * The maximum number of retries permissible.
         */
        protected int MAX_RETRIES = 3;

        /**
         * The maximum number of calls on the queue.
         */
        protected int queueMaxSize = 10;

        /**
         * The maximum age of an item on the queue.
         */
        protected int queueMaxAge = 1500;

        /**
         * The queue timer window to capture calls in.
         */
        protected int queueTimerWindow = 50;

        /**
         * If the timer window should move.
         */
        protected boolean queueTimerMoving = true;

        /**
         * Check to apply to the UI version header.
         */
        protected Function<String,Boolean> uiVersionCheck;

        /**
         * Assign the CSRF token cookie name to lookup the token from.
         * 
         * @param csrfTokenCookie
         *            the cookie name.
         * @return this configuration.
         */
        public Config csrfTokenCookie(String csrfTokenCookie) {
            this.csrfTokenCookie = csrfTokenCookie;
            return this;
        }

        /**
         * Getter for {@link #csrfTokenCookie(String)}.
         */
        public String getCsrfTokenCookie() {
            return csrfTokenCookie;
        }

        /**
         * Assigns a contributor to the building of the outbound request. This
         * can be useful for assigning headers (for example, if using Spring
         * CSRF then it expects to find the token in the headers).
         * 
         * @param contributor
         *            the contributor.
         * @return this configuration.
         */
        public Config requestContributor(Consumer<RequestBuilder> contributor) {
            this.requestContributor = contributor;
            return this;
        }

        /**
         * Assigns a log translator.
         * 
         * @param translator
         *            the translator.
         * @return this configuration.
         */
        public Config logTranslator(ILogTranslator translator) {
            this.logTranslator = translator;
            return this;
        }

        /**
         * Assigns a listener to responses.
         * 
         * @param listener
         *            the listener.
         * @return this configuration.
         */
        public Config remoteResponseListener(IRequestCallbackListener listener) {
            this.responseListener = listener;
            return this;
        }

        /**
         * The maximum size of the request queue.
         * 
         * @param size
         *            the size.
         * @return this configuration.
         */
        public Config queueMaxSize(int size) {
            this.queueMaxSize = size;
            return this;
        }

        /**
         * The maximum age of an item in the queue.
         * 
         * @param millis
         *            the age.
         * @return this configuration.
         */
        public Config queueMaxAge(int millis) {
            this.queueMaxAge = millis;
            return this;
        }

        /**
         * Sets the queue acquisition time in milliseconds.
         * 
         * @param millis
         *            the time.
         * @return this configuration.
         */
        public Config queueAcquisitionWindow(int millis) {
            this.queueTimerWindow = Math.max (0, millis);
            return this;
        }

        /**
         * If the queue acquisition window should be moving.
         * 
         * @param moving
         *            {@code true} if so.
         * @return this configuration.
         */
        public Config queueWindowMoving(boolean moving) {
            this.queueTimerMoving = moving;
            return this;
        }

        /**
         * Sets the logging level of the handler.
         * 
         * @param level
         *            the level.
         * @return this configuration.
         */
        public Config logLevel(LogLevel level) {
            if (level != null)
                this.logLevel = level;
            return this;
        }

        /**
         * Assigns a guard to check the UI version passed on the header and indicate
         * whether flow of action should stop.
         * <p>
         * Generally, on stop, the UI should be reloaded (so updated).
         * 
         * @param uiVersionCheck
         *                       to check the UI version.
         * @return this configuration.
         */
        public Config uiVersionCheck(Function<String,Boolean> uiVersionCheck) {
            this.uiVersionCheck = uiVersionCheck;
            return this;
        }

    }

    /**
     * Configuration for the method.
     */
    private static final Config CONFIG = new Config ();

    /**
     * Obtains the configuration which may be updated.
     * 
     * @return the configuration.
     */
    public static Config config() {
        return CONFIG;
    }

    /**
     * Translates requests for logging purposes.
     */
    public interface ILogTranslator {

        /**
         * Translates a request to a sensible string for logging.
         * 
         * @param request
         *            the request to translate.
         * @return a human readable form.
         */
        public String translate(IRequestExecution<?> request);
    }

    /**
     * Hook to listen to remote call responses.
     */
    public interface IRequestCallbackListener {

        /**
         * On a 200 response (success).
         * 
         * @param tids
         *            the list of TIDs.
         */
        public void on200(String tids);


        /**
         * On a 200 response but with a problem handling the response (i.e.
         * exception).
         * 
         * @param tids
         *            the list of TIDs.
         * @param message
         *            the error message.
         */
        public void on200Exception(String tids, String message);


        /**
         * On a 403 or 302 (which both indicate a loss of session).
         * 
         * @param tids
         *            the list of TIDS.
         */
        public void onLossOfSession(String tids);


        /**
         * On error code.
         * 
         * @param tids
         *            the list of TIDs.
         * @param code
         *            the error code from the response.
         * @param message
         *            the status code from response.
         */
        public void onError(String tids, int code, String message);
    }

    /**
     * Various log levels.
     */
    public enum LogLevel {
        NONE, QUEUE, SUMMARY_NOQUEUE, SUMMARY, FULL, FULL_NOQUEUE;
    }

    /**
     * URL to call to invoke method.
     */
    protected String url;

    /**
     * Name of action service to call.
     */
    private String action;

    /**
     * Name of method to call in the action service.
     */
    private String method;

    /**
     * Number of arguments.
     */
    private int argLength;

    /**
     * Name of the class that the response is embodied in.
     */
    private Class<T> responseClass;

    /**
     * The execution queue.
     */
    private List<IRequestExecution<T>> executionQueue = new ArrayList<IRequestExecution<T>> ();

    /**
     * The time of the oldest entry on the execution queue.
     */
    private long executionQueueOldestEntry = -1;

    /**
     * Timer for the execution queue.
     */
    private Timer executionQueueTimer = new Timer () {

        @Override
        public void run() {
            int calls = flush ();
            if ((calls > 0) && isLoggingQueue ())
                Logger.trace ("ExtDirectRemoteMethod", "sending(" + calls + " calls) [timer]");
        }
    };

    private Timer executionQueueMonitoringTimer = new Timer () {

        @Override
        public void run() {
            if ((executionQueueOldestEntry > 0) && ((System.currentTimeMillis () - executionQueueOldestEntry) > 2 * CONFIG.queueMaxAge)) {
                executionQueueTimer.cancel ();
                List<IRequestExecution<T>> calls = new ArrayList<IRequestExecution<T>> (executionQueue);
                executionQueue.clear ();
                executionQueueOldestEntry = -1;
                Logger.warn ("ExtDirectRemoteMethod: Emergency backup timer cleared queue (" + calls.size () + " calls)");
                sendImpl (calls, 0);
            }
        }
    };

    /**
     * Counter to derive unique transaction IDs from.
     */
    private static int TID_COUNTER;

    /**
     * Default factory for this remote method.
     */
    public static final IRemoteMethodFactory FACTORY = new IRemoteMethodFactory () {

        @Override
        public <T> IRemoteMethod<T> create(String url, String action, String method, int argLength) {
            return new ExtDirectRemoteMethod<T> (url, action, method, argLength);
        }

    };

    /**
     * All the methods that have been created.
     */
    private static List<ExtDirectRemoteMethod<?>> METHODS = new ArrayList<ExtDirectRemoteMethod<?>> ();

    /**
     * Construct with reference data to the remote service router.
     * 
     * @param url
     *            the URL of the service router.
     * @param action
     *            the action being referenced.
     * @param method
     *            the method of the action to be invoked.
     */
    public ExtDirectRemoteMethod(String url, String action, String method, int argLength) {
        this.url = url;
        this.action = action;
        this.method = method;
        this.argLength = argLength;

        // Start the backup timer.
        if ((CONFIG.queueTimerWindow > 0) && (CONFIG.queueMaxSize > 0))
            executionQueueMonitoringTimer.scheduleRepeating (5000);

        // Register the method.
        METHODS.add (this);
    }


    /**
     * Default constructor.
     */
    public ExtDirectRemoteMethod() {
        // Nothing.
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.remote.client.IRemoteMethodInjectable#setResponseClass(java.lang.Class)
     */
    @Override
    public void setResponseClass(Class<T> klass) {
        this.responseClass = klass;
    }


    /**
     * Determines from the response whether there has been a loss of session.
     * <p>
     * The default behaviour is a 403 or if the text body starts with
     * {@code "<!DOCTYPE"} (which looks like an HTML page).
     *
     * @param response
     *            the respone to check.
     * @return {@code true} if deemed a loss of session.
     */
    protected boolean isLossOfSession(Response response) {
        if (response.getStatusCode () == 403)
            return true;
        String text = response.getText ();
        if ((text != null) && text.startsWith ("<!DOCTYPE"))
            return true;
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.remote.client.IRemoteMethod#invoke(com.effacy.jui.remote.client.IRemoteMethodCallback,
     *      com.google.gwt.core.client.JsArray, java.lang.Object)
     */
    @Override
    public void invoke(IRemoteMethodCallback<T> callback, List<Object> args) {
        if (args.size () != argLength)
            callback.onTransportError ("Expected " + argLength + " arguments, got " + args.size());
        int tid = allocateTransactionId ();
        String csrfToken = Cookies.getCookie (CONFIG.csrfTokenCookie);
        ExtDirectRemoteCall callBean = new ExtDirectRemoteCall (this.action, this.method, tid, csrfToken, args.toArray ());
        try {
            send (new RequestExecution (tid, callBean, callback, ExtDirectRemoteMethodCallModifier.immediate));
        } finally {
            ExtDirectRemoteMethodCallModifier.reset ();
        }
    }


    /**
     * Flushes all of the registered methods.
     */
    public static void flushAll() {
        for (ExtDirectRemoteMethod<?> method : METHODS) {
            try {
                method.flush ();
            } catch (Throwable e) {
                Logger.reportUncaughtException (e);
            }
        }
    }


    /**
     * Flushes the current queue of messages.
     * 
     * @return the number of executions that were flushed.
     */
    protected int flush() {
        if (executionQueue.isEmpty ())
            return 0;
        int count = executionQueue.size ();
        List<IRequestExecution<T>> calls = new ArrayList<IRequestExecution<T>> (executionQueue);
        executionQueue.clear ();
        executionQueueOldestEntry = -1;
        sendImpl (calls, 0);
        return count;
    }


    /**
     * Sends a message.
     * 
     * @param call
     *            the execution call.
     */
    protected void send(IRequestExecution<T> call) {
        if (call == null)
            return;

        // The time since the first item was placed on the queue.
        long timeSinceFirst = (executionQueueOldestEntry < 0) ? 0 : (System.currentTimeMillis () - executionQueueOldestEntry);
        executionQueue.add (call);
        if (isLoggingQueue ()) {
            String message = (CONFIG.logTranslator == null) ? null : CONFIG.logTranslator.translate (call);
            if (message == null)
                message = Long.toString (call.getTid ());
            Logger.trace ("ExtDirectRemoteMethod", "send[" + executionQueue.size () + "," + timeSinceFirst + "ms](" + message + ")");
        }

        // Determine if we need to execute the queue.
        if (call.immediate () || (CONFIG.queueTimerWindow <= 0) || (executionQueue.size () >= CONFIG.queueMaxSize) || (timeSinceFirst >= CONFIG.queueMaxAge)) {
            List<IRequestExecution<T>> calls = new ArrayList<IRequestExecution<T>> (executionQueue);
            executionQueue.clear ();
            executionQueueOldestEntry = -1;
            if (isLoggingQueue ())
                Logger.trace ("ExtDirectRemoteMethod", "sending(" + calls.size () + " calls)");
            sendImpl (calls, 0);
            return;
        }

        // Record the time of the first time on the queue.
        if (executionQueue.size () == 1)
            executionQueueOldestEntry = System.currentTimeMillis ();

        // Start or reschedule the timer as need be.
        if (CONFIG.queueTimerMoving || !executionQueueTimer.isRunning ())
            executionQueueTimer.schedule (CONFIG.queueTimerWindow);
    }


    /**
     * Sends a message.
     * 
     * @param tid
     *            the transaction ID.
     * @param requestString
     *            the request string.
     * @param callback
     *            the final call-back handler.
     */
    protected void sendImpl(final List<IRequestExecution<T>> calls, final int retryCount) {
        if ((calls == null) || calls.isEmpty ())
            return;
        if (isLoggingSummary ()) {
            if (isLoggingFull ()) {
                if (calls.size () == 1) {
                    Logger.trace ("ExtDirectRemoteMethod", "-->(" + calls.get (0).getTid () + ") {retry:" + retryCount + "} " + calls.get (0).getRequest ());
                } else {
                    Logger.trace ("ExtDirectRemoteMethod", "-->(");
                    for (IRequestExecution<T> call : calls)
                        Logger.log ("   >>" + call.getTid () + ":" + retryCount + " -- " + call.getRequest ());
                    Logger.log (")");

                }
            } else
                Logger.trace ("ExtDirectRemoteMethod", "-->(" + tidList (calls) + ") {retry:" + retryCount + "}");
        }
        RequestBuilder builder = new RequestBuilder (RequestBuilder.POST, URL.encode (url));
        builder.setHeader ("Content-Type", "application/json; charset=UTF-8");
        builder.setHeader ("Accept", "application/json");
        if (CONFIG.requestContributor != null)
            CONFIG.requestContributor.accept (builder);
        final long sendTime = System.currentTimeMillis ();
        try {
            String callJson = "[";
            for (int i = 0, len = calls.size (); i < len; i++) {
                if (i > 0)
                    callJson += ",";
                callJson += calls.get (i).getRequest ();
            }
            callJson += "]";
            builder.sendRequest (callJson, new RequestCallback () {

                @Override
                public void onError(Request request, Throwable e) {
                    if (isLoggingSummary ()) {
                        long executionTime = System.currentTimeMillis () - sendTime;
                        Logger.error ("ExtDirectRemoteMethod: <--(" + tidList (calls) + ") [" + executionTime + "ms] Error on request", e);
                    }
                    for (IRequestExecution<T> call : calls) {
                        if (call.getCallback () != null)
                            call.getCallback ().onTransportError (e.getLocalizedMessage ());
                    }
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (config().uiVersionCheck != null) {
                        String uiVersion = response.getHeader("Ui-Version");
                        if (!StringSupport.empty (uiVersion) && config().uiVersionCheck.apply (uiVersion))
                            return;
                    }
                    int statusCode = isLossOfSession (response) ? 403 : response.getStatusCode ();
                    if (200 == statusCode) {
                        if (CONFIG.responseListener != null) {
                            try {
                                CONFIG.responseListener.on200 (tidList (calls));
                            } catch (Throwable e) {
                                // Don't worry about this.
                            }
                        }
                        try {
                            JSONArray rootResponse = JSONParser.parseStrict (response.getText ()).isArray ();
                            if (isLoggingSummary ()) {
                                long executionTime = System.currentTimeMillis () - sendTime;
                                Logger.trace ("ExtDirectRemoteMethod", "<--(" + tidList (calls) + ") [" + executionTime + "ms]");
                            } else if (isLoggingFull ()) {
                                long executionTime = System.currentTimeMillis () - sendTime;
                                Logger.trace ("ExtDirectRemoteMethod", "<--(" + tidList (calls) + ") [" + executionTime + "ms] " + rootResponse);
                            }
                            int i = 0;
                            for (IRequestExecution<T> call : calls) {
                                RemoteResponse<T> remoteResponse = buildResponse ((JSONObject) rootResponse.get (i++));
                                dispatchResponse (call.getCallback (), remoteResponse);
                            }
                            if (isLoggingSummary () || isLoggingFull ()) {
                                long executionTime = System.currentTimeMillis () - sendTime;
                                Logger.trace ("ExtDirectRemoteMethod", "^--(" + tidList (calls) + ") [" + executionTime + "ms] {dispatched}");
                            }
                        } catch (Throwable e) {
                            if (CONFIG.responseListener != null) {
                                try {
                                    CONFIG.responseListener.on200Exception (tidList (calls), e.getMessage ());
                                } catch (Throwable ex) {
                                    // Don't worry about this.
                                }
                            }
                            if (isLoggingSummary () || isLoggingFull ())
                                Logger.error ("ExtDirectRemoteMethod: <--(" + tidList (calls) + ") Parse error", e);
                            Logger.reportUncaughtException (e);
                            for (IRequestExecution<T> call : calls) {
                                try {
                                    if (call.getCallback () != null)
                                        call.getCallback ().onTransportError ("Unable to parse response from server (or some other error)");
                                } catch (Throwable ex) {
                                    // Don't worry about this.
                                }
                            }
                        }
                    } else if (403 == statusCode) {
                        if (CONFIG.responseListener != null) {
                            try {
                                CONFIG.responseListener.onLossOfSession (tidList (calls));
                            } catch (Throwable ex) {
                                // Don't worry about this.
                            }
                        }
                        if (isLoggingSummary () || isLoggingFull ())
                            Logger.trace ("ExtDirectRemoteMethod", "<--(" + tidList (calls) + ") Loss of session");
                        for (IRequestExecution<T> call : calls)
                            onLossOfSession (call);
                    } else if (504 == statusCode) {
                        if (retryCount >= CONFIG.MAX_RETRIES) {
                            if (CONFIG.responseListener != null) {
                                try {
                                    CONFIG.responseListener.onError (tidList (calls), response.getStatusCode (), response.getStatusText ());
                                } catch (Throwable ex) {
                                    // Don't worry about this.
                                }
                            }
                            if (isLoggingSummary () || isLoggingFull ())
                                Logger.trace ("ExtDirectRemoteMethod", "<--(" + tidList (calls) + ") Error (" + response.getStatusCode () + ") " + response.getStatusText ());
                            for (IRequestExecution<T> call : calls) {
                                if (call.getCallback () != null)
                                    call.getCallback ().onTransportError (response.getStatusText ());
                            }
                        } else {
                            if (isLoggingSummary () || isLoggingFull ())
                                Logger.trace ("ExtDirectRemoteMethod", "<--(" + tidList (calls) + ") Gateway timeout, retrying...");
                            sendImpl (calls, retryCount + 1);
                        }
                    } else {
                        if (CONFIG.responseListener != null) {
                            try {
                                CONFIG.responseListener.onError (tidList (calls), response.getStatusCode (), response.getStatusText ());
                            } catch (Throwable ex) {
                                // Don't worry about this.
                            }
                        }
                        if (isLoggingSummary () || isLoggingFull ())
                            Logger.trace ("ExtDirectRemoteMethod", "<--(" + tidList (calls) + ") Error (" + response.getStatusCode () + ") " + response.getStatusText ());
                        for (IRequestExecution<T> call : calls) {
                            try {
                                if (call.getCallback () != null)
                                    call.getCallback ().onTransportError (response.getStatusText ());
                            } catch (Throwable e) {
                                // Don't worry about this.
                            }
                        }
                    }
                }
            });
        } catch (RequestException e) {
            if (isLoggingSummary ())
                Logger.error ("ExtDirectRemoteMethod: Request exception", e);
        }
    }


    /**
     * Called when there is a loss of session (a 403 has been returned). The
     * default behaviour is to invoke a transport failure.
     * 
     * @param request
     *            the request that failed.
     * @param callback
     *            the call-back.
     */
    protected void onLossOfSession(IRequestExecution<T> call) {
        if (call.getCallback () != null)
            call.getCallback ().onCancel ();
    }


    /**
     * Creates a new transaction ID.
     * 
     * @return A number to use for a transaction ID.
     */
    protected static int allocateTransactionId() {
        return ++TID_COUNTER;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return url + " " + action + "::" + method;
    }


    /**
     * Builds a typed remote response from a corresponding JSON object. This
     * expects that the object is of type {@link RemoteResponse} and that the
     * underlying response type is that set against this class.
     * 
     * @param response
     *            JSON to build from.
     * @return Remote response.
     */
    @SuppressWarnings("unchecked")
    public RemoteResponse<T> buildResponse(JSONObject response) {
        try {
            JSONObject result = (JSONObject) response.get ("result");
            RemoteResponseType outcome = RemoteResponseType.valueOf (result.get ("outcome").isString ().stringValue ());
            T data = null;
            if (result != null) {
                if (this.responseClass != null)
                    data = (T) Serializer.getInstance ().deSerialize (result.get ("response"), responseClass);
                else
                    data = (T) Serializer.getInstance ().deSerialize (result.get ("response"));
            }
            List<ErrorMessage> messages = Serializer.getInstance ().deSerialize (result.get ("messages").isArray (), ErrorMessage.class);
            RemoteResponse<T> remoteResponse = new RemoteResponse<T> (outcome, messages, data);
            return remoteResponse;
        } catch (ClassCastException e) {
            // From experience this could mean a top-level exception. Easiest to process
            // this as an exception case rather than in the main flow (as it is truely an
            // exception case).
            String type = (response.get("type") == null) ? null : response.get ("type").toString ();
            if ((type != null) && type.contains ("exception")) {
                // This is an exception, so package up and return.
                String message = (response.get ("message") == null) ? null : response.get ("message").isString ().stringValue ();
                if (StringSupport.empty(message))
                    message = "Something went wrong (exception from server)";
                Logger.error ("Exception from server: \"" + message + "\"");
                List<ErrorMessage> messages = new ArrayList<>();
                messages.add (new ErrorMessage (message));
                return new RemoteResponse<T> (RemoteResponseType.ERROR_SYSTEM, messages, null);
            } else {
                // This implies something strange going on with the result, so we should log this.
                Logger.error ("ClassCastException when retrieving result from the RPC response:");
                DomGlobal.console.log (response);
                throw e;
            }
        }
    }


    /**
     * Determines if there is any logging enabled.
     * 
     * @return {@code true} if there is.
     */
    protected boolean isLoggingQueue() {
        return LogLevel.QUEUE.equals (CONFIG.logLevel) || LogLevel.SUMMARY.equals (CONFIG.logLevel) || LogLevel.FULL.equals (CONFIG.logLevel);
    }


    /**
     * Determines if logging is at the summary level.
     * 
     * @return {@code true} if it is.
     */
    protected boolean isLoggingSummary() {
        return LogLevel.SUMMARY.equals (CONFIG.logLevel) || LogLevel.SUMMARY_NOQUEUE.equals (CONFIG.logLevel);
    }


    /**
     * Determines if logging is at the full level.
     * 
     * @return {@code true} if it is.
     */
    protected boolean isLoggingFull() {
        return LogLevel.FULL.equals (CONFIG.logLevel) || LogLevel.FULL_NOQUEUE.equals (CONFIG.logLevel);
    }


    /**
     * Produces a comma separated list of TID's.
     * 
     * @param calls
     *            the calls to extract the TID's from.
     * @return the list of TIDs.
     */
    protected String tidList(List<IRequestExecution<T>> calls) {
        String list = "";
        for (int i = 0, len = calls.size (); i < len; i++) {
            if (i > 0)
                list += ",";
            list += calls.get (i).getTid ();
        }
        return list;
    }

    /**
     * Represents an execution context for a remote call.
     */
    public class RequestExecution implements IRequestExecution<T> {

        /**
         * The transaction ID for matching the response.
         */
        private long tid;

        /**
         * The body of the request to be sent.
         */
        private ExtDirectRemoteCall call;

        /**
         * The request to actually send.
         */
        private String request;

        /**
         * The callback to invoke when a response has been received.
         */
        private IRemoteMethodCallback<T> callback;

        /**
         * If the request should be processed immediately.
         */
        private boolean immediate;

        /**
         * Constructs an execution.
         * 
         * @param tid
         *            the TID for the execution.
         * @param call
         *            the call for the execution.
         * @param callback
         *            the callback for the execution.
         * @param immediate
         *            {@code true} if the request needs to be processed
         *            immediately and not batched.
         */
        public RequestExecution(long tid, ExtDirectRemoteCall call, IRemoteMethodCallback<T> callback, boolean immediate) {
            this.tid = tid;
            this.call = call;
            this.callback = callback;
            this.immediate = immediate;
        }


        /**
         * The call being wrapped.
         * 
         * @return the call.
         */
        public ExtDirectRemoteCall getCall() {
            return call;
        }


        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.remote.client.IRequestExecution#immediate()
         */
        @Override
        public boolean immediate() {
            return immediate;
        }


        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.remote.client.IRequestExecution#resend()
         */
        public void resend() {
            request = null;
            call.setCsrfToken (Cookies.getCookie (CONFIG.csrfTokenCookie));
            ExtDirectRemoteMethod.this.send (this);
        }


        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.remote.client.IRequestExecution#cancel()
         */
        public void cancel() {
            callback.onCancel ();
        }


        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.remote.client.IRequestExecution#getTid()
         */
        public long getTid() {
            return tid;
        }


        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.remote.client.IRequestExecution#getRequest()
         */
        public String getRequest() {
            if (request == null)
                request = Serializer.getInstance ().serializeToJson (call).toString ();
            return request;
        }


        public IRemoteMethodCallback<T> getCallback() {
            return callback;
        }


        /**
         * {@inheritDoc}
         *
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return call.toString ();
        }

    }

}
