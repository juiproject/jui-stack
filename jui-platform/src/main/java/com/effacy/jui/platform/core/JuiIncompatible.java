package com.effacy.jui.platform.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.gwt.core.shared.GwtIncompatible;

/**
 * Used to indicate the some code is not compatible with JUI compilation
 * (whatever compilation system has been employed).
 */
@Retention(RetentionPolicy.CLASS)
@Target({
    ElementType.TYPE, ElementType.METHOD,
    ElementType.CONSTRUCTOR, ElementType.FIELD })
@Documented
@GwtIncompatible
public @interface JuiIncompatible {

}
