package com.effacy.jui.ui.client.control;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Factory methods for common {@link AvatarSelectorControl.IAvatarProvider} implementations.
 *
 * @see AvatarSelectorControl.IAvatarProvider
 * @see AvatarSelectorControl
 */
public class AvatarProviders {

    /************************************************************************
     * DiceBear style constants.
     ************************************************************************/

    public static final String DICEBEAR_ADVENTURER = "adventurer";
    public static final String DICEBEAR_ADVENTURER_NEUTRAL = "adventurer-neutral";
    public static final String DICEBEAR_AVATAAARS = "avataaars";
    public static final String DICEBEAR_AVATAAARS_NEUTRAL = "avataaars-neutral";
    public static final String DICEBEAR_BIG_EARS = "big-ears";
    public static final String DICEBEAR_BIG_EARS_NEUTRAL = "big-ears-neutral";
    public static final String DICEBEAR_BIG_SMILE = "big-smile";
    public static final String DICEBEAR_BOTTTS = "bottts";
    public static final String DICEBEAR_BOTTTS_NEUTRAL = "bottts-neutral";
    public static final String DICEBEAR_CROODLES = "croodles";
    public static final String DICEBEAR_CROODLES_NEUTRAL = "croodles-neutral";
    public static final String DICEBEAR_DYLAN = "dylan";
    public static final String DICEBEAR_FUN_EMOJI = "fun-emoji";
    public static final String DICEBEAR_GLASS = "glass";
    public static final String DICEBEAR_ICONS = "icons";
    public static final String DICEBEAR_IDENTICON = "identicon";
    public static final String DICEBEAR_INITIALS = "initials";
    public static final String DICEBEAR_LORELEI = "lorelei";
    public static final String DICEBEAR_LORELEI_NEUTRAL = "lorelei-neutral";
    public static final String DICEBEAR_MICAH = "micah";
    public static final String DICEBEAR_MINIAVS = "miniavs";
    public static final String DICEBEAR_NOTIONISTS = "notionists";
    public static final String DICEBEAR_NOTIONISTS_NEUTRAL = "notionists-neutral";
    public static final String DICEBEAR_OPEN_PEEPS = "open-peeps";
    public static final String DICEBEAR_PERSONAS = "personas";
    public static final String DICEBEAR_PIXEL_ART = "pixel-art";
    public static final String DICEBEAR_PIXEL_ART_NEUTRAL = "pixel-art-neutral";
    public static final String DICEBEAR_RINGS = "rings";
    public static final String DICEBEAR_SHAPES = "shapes";
    public static final String DICEBEAR_THUMBS = "thumbs";
    public static final String DICEBEAR_TOON_HEAD = "toon-head";

    /************************************************************************
     * Providers.
     ************************************************************************/

    /**
     * Creates a DiceBear avatar provider.
     *
     * @param style
     *              the DiceBear style collection (e.g. "avataaars", "bottts",
     *              "initials").
     * @param seed
     *              the seed value to generate consistent avatars.
     * @param count
     *              the number of avatars to generate.
     * @return the provider.
     */
    public static AvatarSelectorControl.IAvatarProvider diceBear(String style, String seed, int count) {
        String fSeed = (seed == null) ? "avatars" : seed.trim().toLowerCase();
        return (imageSize, context) -> {
            List<String> urls = new ArrayList<>();
            for (int i = 1; i <= count; i++)
                urls.add("https://api.dicebear.com/9.x/" + style + "/svg?seed=" + fSeed + i);
            return urls;
        };
    }

    /**
     * See {@link #diceBear(String, String, int)} with no seed value.
     */
    public static AvatarSelectorControl.IAvatarProvider diceBear(String style, int count) {
        return diceBear(style, null, count);
    }

    /**
     * See {@link #diceBear(String, String, int)} with no seed value and 12 avatars.
     */
    public static AvatarSelectorControl.IAvatarProvider diceBear(String style) {
        return diceBear(style, null, 12);
    }

    /**
     * Creates a RoboHash avatar provider.
     *
     * @param set
     *            the robot set (1 through 5).
     * @param count
     *              the number of avatars to generate.
     * @return the provider.
     */
    public static AvatarSelectorControl.IAvatarProvider roboHash(int set, int count) {
        return (imageSize, context) -> {
            List<String> urls = new ArrayList<>();
            for (int i = 1; i <= count; i++)
                urls.add("https://robohash.org/avatar" + i + ".png?size=" + imageSize + "x" + imageSize + "&set=set" + set);
            return urls;
        };
    }

    /**
     * Creates a RoboHash avatar provider with 12 avatars.
     *
     * @param set
     *            the robot set (1 through 5).
     * @return the provider.
     */
    public static AvatarSelectorControl.IAvatarProvider roboHash(int set) {
        return roboHash(set, 12);
    }

    /**
     * See {@link #gravatar(boolean)} with fallback styles included.
     *
     * @return the provider.
     */
    public static AvatarSelectorControl.IAvatarProvider gravatar() {
        return gravatar(true);
    }

    /**
     * Creates a Gravatar provider that generates URLs using the user's email
     * hash plus built-in fallback styles (identicon, monsterid, wavatar,
     * retro, robohash).
     * <p>
     * Returns an empty list if no email is available in the context.
     *
     * @param includeFallbacks
     *                         whether to include fallback styles in the generated
     *                         URLs.
     * @return the provider.
     */
    public static AvatarSelectorControl.IAvatarProvider gravatar(boolean includeFallbacks) {
        String[] fallbacks = { "identicon", "monsterid", "wavatar", "retro", "robohash" };
        return (imageSize, context) -> {
            if ((context == null) || (context.email() == null) || context.email().isEmpty())
                return Collections.emptyList();
            String hash = md5Hex(context.email().trim().toLowerCase());
            if (hash.isEmpty())
                return Collections.emptyList();
            List<String> urls = new ArrayList<>();
            String base = "https://www.gravatar.com/avatar/" + hash + "?s=" + imageSize;
            // Primary gravatar (actual user image or mystery-person default).
            urls.add(base + "&d=mp");
            // Fallback style variations (forced so each shows its distinct style).
            if (includeFallbacks) {
                for (String style : fallbacks)
                    urls.add(base + "&d=" + style + "&f=y");
            }
            return urls;
        };
    }

    /************************************************************************
     * Utilities.
     ************************************************************************/

    /**
     * Computes the MD5 hex digest of the given input string.
     */
    private static String md5Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}