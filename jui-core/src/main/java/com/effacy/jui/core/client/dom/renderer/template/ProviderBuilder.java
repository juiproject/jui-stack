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
package com.effacy.jui.core.client.dom.renderer.template;

import java.util.List;
import java.util.function.Function;

import com.effacy.jui.core.client.dom.renderer.template.Provider.ConstantProvider;
import com.effacy.jui.core.client.dom.renderer.template.Provider.DelegatingProvider;
import com.effacy.jui.core.client.dom.renderer.template.Provider.IfElseProvider;
import com.effacy.jui.core.client.dom.renderer.template.Provider.ProviderWithCondition;
import com.effacy.jui.core.client.dom.renderer.template.Provider.StringProvider;
import com.effacy.jui.core.client.dom.renderer.template.Provider.TextFormatter;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.LoopCondition;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.LoopContext;
import com.effacy.jui.core.client.util.FormatSupport;
import com.effacy.jui.platform.util.client.StringSupport;
import com.google.gwt.i18n.client.NumberFormat;

/**
 * Provides a collection of support methods that allows for direct casting.
 */
public final class ProviderBuilder {

    /**
     * Convenience to enable a provider to be specified using lambdas (thus
     * explicitly casts).
     * <p>
     * See {@link ProviderSupport#create(Provider)}.
     * 
     * @param <P>
     *                 the property type being extracted.
     * @param <D>
     *                 the data type being extracted from.
     * @param provider
     *                 the provider (expressed as a lambda).
     * @return the passed provider.
     */
    static public <P, D> Provider<P, D> create(Provider<P, D> provider) {
        return provider;
    }

    /**
     * Convenience to enable a provider to be specified using lambdas (thus
     * explicitly casts).
     * <p>
     * See {@link ProviderSupport#create(Provider, Condition)}.
     * 
     * @param <P>
     *                 the property type being extracted.
     * @param <D>
     *                 the data type being extracted from.
     * @param provider
     *                 the provider (expressed as a lambda).
     * @return the passed provider.
     */
    static public <P, D> Provider<P, D> create(Provider<P, D> provider, Condition<D> condition) {
        if (condition != null)
            return new DelegatingProvider<P, D> (provider).condition (condition);
        return provider;
    }

    /**
     * Convenience to enable a provider to be specified using lambdas (thus
     * explicitly casts).
     * <p>
     * See {@link ProviderSupport#create(Provider, Condition)}.
     * 
     * @param <P>
     *              the property type being extracted.
     * @param <D>
     *              the data type being extracted from.
     * @param value
     *              the value to pass.
     * @return the passed provider.
     */
    static public <P, D> Provider<P, D> create(P value, Condition<D> condition) {
        if (condition != null)
            return new DelegatingProvider<P, D> (d -> value).condition (condition);
        return (d -> value);
    }

    /**
     * Convenience method. See {@link Provider#formatter(String)}.
     */
    static public <D> TextFormatter<D> formatter(String content) {
        return new TextFormatter<D> (content);
    }

    /**
     * See {@link #create(Provider)} but where the property is a string.
     */
    static public <D> ProviderWithCondition<String, D> fmtnum(Provider<? extends Number, D> provider) {
        return fmtnum (provider, null);
    }

    /**
     * See {@link #create(Provider)} but where the property is a string.
     */
    static public <D> ProviderWithCondition<String, D> fmtnum(final Provider<? extends Number, D> provider, final String format) {
        return new ProviderWithCondition<String, D> () {

            public String get(D data) {
                Number value = provider.get (data);
                NumberFormat fomatter = StringSupport.empty (format) ? NumberFormat.getFormat ("#,##0;-#,##0") : NumberFormat.getFormat (format);
                return fomatter.format (value);
            }
        };
    }

    /**
     * Conditional list size provider.
     * 
     * @param list
     *             the list provider.
     * @return the provider for the size of the list.
     */
    static public <D, R> ProviderWithCondition<Integer, D> size(final Provider<List<R>, D> list) {
        return new ProviderWithCondition<Integer, D> () {

            @Override
            public Integer get(D data) {
                if (list == null)
                    return 0;
                return list.get (data).size ();
            }

        };
    }

    /**
     * Used to extract data from the loop context.
     * 
     * @param converter
     *                  the value converter from the loop context.
     * @return the associated provider.
     */
    static public <D> Provider<String, D> loop(Function<LoopContext, String> converter) {
        return new Provider<String, D> () {

            @Override
            public String get(D data) {
                // This is actually never called.
                return null;
            }

            @Override
            public String get(D data, LoopContext context) {
                return converter.apply (context);
            }

        };
    }

    /**
     * See {@link #create(Provider)} but where the property is a string.
     */
    static public <D> StringProvider<D> string(String str) {
        return string (constant (str));
    }

    /**
     * See {@link #create(Provider)} but where the property is a string.
     */
    static public <D> StringProvider<D> string(String str, Condition<D> condition) {
        return new StringProvider<D> (constant (str)).condition (condition);
    }

    /**
     * See {@link #create(Provider)} but where the property is a string.
     */
    static public <D> StringProvider<D> string(Provider<?, D> provider) {
        return new StringProvider<D> (provider);
    }

    /**
     * See {@link #create(Provider)} but where the property is a string.
     */
    static public <D> StringProvider<D> string(Provider<?, D> provider, Condition<D> condition) {
        return new StringProvider<D> (provider).condition (condition);
    }

    /**
     * See {@link #create(Provider)} but where the property is a string.
     */
    static public <D> StringProvider<D> stringInLoop(String str, LoopCondition condition) {
        StringProvider<D> provider = string (str);
        provider.loopCondition (condition);
        return provider;
    }

    /**
     * See {@link #create(Provider)} but where the property is a string.
     */
    static public <D> Provider<String, D> string(Provider<?, D> provider, String defaultValue) {
        return new DelegatingProvider<String, D> (new StringProvider<D> (provider), defaultValue);
    }

    /**
     * See {@link #create(Provider)} but where the property is a string.
     */
    static public <D> StringProvider<D> format(Provider<?, D> provider) {
        return string (d -> FormatSupport.format (provider.get (d)));
    }

    /**
     * See {@link #create(Provider)} but where the property is a string.
     */
    static public <D> StringProvider<D> asNumber(Provider<? extends Number, D> provider) {
        return string (d -> FormatSupport.asNumber (provider.get (d)));
    }

    /**
     * Creates a default string provider.
     */
    static public <P, D> ConstantProvider<P, D> constant(P content) {
        return new ConstantProvider<P, D> (content);
    }

    /**
     * Creates a default string provider with a condition (will return {@code null}
     * if the condition is not met).
     */
    static public <P, D> IfElseProvider<P, D> constant(P content, Condition<D> condition) {
        return new IfElseProvider<P, D> (condition, new ConstantProvider<P, D> (content));
    }

    /**
     * Creates a default string provider with a condition (will return {@code null}
     * if the condition is not met).
     */
    static public <P, D> ConstantProvider<P, D> constantInLoop(P content, LoopCondition condition) {
        ConstantProvider<P, D> provider = constant (content);
        provider.loopCondition (condition);
        return provider;
    }

    /**
     * A mechanism that converts the types of a provider.
     */
    static public <A, B, D> Provider<A, D> convert(final Provider<B, D> provider, final Function<B, A> converter) {
        return new Provider<A, D> () {

            @Override
            public A get(D data) {
                return converter.apply (provider.get (data));
            }

        };
    }

    /**
     * A convenience to pick the first non-null value from the list.
     * 
     * @param values
     *               the values to select from.
     * @return the first non-null (or {@code null} if there are none).
     */
    @SafeVarargs
    static public <A> A notNull(A... values) {
        if (values == null)
            return null;
        for (A value : values) {
            if (value != null)
                return value;
        }
        return null;
    }

    /**
     * A convenience to pick the first non-empty value from the list (converted to a
     * string using {@link FormatSupport#format(Object)}).
     * 
     * @param values
     *               the values to select from.
     * @return the first non-empty (or {@code null} if there are none).
     */
    @SafeVarargs
    static public <A> String notEmpty(A... values) {
        if (values == null)
            return null;
        for (A value : values) {
            String converted = FormatSupport.format (value);
            if (!StringSupport.empty (converted))
                return converted;
        }
        return null;
    }

    /**
     * Provide non-construct constructor.
     */
    private ProviderBuilder() {
        // Nothing.
    }
}
