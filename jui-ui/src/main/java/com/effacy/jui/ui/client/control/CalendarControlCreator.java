package com.effacy.jui.ui.client.control;

import java.util.function.Consumer;

import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.platform.util.client.With;

public class CalendarControlCreator {

    /**
     * Convenience to build a calendar control.
     * 
     * @param el
     *            the element to build into.
     * @param cfg
     *            to configure the control.
     * @return the button instance.
     */
    public static CalendarControl $(ContainerBuilder<?> el, Consumer<CalendarControl.Config> cfg) {
        return With.$ (build (cfg), cpt -> el.render (cpt));
    }

    /**
     * Convenience to obtain a calendar configuration.
     * 
     * @return the button configuration instance.
     */
    public static CalendarControl.Config create() {
        return new CalendarControl.Config ();
    }

    /**
     * Convenience to build a calendar control.
     * 
     * @param data
     *             (optional) layout data to associate with the instance.
     * @return the control instance.
     */
    public static CalendarControl build(LayoutData...data) {
        return build (null, data);
    }

    /**
     * Convenience to build a calendar control.
     * 
     * @param cfg
     *             to configure the control.
     * @param data
     *             (optional) layout data to associate with the instance.
     * @return the control instance.
     */
    public static CalendarControl build(Consumer<CalendarControl.Config> cfg, LayoutData...data) {
        return build (cfg, null, data);
    }

    /**
     * Convenience to build a calendar control.
     * 
     * @param cfg
     *                to configure the control.
     * @param applier
     *                to apply changes to the created control.
     * @param data
     *                (optional) layout data to associate with the instance.
     * @return the control instance.
     */
    public static CalendarControl build(Consumer<CalendarControl.Config> cfg, Consumer<CalendarControl> applier, LayoutData...data) {
        CalendarControl.Config config = new CalendarControl.Config ();
        if (cfg != null)
            cfg.accept (config);
        CalendarControl ctl = config.build (data);
        if (applier != null)
            applier.accept (ctl);
        return ctl;
    }
}
