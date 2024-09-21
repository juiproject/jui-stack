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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;

import com.effacy.jui.rpc.extdirect.Router;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A simple version that leaves the token unchanged.
 *
 * @author Jeremy Buckley
 */
public class StandardCSRFHandler implements ICRFHandler {
    /**
     * The message digest algorithm.
     */
    private static final String ALGORITHM = "SHA-1";

    /**
     * Static random instance which is seeded once with the current time.
     */
    private static final Random RANDOM = new Random (System.currentTimeMillis ());

    /**
     * The cookie that should contain the CSRF token.
     */
    public static final String CSRF_COOKIE = "CSRFTOKENID";

    /**
     * The attribute key for the CSRF code.
     */
    private static final String CSRF_SESSION = Router.class.getName () + "." + CSRF_COOKIE;

    /**
     * Sets the CSRF token cookie to be HTTP only.
     */
    protected boolean useCsrfHttpOnly = false;

    /**
     * Sets the CSRF token cookie to be secure.
     */
    protected boolean useCsrfSecure = false;

    /**
     * When using CSRF tokens then cookie should be set to HttpOnly. In this case it
     * should not be accessible in script so not able to be sent during remoting
     * requests. In this case the check will only be performed on the cookies
     * returned in the request.
     * 
     * @param useCsrfHttpOnly
     *                        {@code true} if to ensure that requests are HTTP only
     *                        (default is {@code false}).
     */
    public void setUseCsrfHttpOnly(boolean useCsrfHttpOnly) {
        this.useCsrfHttpOnly = useCsrfHttpOnly;
    }

    public void setUseCsrfSecure(boolean useCsrfSecure) {
        this.useCsrfSecure = useCsrfSecure;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.rpc.extdirect.csrf.ICRFHandler#validate(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public boolean validate(String passedToken, String token) {
        if ((passedToken == null) || (token == null))
            return false;
        return passedToken.equals (token);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.rpc.extdirect.csrf.ICRFHandler#create(jakarta.servlet.http.HttpServletRequest)
     */
    @Override
    public Optional<String> generate(HttpServletRequest request, HttpServletResponse response) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance (ALGORITHM);
            byte[] bytes = new byte[64];
            RANDOM.nextBytes (bytes);
            String fullCode = Base64.encodeBase64URLSafeString (messageDigest.digest (bytes));
            String cookieCode = fullCode.substring (0, Math.min (10, fullCode.length ()));
            request.getSession ().setAttribute (CSRF_SESSION, cookieCode);

            Cookie csrfCookie = new Cookie (CSRF_COOKIE, cookieCode);
            csrfCookie.setHttpOnly (useCsrfHttpOnly);
            csrfCookie.setSecure (useCsrfSecure);
            response.addCookie (csrfCookie);
            return Optional.of (cookieCode);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException ("No such algorithm [" + ALGORITHM + "]");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.rpc.extdirect.csrf.ICRFHandler#token(jakarta.servlet.http.HttpServletRequest)
     */
    public Optional<String> token(HttpServletRequest request) {
        String token = (String) request.getSession ().getAttribute (CSRF_SESSION);
        return Optional.ofNullable (token);
    }

}
