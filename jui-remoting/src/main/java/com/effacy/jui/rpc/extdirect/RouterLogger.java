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
package com.effacy.jui.rpc.extdirect;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.support.TransactionTemplate;

import com.effacy.jui.rpc.handler.exception.NoProcessorException;
import com.effacy.jui.rpc.handler.exception.ProcessorException;

public class RouterLogger implements IRouterLogger {

    private static Logger LOG = LoggerFactory.getLogger (RouterLogger.class);

    private static boolean DISABLED = true;

    private static ThreadLocal<RouterLogger> LOGGER = new ThreadLocal<RouterLogger> ();

    private static RouterLogger getLogger() {
        if (LOGGER.get () == null)
            LOGGER.set (new RouterLogger ());
        return LOGGER.get ();
    }

    class Message {

        private int depth;

        private String message;

        public Message(int depth, String message) {
            this.depth = depth;
            this.message = message;
        }


        public void print(PrintWriter pw) {
            for (int i = 0; i < depth; i++)
                pw.print (" ");
            pw.println (message);
        }
    }

    private List<Message> messages = new ArrayList<Message> ();

    private int depth = 0;

    private boolean alert = false;

    private void _log(String message) {
        messages.add (new Message (depth, message));
    }


    private void _indent() {
        depth++;
    }


    private void _outdent() {
        depth--;
    }


    private void _alert() {
        alert = true;
    }


    private void _clear() {
        messages.clear ();
        depth = 0;
        alert = false;
    }


    private void _log() {
        if (!messages.isEmpty ()) {
            StringWriter sw = new StringWriter ();
            PrintWriter pw = new PrintWriter (sw);
            if (alert)
                pw.print ("ALERT!!!");
            pw.println ("[" + Thread.currentThread ().getId () + ":" + Thread.currentThread ().getName () + "]");
            for (Message msg : messages)
                msg.print (pw);
            pw.print ("<<<");
            pw.flush ();
            LOG.error (sw.toString ());
        }
    }


    public static void log(String message) {
        if (DISABLED)
            return;
        getLogger ()._log (message);
    }


    public static void indent() {
        if (DISABLED)
            return;
        getLogger ()._indent ();
    }


    public static void indent(String message) {
        if (DISABLED)
            return;
        getLogger ()._indent ();
        getLogger ()._log (message);
    }


    public static void outdent() {
        if (DISABLED)
            return;
        getLogger ()._outdent ();
    }


    /**
     * Safely extracts the name from an enum.
     * 
     * @param value
     *            the enum to extract the name from.
     * @return the name or <code>"null"</code> if {@code null}.
     */
    public static <T extends Enum<T>> String name(T value) {
        if (value == null)
            return "null";
        return value.name ();
    }


    public static String format(TransactionStatus status) {
        if (status == null)
            return "null";
        StringBuffer sb = new StringBuffer ();
        sb.append ("{");
        sb.append ("new_tx=");
        sb.append (status.isNewTransaction ());
        sb.append (",rollbackOnly=");
        sb.append (status.isRollbackOnly ());
        sb.append (",completed=");
        sb.append (status.isCompleted ());
        sb.append (",save_point=");
        sb.append (status.hasSavepoint ());
        sb.append (",id=");
        sb.append (status.getClass ().getSimpleName ());
        sb.append (":");
        sb.append (System.identityHashCode (status));
        sb.append ("}");
        return sb.toString ();
    }


    public static String format(TransactionTemplate template) {
        if (template == null)
            return "null";
        StringBuffer sb = new StringBuffer ();
        sb.append ("{");
        sb.append ("name=");
        sb.append (template.getName ());
        sb.append (",iso_level=");
        sb.append (template.getIsolationLevel ());
        sb.append (",prop_behav=");
        sb.append (template.getPropagationBehavior ());
        sb.append (",tx_manager=");
        sb.append (format (template.getTransactionManager ()));
        return sb.toString ();
    }


    public static String format(PlatformTransactionManager tx) {
        if (tx == null)
            return "null";
        StringBuffer sb = new StringBuffer ();
        sb.append ("{");
        sb.append ("id=");
        sb.append (tx.getClass ().getSimpleName ());
        sb.append (":");
        sb.append (System.identityHashCode (tx));
        sb.append ("}");
        sb.append ("}");
        return sb.toString ();
    }


    public static String format(TransactionAttribute attr) {
        if (attr == null)
            return "null";
        StringBuffer sb = new StringBuffer ();
        sb.append ("{id=");
        sb.append (attr.getClass ().getSimpleName ());
        sb.append (":");
        sb.append (System.identityHashCode (attr));
        sb.append (",name=");
        sb.append (attr.getName ());
        sb.append (",iso_lev=");
        sb.append (attr.getIsolationLevel ());
        sb.append (",prop_behav=");
        sb.append (attr.getPropagationBehavior ());
        sb.append (",qualifier=");
        sb.append (attr.getQualifier ());
        sb.append ("}");
        return sb.toString ();
    }


    /**
     * Registers an uncaught (unchecked) throwable.
     * 
     * @param e
     *            the exception.
     */
    public static void uncaught(Throwable e) {
        LOG.error ("Uncaught exception from router: ", e);
    }


    /**
     * Records that an exception was thrown.
     * 
     * @param e
     *            the exception.
     */
    public static void exception(Throwable e) {
        // We need to alert when there is a no processor, for processor
        // exceptions these are part of the application flow so don't need to be
        // registered, all others indicate a problem.
        if (e instanceof NoProcessorException)
            LOG.error (e.getMessage ());
        else if (!(e instanceof ProcessorException))
            LOG.error ("Exception from processing: ", e);
    }


    public static void alert() {
        if (DISABLED)
            return;
        getLogger ()._alert ();
    }


    /**
     * Clears the messages in the logger.
     */
    static void clear() {
        getLogger ()._clear ();
    }


    static void log() {
        if (DISABLED)
            return;
        getLogger ()._log ();
    }


    public static void disable() {
        DISABLED = true;
    }


    public static void enable() {
        DISABLED = false;
    }


    public static void performance(long time) {
        // Nothing.
    }
}
