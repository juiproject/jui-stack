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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.gwtproject.regexp.shared.RegExp;

import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.LoopCondition;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.LoopContext;
import com.effacy.jui.core.client.util.FormatSupport;
import com.effacy.jui.platform.util.client.StringSupport;

/**
 * Mechanism for providing something extracted from data. Includes
 * {@link Condition} which defaults to {@code true}.
 */
public interface Provider<P, D> extends Condition<D>, LoopCondition {

    /**
     * Creates a provider that gains access to the loop condition.
     * 
     * @param provider
     *                 the function that converts the the loop context data to the
     *                 target type.
     * @return a suitable provider instance.
     */
    public static <P, D> Provider<P, D> loop(Function<LoopContext, P> provider) {
        return new Provider<P, D> () {

            @Override
            public P get(D data) {
                return provider.apply (null);
            }

            @Override
            public P get(D data, LoopContext context) {
                return provider.apply (context);
            }

        };
    }

    /**
     * Given the data provide something from that data.
     * 
     * @param data
     *             the data.
     * @return what is intended to be provided.
     */
    public P get(D data);

    /**
     * Get a value from the underlying data coupled with the loop context.
     * 
     * @param data
     *                the data.
     * @param com.effacy.jui.core.client
     *                the loop context.
     * @return the value.
     */
    default public P get(D data, LoopContext context) {
        return get (data);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dom.TemplateBuilder.Condition#testLoop(java.lang.Object)
     */
    @Override
    default public boolean test(D data) {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.LoopCondition#testLoop(com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.LoopContext)
     */
    @Override
    default boolean testLoop(LoopContext loop) {
        return true;
    }

    /**
     * Convenience to generate a chaining of providers.
     */
    @SafeVarargs
    public static <P, D> ChainProvider<P, D> chain(Provider<P, D>... providers) {
        return new ChainProvider<P, D> (providers);
    }

    /************************************************************************
     * Standard implementations.
     ************************************************************************/

    /**
     * A provider where a condition may be supplied.
     */
    public static abstract class ProviderWithCondition<P, D> implements Provider<P, D> {

        /**
         * The data-condition to evaluate.
         */
        protected Condition<D> condition;

        /**
         * The loop-condition to evaluate.
         */
        protected LoopCondition loopCondition;

        /**
         * Assigns a condition to the provider.
         * 
         * @param condition
         *                  the condition.
         */
        @SuppressWarnings("unchecked")
        public <T extends Provider<P, D>> T condition(Condition<D> condition) {
            this.condition = condition;
            return (T) this;
        }

        /**
         * Assigns a condition to the provider.
         * 
         * @param condition
         *                  the condition.
         */
        @SuppressWarnings("unchecked")
        public <T extends Provider<P, D>> T loopCondition(LoopCondition loopCondition) {
            this.loopCondition = loopCondition;
            return (T) this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.TemplateBuilder.Provider#testLoop(java.lang.Object)
         */
        @Override
        public boolean test(D data) {
            if (condition != null)
                return condition.test (data);
            return Provider.super.test (data);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.renderer.template.Provider#testLoop(com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.LoopContext)
         */
        @Override
        public boolean testLoop(LoopContext loop) {
            // if ((loop != null) && (loopCondition != null))
            if (loopCondition != null)
                return loopCondition.testLoop (loop);
            return Provider.super.testLoop (loop);
        }

    }

    /**
     * Wrapper around a provider to provide a default value where the wrapped
     * provided returns a {@code null}.
     */
    public static class DelegatingProvider<P, D> extends ProviderWithCondition<P, D> {

        /**
         * The underlying provider.
         */
        private Provider<P, D> provider;

        /**
         * Default value.
         */
        private P defaultValue;

        /**
         * Construct with provider and default.
         * 
         * @param provider
         *                 the provider to provide a default for.
         */
        public DelegatingProvider(Provider<P, D> provider) {
            this.provider = provider;
        }

        /**
         * Construct with provider and default.
         * 
         * @param provider
         *                     the provider to provide a default for.
         * @param defaultValue
         *                     the default value when the provider returns {@code null}.
         */
        public DelegatingProvider(Provider<P, D> provider, P defaultValue) {
            this.provider = provider;
            this.defaultValue = defaultValue;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.TemplateBuilder.Provider#get(java.lang.Object)
         */
        @Override
        public P get(D data) {
            if (provider == null)
                return defaultValue;
            P value = provider.get (data);
            return ((value == null) || ((value instanceof String) && StringSupport.empty ((String) value))) ? defaultValue : value;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.TemplateBuilder.ProviderWithCondition#testLoop(java.lang.Object)
         */
        @Override
        public boolean test(D data) {
            if (!super.test (data))
                return false;
            return (provider != null) ? provider.test (data) : false;
        }
    }

    /**
     * A provider that always returns the same (constant) value.
     */
    public static class ConstantProvider<P, D> extends ProviderWithCondition<P, D> {

        /**
         * The constant to provide.
         */
        private P value;

        /**
         * Construct with the context.
         * 
         * @param value
         *              the constant to be returned.
         */
        public ConstantProvider(P value) {
            this.value = value;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.TemplateBuilder.Provider#get(java.lang.Object)
         */
        @Override
        public P get(D data) {
            return value;
        }

    }

    /**
     * A means to convert a general value to a string value. Uses
     * {@link Object#toString()}.
     */
    public static class StringProvider<D> extends ProviderWithCondition<String, D> {

        /**
         * The provider being warpped.
         */
        private Provider<?, D> provider;

        /**
         * An option default value.
         */
        private String defaultValue;

        /**
         * Construct with the provider whose value is to be to converted.
         * 
         * @param provider
         *                 the provider to convert.
         */
        public StringProvider(Provider<?, D> provider) {
            this.provider = provider;
        }

        /**
         * Construct with the provider whose value is to be to converted.
         * 
         * @param provider
         *                     the provider to convert.
         * @param defaultValue
         *                     (optional) default value if the provider returns an empty
         *                     result ({@code null} or empty string)).
         */
        public StringProvider(Provider<?, D> provider, String defaultValue) {
            this.provider = provider;
            this.defaultValue = defaultValue;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.TemplateBuilder.Provider#get(java.lang.Object)
         */
        @Override
        public String get(D data) {
            Object value = (provider == null) ? null : provider.get (data);
            String formattedValue = FormatSupport.format (value, defaultValue);
            if (!StringSupport.empty (formattedValue))
                return formattedValue;
            if (defaultValue != null)
                return defaultValue;
            if (value == null)
                return "[NULL]";
            return value.toString ();
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.TemplateBuilder.ProviderWithCondition#testLoop(java.lang.Object)
         */
        @Override
        public boolean test(D data) {
            if (!super.test (data))
                return false;
            return (provider != null) ? provider.test (data) : false;
        }

    }

    /**
     * Resolves a list of providers.
     */
    public static class ChainProvider<P, D> extends ProviderWithCondition<P, D> {

        /**
         * Ordered list of providers.
         */
        private List<Provider<P, D>> providers = new ArrayList<Provider<P, D>> ();

        /**
         * Construct with providers. Each provider is resolved in-turn until a
         * non-{@code null} value is returned.
         * 
         * @param providers
         *                  the providers.
         */
        @SafeVarargs
        public ChainProvider(Provider<P, D>... providers) {
            for (Provider<P, D> provider : providers) {
                if (provider == null)
                    continue;
                this.providers.add (provider);
            }
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.TemplateBuilder.Provider#get(java.lang.Object)
         */
        @Override
        public P get(D data) {
            for (Provider<P, D> provider : providers) {
                if (!provider.test (data))
                    continue;
                P value = provider.get (data);
                if (value != null)
                    return value;
            }
            return null;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.TemplateBuilder.ProviderWithCondition#testLoop(java.lang.Object)
         */
        @Override
        public boolean test(D data) {
            if (!super.test (data))
                return false;
            for (Provider<P, D> provider : providers) {
                if (provider.test (data))
                    return true;
            }
            return false;
        }

        public ChainProvider<P, D> chain(Provider<P, D> provider) {
            if (provider != null)
                providers.add (provider);
            return this;
        }
    }

    /**
     * A provider that casts from one value to another. CastingProvider.
     */
    public static abstract class CastingProvider<F, T, D> implements Provider<T, D> {

        /**
         * The delegate.
         */
        private Provider<F, D> delegate;

        /**
         * Construct with a delegate.
         * 
         * @param delegate
         *                 the delegate.
         */
        public CastingProvider(Provider<F, D> delegate) {
            this.delegate = delegate;
        }

        /**
         * Implements the cast.
         */
        protected abstract T cast(F value);

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.renderer.Provider#get(java.lang.Object)
         */
        @Override
        public T get(D data) {
            return cast (delegate.get (data));
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.renderer.Provider#test(java.lang.Object)
         */
        @Override
        public boolean test(D data) {
            return delegate.test (data);
        }

        /**
         * Convenience to construct a casting provider.
         */
        public static <F, T, D> CastingProvider<F, T, D> create(Provider<F, D> delegate, final Function<F, T> cast) {
            return new CastingProvider<F, T, D> (delegate) {

                protected T cast(F value) {
                    return cast.apply (value);
                }
            };
        }

    }

    /**
     * A provider that uses a condition to select between two other providers. This
     * is akin to the "?" ":" statement.
     */
    public static class IfElseProvider<P, D> extends ProviderWithCondition<P, D> {

        /**
         * The switch condition.
         */
        private Condition<D> optionCondition;

        /**
         * The affirmative provider.
         */
        private Provider<P, D> optionTrue;

        /**
         * The negative provider.
         */
        private Provider<P, D> optionFalse;

        /**
         * Construct with a condition but a single provider (the fail alternative is a
         * {@code null} provider).
         * 
         * @param condition
         *                  the switch condition.
         * @param provider
         *                  the provider to use in the affirmative case.
         */
        public IfElseProvider(Condition<D> condition, Provider<P, D> provider) {
            this (condition, provider, null);
        }

        /**
         * Construct with a condition and providers for the two outcomes.
         * 
         * @param condition
         *                    the switch condition.
         * @param optionTrue
         *                    the provider for when the condition is in the affirmative.
         * @param optionFalse
         *                    the provider for when the condition is in the negative.
         */
        public IfElseProvider(Condition<D> optionCondition, Provider<P, D> optionTrue, Provider<P, D> optionFalse) {
            this.optionTrue = optionTrue;
            this.optionCondition = optionCondition;
            this.optionFalse = optionFalse;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.TemplateBuilder.Provider#get(java.lang.Object)
         */
        @Override
        public P get(D data) {
            if ((optionCondition == null) || !optionCondition.test (data)) {
                if (optionFalse == null)
                    return null;
                return optionFalse.get (data);
            }
            return (optionTrue == null) ? null : optionTrue.get (data);
        }
    }

    /**
     * Providers a means to generate a string of text using placeholders.
     * <p>
     * Placeholders are expressed inline by a number (the placeholder reference)
     * surrounded by parenthesis (such as
     * <code>"Thank you {0} for your contribution of {1}"</code>).
     * <p>
     * Specification of the placeholder values are via providers associated with the
     * relevant placeholder references. These are specified using the family of
     * {@link TextFormatter#value(Provider)} and
     * {@link TextFormatter#option(Condition, String, String)} methods.
     * <p>
     * Currently the placeholder references are fixed at the number 0-7.
     */
    public static class TextFormatter<D> implements Provider<String, D> {

        /**
         * A condition to evaluate before delivering the content.
         */
        private Condition<D> condition;

        /**
         * The string being formatted.
         */
        private String content;

        /**
         * Maps of placeholder references to placeholder value providers.
         */
        private Map<Integer, Provider<String, D>> values = new HashMap<Integer, Provider<String, D>> ();

        /**
         * The current placeholder value counter (for auto-placeholder reference
         * generation).
         */
        private int idx = 0;

        /**
         * Replacement strings for the placeholders (this is a first pass approach with
         * anticipation of making this more flexibly by allowing arbitrary placeholder
         * references).
         */
        private static RegExp[] REPS = new RegExp[] { RegExp.compile ("\\{0\\}"), RegExp.compile ("\\{1\\}"), RegExp.compile ("\\{2\\}"), RegExp.compile ("\\{3\\}"), RegExp.compile ("\\{4\\}"), RegExp.compile ("\\{5\\}"), RegExp.compile ("\\{6\\}"), RegExp.compile ("\\{7\\}") };

        /**
         * Construct with the base string to format. This contains the placeholders
         * (which are of the form <code>"{0}"</code>).
         * 
         * @param content
         */
        public TextFormatter(String content) {
            this.content = content;
        }

        /**
         * Assigns a condition that, if not {@code true}, then will deliver a
         * {@code null} content value. This is useful when being employed in a chain.
         * 
         * @param condition
         *                  the condition to evaluate.
         * @return the condition.
         */
        public TextFormatter<D> condition(Condition<D> condition) {
            this.condition = condition;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.TemplateBuilder.Condition#testLoop(java.lang.Object)
         */
        @Override
        public boolean test(D data) {
            if (condition != null)
                return condition.test (data);
            return true;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.TemplateBuilder.Provider#get(java.lang.Object)
         */
        @Override
        public String get(D data) {
            if (content == null)
                return "";

            String result = content;
            for (Integer placeholder : values.keySet ()) {
                if (placeholder < 0)
                    continue;
                if (placeholder > 7)
                    continue;
                result = REPS[placeholder].replace (result, values.get (placeholder).get (data));
            }

            return result;
        }

        /**
         * Assign a placeholder value provider for the current placeholder index (this
         * starts at 0 and will be incremented by one).
         * 
         * @param provider
         *                 the value provider.
         * @return this formatter.
         */
        public TextFormatter<D> value(Provider<?, D> provider) {
            return value (idx, provider);
        }

        /**
         * See {@link #value(Provider)} but is assigned for the specified placeholder
         * reference. The internal reference will be assigned to the plus one.
         * 
         * @param place
         *                 the placeholder reference.
         * @param provider
         *                 the value provider.
         * @return this formatter.
         */
        public TextFormatter<D> value(int place, final Provider<?, D> provider) {
            if (provider != null)
                values.put (idx++, new Provider<String, D> () {

                    public String get(D data) {
                        Object value = provider.get (data);
                        if (value == null)
                            return "[null]";
                        if (value instanceof String)
                            return (String) value;
                        return value.toString ();
                    }
                });
            return this;
        }

        /**
         * Convenience to provide a {@link IfElseProvider} around the given constants.
         * See {@link #value(Provider)} (this is ultimately called).
         * 
         * @param condition
         *                    the switch condition.
         * @param optionTrue
         *                    the value in the affirmative.
         * @param optionFalse
         *                    the value in the negative.
         * @return this formatter.
         */
        public TextFormatter<D> option(Condition<D> condition, String optionTrue, String optionFalse) {
            return option (idx, condition, optionTrue, optionFalse);
        }

        /**
         * See {@link #option(Condition, String, String)} and
         * {@link #value(int, Provider)}.
         * 
         * @param place
         *                    the placeholder reference.
         * @param condition
         *                    the switch condition.
         * @param optionTrue
         *                    the value in the affirmative.
         * @param optionFalse
         *                    the value in the negative.
         * @return this formatter.
         */
        public TextFormatter<D> option(int place, Condition<D> condition, String optionTrue, String optionFalse) {
            value (place, new IfElseProvider<String, D> (condition, new ConstantProvider<String, D> (optionTrue), new ConstantProvider<String, D> (optionFalse)));
            return this;
        }
    }
}
