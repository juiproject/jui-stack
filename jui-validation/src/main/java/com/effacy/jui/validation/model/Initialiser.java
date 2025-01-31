package com.effacy.jui.validation.model;

import com.effacy.jui.platform.core.client.EntryPoint;
import com.effacy.jui.platform.util.client.ScriptInjector;

/**
 * Performs any global initialisation for the module.
 */
public class Initialiser implements EntryPoint {

    @Override
    public void onModuleLoad() {
        ScriptInjector.injectFromModuleBase ("jui_validation.js");
    }
}
