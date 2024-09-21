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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.effacy.jui.rpc.client.IRemoteMethod;
import com.effacy.jui.rpc.client.IRemoteMethodFactory;
import com.effacy.jui.rpc.client.IRequestExecution;

import elemental2.dom.DomGlobal;

/**
 * A variant of {@link ExtDirectRemoteMethod} that presents a login dialog to
 * attempt to login after a loss of session.
 * <p>
 * Multiple remote calls through the same remoting URL will not result in
 * multiple dialogs. Once a dialog has been presented all further remote requsts
 * that have failed are queued and processed only when the dialog is actioned.
 * 
 * @author Jeremy Buckley
 */
public class LoginExtDirectRemoteMethod<T> extends ExtDirectRemoteMethod<T> {

    /**
     * Constructs a factory using an optional configuration.
     * 
     * @param configuration
     *                      the configuration to use.
     * @return the factory.
     */
    public static IRemoteMethodFactory factory(final IConfiguration configuration) {
        return new IRemoteMethodFactory () {

            @Override
            public <T> IRemoteMethod<T> create(String url, String action, String method, int argLength) {
                Handler handler = HANDLERS.get (url);
                if (handler == null) {
                    handler = new Handler (url, configuration);
                    HANDLERS.put (url, handler);
                }
                return new LoginExtDirectRemoteMethod<T> (handler, action, method, argLength);
            }

        };
    }

    /**
     * Handlers mapped by URL (one per URL).
     */
    private static Map<String, Handler> HANDLERS = new HashMap<String, Handler> ();

    /**
     * The handler to use for login attempts.
     */
    private Handler handler;

    /**
     * Construct with reference data to the remote service router.
     * 
     * @param url
     *               the URL of the service router.
     * @param action
     *               the action being referenced.
     * @param method
     *               the method of the action to be invoked.
     */
    public LoginExtDirectRemoteMethod(Handler handler, String action, String method, int argLength) {
        super (handler.loginUrl (), action, method, argLength);
        this.handler = handler;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.remote.client.ExtDirectRemoteMethod#onLossOfSession(long,
     *      java.lang.String, com.effacy.jui.remote.client.IRemoteMethodCallback)
     */
    @Override
    protected void onLossOfSession(IRequestExecution<T> execution) {
        handler.onLossOfSession (execution);
    }

    /**
     * External configuration that can be applied to the login.
     */
    public interface IConfiguration {

        /**
         * The URL to direct to upon cancellation (unsuccessful login).
         * 
         * @return the URL.
         */
        public String cancelUrl();

        /**
         * The user login to populate into the form.
         *
         * @return the user's login name.
         */
        public String userName();

        /**
         * The user display name to display in the form.
         * 
         * @return the user's actual name
         */
        public String userDisplayName();

        /**
         * Shows the login window as a hook.
         * 
         * @param loginUrl
         *                 the URL to use to pass through login information.
         * @param cb
         *                 the window callback.
         * @return {@code true} if the window was shown.
         */
        public boolean showLoginWindow(String loginUrl, ILoginWindowCallback cb);
    }

    /**
     * Login window callback to inform the call of the outcome.
     */
    @FunctionalInterface
    public interface ILoginWindowCallback {

        /**
         * Invoked when the login window is closed.
         * 
         * @param success
         *                if the login was successful.
         */
        public void close(boolean success);
    }

    /**
     * Handles the presentation of a login dialog and processing of the result.
     * Multiple remote executions that fail as a result of session loss are simply
     * queued until the presented dialog processes correctly. Then all outstanding
     * requests are processed at once.
     */
    private static class Handler {

        /**
         * The login url.
         */
        private String loginUrl;

        /**
         * The URL to redirect to if the login is cancelled.
         */
        private IConfiguration configration;

        /**
         * If a dialog is showing.
         */
        private boolean dialogShowing = false;

        /**
         * List of executions to process.
         */
        private List<IRequestExecution<?>> executionList = new ArrayList<IRequestExecution<?>> ();

        /**
         * Constructs the handler against the given URL.
         * 
         * @param url
         *                  the remoting url.
         * @param cancelUrl
         *                  the url to direct to on cancel.
         */
        public Handler(String loginUrl, IConfiguration configration) {
            this.loginUrl = loginUrl;
            this.configration = configration;
        }

        /**
         * Obtains the url used to execute the login. This is just used to scope
         * handlers.
         * 
         * @return the url.
         */
        public String loginUrl() {
            return loginUrl;
        }

        /**
         * Called when there is a loss of session for an execution.
         * 
         * @param execution
         *                  the execution that could not be processed as a result of a
         *                  loss of session.
         */
        public void onLossOfSession(IRequestExecution<?> execution) {
            synchronized (executionList) {
                executionList.add (execution);
            }
            if (!dialogShowing) {
                dialogShowing = true;
                configration.showLoginWindow (loginUrl, new ILoginWindowCallback () {

                    @Override
                    public void close(boolean success) {
                        List<IRequestExecution<?>> executions = new ArrayList<IRequestExecution<?>> ();
                        synchronized (executionList) {
                            executions.addAll (executionList);
                            executionList.clear ();
                        }
                        for (IRequestExecution<?> execution : executions) {
                            if (success)
                                execution.resend ();
                            else
                                execution.cancel ();
                        }
                        if (!success && (configration.cancelUrl () != null))
                            DomGlobal.window.open (configration.cancelUrl (), "_self", "");
                        dialogShowing = false;
                    }

                });
            }
        }

    }
}
