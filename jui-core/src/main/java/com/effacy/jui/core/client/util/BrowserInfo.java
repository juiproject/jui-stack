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
package com.effacy.jui.core.client.util;

public final class BrowserInfo {

    /**
     * Is {@code true} if the browser uses the webkit engine.
     */
    public static boolean isWebKit;

    /**
     * Is {@code true} if the browser is safari.
     */
    public static boolean isSafari;

    /**
     * Is {@code true} if the browser is safari2.
     */
    public static boolean isSafari2;

    /**
     * Is {@code true} if the browser is safari3.
     */
    public static boolean isSafari3;

    /**
     * Is {@code true} if the browser is safari4.
     */
    public static boolean isSafari4;

    /**
     * Is {@code true} if the browser is chrome.
     */
    public static boolean isChrome;

    /**
     * Is {@code true} if the browser is opera.
     */
    public static boolean isOpera;

    /**
     * Is {@code true} if the browser is ie.
     */
    public static boolean isIE;

    /**
     * Is {@code true} if the browser is ie6.
     */
    public static boolean isIE6;

    /**
     * Is {@code true} if the browser is ie7.
     */
    public static boolean isIE7;

    /**
     * Is {@code true} if the browser is ie8.
     */
    public static boolean isIE8;

    /**
     * Is {@code true} if the browser is ie9.
     */
    public static boolean isIE9;

    /**
     * Is {@code true} if the browser is ie10.
     */
    public static boolean isIE10;

    /**
     * Is {@code true} if the browser is gecko.
     */
    public static boolean isGecko;

    /**
     * Is {@code true} if the browser is gecko2.
     */
    public static boolean isGecko2;

    /**
     * Is {@code true} if the browser is gecko3.
     */
    public static boolean isGecko3;

    /**
     * Is {@code true} if the browser is gecko3.5.
     */
    public static boolean isGecko35;

    /**
     * Is {@code true} if the browser is in strict mode.
     */
    public static boolean isStrict;

    /**
     * Is {@code true} if using https.
     */
    public static boolean isSecure;

    /**
     * Is {@code true} if mac os.
     */
    public static boolean isMac;

    /**
     * Is {@code true} if linux os.
     */
    public static boolean isLinux;

    /**
     * Is {@code true} if windows os.
     */
    public static boolean isWindows;

    /**
     * Is {@code true} if is air.
     */
    public static boolean isAir;

    /**
     * Flag indicating if the info has been initialized.
     */
    private static boolean INITIALIZED = false;


    /**
     * Returns the user agent.
     * 
     * @return The user agent.
     */
    public native static String getUserAgent() /*-{
		return $wnd.navigator.userAgent.toLowerCase();
    }-*/;


    /**
     * Initialises the browser info.
     */
    public static void init() {
        if (INITIALIZED)
            return;
        INITIALIZED = true;

        String userAgent = getUserAgent ();

        isOpera = userAgent.contains ("opera");
        isIE = !isOpera && userAgent.contains ("msie");
        isIE7 = !isOpera && userAgent.contains ("msie 7");
        isIE8 = !isOpera && userAgent.contains ("msie 8");
        isIE9 = !isOpera && userAgent.contains ("msie 9");
        isIE10 = !isOpera && userAgent.contains ("msie 10");
        isIE6 = isIE && !isIE7 && !isIE8 && !isIE9 && !isIE10;

        isChrome = !isIE && userAgent.contains ("chrome");

        isWebKit = userAgent.contains ("webkit");

        isSafari = !isChrome && userAgent.contains ("safari");
        isSafari3 = isSafari && userAgent.contains ("version/3");
        isSafari4 = isSafari && userAgent.contains ("version/4");
        isSafari2 = isSafari && !isSafari3 && !isSafari4;

        isGecko = !isWebKit && userAgent.contains ("gecko");

        isGecko35 = isGecko && userAgent.contains ("rv:1.9.1");
        isGecko3 = isGecko && userAgent.contains ("rv:1.9.");

        isGecko2 = isGecko && !isGecko3;

        isWindows = (userAgent.contains ("windows") || userAgent.contains ("win32"));
        isMac = (userAgent.contains ("macintosh") || userAgent.contains ("mac os x"));
        isAir = (userAgent.contains ("adobeair"));
        isLinux = (userAgent.contains ("linux"));
    }


    /**
     * Private constructor.
     */
    private BrowserInfo() {
        // Nothing.
    }

}
