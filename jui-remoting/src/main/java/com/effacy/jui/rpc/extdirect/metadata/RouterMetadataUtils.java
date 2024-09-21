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
package com.effacy.jui.rpc.extdirect.metadata;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Utilities for working with router and associated meta-data.
 * 
 * @author Jeremy Buckley
 */
public final class RouterMetadataUtils {

    /**
     * Resolve meta-data for an action.
     * 
     * @param name
     *            the action name.
     * @param metadata
     *            the router meta-data.
     * @return The action meta-data (or {@code null} if not mapped).
     */
    public static IActionMetadata resolveMetadata(String name, IRouterMetadata metadata) {
        for (IActionMetadata meta : metadata.getActionMetadata ())
            if (meta.getActionName ().equals (name))
                return meta;
        return null;
    }


    /**
     * Resolve meta-data for a method.
     * 
     * @param name
     *            the method name.
     * @param metadata
     *            the action meta-data.
     * @return The method meta-data (or {@code null} if not mapped).
     */
    public static IMethodMetadata resolveMetadata(String name, IActionMetadata metadata) {
        for (IMethodMetadata meta : metadata.getMethodMetadata ())
            if (meta.getMethodName ().equals (name))
                return meta;
        return null;
    }


    /**
     * Resolve meta-data for a method from a router metadata.
     * 
     * @param name
     *            the action name.
     * @param metadata
     *            the router meta-data.
     * @return The method meta-data (or {@code null} if not mapped).
     */
    public static IMethodMetadata resolveMetadata(String actionName, String methodName, IRouterMetadata metadata) {
        IActionMetadata actionMetadata = resolveMetadata (actionName, metadata);
        if (actionMetadata == null)
            return null;
        return resolveMetadata (methodName, actionMetadata);
    }


    /**
     * Resolve the argument list for a method from router meta-data.
     * 
     * @param actionName
     *            the action name.
     * @param methodName
     *            the method name.
     * @param metadata
     *            the router meta-data.
     * @return The method type list (or {@code null} if not mapped).
     */
    public static List<IParameterMetadata> resolveMethodTypes(String actionName, String methodName, IRouterMetadata metadata) {
        IMethodMetadata methodMetadata = resolveMetadata (actionName, methodName, metadata);
        if (methodMetadata == null)
            return null;
        return methodMetadata.getParameterMetadata ();
    }


    /**
     * Converts a record to a JavaScript declaration.
     * 
     * @param record
     *            the record to generate the JavaScript from.
     * @param pw
     *            the print writer to write to.
     * @param includeCreateBlank
     *            if the create blank method should be included.
     */
    public static void toJavaScript(IRecordMetadata record, PrintWriter pw, boolean includeCreateBlank) {
        pw.print ("var " + record.getName () + " = new Ext.data.Record.create([");
        boolean firstField = true;
        for (IRecordFieldMetadata field : record.getFields ()) {
            List<String> pairs = new ArrayList<String> ();
            createPair ("name", field.getName (), true, pairs);
            createPair ("mapping", field.getMapping (), true, pairs);
            createPair ("convert", field.getConvert (), false, pairs);
            createPair ("dateFormat", field.getDateFormat (), true, pairs);
            createPair ("defaultValue", field.getDefaultValue (), true, pairs);
            //createPair ("sortDir", field.getSortDir (), true, pairs);
            createPair ("sortType", field.getSortType (), false, pairs);
            //createPair ("type", field.getType (), true, pairs);
            createPair ("allowBlank", field.isAllowBlank (), false, pairs);
            if (!pairs.isEmpty ()) {
                if (!firstField)
                    pw.print (',');
                else
                    firstField = false;
                pw.println ('{');
                boolean firstEntry = true;
                for (String pair : pairs) {
                    if (!firstEntry)
                        pw.println (',');
                    else
                        firstEntry = false;
                    pw.print (pair);
                }
                pw.println ();
                pw.print ('}');
            }
        }
        pw.println ("]);");

        if (includeCreateBlank) {
            pw.println (record.getName () + ".createBlank = function() {");
            pw.println (" return new " + record.getName () + "({");
            List<String> pairs = new ArrayList<String> ();
            for (IRecordFieldMetadata field : record.getFields ())
                createCreatePair (field.getName (), field.getCreateValue (), field.getType (), pairs);
            if (!pairs.isEmpty ()) {
                boolean firstEntry = true;
                for (String pair : pairs) {
                    if (!firstEntry)
                        pw.println (',');
                    else
                        firstEntry = false;
                    pw.print (pair);
                }
                pw.println ();
            }
            pw.println (" });");
            pw.println ("};");
        }
    }


    /**
     * Generates a string representing a JavaScript name-value pair for an
     * association.
     * 
     * @param name
     *            the name (cannot be {@code null}).
     * @param value
     *            the value (can be {@code null}).
     * @param pairs
     *            an array of strings to add too (only if value is not {@code
     *            null}).
     */
    private static void createPair(String name, Object value, boolean quote, List<String> pairs) {
        if (value == null)
            return;
        StringBuffer sb = new StringBuffer ();
        sb.append ("   ");
        sb.append (name);
        sb.append (" : ");

        if (value instanceof Boolean) {
            value = ((Boolean) value) ? "true" : "false";
        } else if (value instanceof FieldType) {
            if (FieldType.AUTO.equals (value))
                return;
            value = ((FieldType) value).name ().toLowerCase ();
        //} else if (value instanceof SortDirection) {
        //    value = ((SortDirection) value).name ();
        }

        if (quote) {
            sb.append ('\'');
            sb.append (value.toString ().replace ("'", "\\'"));
            sb.append ('\'');
        } else {
            sb.append (value.toString ());
        }
        pairs.add (sb.toString ());
    }


    /**
     * Generates a string representing a JavaScript name-value pair for an
     * association.
     * 
     * @param name
     *            the name (cannot be {@code null}).
     * @param value
     *            the value (can be {@code null}).
     * @param type
     *            the value type.
     * @param pairs
     *            an array of strings to add too (only if value is not {@code
     *            null}).
     */
    private static void createCreatePair(String name, Object value, FieldType type, List<String> pairs) {
        if (value == null) {
            if (FieldType.STRING.equals (type))
                value = "";
            else if (FieldType.BOOLEAN.equals (type))
                value = "false";
            else if (FieldType.FLOAT.equals (type))
                value = "0.0";
            else if (FieldType.INT.equals (type))
                value = "0";
            else if (FieldType.DATE.equals (type))
                value = DateFormat.getDateInstance ().format (new Date ());
            else
                return;
        }
        createPair (name, value, type.isQuotable (), pairs);
    }


    /**
     * Private constructor.
     */
    private RouterMetadataUtils() {
        // Nothing.
    }
}
