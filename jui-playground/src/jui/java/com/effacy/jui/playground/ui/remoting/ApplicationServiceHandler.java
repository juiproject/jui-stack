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
package com.effacy.jui.playground.ui.remoting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.AbstractAction;

import com.effacy.jui.core.client.IClosable;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.rpc.client.ErrorMessage;
import com.effacy.jui.rpc.client.IRemoteMethodCallback;
import com.effacy.jui.rpc.client.RemoteMethodCallback;
import com.effacy.jui.rpc.client.RemoteResponseType;
import com.effacy.jui.rpc.handler.client.command.ICommand;
import com.effacy.jui.rpc.handler.client.query.Query;
import com.effacy.jui.ui.client.NotificationDialog;

public class ApplicationServiceHandler<V> {

    public interface ICompletionCallback {

        public void complete();
    }

    /**
     * Used to capture a more comprehensive flow of process.
     */
    public interface IProcessCallback<V> extends ICompletionCallback {

        public void before();

        public void fail(List<ErrorMessage> messages);

        public void success(V outcome);
    }

    public interface IExecutionCallback {

        /**
         * Called on successful completion.
         */
        public void success();

        /**
         * Called on cancellation.
         */
        public void cancel();

        /**
         * Called on when there is a failure.
         */
        public void fail();
    }

    public static class ConvertingProcessCallback<V, W> implements IProcessCallback<V> {

        private IProcessCallback<W> delegate;

        private Function<V, W> converter;

        public ConvertingProcessCallback(IProcessCallback<W> delegate) {
            this.delegate = delegate;
        }

        public ConvertingProcessCallback(IProcessCallback<W> delegate, Function<V, W> converter) {
            this.delegate = delegate;
            this.converter = converter;
        }

        @Override
        public void complete() {
            delegate.complete ();
        }

        @Override
        public void before() {
            delegate.before ();
        }

        @Override
        public void fail(List<ErrorMessage> messages) {
            delegate.fail (messages);
        }

        @Override
        public void success(V outcome) {
            delegate.success (convert (outcome));
        }

        protected W convert(V outcome) {
            if (converter != null)
                return converter.apply (outcome);
            return null;
        }

    }

    /**
     * Completion callback.
     */
    private ICompletionCallback completionCallback;

    /**
     * Method callback.
     */
    private IRemoteMethodCallback<V> methodCallback;

    /**
     * Process callback.
     */
    private IProcessCallback<V> processDelegate;

    /**
     * Invoked on success.
     */
    private Consumer<V> successHandler;

    /**
     * Invoked on a failure.
     */
    private BiConsumer<List<ErrorMessage>, RemoteResponseType> failHandler;

    /**
     * Invoked when execution has begun.
     */
    private Consumer<Void> beforeHandler;

    /**
     * Invoked after completion.
     */
    private Consumer<Boolean> completionHandler;


    /**
     * Register a before execution handler.
     * 
     * @param <A>
     * @param beforeHandler
     *                      the handler.
     * @return this instance.
     */
    @SuppressWarnings("unchecked")
    public <A extends ApplicationServiceHandler<V>> A onBefore(Consumer<Void> beforeHandler) {
        this.beforeHandler = beforeHandler;
        return (A) this;
    }

    /**
     * Register a success handler.
     * 
     * @param <A>
     * @param successHandler
     *                       the handler.
     * @return this instance.
     */
    @SuppressWarnings("unchecked")
    public <A extends ApplicationServiceHandler<V>> A onSuccessful(Consumer<V> successHandler) {
        this.successHandler = successHandler;
        return (A) this;
    }

    /**
     * Register a completion handler.
     * 
     * @param <A>
     * @param completionHandler
     *                          the handler.
     * @return this instance.
     */
    @SuppressWarnings("unchecked")
    public <A extends ApplicationServiceHandler<V>> A onCompletion(Consumer<Boolean> completionHandler) {
        this.completionHandler = completionHandler;
        return (A) this;
    }

    /**
     * Register a fail handler.
     * 
     * @param <A>
     * @param failHandler
     *                    the handler.
     * @return this instance.
     */
    @SuppressWarnings("unchecked")
    public <A extends ApplicationServiceHandler<V>> A onFailure(BiConsumer<List<ErrorMessage>, RemoteResponseType> failHandler) {
        this.failHandler = failHandler;
        return (A) this;
    }

    /**
     * Register a fail handler.
     * 
     * @param <A>
     * @param failHandler
     *                    the handler.
     * @return this instance.
     */
    @SuppressWarnings("unchecked")
    public <A extends ApplicationServiceHandler<V>> A closeOnFail(final IClosable closable) {
        this.failHandler = new BiConsumer<List<ErrorMessage>, RemoteResponseType> () {

            @Override
            public void accept(List<ErrorMessage> t, RemoteResponseType s) {
                closable.close ();
                NotificationDialog.error ("Sorry, a problem occurred", "We were not able to retrieve the record and could either be that it has since been removed, you no longer have the rights to access it or there simply was a problem.  Please try again later and if the problem persists contact customer support.", null);
            }

        };
        return (A) this;
    }

    /**
     * Register a method callback handler.
     * 
     * @param <A>
     * @param methodCallback
     *                       the handler.
     * @return this instance.
     */
    @SuppressWarnings("unchecked")
    public <A extends ApplicationServiceHandler<V>> A callback(IRemoteMethodCallback<V> methodCallback) {
        this.methodCallback = methodCallback;
        return (A) this;
    }

    /**
     * Register a process callback handler.
     * 
     * @param <A>
     * @param processDelegate
     *                        the handler.
     * @return this instance.
     */
    @SuppressWarnings("unchecked")
    public <A extends ApplicationServiceHandler<V>> A callback(IProcessCallback<V> processDelegate) {
        this.processDelegate = processDelegate;
        return (A) this;
    }

    /**
     * Remotely executes the passed commands.
     * 
     * @param commands
     *                 the commands to execute.
     */
    public void remoteExecute(ICommand... commands) {
        remoteExecute ((ICompletionCallback) null, (String) null, (Query<V>) null, commands);
    }

    /**
     * Remotely executes the passed commands.
     * 
     * @param commands
     *                 the commands to execute.
     */
    public void remoteExecute(String notification, ICommand... commands) {
        remoteExecute ((ICompletionCallback) null, notification, (Query<V>) null, commands);
    }

    /**
     * Remotely executes the passed commands.
     * 
     * @param query
     *                 the query to perform after executing the commands (may be
     *                 {@code null}).
     * @param commands
     *                 the commands to execute.
     */
    public void remoteExecute(Query<V> query, ICommand... commands) {
        remoteExecute ((ICompletionCallback) null, (String) null, query, commands);
    }

    /**
     * Remotely executes the passed commands.
     * 
     * @param query
     *                 the query to perform after executing the commands (may be
     *                 {@code null}).
     * @param commands
     *                 the commands to execute.
     */
    public <T> void remoteExecute(Query<T> query, Function<T, V> queryResultConverter, ICommand... commands) {
        remoteExecute ((ICompletionCallback) null, (String) null, query, queryResultConverter, commands);
    }

    /**
     * Remotely executes the passed commands.
     * 
     * @param query
     *                 the query to perform after executing the commands (may be
     *                 {@code null}).
     * @param commands
     *                 the commands to execute.
     */
    public void remoteExecute(String notification, Query<V> query, ICommand... commands) {
        remoteExecute ((ICompletionCallback) null, notification, query, commands);
    }

    /**
     * Remotely executes the passed commands.
     * 
     * @param query
     *                 the query to perform after executing the commands (may be
     *                 {@code null}).
     * @param commands
     *                 the commands to execute.
     */
    public <T> void remoteExecute(String notification, Query<T> query, Function<T, V> queryResultConverter, ICommand... commands) {
        remoteExecute ((ICompletionCallback) null, notification, query, queryResultConverter, commands);
    }

    /**
     * Remotely executes the passed commands with a call-back on completion.
     * 
     * @param cb
     *                 the call-back.
     * @param query
     *                 the query to perform after executing the commands (may be
     * @param commands
     *                 the commands to execute.
     */
    public void remoteExecute(ICompletionCallback cb, ICommand... commands) {
        remoteExecute (cb, (String) null, (Query<V>) null, commands);
    }

    /**
     * Remotely executes the passed commands with a call-back on completion.
     * 
     * @param cb
     *                 the call-back.
     * @param commands
     *                 the commands to execute.
     */
    public void remoteExecute(ICompletionCallback cb, String notification, Query<V> query, ICommand... commands) {
        remoteExecute (cb, notification, query, v -> v, commands);
    }

    /**
     * Remotely executes the passed commands with a call-back on completion.
     * 
     * @param cb
     *                 the call-back.
     * @param commands
     *                 the commands to execute.
     */
    public <T> void remoteExecute(ICompletionCallback cb, final String notification, Query<T> query, Function<T, V> queryResultConverter, ICommand... commands) {
        onBeforeExecute ();
        if (beforeHandler != null)
            beforeHandler.accept (null);
        if (processDelegate != null)
            processDelegate.before ();
        if (query == null) {
            ApplicationService.INSTANCE.execute (new LocalRemoteMethodCallback<Void> (cb) {

                @Override
                public void onSuccess() {
                    if (notification != null)
                        ApplicationServiceHandler.this.notify (notification);
                    ApplicationServiceHandler.this._success ((V) null);
                    ApplicationServiceHandler.this.onAfterExecute ();
                }

                @Override
                public void onSuccess(Void response, List<ErrorMessage> messages) {
                    if (methodCallback != null)
                        methodCallback.onSuccess (null, messages);
                    super.onSuccess (response, messages);
                }

                @Override
                public void onError(Void response, List<ErrorMessage> messages, RemoteResponseType status) {
                    if (methodCallback != null)
                        methodCallback.onError (null, messages, status);
                    if (!ApplicationServiceHandler.this._failure (messages, status))
                        super.onError (response, messages, status);
                    ApplicationServiceHandler.this.onAfterExecute ();
                }

                @Override
                public void onCancel() {
                    if (methodCallback != null)
                        methodCallback.onCancel ();
                    ApplicationServiceHandler.this.onCancel ();
                    ApplicationServiceHandler.this.onAfterExecute ();
                }

                @Override
                public void onTransportError(String message) {
                    if (methodCallback != null)
                        methodCallback.onTransportError (message);
                    super.onTransportError (message);
                    ApplicationServiceHandler.this.onAfterExecute ();

                }

            }, commands);
        } else {
            ApplicationService.INSTANCE.query (new LocalRemoteMethodCallback<T> (cb) {

                @Override
                public void onSuccess(T response, List<ErrorMessage> messages) {
                    if (methodCallback != null)
                        methodCallback.onSuccess (queryResultConverter.apply (response), messages);
                    if (notification != null)
                        ApplicationServiceHandler.this.notify (notification);
                    ApplicationServiceHandler.this._success (queryResultConverter.apply (response));
                    super.onSuccess (response, messages);
                    ApplicationServiceHandler.this.onAfterExecute ();
                }

                @Override
                public void onError(T response, List<ErrorMessage> messages, RemoteResponseType status) {
                    if (methodCallback != null)
                        methodCallback.onError (queryResultConverter.apply (response), messages, status);
                    if (!ApplicationServiceHandler.this._failure (messages, status))
                        super.onError (response, messages, status);
                    ApplicationServiceHandler.this.onAfterExecute ();
                }

                @Override
                public void onCancel() {
                    if (methodCallback != null)
                        methodCallback.onCancel ();
                    ApplicationServiceHandler.this.onCancel ();
                    ApplicationServiceHandler.this.onAfterExecute ();
                }

                @Override
                public void onTransportError(String message) {
                    if (methodCallback != null)
                        methodCallback.onTransportError (message);
                    super.onTransportError (message);
                    ApplicationServiceHandler.this.onAfterExecute ();

                }

            }, query, commands);
        }
    }

    /**
     * Display an error message then invoke {@link AbstractAction#onCancel()}.
     * 
     * @param title
     *                the title of the message.
     * @param message
     *                the message body.
     */
    protected void error(String title, String message) {
        NotificationDialog.error (title, message, t -> {
            ApplicationServiceHandler.this.onCancel ();
        });
    }

    /**
     * Displays a notification message (at the top of the page).
     * 
     * @param message
     *                the message to notify.
     */
    protected void notify(String message) {
        // TODO: Display a notifier.
    }

    /**
     * Internal.
     */
    protected void _success(V response) {
        if (successHandler != null)
            successHandler.accept (response);
        if (processDelegate != null)
            processDelegate.success (response);
        onSuccess (response);
    }

    /**
     * Called on success. Will perform any store update and then will call
     * {@link #onSuccess()}.
     */
    protected void onSuccess(V response) {
        onSuccess ();
    }

    /**
     * Called by {@link #onSuccess(Object)} when the response is not needed. By
     * default this handles the various post-success actions registered against the
     * action.
     */
    protected void onSuccess() {
        // Nothing.
    }

    /**
     * Internal.
     * 
     * @return {@code true} if the errors were handled.
     */
    protected boolean _failure(List<ErrorMessage> errors, RemoteResponseType status) {
        boolean handled = onFailure (errors, status);
        if (failHandler != null) {
            failHandler.accept (errors, status);
            handled = true;
        }
        return handled;
    }

    /**
     * Called when the action fails, passing the set of error messages. By default
     * this will call any registered completion callback.
     * 
     * @param errors
     *               the error messages.
     */
    protected boolean onFailure(List<ErrorMessage> errors, RemoteResponseType status) {
        if (completionCallback != null)
            completionCallback.complete ();
        if (processDelegate != null) {
            processDelegate.fail (errors);
            processDelegate.complete ();
            return true;
        }
        return false;
    }

    /**
     * Called when the action has been canceled (generally when the action is being
     * confirmed). By default this will call any registered completion callback.
     */
    protected void onCancel() {
        // Nothing.
    }

    /**
     * Invoked prior to starting the execution.
     */
    public void onBeforeExecute() {
        // Nothing.
    }

    /**
     * Invoked after the execution (and callbacks) have been invoked.
     */
    protected void onAfterExecute() {
        if (completionCallback != null)
            completionCallback.complete ();
        if (completionHandler != null)
            completionHandler.accept (false);
        if (processDelegate != null)
            processDelegate.complete ();
    }

    /**
     * A call-back that provides for automated error handling (messages and the
     * like).
     *
     * @author Jeremy Buckley
     */
    public static class LocalRemoteMethodCallback<T> extends RemoteMethodCallback<T> {

        private ICompletionCallback completionCallback;

        private IExecutionCallback executionCallback;

        public LocalRemoteMethodCallback() {
            super ();
        }

        public LocalRemoteMethodCallback(ICompletionCallback completionCallback) {
            super ();
            this.completionCallback = completionCallback;
        }

        public LocalRemoteMethodCallback(IExecutionCallback requestCallback) {
            super ();
            this.executionCallback = requestCallback;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.gwt.remote.client.RemoteMethodCallback#onTransportError(java.lang.String)
         */
        @Override
        public void onTransportError(String message) {
            NotificationDialog.error ("A problem occurred", "There was a problem communicating with the server; this could be a problem with the network.  Please try again later.", null);
            // MessageWindow.error ("A problem occurred", "There was a problem
            // communicating with the server; this could be a problem with the
            // network. Please try again later.");
            onComplete (false);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.gwt.remote.client.RemoteMethodCallback#onError(java.lang.Object,
         *      java.util.List, com.effacy.gwt.remote.response.RemoteResponseType)
         */
        @Override
        public void onError(T response, List<ErrorMessage> messages, RemoteResponseType status) {
            errorDialog (messages, status, () -> onComplete (false));
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.gwt.remote.client.RemoteMethodCallback#onValidationError(java.lang.Object,
         *      java.util.List)
         */
        @Override
        public void onValidationError(T response, List<ErrorMessage> messages) {
            super.onValidationError (response, messages);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.gwt.remote.client.RemoteMethodCallback#onSuccess(java.lang.Object,
         *      java.util.List)
         */
        @Override
        public void onSuccess(T response, List<ErrorMessage> messages) {
            onSuccess (response);
            super.onSuccess (response, messages);
        }

        protected void onSuccess(T response) {
            onSuccess ();
        }

        protected void onSuccess() {
            // Nothing.
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.gwt.remote.client.RemoteMethodCallback#onComplete(boolean)
         */
        @Override
        protected void onComplete(boolean success) {
            if (completionCallback != null)
                completionCallback.complete ();
            if (executionCallback != null) {
                if (success)
                    executionCallback.success ();
                else
                    executionCallback.fail ();
            }
            super.onComplete (success);
        }

    }

    /**
     * Convenience to display an error dialog formatting the passed messages and
     * interpreting the passed status in a consistent manner.
     * 
     * @param messages
     *                 the error messages.
     * @param status
     *                 the call status.
     * @param cb
     *                 (optional) a completion callback once the dialog has been
     *                 confirmed.
     */
    public static void errorDialog(List<ErrorMessage> messages, RemoteResponseType status, ICompletionCallback cb) {
        String title = "A problem occurred";
        String message = "Sorry, there was a problem performing your request. Please try again later.";
        if (status == RemoteResponseType.ERROR_ACCESS_RIGHTS) {
            title = "Invalid access rights";
            message = "Sorry, it appears you do not have sufficient access rights to do this.  Please contact you administrator.";
        }
        List<String> fieldMessages = new ArrayList<String> ();
        if (messages != null) {
            for (ErrorMessage fieldMessage : messages) {
                if (!StringSupport.empty (fieldMessage.getMessage ()))
                    fieldMessages.add (StringSupport.escape (fieldMessage.getMessage ()));
            }
        }
        if (fieldMessages.size () == 1) {
            message = fieldMessages.get (0);
        } else if (fieldMessages.size () > 1) {
            message = "Sorry, the following problems occurred:<ul>";
            for (String fieldMessage : fieldMessages)
                message += "<li>" + fieldMessage + "</li>";
            message += "</ul>";
        }
        NotificationDialog.error (title, message, t -> {
            if (cb != null)
                cb.complete ();
        });
    }
}
