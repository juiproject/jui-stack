package com.effacy.jui.core.client.control;

import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.effacy.jui.platform.util.client.TimerSupport.ITimer;

public class DelayedActionHandler {

    /**
     * See {@link #threshold(int)}.
     */
    protected int threshold = 300;

    /**
     * See {@link #maxCount(int)}.
     */
    protected int maxCount = 6;

    /**
     * Internal count of un-fired invocations.
     */
    private int count = 0;

    /**
     * The fire delay timer.
     */
    protected ITimer timer = TimerSupport.timer(() -> fire ());

    /**
     * Receives the value.
     */
    private Invoker handler;

    /**
     * When primed the handler will pass through directly on the first modification
     */
    private boolean prime;

    /**
     * Construct with what will handle the event.
     * 
     * @param handler
     *                 the handler to invoke.
     */
    public DelayedActionHandler(Invoker handler) {
        this.handler = handler;
    }

    /**
     * Assigns the threshold time period to wait after a modification event before
     * firing.
     * <p>
     * Cannot be less than 10ms.
     * 
     * @param threshold
     *                  the time delay in ms (default is 300).
     * @return this handler instance.
     */
    public DelayedActionHandler threshold(int threshold) {
        this.threshold = Math.max (10, threshold);
        return this;
    }

    /**
     * The maximum number of modification events to wait for before firing.
     * <p>
     * Cannot be less than 1 or greater than 10.
     * 
     * @param maxCount
     *                 the number of events (default is 6).
     * @return this handler instance.
     */
    public DelayedActionHandler maxCount(int maxCount) {
        this.maxCount = Math.max (1, Math.min (10, maxCount));
        return this;
    }

    /**
     * Fires a receiver event.
     */
    protected void fire() {
        try {
            handler.invoke ();
        } catch (Throwable e) {
            Logger.reportUncaughtException (e);
        }
        count = 0;
    }

    /**
     * Modifies the value.
     * 
     * @param value
     *              the value.
     */
    public void modified() {
        // If primed then we act immediately.
        if (prime) {
            this.prime = false;
            fire();
            return;
        }

        // Cancel what is in progress and assign the revised value.
        timer.cancel ();
        
        // Check if we have exceeded our update threshold. If not, then re-schedule.
        if (count++ >= maxCount)
            fire ();
        else
            timer.schedule (threshold);
    }

    /**
     * Resets the handler clearing the current count and timer.
     * <p>
     * Note that priming allows one to immediately respond to the first modification
     * event which can improve user experience by not inducing an obvious delay on
     * start.
     * 
     * @param prime
     *              {@code true} to prime the handler to fire immediately on the
     *              modification event prior to going into delayed mode.
     */
    public void reset(boolean prime) {
        reset();
        this.prime = prime;
    }

    /**
     * Resets the handler clearing the current count, timer and sets the value to
     * {@code null}.
     */
    public void reset() {
        count = 0;
        timer.cancel();
    }

}
