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
package com.effacy.jui.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.tapestry.valid.IValidator;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for testers that implements the core {@link IResolvable} and
 * {@link ITester} methods.
 */
public abstract class Tester<T extends Tester<T>> implements ITester<T>, IResolvable {

    /**
     * Logging.
     */
    private static Logger LOG = LoggerFactory.getLogger (Tester.class);

    /**
     * Used soley to apply scope to the test ID's.
     */
    public static class Scoper extends Tester<Scoper> {

        /**
         * The scope being applied.
         */
        private String testIdScope;

        /**
         * Construct with scope.
         * @param testIdScope the scope to apply.
         */
        public Scoper(String testIdScope) {
            this.testIdScope = testIdScope;
        }

        @Override
        public <T extends IResolvable> Scoper with(T resolver, Consumer<T> with) {
            if (resolver != null) {
                resolver.resolve (page.wrap (testIdScope));
                if (with != null)
                    with.accept (resolver);
            }
            return this;
        }
        
    }
    
    /**
     * The page post-resolution.
     */
    protected IPage page;

    /**
     * The validators to validate (added to prior to resoultion).
     */
    protected List<IValidatable> validators = new ArrayList<>();

    /**
     * The child resolvers to resolve.
     */
    protected Set<IResolvable> children = new HashSet<> ();

    /**
     * Determines if the tester has been resolved (or not).
     */
    public boolean isResolved() {
        return (page != null);
    }

    @Override
    public final void resolve(IPage page) {
        this.page = page;
        Assertions.assertNotNull (page, "Page cannot be null");
        this.children.forEach (child->child.resolve (page));
        resolve ();
        validators.forEach (v -> v.validate ());
    }

    /**
     * Called by {@link #resolve(IPage)} after all the child resolvables have been
     * resolved but before any validators are processed.
     */
    public void resolve() {
        // Nothing.
    }

    /**
     * Registers a child resolvable to be resolved on calls to
     * {@link #resolve(IPage)}.
     * 
     * @param <R>        the type.
     * @param resolvable the resolvable to register ({@code null}-safe).
     * @return the passed resolvable.
     */
    protected <R extends IResolvable> R register(R resolvable) {
        if (resolvable != null)
            children.add (resolvable);
        return resolvable;
    }

    /**
     * See {@link #validate(IValidatable, int, long)} but with the count and delay
     * defaults (10 and 500 respectively).
     * 
     * @param validatable the validatable to run.
     */
    protected void validate(IValidatable validatable) {
        validate (validatable, 10, 500L);
    }

    /**
     * Registers a validatable for validation.
     * <p>
     * If the node has been resolved this the validatable will be validated
     * immediately. If not then it will be retained and validated when
     * {@link #resolve(IPage)} is called (actually called from {@link #resolve()}).
     * <p>
     * If the passed validatable us not a {@link RetryValidator} then it will be
     * wrapped in one using the passed retry count and intra-retry delay. This means
     * that failures will be retried (which can allow for resilience as state is
     * changed).
     * 
     * @param validatable the validatable to validate.
     * @param retries     the number of retries to apply.
     * @param delay       the delay in milliseconds between retries.
     */
    protected void validate(IValidatable validatable, int retries, long delay) {
        if (validatable == null)
            return;
        if (!(validatable instanceof Tester<?>.RetryValidator))
            validatable = new RetryValidator (validatable, retries, delay);
        if (isResolved ())
            validatable.validate ();
        else
            this.validators.add (validatable);
    }

    /**
     * Sleeps for the given period.
     * 
     * @param delay the period in milliseconds.
     */
    protected synchronized void sleep(long delay) {
        if (delay <= 0)
            return;
        synchronized (page) {
            try {
                page.wait (delay);
            } catch (InterruptedException e) {
                Assertions.fail ("Test run interrupted");
            }
        }
    }
    
    /**
     * The page that is set post resolution.
     * 
     * @return the page.
     */
    public IPage getPage() {
        return page;
    }

    /**
     * A type of {@link IValidator} that supports retries on failure.
     */
    public class RetryValidator implements IValidatable {

        private IValidatable validator;

        private int retryCount;

        private long delay;

        public RetryValidator (IValidatable validator, int retryCount, long delay) {
            this.validator = validator;
            this.retryCount = retryCount;
            this.delay = delay;
        }

        @Override
        public void validate() {
            int retries = 0;
            while (true) {
                try {
                    validator.validate ();
                    return;
                } catch (AssertionError e) {
                    if (retries++ > retryCount)
                        throw e;
                    LOG.debug ("Retrying validation: " + retryCount);
                    synchronized (page) {
                        try {
                            page.wait (delay);
                        } catch (InterruptedException ex) {
                            throw e;
                        }
                    }
                }
            }
        }
        
    }
}
