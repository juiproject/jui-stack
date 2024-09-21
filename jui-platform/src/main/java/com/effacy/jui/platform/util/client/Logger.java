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
package com.effacy.jui.platform.util.client;

import java.util.function.BiConsumer;

import com.google.gwt.core.client.GWT;

/**
 * Application logging framework.
 */
public class Logger {

    /**
     * Handler for uncaught exceptions.
     */
    private static BiConsumer<Throwable,String> UNCAUGHT_EXCEPTION_HANDLER;

    /**
     * Determines if info level logging should be written.
     */
    public static boolean LOG_INFO = true;

    /**
     * Determines if warning level logging should be written.
     */
    public static boolean LOG_WARN = true;

    /**
     * Determines if error level logging should be written.
     */
    public static boolean LOG_ERROR = true;

    /**
     * Determines if trace level logging should be written.
     */
    public static boolean LOG_TRACE = true;

    /**
     * Determines if event level logging should be written.
     */
    public static boolean LOG_EVENT = true;

    /**
     * Registars a handler for uncaught exceptions. Assigning one will override the
     * default behaviour (which logs to the console).
     * <p>
     * The handler is passed the exception and a string that represents some
     * additional context that may be useful.
     * 
     * @param handler
     *                the handler.
     */
    public static void registerUncaughtExceptionHandler(BiConsumer<Throwable,String> handler) {
        UNCAUGHT_EXCEPTION_HANDLER = handler;
    }

    /**
     * Handle an uncaught exception.
     * 
     * @param e
     *          the exception.
     */
    public static void reportUncaughtException(Throwable e) {
        reportUncaughtException (e, null);
    }

    /**
     * Handle an uncaught exception.
     * 
     * @param e
     *                 the exception.
     * @param reporter
     *                 the object that is reporting the exception.
     */
    public static void reportUncaughtException(Throwable e, Object reporter) {
        if (UNCAUGHT_EXCEPTION_HANDLER != null) {
            String context = (reporter != null) ? reporter.getClass ().getName () : null;
            UNCAUGHT_EXCEPTION_HANDLER.accept (e, context);
        } else {
            if (reporter != null)
                error (reporter.getClass ().getName () + " reported an exception");
            error ("Uncaught exception", e);
        }
    }

    /**
     * Perform a standard console log (with no formatting and level).
     * 
     * @param message
     *                the message to log.
     */
    public static void log(String message) {
        console (message);
    }

    /**
     * See {@link #error(String, Throwable)}.
     */
    @Deprecated
    public static void log(String message, Throwable e) {
        error (message, e);
    }

    /**
     * Log an info message.
     * <p>
     * Note that if {@link Logger#LOG_INFO} is {@code false} then the message will
     * not be logged.
     * 
     * @param message
     *                the message.
     */
    public static void info(String message) {
        if (!LOG_INFO)
            return;
        console ("[INFO]", "blue", message);
    }

    /**
     * Log a warning message.
     * <p>
     * Note that if {@link Logger#LOG_WARN} is {@code false} then the message will
     * 
     * @param message
     *                the message.
     */
    public static void warn(String message) {
        console ("[WARN]", "orange", message);
        if (!LOG_WARN)
            return;
    }

    /**
     * Log an error message.
     * <p>
     * Note that if {@link Logger#LOG_ERROR} is {@code false} then the message will
     * 
     * @param message
     *                the message.
     */
    public static void error(String message) {
        if (!LOG_ERROR)
            return;
        console ("[ERROR]", "red", message);
    }

    /**
     * Log an error message.
     * <p>
     * Note that if {@link Logger#LOG_ERROR} is {@code false} then the message will
     * 
     * @param message
     *                the message.
     * @param e
     *                any throwable to include.
     */
    public static void error(String message, Throwable e) {
        if (!LOG_ERROR)
            return;
        console ("[ERROR]", "red", message);
        if (e != null)
            console (e);
    }

    /**
     * Log a trace message.
     * <p>
     * Note that if {@link Logger#LOG_TRACE} is {@code false} then the message will
     * 
     * @param message
     *                the message.
     */
    public static void trace(String prefix, String message) {
        if (!LOG_TRACE)
            return;
        console (prefix, "green", message);
    }

    /**
     * Log a event message.
     * <p>
     * Note that if {@link Logger#LOG_EVENT} is {@code false} then the message will
     * 
     * @param message
     *                the message.
     */
    public static void event(String message) {
        if (!LOG_TRACE)
            return;
        console ("[EVENT]", "purple", message);
    }

    /**
     * Internal console log implementation.
     */
    protected static native void console(String msg, Throwable e) /*-{
        $wnd.console.error (msg, e.stack);
    }-*/;

    /**
     * Internal console log implementation.
     */
    protected static native void console(String msg) /*-{
        $wnd.console.log (msg);
    }-*/;

    /**
     * Internal console log implementation.
     */
    protected static native void console(String prefix, String color, String msg) /*-{
        $wnd.console.log ("%c" + prefix, "color: " + color, msg);
    }-*/;

    /**
     * Internal console log implementation.
     */
    protected static void console(Throwable e) {
        // Take advantage of writing out a stack trace (which GWT.log does a good job
        // of).
        GWT.log (null, e);
    }
}
