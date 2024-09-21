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
package com.effacy.jui.validation.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.validation.model.validator.EmailValidator;

/**
 * Tests {@link EmailValidator}.
 */
public class EmailValidatorTest {

    /**
     * Standard passes.
     */
    @Test
    public void test_valid() {
        EmailValidator validator = new EmailValidator();

        Assertions.assertTrue (validator.validate ("hubba@hubba.com"));
        Assertions.assertTrue (validator.validate ("hubba2@hubba.com"));
        Assertions.assertTrue (validator.validate ("hubba+bubba@hubba.com"));
    }

    /**
     * Tests the ignoring of trailing and leading whitespace.
     */
    @Test
    public void test_valid_whitespace() {
        EmailValidator validator = new EmailValidator();

        Assertions.assertTrue (validator.validate ("hubba@hubba.com   "));
        Assertions.assertTrue (validator.validate ("   hubba@hubba.com"));
        Assertions.assertTrue (validator.validate ("  hubba@hubba.com   "));
    }

    /**
     * Test the empty case (which this validator passes assuming empty assertions
     * are done separately).
     */
    @Test
    public void test_valid_empty() {
        EmailValidator validator = new EmailValidator();

        Assertions.assertTrue (validator.validate (""));
        Assertions.assertTrue (validator.validate ("  "));
        Assertions.assertTrue (validator.validate (null));
    }

    /**
     * Standard failures.
     */
    @Test
    public void test_invalid() {
        EmailValidator validator = new EmailValidator();

        Assertions.assertFalse (validator.validate ("hubba@hubba,com"));
        Assertions.assertFalse (validator.validate ("hubba"));
    }

    /**
     * Standard failures.
     */
    @Test
    public void test_invalid_unicode() {
        EmailValidator validator = new EmailValidator();

        // These are recorded as a failure (the regular expression is a GMail compliant
        // version of the OWASP validation - see reference provided on {@link
        // EmailValidator}). This appears not to support non-latin characters. Maybe
        // later this would be good to support. For that reason this test is included
        // as an assertion of this not currently supported (and deliberately by choice
        // of source of the regular expression).
        Assertions.assertFalse (validator.validate ("用户名@领域.电脑"));
    }
}
