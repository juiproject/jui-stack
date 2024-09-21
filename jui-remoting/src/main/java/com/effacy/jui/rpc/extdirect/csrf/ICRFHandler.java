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
package com.effacy.jui.rpc.extdirect.csrf;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Use to encode and validate a CSRF token.
 *
 * @author Jeremy Buckley
 */
public interface ICRFHandler {

    /**
     * Generate and returns a CSRF token. This token will be passed through the API
     * declaration for use by the remoting mechanism. If nothing is returned then
     * the CSRF mechanism will be disabled.
     * <p>
     * If another CSRF mechanism is being used then this method can be used to
     * extract that CSRF token from that mechanism and return it. It will be up to
     * the remoting mechanism to apply that token in such a way so that it can be
     * picked up by the secondary mechanism.
     * <p>
     * When used internally this token needs to be able to be validated against
     * {@link #token(HttpServletRequest)} via {@link #validate(String, String)}.
     * 
     * @param request
     * @param response
     * @return the token that should be passed back during the api declaration (a
     *         GET on the remoting endpoint).
     */
    public Optional<String> generate(HttpServletRequest request, HttpServletResponse response);

    /**
     * Obtains the token that we should be verifying against (and is associated the
     * to the token from {@link #generate(HttpServletRequest, HttpServletResponse)};
     * it need not be the same but simply what can be verified by a call to
     * {@link #validate(String, String)}).
     * 
     * @param request
     * @return the token for validation.
     */
    public Optional<String> token(HttpServletRequest request);

    /**
     * Validate the passed token (extracted from the request) and the actual token.
     * 
     * @param passedToken
     *                    the actual token to test.
     * @param token
     *                    the CSRF token to decode.
     * @return {@code true} if there is a match.
     */
    public boolean validate(String passedToken, String token);
}
