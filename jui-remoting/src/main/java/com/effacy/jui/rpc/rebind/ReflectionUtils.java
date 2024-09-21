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
package com.effacy.jui.rpc.rebind;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JPackage;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

/**
 * Utilities for dealing with classes.
 * 
 * @author Jeremy Buckley
 */
public final class ReflectionUtils {

    /**
     * Scans all packages for classes that have the passed annotation.
     * 
     * @param typeOracle
     *            the oracle for types.
     * @param annotation
     *            the annotation to search for.
     * @return A list of classes that have the annotation.
     */
    public static List<JClassType> scanForAnnotations(TypeOracle typeOracle, Class<? extends Annotation> annotation) {
        List<JClassType> matchingClasses = new ArrayList<JClassType> ();
        for (JPackage pack : typeOracle.getPackages ()) {
            for (JClassType type : pack.getTypes ()) {
                if (type.getAnnotation (annotation) != null)
                    matchingClasses.add (type);
            }
        }
        return matchingClasses;
    }


    /**
     * Private constructor.
     */
    private ReflectionUtils() {
        // Nothing.
    }
}
