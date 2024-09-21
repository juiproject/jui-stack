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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.web.csrf.CsrfToken;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Implements the Xor encoding of a CSRF token as used by Spring.
 * <p>
 * The token is XOR'd with a random collection of bytes then expanded by that
 * random collection (then encoded).
 *
 * @author Jeremy Buckley
 */
public class XorCSRFHandler extends StandardCSRFHandler {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.rpc.extdirect.csrf.ICRFHandler#generate(jakarta.servlet.http.HttpServletRequest,
     *      jakarta.servlet.http.HttpServletResponse)
     */
    @Override
    public Optional<String> generate(HttpServletRequest request, HttpServletResponse response) {
        // First try to extract the token from the underlying source. This may generate
        // a new token if need be.
        CsrfToken csrfToken = (CsrfToken) request.getAttribute (CsrfToken.class.getName ());
        String token = csrfToken.getToken ();
        if (!StringUtils.isBlank (token)) {
            Cookie csrfCookie = new Cookie (CSRF_COOKIE, token);
            csrfCookie.setHttpOnly (useCsrfHttpOnly);
            csrfCookie.setSecure (useCsrfSecure);
            response.addCookie (csrfCookie);
            return Optional.of (token);
        }
        return Optional.empty ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.rpc.extdirect.csrf.ICRFHandler#token(jakarta.servlet.http.HttpServletRequest)
     */
    @Override
    public Optional<String> token(HttpServletRequest request) {
        return Optional.empty ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.rpc.extdirect.csrf.ICRFHandler#decode(java.lang.String)
     */
    @Override
    public boolean validate(String passedToken, String token) {
        byte[] actualBytes;
        try {
            actualBytes = Base64.getUrlDecoder ().decode (passedToken);
        } catch (Exception ex) {
            return false;
        }

        byte[] tokenBytes = utf8encode (token);
        int tokenSize = tokenBytes.length;
        if (actualBytes.length < tokenSize) {
            return false;
        }

        // extract token and random bytes
        int randomBytesSize = actualBytes.length - tokenSize;
        byte[] xoredCsrf = new byte[tokenSize];
        byte[] randomBytes = new byte[randomBytesSize];

        System.arraycopy (actualBytes, 0, randomBytes, 0, randomBytesSize);
        System.arraycopy (actualBytes, randomBytesSize, xoredCsrf, 0, tokenSize);

        byte[] csrfBytes = xorCsrf (randomBytes, xoredCsrf);
        if (tokenBytes.length != csrfBytes.length)
            return false;
        for (int i = 0; i < tokenBytes.length; i++) {
            if (tokenBytes[i] != csrfBytes[i])
                return false;
        }
        return true;
    }

    /**
     * Get the bytes of the String in UTF-8 encoded form.
     */
    private byte[] utf8encode(CharSequence string) {
        try {
            ByteBuffer bytes = CHARSET.newEncoder ().encode (CharBuffer.wrap (string));
            byte[] bytesCopy = new byte[bytes.limit ()];
            System.arraycopy (bytes.array (), 0, bytesCopy, 0, bytes.limit ());
            return bytesCopy;
        } catch (CharacterCodingException ex) {
            throw new IllegalArgumentException ("Encoding failed", ex);
        }
    }

    private byte[] xorCsrf(byte[] randomBytes, byte[] csrfBytes) {
        int len = Math.min (randomBytes.length, csrfBytes.length);
        byte[] xoredCsrf = new byte[len];
        System.arraycopy (csrfBytes, 0, xoredCsrf, 0, csrfBytes.length);
        for (int i = 0; i < len; i++) {
            xoredCsrf[i] ^= randomBytes[i];
        }
        return xoredCsrf;
    }

}
