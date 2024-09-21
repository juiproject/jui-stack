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
package com.effacy.jui.rpc.extdirect.client.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.rpc.client.ErrorMessage;
import com.effacy.jui.rpc.client.IRemoteMethodCallback;
import com.effacy.jui.rpc.client.RemoteMethodCallback;
import com.effacy.jui.rpc.client.RemoteResponseType;
import com.effacy.jui.rpc.extdirect.client.ExtDirectRemoteMethod;

/**
 * A base class for implementing service specific handling.
 * <p>
 * Accessing a service handler directly requires passing an approproate instance
 * of {@link IRemoteMethodCallback} that is able to respond to the various
 * outcomes that a remote call can generate (for example, error conditions).
 * Additional code will often be needed to alter the state of the UI during the
 * course of the remote call (i.e. showing a loading message). Many of these
 * behaviours can be standardised and optional callbacks used to invoke specific
 * behaviour, all these employ a builder-like mechanism. This is what this class
 * provides.
 * <p>
 * To use one must extend the class and provide implementations for the various
 * abstract methods. In addition one needs to provide explicit implementations
 * for delegating through to the underlying {@link IService}. These
 * implementations are expected to make a call to
 * {@link #startRemoteExecution(ICompletionCallback, String, Function)} to
 * obtain a suitable {@link IRemoteMethodCallback} (that is configured to
 * handler the response delegating through to the various handlers and
 * callbacks).
 */
public abstract class AbstractServiceHandler<V,Q extends AbstractServiceHandler<V,Q>> {

    /**
     * For notifying completion (of some task or activity).
     */
    @FunctionalInterface
    public interface ICompletionCallback {

        /**
         * Invoked on completion.
         */
        public void complete();
    }

    /**
     * Used to capture a more comprehensive flow of process.
     */
    public interface IProcessCallback<V> extends ICompletionCallback {

        public void before();

        public void fail(List<ErrorMessage> messages);

        public void success(V outcome);

        /**
         * Creates a {@link IProcessCallback} that converts from one type to another and
         * delegates the conversion.
         * 
         * @param <V>       the type to convert from.
         * @param <W>       the type to connvert to.
         * @param delegate
         *                  the delegate callback.
         * @param converter
         *                  type converter.
         * @return the converting callback.
         */
        public static <V,W> IProcessCallback<V> convert(IProcessCallback<W> delegate, Function<V, W> converter) {
            return new IProcessCallback<V> () {
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
                    W converted = null;
                    if (converter != null)
                        converted = converter.apply (outcome);
                    delegate.success (converted);
                }
            };
        }
    }

    /**
     * For notifying the outcome of a remote call.
     */
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
     * Flushes all accumulated calls.
     */
    public static void flush() {
        ExtDirectRemoteMethod.flushAll ();
    }

    /**
     * Register a before execution handler.
     * 
     * @param beforeHandler
     *                      the handler.
     * @return this instance.
     */
    @SuppressWarnings("unchecked")
    public Q onBefore(Consumer<Void> beforeHandler) {
        this.beforeHandler = beforeHandler;
        return (Q) this;
    }

    /**
     * Register a success handler.
     * 
     * @param successHandler
     *                       the handler.
     * @return this instance.
     */
    @SuppressWarnings("unchecked")
    public Q onSuccessful(Consumer<V> successHandler) {
        this.successHandler = successHandler;
        return (Q) this;
    }

    /**
     * Register a completion handler.
     * 
     * @param completionHandler
     *                          the handler.
     * @return this instance.
     */
    @SuppressWarnings("unchecked")
    public Q onCompletion(Consumer<Boolean> completionHandler) {
        this.completionHandler = completionHandler;
        return (Q) this;
    }

    /**
     * Register a fail handler.
     * 
     * @param failHandler
     *                    the handler.
     * @return this instance.
     */
    @SuppressWarnings("unchecked")
    public Q onFailure(BiConsumer<List<ErrorMessage>, RemoteResponseType> failHandler) {
        this.failHandler = failHandler;
        return (Q) this;
    }


    /**
     * Register a method callback handler.
     * 
     * @param methodCallback
     *                       the handler.
     * @return this instance.
     */
    @SuppressWarnings("unchecked")
    public Q callback(IRemoteMethodCallback<V> methodCallback) {
        this.methodCallback = methodCallback;
        return (Q) this;
    }

    /**
     * Register a process callback handler.
     * 
     * @param processDelegate
     *                        the handler.
     * @return this instance.
     */
    @SuppressWarnings("unchecked")
    public Q callback(IProcessCallback<V> processDelegate) {
        this.processDelegate = processDelegate;
        return (Q) this;
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
        errors = normaliseErrorMessages (errors, status);
        boolean handled = onFailure (errors, status);
        if (failHandler != null) {
            failHandler.accept (errors, status);
            handled = true;
        }
        return handled;
    }

    /**
     * Normalisaes the error messages as recieved from
     * {@link #_failure(List, RemoteResponseType)}.
     * <p>
     * The default implementation just ensures the errors list is non-null. You can
     * override as you see fit (for example, to create error messages based on the
     * status).
     * 
     * @param errors
     *               the errors received and to be normalised.
     * @param status
     *               the associated status.
     * @return the normalised messages.
     */
    protected List<ErrorMessage> normaliseErrorMessages(List<ErrorMessage> errors, RemoteResponseType status) {
        if (errors == null)
            return new ArrayList<>();
        return errors;
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
    protected void onAfterExecute(boolean success) {
        if (completionCallback != null)
            completionCallback.complete ();
        if (completionHandler != null)
            completionHandler.accept (success);
        if (processDelegate != null)
            processDelegate.complete ();
    }


    /**
     * Crteates an instance of {@link IRemoteMethodCallback} that can be used in a
     * remote query.
     * 
     * @param <R>
     *                             the query return type.
     * @param completionCallback
     *                             to be called on completion.
     * @param notification
     *                             (optional) a message to display.
     * @param queryResultConverter
     *                             to convert from the query return type to the
     *                             value type.
     * @return the remote method callback instance.
     */
    protected <R> IRemoteMethodCallback<R> startRemoteExecution(ICompletionCallback completionCallback, String notification, Function<R, V> queryResultConverter) {
        onBeforeExecute ();
        if (beforeHandler != null)
            beforeHandler.accept (null);
        if (processDelegate != null)
            processDelegate.before ();
        return new LocalRemoteMethodCallback<R> (completionCallback, notification, queryResultConverter);
    }

    /**
     * A call-back that provides for automated error handling (messages and the
     * like).
     *
     * @author Jeremy Buckley
     */
    public class LocalRemoteMethodCallback<T> extends RemoteMethodCallback<T> {

        private ICompletionCallback completionCallback;

        private String notification;

        private Function<T, V> queryResultConverter;

        public LocalRemoteMethodCallback() {
            super ();
        }

        public LocalRemoteMethodCallback(ICompletionCallback completionCallback, String notification, Function<T, V> queryResultConverter) {
            super ();
            this.completionCallback = completionCallback;
            this.notification = notification;
            this.queryResultConverter = queryResultConverter;
        }

        @Override
        public void onTransportError(String message) {
            if (methodCallback != null)
                methodCallback.onTransportError (message);
            notifyError ("A problem occurred", "There was a problem communicating with the server; this could be a problem with the network.  Please try again later.", null);
            onComplete (false);
            AbstractServiceHandler.this.onAfterExecute (false);
        }

        @Override
        public void onCancel() {
            if (methodCallback != null)
                methodCallback.onCancel ();
            AbstractServiceHandler.this.onCancel ();
            AbstractServiceHandler.this.onAfterExecute (false);
        }

        @Override
        public void onError(T response, List<ErrorMessage> messages, RemoteResponseType status) {
            if (methodCallback != null)
                methodCallback.onError (queryResultConverter.apply (response), messages, status);
            if (!AbstractServiceHandler.this._failure (messages, status))
                notifyError (messages, status, () -> onComplete (false));
                AbstractServiceHandler.this.onAfterExecute (false);
        }

        @Override
        public void onSuccess(T response, List<ErrorMessage> messages) {
            if (methodCallback != null)
                methodCallback.onSuccess (queryResultConverter.apply (response), messages);
            if (notification != null)
                AbstractServiceHandler.this.notifyAction (notification);
            if (queryResultConverter != null)
                AbstractServiceHandler.this._success (queryResultConverter.apply (response));
            else
                AbstractServiceHandler.this._success ((V) response);
            super.onSuccess (response, messages);
            AbstractServiceHandler.this.onAfterExecute (true);
        }

        @Override
        protected void onComplete(boolean success) {
            if (completionCallback != null)
                completionCallback.complete ();
            super.onComplete (success);
        }

    }

    /**
     * Notify on error.
     * 
     * @param messages
     *                 the messages passed back.
     * @param status
     *                 the error status.
     * @param cb
     *                 callback to continue.
     */
    protected void notifyError(List<ErrorMessage> messages, RemoteResponseType status, ICompletionCallback cb) {
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
                message += "<li>" + StringSupport.escape (fieldMessage) + "</li>";
            message += "</ul>";
        }
        notifyError (title, message, cb);
    }

    /**
     * Displays an error message (i.e. in a dialog).
     * 
     * @param title
     *                the title of the message (if relevant).
     * @param content
     *                the error message itself (this may contain HTML markup).
     * @param cb
     *                (optional) a callback once the message has been confirmed (if
     *                relevant, but if present it must be invoked).
     */
    protected abstract void notifyError(String title, String content, ICompletionCallback cb);



    /**
     * Displays a notification message (i.e. at the top of the page).
     * 
     * @param message
     *                the message to notify.
     */
    protected abstract void notifyAction(String message);

}
