package com.effacy.jui.ui.client.common;

import org.gwtproject.timer.client.Timer;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.util.Autosaver;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;

/**
 * The whole of the feedback for a surface that has no Save button.
 * <p>
 * An {@link Autosaver} decides <i>when</i> to save and <i>how</i> to recover; it
 * says nothing about how that reads. This is the other half. It is an
 * {@link Autosaver.IStateListener}, so it is wired with
 * {@code Autosaver.create(...).onState(status)} and placed wherever the surface's
 * chrome has room — conventionally at the far end of the editing toolbar, which is
 * where somebody looks when they want to know whether it is safe to leave.
 * <p>
 * <b>It is quiet, and it never interrupts.</b> A failed save is a red line here
 * and nothing else: the autosaver is already retrying and the content is not
 * lost, so a dialog would be asking the user to handle something that is being
 * handled.
 * <p>
 * <b>What it says has to keep being true.</b> Two consequences, and they are the
 * reason this is a component rather than a label the host writes into:
 * <ul>
 * <li>A save reports its <b>age</b>, and the age counts up — <i>just now</i>, then
 * <i>8s ago</i>, then <i>3m ago</i>. A label reading <i>just now</i> twenty minutes
 * later is answering the question actually being asked ("is what I typed safe?")
 * with a stale yes.
 * <li>An <b>edit</b> shows the moment it is made, off {@link Autosaver.State#PENDING}.
 * Between a keystroke and the debounce firing there is unsaved work, and a status
 * still reading <i>Saved</i> through that window is wrong for as long as the window
 * lasts.
 * </ul>
 * Out-of-band messages — a load in progress, a load that failed — go through
 * {@link #message(String)} / {@link #error(String)}, so a host has one place that
 * speaks about the content's safety rather than two.
 * <p>
 * <b>Sessions.</b> {@link #clear()} both blanks the label and forgets any save, so
 * the next edit reads as <i>Save pending</i> rather than qualifying itself against
 * a save belonging to whatever was open before. Call it when the surface is opened
 * or when a different record is loaded into it.
 */
public class SaveStatus extends SimpleComponent implements Autosaver.IStateListener {

    /**
     * How often the age is redrawn while it is still counting in seconds.
     * <p>
     * Every second was more than the label is worth: nobody reads a save age to the
     * second, and the difference is a timer waking five times as often behind a
     * line of text that changes by one word. On this period the seconds shown land
     * on multiples of it, which also reads as an estimate rather than a stopwatch.
     */
    private static final int TICK_MS = 5000;

    /**
     * How long a save reads as "just now" before it starts counting seconds.
     * <p>
     * The tick period, so that the first thing the clock does is replace it. A
     * shorter window could not be honoured anyway — nothing redraws in between —
     * and a longer one would leave "just now" standing after the age was known.
     */
    private static final long JUST_NOW_MS = TICK_MS;

    /** The current message, held for the case where it arrives before the render. */
    private String text = "";

    /** Whether the current message is a failure. */
    private boolean failure;

    /**
     * When the last successful save landed, or {@code 0} for none in this session.
     * It is what separates "Saved · pending" from "Save pending", and
     * {@link #clear()} resets it — which is what makes a session a session.
     */
    private long savedAt;

    /** Counts the age of the last save up; only running while that is on show. */
    private Timer ageTimer;

    private Element labelEl;

    public SaveStatus() {
        renderer (root -> {
            Span.$ (root).style ("label").by ("label");
        }, dom -> {
            labelEl = dom.first ("label");
            draw ();
        });
    }

    /************************************************************************
     * The autosave lifecycle.
     ************************************************************************/

    @Override
    public void onState(Autosaver.State state) {
        // Named for what is on screen rather than for what is happening: before the
        // first save of a session there is nothing to qualify, so it is the save
        // that is pending; after one, what is on screen IS saved and it is the
        // latest edit that is not.
        if (state == Autosaver.State.PENDING) {
            set ((savedAt > 0) ? "Saved · pending" : "Save pending", false);
            return;
        }
        if (state == Autosaver.State.SAVING) {
            set ("Saving…", false);
            return;
        }
        if (state == Autosaver.State.SAVED) {
            saved ();
            return;
        }
        // Says what is happening rather than what went wrong, because what is
        // happening is a retry the user need do nothing about.
        if (state == Autosaver.State.ERROR) {
            set ("Problem saving — retrying…", true);
            return;
        }
        // A save that succeeded after failing. Reported once and never followed by
        // a SAVED, so the age has to start here — the message stands for as long as
        // a fresh save reads as "just now" and then settles into the ordinary form.
        if (state == Autosaver.State.RECOVERED) {
            savedAt = System.currentTimeMillis ();
            set ("Save recovered", false);
            scheduleAge ();
            return;
        }
        set (null, false);
    }

    /************************************************************************
     * Host messages.
     ************************************************************************/

    /**
     * Shows an ordinary message — a load in progress, or a settled state the
     * autosaver has not reported (nothing has been edited yet).
     *
     * @param text
     *             the message ({@code null} shows nothing).
     */
    public void message(String text) {
        set (text, false);
    }

    /**
     * Shows a failure message.
     *
     * @param text
     *             the message.
     */
    public void error(String text) {
        set (text, true);
    }

    /**
     * Shows nothing, and starts a new session — so the next edit reads as "Save
     * pending" rather than claiming a save that belongs to a previous one.
     */
    public void clear() {
        savedAt = 0;
        set (null, false);
    }

    /************************************************************************
     * The age of the last save.
     ************************************************************************/

    /** Records a save and puts its age on show. */
    private void saved() {
        savedAt = System.currentTimeMillis ();
        set ("Saved · " + age (0), false);
        scheduleAge ();
    }

    /**
     * Redraws the age and schedules the next redraw.
     * <p>
     * Rescheduled each time rather than repeating on a fixed period: the label only
     * changes as fast as its own units do, so a minutes-old save does not need a
     * timer running at 1Hz behind it for the rest of the session.
     */
    private void scheduleAge() {
        stopAge ();
        long elapsed = System.currentTimeMillis () - savedAt;
        int next = TICK_MS;
        if (elapsed >= 3600000)
            next = 300000;
        else if (elapsed >= 60000)
            next = 30000;
        ageTimer = new Timer () {

            @Override
            public void run() {
                // label(), not set(): set() stops the clock, which is right for
                // every other route to the label and would be this one cancelling
                // itself. Anything else that writes the label has already stopped
                // us, so arriving here means the age is still what is on show.
                label ("Saved · " + age (System.currentTimeMillis () - savedAt), false);
                scheduleAge ();
            }

        };
        ageTimer.schedule (next);
    }

    private void stopAge() {
        if (ageTimer == null)
            return;
        ageTimer.cancel ();
        ageTimer = null;
    }

    /** The elapsed time, in the largest unit that is not yet a rounding error. */
    private static String age(long ms) {
        if (ms < JUST_NOW_MS)
            return "just now";
        long seconds = ms / 1000;
        if (seconds < 60)
            return seconds + "s ago";
        long minutes = seconds / 60;
        if (minutes < 60)
            return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24)
            return hours + "h ago";
        return (hours / 24) + "d ago";
    }

    @Override
    protected void onDispose() {
        stopAge ();
        super.onDispose ();
    }

    /************************************************************************
     * Rendering.
     ************************************************************************/

    /**
     * Writes the label, and stops any age clock: whatever is being said now is what
     * the label is about, and an age still counting behind it would overwrite it a
     * second later. Every route in but the clock's own goes through here.
     */
    private void set(String text, boolean failure) {
        stopAge ();
        label (text, failure);
    }

    /** Writes the label, leaving the age clock alone. */
    private void label(String text, boolean failure) {
        this.text = StringSupport.safe (text);
        this.failure = failure;
        draw ();
    }

    private void draw() {
        if (labelEl == null)
            return;
        labelEl.textContent = text;
        labelEl.classList.toggle ("error", failure);
        getRoot ().classList.toggle ("empty", StringSupport.empty (text));
    }

    /************************************************************************
     * CSS.
     ************************************************************************/

    @Override
    protected ILocalCSS styles() {
        return LocalCSS.instance ();
    }

    public static interface ILocalCSS extends IComponentCSS {}

    /*
     * The --jui-savestatus-* tokens are deliberately NOT declared on .component,
     * only read at the point of use with a fallback.
     *
     * A custom property declared ON an element beats a value inherited from any
     * ancestor, however specific that ancestor's selector. Declaring the defaults
     * here would therefore make them unoverridable from outside: a host setting
     * --jui-savestatus-size on the toolbar strip it sits in would be silently
     * ignored, and the only way in would be an inline style on this very element.
     * Read-with-fallback inherits, so any ancestor can retheme it — which is what a
     * component that lives inside somebody else's chrome needs.
     *
     * The fallbacks chain to the theme's role tokens, so an application that fills
     * those in gets its own colours without naming this component at all.
     */
    @CssResource(value = IComponentCSS.COMPONENT_CSS, stylesheet = """
.component {
    display: inline-flex;
    align-items: center;
}
.component .label {
    font-size: var(--jui-savestatus-size, 0.8125rem);
    color: var(--jui-savestatus-color, var(--jui-role-text-muted, #64748b));
    white-space: nowrap;
}
.component .label.error {
    color: var(--jui-savestatus-color-error, var(--jui-role-feedback-error, #c0334b));
    font-weight: var(--jui-savestatus-weight-error, 600);
}
""")
    public static abstract class LocalCSS implements ILocalCSS {

        private static LocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (LocalCSS) GWT.create (LocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

}
