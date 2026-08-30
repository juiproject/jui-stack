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
package com.effacy.jui.core.client.util;

import org.gwtproject.timer.client.Timer;

import com.effacy.jui.platform.util.client.Logger;

import elemental2.dom.Element;
import elemental2.dom.Node;
import jsinterop.base.Js;

/**
 * Orchestrates debounced, resilient autosaving of an editable surface.
 * <p>
 * The caller supplies a single {@link ISaveOperation} that knows how to persist the
 * current content (typically reading the live value and issuing a remote call), and
 * reports the outcome back via the {@link ICompletion} it is handed. The autosaver
 * takes care of <em>when</em> to save and <em>how</em> to recover:
 * <ul>
 * <li><b>Debounce</b> — {@link #change()} (re)arms a timer; the save fires only once
 *     activity has settled for {@link #delay(int)} milliseconds, so a burst of edits
 *     yields one save rather than one per keystroke. The wait is reported as
 *     {@link State#PENDING}, so a presentation can say there is unsaved work rather
 *     than going on claiming the last save through the whole settling window.</li>
 * <li><b>Flush</b> — {@link #flush()} saves immediately if there is unsaved content
 *     (for example on loss of focus). Wire it to a surface's own managed focus-loss hook
 *     where one exists (e.g. {@code FormattedTextEditor.Config.onFocusLost}); when the
 *     surface exposes only its DOM element, {@link #flushOnFocusLoss(Element)} is a raw
 *     (lifecycle-free) fallback.</li>
 * <li><b>Resilience</b> — a failed save does not surface a modal error and does not
 *     drop the edit: the state moves to {@link State#ERROR} and the save is retried on
 *     an exponential backoff ({@link #retry(int, int)}) until it succeeds, at which
 *     point the state briefly reports {@link State#RECOVERED}. Edits made while in
 *     error are folded into the next attempt.</li>
 * </ul>
 * The autosaver is presentation-agnostic: it reports lifecycle via {@link State} to an
 * {@link IStateListener}, and the caller renders that however it likes (e.g. a toolbar
 * label that turns red on {@link State#ERROR}).
 * <p>
 * A save operation is never run concurrently with itself: while one is in flight,
 * further triggers simply mark the content dirty and a follow-up save runs when the
 * in-flight one completes.
 */
public class Autosaver {

    /**
     * The autosave lifecycle state (reported to an {@link IStateListener}).
     */
    public enum State {
        /**
         * No unsaved content and no save in progress (the initial state).
         */
        IDLE,

        /**
         * There is unsaved content and a save has not started yet — the debounce is
         * settling, or a save already in flight will be followed by another.
         * <p>
         * Reported on every {@link #change()}. It exists because the window between
         * an edit and the save firing is real time in which the content is not
         * saved, and a presentation with no state for it can only carry on saying
         * whatever it last said — usually <i>Saved</i>, which is precisely wrong for
         * exactly as long as the window lasts.
         */
        PENDING,

        /**
         * A save is in progress.
         */
        SAVING,

        /**
         * The most recent save succeeded.
         */
        SAVED,

        /**
         * The most recent save failed; a retry is scheduled (the content is retained).
         */
        ERROR,

        /**
         * A save succeeded after one or more failures. Reported once on recovery (the
         * state then settles to {@link #SAVED}/{@link #SAVING} as normal).
         */
        RECOVERED;
    }

    /**
     * Reports the outcome of a {@link ISaveOperation} back to the autosaver. Exactly
     * one of {@link #success()} / {@link #failure()} must be invoked (once).
     */
    public interface ICompletion {

        /**
         * The save completed successfully.
         */
        void success();

        /**
         * The save failed (it will be retried).
         */
        void failure();
    }

    /**
     * Persists the current content. Invoked by the autosaver when a save is due; must
     * report the outcome via the supplied {@link ICompletion} (synchronously or, more
     * usually, asynchronously once a remote call returns).
     */
    @FunctionalInterface
    public interface ISaveOperation {

        /**
         * Performs the save, reporting the outcome via {@code completion}.
         *
         * @param completion
         *                   the outcome sink.
         */
        void save(ICompletion completion);
    }

    /**
     * Listens for autosave {@link State} transitions (for presentation).
     */
    @FunctionalInterface
    public interface IStateListener {

        /**
         * Invoked on each state transition.
         *
         * @param state
         *              the new state.
         */
        void onState(State state);
    }

    /**
     * See {@link #delay(int)}.
     */
    private int delay = 3000;

    /**
     * See {@link #retry(int, int)}.
     */
    private int retryInitial = 3000;

    /**
     * See {@link #retry(int, int)}.
     */
    private int retryMax = 30000;

    /**
     * The save operation.
     */
    private ISaveOperation operation;

    /**
     * The state listener (or {@code null}).
     */
    private IStateListener stateListener;

    /**
     * There is content changed since the last save was started.
     */
    private boolean dirty;

    /**
     * A save is currently in flight.
     */
    private boolean saving;

    /**
     * The current save sequence has failed at least once (drives {@link State#RECOVERED}).
     */
    private boolean failed;

    /**
     * The current (backoff) retry delay in ms.
     */
    private int retryDelay = 3000;

    /**
     * The current state.
     */
    private State state = State.IDLE;

    /**
     * Debounce timer (fires a save once edits settle).
     */
    private Timer debounceTimer = new Timer () {

        @Override
        public void run() {
            save ();
        }

    };

    /**
     * Retry timer (fires a save after a failure, on backoff).
     */
    private Timer retryTimer = new Timer () {

        @Override
        public void run() {
            save ();
        }

    };

    /**
     * Creates an autosaver around a save operation.
     *
     * @param operation
     *                  the operation that persists the current content.
     * @return the autosaver.
     */
    public static Autosaver create(ISaveOperation operation) {
        return new Autosaver (operation);
    }

    /**
     * Construct an autosaver around a save operation.
     *
     * @param operation
     *                  the operation that persists the current content.
     */
    public Autosaver(ISaveOperation operation) {
        this.operation = operation;
    }

    /**
     * Assigns the debounce delay: how long edit activity must settle before a save
     * fires. A burst of {@link #change()} calls within this window yields a single
     * save.
     *
     * @param ms
     *           the delay in ms (default 3000). Zero saves synchronously on change.
     * @return this autosaver instance.
     */
    public Autosaver delay(int ms) {
        this.delay = Math.max (0, ms);
        return this;
    }

    /**
     * Assigns the failure retry backoff. After a failed save the content is retried
     * after {@code initialMs}, doubling on each subsequent failure up to {@code maxMs},
     * indefinitely until a save succeeds.
     *
     * @param initialMs
     *                  the first retry delay in ms (default 3000).
     * @param maxMs
     *                  the maximum retry delay in ms (default 30000).
     * @return this autosaver instance.
     */
    public Autosaver retry(int initialMs, int maxMs) {
        this.retryInitial = Math.max (100, initialMs);
        this.retryMax = Math.max (this.retryInitial, maxMs);
        this.retryDelay = this.retryInitial;
        return this;
    }

    /**
     * Registers a listener for state transitions (for presentation).
     *
     * @param listener
     *                 the listener.
     * @return this autosaver instance.
     */
    public Autosaver onState(IStateListener listener) {
        this.stateListener = listener;
        return this;
    }

    /**
     * The current state.
     *
     * @return the state.
     */
    public State state() {
        return state;
    }

    /**
     * Whether there is unsaved content (changed, or a failed save awaiting retry).
     *
     * @return {@code true} if unsaved.
     */
    public boolean dirty() {
        return dirty;
    }

    /**
     * Notifies that the content changed. (Re)arms the debounce so a save fires once
     * activity settles.
     */
    public void change() {
        dirty = true;
        retryTimer.cancel ();
        debounceTimer.cancel ();
        // Before the save, not after: with no delay the save runs synchronously
        // below and reports SAVING, and a PENDING announced afterwards would leave
        // the presentation claiming an edit was waiting when it was already in
        // flight. Announced first, the two read in the order they happen.
        setState (State.PENDING);
        if (delay <= 0)
            save ();
        else
            debounceTimer.schedule (delay);
    }

    /**
     * Saves immediately if there is unsaved content (for example on loss of focus).
     * Any pending debounce is cancelled. A no-op when nothing is unsaved.
     */
    public void flush() {
        debounceTimer.cancel ();
        retryTimer.cancel ();
        if (dirty)
            save ();
    }

    /**
     * Wires {@link #flush()} to the loss of focus of an element: when focus leaves
     * {@code root} (and does not move to a descendant of it) a flush is triggered.
     * <p>
     * <b>Fallback only.</b> This attaches a raw DOM listener that is never removed, so
     * it sits outside the JUI component/event lifecycle (no managed disposal). Prefer a
     * component-managed focus-loss hook where the surface offers one — for example
     * {@code FormattedTextEditor.Config.onFocusLost(this::flush)} — and reach for this
     * only when the editing surface exposes no such hook (so all you have is its DOM
     * element). Attach at most once per element (there is no de-registration).
     *
     * @param root
     *             the element whose focus loss should flush (typically an editor's
     *             root); {@code null} is ignored.
     * @return this autosaver instance.
     */
    public Autosaver flushOnFocusLoss(Element root) {
        if (root == null)
            return this;
        root.addEventListener ("focusout", evt -> {
            elemental2.dom.FocusEvent fe = Js.uncheckedCast (evt);
            Node related = Js.uncheckedCast (fe.relatedTarget);
            // Ignore focus moving within the element (e.g. into a toolbar button).
            if ((related == null) || !root.contains (related))
                flush ();
        });
        return this;
    }

    /**
     * Runs the save operation (unless one is already in flight, or there is nothing to
     * save).
     */
    private void save() {
        if (saving)
            return;
        if (!dirty)
            return;
        saving = true;
        // Capture point: edits made from here re-set dirty and are saved on the next pass.
        dirty = false;
        setState (State.SAVING);
        try {
            operation.save (new ICompletion () {

                @Override
                public void success() {
                    onSuccess ();
                }

                @Override
                public void failure() {
                    onFailure ();
                }

            });
        } catch (Throwable e) {
            Logger.reportUncaughtException (e);
            onFailure ();
        }
    }

    /**
     * Handles a successful save.
     */
    private void onSuccess() {
        saving = false;
        boolean recovered = failed;
        failed = false;
        retryDelay = retryInitial;
        if (recovered)
            setState (State.RECOVERED);
        // Fold in any edits made during the save.
        if (dirty) {
            save ();
            return;
        }
        if (!recovered)
            setState (State.SAVED);
    }

    /**
     * Handles a failed save: retain the content and schedule a backoff retry.
     */
    private void onFailure() {
        saving = false;
        failed = true;
        // Keep the content pending so the retry (or a subsequent edit) re-saves it.
        dirty = true;
        setState (State.ERROR);
        retryTimer.cancel ();
        retryTimer.schedule (retryDelay);
        retryDelay = Math.min (retryMax, retryDelay * 2);
    }

    /**
     * Transitions state and notifies the listener.
     */
    private void setState(State state) {
        this.state = state;
        if (stateListener == null)
            return;
        try {
            stateListener.onState (state);
        } catch (Throwable e) {
            Logger.reportUncaughtException (e);
        }
    }
}
