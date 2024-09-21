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
package com.effacy.jui.ui.client.icon;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.effacy.jui.platform.css.client.CssDeclaration;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.core.shared.GwtIncompatible;

import elemental2.dom.Element;

/**
 * Injects the FontAwesome (free) font family.
 * <p>
 * See the {@link README.md} file in this package for instructions on upgrading.
 * <p>
 * For licensing information see {@link https://github.com/FortAwesome/Font-Awesome}.
 *
 * @author Jeremy Buckley
 */
public class FontAwesome {

    /**
     * FontAwesome version.
     */
    public static final String VERSION = "6.4.2";

    /**
     * Determines if it has been injected.
     */
    private static boolean INJECTED = false;

    /**
     * The font families declared may or may not exist depending on whether you are
     * using the free or professional version.
     */
    @CssResource ({ "fontawesome.css", "fa-brands.css", "fa-regular.css", "fa-solid.css" })
    @CssResource.Font (name = "Font Awesome 6 Brands", noinline = true, weight = "400", sources = { "fa-brands-400.woff2" })
    @CssResource.Font (name = "Font Awesome 6 Free", noinline = true, weight = "100", optional = true, sources = { "fa-regular-100.woff2" })
    @CssResource.Font (name = "Font Awesome 6 Free", noinline = true, weight = "300", optional = true, sources = { "fa-regular-300.woff2" })
    @CssResource.Font (name = "Font Awesome 6 Free", noinline = true, weight = "400", sources = { "fa-regular-400.woff2" })
    @CssResource.Font (name = "Font Awesome 6 Free", noinline = true, weight = "900", sources = { "fa-solid-900.woff2" })
    @CssResource.Font (name = "Font Awesome 6 Duotone", noinline = true, weight = "900", optional = true, sources = { "fa-duotone-900.woff2" })
    @CssResource.Font (name = "Font Awesome 6 Sharp", noinline = true, weight = "300", optional = true, sources = { "fa-sharp-300.woff2" })
    @CssResource.Font (name = "Font Awesome 6 Sharp", noinline = true, weight = "400", optional = true, sources = { "fa-sharp-400.woff2" })
    @CssResource.Font (name = "Font Awesome 6 Sharp", noinline = true, weight = "900", optional = true, sources = { "fa-sharp-900.woff2" })
    public static abstract class FontCSS implements CssDeclaration {}

    /**
     * Instance of the font family.
     */
    private static final FontCSS INSTANCE = (FontCSS) GWT.create (FontCSS.class);

    /**
     * Private constructor.
     */
    private FontAwesome() {
        // Nothing.
    }

    /**
     * Inject the font to make it available.
     */
    public static final void inject() {
        if (!INJECTED) {
            INSTANCE.ensureInjected ();
            INJECTED = true;
        }
    }

    /**
     * Determines if it has been injected or not.
     * 
     * @return {@code true} if it has.
     */
    public static boolean isInjected() {
        return INJECTED;
    }

    /**
     * Various font styling options.
     */
    public enum Option {
        /**
         * The bold (solid) icon variant (the default).
         */
        BOLD,

        /**
         * The regular icon variant (not all icons are supplied with this in the free version).
         */
        REGULAR,

        /**
         * The light icon variant (not icons are supplied with this in the free
         * version).
         */
        LIGHT,

        /**
         * The thin icon variant (not icons are supplied with this in the free
         * version).
         */
        THIN,

        /**
         * Flip the icon horizonatally.
         */
        FLIP_HORIZONTAL,

        /**
         * Flip the icon vertically.
         */
        FLIP_VERTICAL,

        /**
         * Display the icon with the spinning effect.
         */
        SPIN,

        /**
         * Display the icon with the spinning effect (the the reverse direction).
         */
        SPIN_REVERSE,

        /**
         * Display the icon with the spinning pulse effect.
         */
        SPIN_PULSE,

        /**
         * Display with a beat effect.
         */
        BEAT,

        /**
         * Display with a beat effect.
         */
        BEAT_FADE,

        /**
         * Display with a bounce effect.
         */
        BOUNCE,

        /**
         * Display with a fade effect.
         */
        FADE,

        /**
         * Display with a shake effect.
         */
        SHAKE,

        /**
         * Display with a flipping effect.
         */
        FLIP,

        /**
         * Display with a border.
         */
        BORDER;
    }

    /************************************************************************
     * Support
     ************************************************************************/

    /**
     * Convenience to apply a collection of classes, separated by spaces, to a raw
     * element.
     * 
     * @param el
     *                the element to apply to.
     * @param classes
     *                the classes to apply.
     * @return the passed element.
     */
    public static Element apply(Element el, String classes) {
        if ((el != null) && !StringSupport.empty (classes)) {
            for (String css : classes.split(" ")) {
                css = css.trim();
                if (!StringSupport.empty (css))
                    el.classList.add (css);
            }
        }
        return el;
    }

    /************************************************************************
     * Icons
     ************************************************************************/

    public static String fillDrip(Option... options) {
        return format ("fill-drip", options);
    }

    public static String arrowsToCircle(Option... options) {
        return format ("arrows-to-circle", options);
    }

    public static String circleChevronRight(Option... options) {
        return format ("circle-chevron-right", options);
    }

    public static String chevronCircleRight(Option... options) {
        return format ("chevron-circle-right", options);
    }

    public static String at(Option... options) {
        return format ("at", options);
    }

    public static String trashCan(Option... options) {
        return format ("trash-can", options);
    }

    public static String trashAlt(Option... options) {
        return format ("trash-alt", options);
    }

    public static String textHeight(Option... options) {
        return format ("text-height", options);
    }

    public static String userXmark(Option... options) {
        return format ("user-xmark", options);
    }

    public static String userTimes(Option... options) {
        return format ("user-times", options);
    }

    public static String stethoscope(Option... options) {
        return format ("stethoscope", options);
    }

    public static String message(Option... options) {
        return format ("message", options);
    }

    public static String commentAlt(Option... options) {
        return format ("comment-alt", options);
    }

    public static String info(Option... options) {
        return format ("info", options);
    }

    public static String downLeftAndUpRightToCenter(Option... options) {
        return format ("down-left-and-up-right-to-center", options);
    }

    public static String compressAlt(Option... options) {
        return format ("compress-alt", options);
    }

    public static String explosion(Option... options) {
        return format ("explosion", options);
    }

    public static String fileLines(Option... options) {
        return format ("file-lines", options);
    }

    public static String fileAlt(Option... options) {
        return format ("file-alt", options);
    }

    public static String fileText(Option... options) {
        return format ("file-text", options);
    }

    public static String waveSquare(Option... options) {
        return format ("wave-square", options);
    }

    public static String ring(Option... options) {
        return format ("ring", options);
    }

    public static String buildingUn(Option... options) {
        return format ("building-un", options);
    }

    public static String diceThree(Option... options) {
        return format ("dice-three", options);
    }

    public static String calendarDays(Option... options) {
        return format ("calendar-days", options);
    }

    public static String calendarAlt(Option... options) {
        return format ("calendar-alt", options);
    }

    public static String anchorCircleCheck(Option... options) {
        return format ("anchor-circle-check", options);
    }

    public static String buildingCircleArrowRight(Option... options) {
        return format ("building-circle-arrow-right", options);
    }

    public static String volleyball(Option... options) {
        return format ("volleyball", options);
    }

    public static String volleyballBall(Option... options) {
        return format ("volleyball-ball", options);
    }

    public static String arrowsUpToLine(Option... options) {
        return format ("arrows-up-to-line", options);
    }

    public static String sortDown(Option... options) {
        return format ("sort-down", options);
    }

    public static String sortDesc(Option... options) {
        return format ("sort-desc", options);
    }

    public static String circleMinus(Option... options) {
        return format ("circle-minus", options);
    }

    public static String minusCircle(Option... options) {
        return format ("minus-circle", options);
    }

    public static String doorOpen(Option... options) {
        return format ("door-open", options);
    }

    public static String rightFromBracket(Option... options) {
        return format ("right-from-bracket", options);
    }

    public static String signOutAlt(Option... options) {
        return format ("sign-out-alt", options);
    }

    public static String atom(Option... options) {
        return format ("atom", options);
    }

    public static String soap(Option... options) {
        return format ("soap", options);
    }

    public static String icons(Option... options) {
        return format ("icons", options);
    }

    public static String heartMusicCameraBolt(Option... options) {
        return format ("heart-music-camera-bolt", options);
    }

    public static String microphoneLinesSlash(Option... options) {
        return format ("microphone-lines-slash", options);
    }

    public static String microphoneAltSlash(Option... options) {
        return format ("microphone-alt-slash", options);
    }

    public static String bridgeCircleCheck(Option... options) {
        return format ("bridge-circle-check", options);
    }

    public static String pumpMedical(Option... options) {
        return format ("pump-medical", options);
    }

    public static String fingerprint(Option... options) {
        return format ("fingerprint", options);
    }

    public static String handPointRight(Option... options) {
        return format ("hand-point-right", options);
    }

    public static String magnifyingGlassLocation(Option... options) {
        return format ("magnifying-glass-location", options);
    }

    public static String searchLocation(Option... options) {
        return format ("search-location", options);
    }

    public static String forwardStep(Option... options) {
        return format ("forward-step", options);
    }

    public static String stepForward(Option... options) {
        return format ("step-forward", options);
    }

    public static String faceSmileBeam(Option... options) {
        return format ("face-smile-beam", options);
    }

    public static String smileBeam(Option... options) {
        return format ("smile-beam", options);
    }

    public static String flagCheckered(Option... options) {
        return format ("flag-checkered", options);
    }

    public static String football(Option... options) {
        return format ("football", options);
    }

    public static String footballBall(Option... options) {
        return format ("football-ball", options);
    }

    public static String schoolCircleExclamation(Option... options) {
        return format ("school-circle-exclamation", options);
    }

    public static String crop(Option... options) {
        return format ("crop", options);
    }

    public static String anglesDown(Option... options) {
        return format ("angles-down", options);
    }

    public static String angleDoubleDown(Option... options) {
        return format ("angle-double-down", options);
    }

    public static String usersRectangle(Option... options) {
        return format ("users-rectangle", options);
    }

    public static String peopleRoof(Option... options) {
        return format ("people-roof", options);
    }

    public static String peopleLine(Option... options) {
        return format ("people-line", options);
    }

    public static String beerMugEmpty(Option... options) {
        return format ("beer-mug-empty", options);
    }

    public static String beer(Option... options) {
        return format ("beer", options);
    }

    public static String diagramPredecessor(Option... options) {
        return format ("diagram-predecessor", options);
    }

    public static String arrowUpLong(Option... options) {
        return format ("arrow-up-long", options);
    }

    public static String longArrowUp(Option... options) {
        return format ("long-arrow-up", options);
    }

    public static String fireFlameSimple(Option... options) {
        return format ("fire-flame-simple", options);
    }

    public static String burn(Option... options) {
        return format ("burn", options);
    }

    public static String person(Option... options) {
        return format ("person", options);
    }

    public static String male(Option... options) {
        return format ("male", options);
    }

    public static String laptop(Option... options) {
        return format ("laptop", options);
    }

    public static String fileCsv(Option... options) {
        return format ("file-csv", options);
    }

    public static String menorah(Option... options) {
        return format ("menorah", options);
    }

    public static String truckPlane(Option... options) {
        return format ("truck-plane", options);
    }

    public static String recordVinyl(Option... options) {
        return format ("record-vinyl", options);
    }

    public static String faceGrinStars(Option... options) {
        return format ("face-grin-stars", options);
    }

    public static String grinStars(Option... options) {
        return format ("grin-stars", options);
    }

    public static String bong(Option... options) {
        return format ("bong", options);
    }

    public static String spaghettiMonsterFlying(Option... options) {
        return format ("spaghetti-monster-flying", options);
    }

    public static String pastafarianism(Option... options) {
        return format ("pastafarianism", options);
    }

    public static String arrowDownUpAcrossLine(Option... options) {
        return format ("arrow-down-up-across-line", options);
    }

    public static String spoon(Option... options) {
        return format ("spoon", options);
    }

    public static String utensilSpoon(Option... options) {
        return format ("utensil-spoon", options);
    }

    public static String jarWheat(Option... options) {
        return format ("jar-wheat", options);
    }

    public static String envelopesBulk(Option... options) {
        return format ("envelopes-bulk", options);
    }

    public static String mailBulk(Option... options) {
        return format ("mail-bulk", options);
    }

    public static String fileCircleExclamation(Option... options) {
        return format ("file-circle-exclamation", options);
    }

    public static String circleH(Option... options) {
        return format ("circle-h", options);
    }

    public static String hospitalSymbol(Option... options) {
        return format ("hospital-symbol", options);
    }

    public static String pager(Option... options) {
        return format ("pager", options);
    }

    public static String addressBook(Option... options) {
        return format ("address-book", options);
    }

    public static String contactBook(Option... options) {
        return format ("contact-book", options);
    }

    public static String strikethrough(Option... options) {
        return format ("strikethrough", options);
    }

    public static String landmarkFlag(Option... options) {
        return format ("landmark-flag", options);
    }

    public static String pencil(Option... options) {
        return format ("pencil", options);
    }

    public static String pencilAlt(Option... options) {
        return format ("pencil-alt", options);
    }

    public static String backward(Option... options) {
        return format ("backward", options);
    }

    public static String caretRight(Option... options) {
        return format ("caret-right", options);
    }

    public static String comments(Option... options) {
        return format ("comments", options);
    }

    public static String paste(Option... options) {
        return format ("paste", options);
    }

    public static String fileClipboard(Option... options) {
        return format ("file-clipboard", options);
    }

    public static String codePullRequest(Option... options) {
        return format ("code-pull-request", options);
    }

    public static String clipboardList(Option... options) {
        return format ("clipboard-list", options);
    }

    public static String truckRampBox(Option... options) {
        return format ("truck-ramp-box", options);
    }

    public static String truckLoading(Option... options) {
        return format ("truck-loading", options);
    }

    public static String userCheck(Option... options) {
        return format ("user-check", options);
    }

    public static String vialVirus(Option... options) {
        return format ("vial-virus", options);
    }

    public static String sheetPlastic(Option... options) {
        return format ("sheet-plastic", options);
    }

    public static String blog(Option... options) {
        return format ("blog", options);
    }

    public static String userNinja(Option... options) {
        return format ("user-ninja", options);
    }

    public static String personArrowUpFromLine(Option... options) {
        return format ("person-arrow-up-from-line", options);
    }

    public static String scrollTorah(Option... options) {
        return format ("scroll-torah", options);
    }

    public static String torah(Option... options) {
        return format ("torah", options);
    }

    public static String broomBall(Option... options) {
        return format ("broom-ball", options);
    }

    public static String quidditch(Option... options) {
        return format ("quidditch", options);
    }

    public static String quidditchBroomBall(Option... options) {
        return format ("quidditch-broom-ball", options);
    }

    public static String toggleOff(Option... options) {
        return format ("toggle-off", options);
    }

    public static String boxArchive(Option... options) {
        return format ("box-archive", options);
    }

    public static String archive(Option... options) {
        return format ("archive", options);
    }

    public static String personDrowning(Option... options) {
        return format ("person-drowning", options);
    }

    public static String arrowDown91(Option... options) {
        return format ("arrow-down-9-1", options);
    }

    public static String sortNumericDesc(Option... options) {
        return format ("sort-numeric-desc", options);
    }

    public static String sortNumericDownAlt(Option... options) {
        return format ("sort-numeric-down-alt", options);
    }

    public static String faceGrinTongueSquint(Option... options) {
        return format ("face-grin-tongue-squint", options);
    }

    public static String grinTongueSquint(Option... options) {
        return format ("grin-tongue-squint", options);
    }

    public static String sprayCan(Option... options) {
        return format ("spray-can", options);
    }

    public static String truckMonster(Option... options) {
        return format ("truck-monster", options);
    }

    public static String earthAfrica(Option... options) {
        return format ("earth-africa", options);
    }

    public static String globeAfrica(Option... options) {
        return format ("globe-africa", options);
    }

    public static String rainbow(Option... options) {
        return format ("rainbow", options);
    }

    public static String circleNotch(Option... options) {
        return format ("circle-notch", options);
    }

    public static String tabletScreenButton(Option... options) {
        return format ("tablet-screen-button", options);
    }

    public static String tabletAlt(Option... options) {
        return format ("tablet-alt", options);
    }

    public static String paw(Option... options) {
        return format ("paw", options);
    }

    public static String cloud(Option... options) {
        return format ("cloud", options);
    }

    public static String trowelBricks(Option... options) {
        return format ("trowel-bricks", options);
    }

    public static String faceFlushed(Option... options) {
        return format ("face-flushed", options);
    }

    public static String flushed(Option... options) {
        return format ("flushed", options);
    }

    public static String hospitalUser(Option... options) {
        return format ("hospital-user", options);
    }

    public static String tentArrowLeftRight(Option... options) {
        return format ("tent-arrow-left-right", options);
    }

    public static String gavel(Option... options) {
        return format ("gavel", options);
    }

    public static String legal(Option... options) {
        return format ("legal", options);
    }

    public static String binoculars(Option... options) {
        return format ("binoculars", options);
    }

    public static String microphoneSlash(Option... options) {
        return format ("microphone-slash", options);
    }

    public static String boxTissue(Option... options) {
        return format ("box-tissue", options);
    }

    public static String motorcycle(Option... options) {
        return format ("motorcycle", options);
    }

    public static String bellConcierge(Option... options) {
        return format ("bell-concierge", options);
    }

    public static String conciergeBell(Option... options) {
        return format ("concierge-bell", options);
    }

    public static String penRuler(Option... options) {
        return format ("pen-ruler", options);
    }

    public static String pencilRuler(Option... options) {
        return format ("pencil-ruler", options);
    }

    public static String peopleArrows(Option... options) {
        return format ("people-arrows", options);
    }

    public static String peopleArrowsLeftRight(Option... options) {
        return format ("people-arrows-left-right", options);
    }

    public static String marsAndVenusBurst(Option... options) {
        return format ("mars-and-venus-burst", options);
    }

    public static String squareCaretRight(Option... options) {
        return format ("square-caret-right", options);
    }

    public static String caretSquareRight(Option... options) {
        return format ("caret-square-right", options);
    }

    public static String scissors(Option... options) {
        return format ("scissors", options);
    }

    public static String cut(Option... options) {
        return format ("cut", options);
    }

    public static String sunPlantWilt(Option... options) {
        return format ("sun-plant-wilt", options);
    }

    public static String toiletsPortable(Option... options) {
        return format ("toilets-portable", options);
    }

    public static String hockeyPuck(Option... options) {
        return format ("hockey-puck", options);
    }

    public static String table(Option... options) {
        return format ("table", options);
    }

    public static String magnifyingGlassArrowRight(Option... options) {
        return format ("magnifying-glass-arrow-right", options);
    }

    public static String tachographDigital(Option... options) {
        return format ("tachograph-digital", options);
    }

    public static String digitalTachograph(Option... options) {
        return format ("digital-tachograph", options);
    }

    public static String usersSlash(Option... options) {
        return format ("users-slash", options);
    }

    public static String clover(Option... options) {
        return format ("clover", options);
    }

    public static String reply(Option... options) {
        return format ("reply", options);
    }

    public static String mailReply(Option... options) {
        return format ("mail-reply", options);
    }

    public static String starAndCrescent(Option... options) {
        return format ("star-and-crescent", options);
    }

    public static String houseFire(Option... options) {
        return format ("house-fire", options);
    }

    public static String squareMinus(Option... options) {
        return format ("square-minus", options);
    }

    public static String minusSquare(Option... options) {
        return format ("minus-square", options);
    }

    public static String helicopter(Option... options) {
        return format ("helicopter", options);
    }

    public static String compass(Option... options) {
        return format ("compass", options);
    }

    public static String squareCaretDown(Option... options) {
        return format ("square-caret-down", options);
    }

    public static String caretSquareDown(Option... options) {
        return format ("caret-square-down", options);
    }

    public static String fileCircleQuestion(Option... options) {
        return format ("file-circle-question", options);
    }

    public static String laptopCode(Option... options) {
        return format ("laptop-code", options);
    }

    public static String swatchbook(Option... options) {
        return format ("swatchbook", options);
    }

    public static String prescriptionBottle(Option... options) {
        return format ("prescription-bottle", options);
    }

    public static String bars(Option... options) {
        return format ("bars", options);
    }

    public static String navicon(Option... options) {
        return format ("navicon", options);
    }

    public static String peopleGroup(Option... options) {
        return format ("people-group", options);
    }

    public static String hourglassEnd(Option... options) {
        return format ("hourglass-end", options);
    }

    public static String hourglass3(Option... options) {
        return format ("hourglass-3", options);
    }

    public static String heartCrack(Option... options) {
        return format ("heart-crack", options);
    }

    public static String heartBroken(Option... options) {
        return format ("heart-broken", options);
    }

    public static String squareUpRight(Option... options) {
        return format ("square-up-right", options);
    }

    public static String externalLinkSquareAlt(Option... options) {
        return format ("external-link-square-alt", options);
    }

    public static String faceKissBeam(Option... options) {
        return format ("face-kiss-beam", options);
    }

    public static String kissBeam(Option... options) {
        return format ("kiss-beam", options);
    }

    public static String film(Option... options) {
        return format ("film", options);
    }

    public static String rulerHorizontal(Option... options) {
        return format ("ruler-horizontal", options);
    }

    public static String peopleRobbery(Option... options) {
        return format ("people-robbery", options);
    }

    public static String lightbulb(Option... options) {
        return format ("lightbulb", options);
    }

    public static String caretLeft(Option... options) {
        return format ("caret-left", options);
    }

    public static String circleExclamation(Option... options) {
        return format ("circle-exclamation", options);
    }

    public static String exclamationCircle(Option... options) {
        return format ("exclamation-circle", options);
    }

    public static String schoolCircleXmark(Option... options) {
        return format ("school-circle-xmark", options);
    }

    public static String arrowRightFromBracket(Option... options) {
        return format ("arrow-right-from-bracket", options);
    }

    public static String signOut(Option... options) {
        return format ("sign-out", options);
    }

    public static String circleChevronDown(Option... options) {
        return format ("circle-chevron-down", options);
    }

    public static String chevronCircleDown(Option... options) {
        return format ("chevron-circle-down", options);
    }

    public static String unlockKeyhole(Option... options) {
        return format ("unlock-keyhole", options);
    }

    public static String unlockAlt(Option... options) {
        return format ("unlock-alt", options);
    }

    public static String cloudShowersHeavy(Option... options) {
        return format ("cloud-showers-heavy", options);
    }

    public static String headphonesSimple(Option... options) {
        return format ("headphones-simple", options);
    }

    public static String headphonesAlt(Option... options) {
        return format ("headphones-alt", options);
    }

    public static String sitemap(Option... options) {
        return format ("sitemap", options);
    }

    public static String circleDollarToSlot(Option... options) {
        return format ("circle-dollar-to-slot", options);
    }

    public static String donate(Option... options) {
        return format ("donate", options);
    }

    public static String memory(Option... options) {
        return format ("memory", options);
    }

    public static String roadSpikes(Option... options) {
        return format ("road-spikes", options);
    }

    public static String fireBurner(Option... options) {
        return format ("fire-burner", options);
    }

    public static String flag(Option... options) {
        return format ("flag", options);
    }

    public static String hanukiah(Option... options) {
        return format ("hanukiah", options);
    }

    public static String feather(Option... options) {
        return format ("feather", options);
    }

    public static String volumeLow(Option... options) {
        return format ("volume-low", options);
    }

    public static String volumeDown(Option... options) {
        return format ("volume-down", options);
    }

    public static String commentSlash(Option... options) {
        return format ("comment-slash", options);
    }

    public static String cloudSunRain(Option... options) {
        return format ("cloud-sun-rain", options);
    }

    public static String compress(Option... options) {
        return format ("compress", options);
    }

    public static String wheatAwn(Option... options) {
        return format ("wheat-awn", options);
    }

    public static String wheatAlt(Option... options) {
        return format ("wheat-alt", options);
    }

    public static String ankh(Option... options) {
        return format ("ankh", options);
    }

    public static String handsHoldingChild(Option... options) {
        return format ("hands-holding-child", options);
    }

    public static String asterisk(Option... options) {
        return format ("asterisk", options);
    }

    public static String squareCheck(Option... options) {
        return format ("square-check", options);
    }

    public static String checkSquare(Option... options) {
        return format ("check-square", options);
    }

    public static String pesetaSign(Option... options) {
        return format ("peseta-sign", options);
    }

    public static String heading(Option... options) {
        return format ("heading", options);
    }

    public static String header(Option... options) {
        return format ("header", options);
    }

    public static String ghost(Option... options) {
        return format ("ghost", options);
    }

    public static String list(Option... options) {
        return format ("list", options);
    }

    public static String listSquares(Option... options) {
        return format ("list-squares", options);
    }

    public static String squarePhoneFlip(Option... options) {
        return format ("square-phone-flip", options);
    }

    public static String phoneSquareAlt(Option... options) {
        return format ("phone-square-alt", options);
    }

    public static String cartPlus(Option... options) {
        return format ("cart-plus", options);
    }

    public static String gamepad(Option... options) {
        return format ("gamepad", options);
    }

    public static String circleDot(Option... options) {
        return format ("circle-dot", options);
    }

    public static String dotCircle(Option... options) {
        return format ("dot-circle", options);
    }

    public static String faceDizzy(Option... options) {
        return format ("face-dizzy", options);
    }

    public static String dizzy(Option... options) {
        return format ("dizzy", options);
    }

    public static String egg(Option... options) {
        return format ("egg", options);
    }

    public static String houseMedicalCircleXmark(Option... options) {
        return format ("house-medical-circle-xmark", options);
    }

    public static String campground(Option... options) {
        return format ("campground", options);
    }

    public static String folderPlus(Option... options) {
        return format ("folder-plus", options);
    }

    public static String futbol(Option... options) {
        return format ("futbol", options);
    }

    public static String futbolBall(Option... options) {
        return format ("futbol-ball", options);
    }

    public static String soccerBall(Option... options) {
        return format ("soccer-ball", options);
    }

    public static String paintbrush(Option... options) {
        return format ("paintbrush", options);
    }

    public static String paintBrush(Option... options) {
        return format ("paint-brush", options);
    }

    public static String lock(Option... options) {
        return format ("lock", options);
    }

    public static String gasPump(Option... options) {
        return format ("gas-pump", options);
    }

    public static String hotTubPerson(Option... options) {
        return format ("hot-tub-person", options);
    }

    public static String hotTub(Option... options) {
        return format ("hot-tub", options);
    }

    public static String mapLocation(Option... options) {
        return format ("map-location", options);
    }

    public static String mapMarked(Option... options) {
        return format ("map-marked", options);
    }

    public static String houseFloodWater(Option... options) {
        return format ("house-flood-water", options);
    }

    public static String tree(Option... options) {
        return format ("tree", options);
    }

    public static String bridgeLock(Option... options) {
        return format ("bridge-lock", options);
    }

    public static String sackDollar(Option... options) {
        return format ("sack-dollar", options);
    }

    public static String penToSquare(Option... options) {
        return format ("pen-to-square", options);
    }

    public static String edit(Option... options) {
        return format ("edit", options);
    }

    public static String carSide(Option... options) {
        return format ("car-side", options);
    }

    public static String shareNodes(Option... options) {
        return format ("share-nodes", options);
    }

    public static String shareAlt(Option... options) {
        return format ("share-alt", options);
    }

    public static String heartCircleMinus(Option... options) {
        return format ("heart-circle-minus", options);
    }

    public static String hourglassHalf(Option... options) {
        return format ("hourglass-half", options);
    }

    public static String hourglass2(Option... options) {
        return format ("hourglass-2", options);
    }

    public static String microscope(Option... options) {
        return format ("microscope", options);
    }

    public static String sink(Option... options) {
        return format ("sink", options);
    }

    public static String bagShopping(Option... options) {
        return format ("bag-shopping", options);
    }

    public static String shoppingBag(Option... options) {
        return format ("shopping-bag", options);
    }

    public static String arrowDownZA(Option... options) {
        return format ("arrow-down-z-a", options);
    }

    public static String sortAlphaDesc(Option... options) {
        return format ("sort-alpha-desc", options);
    }

    public static String sortAlphaDownAlt(Option... options) {
        return format ("sort-alpha-down-alt", options);
    }

    public static String mitten(Option... options) {
        return format ("mitten", options);
    }

    public static String personRays(Option... options) {
        return format ("person-rays", options);
    }

    public static String users(Option... options) {
        return format ("users", options);
    }

    public static String eyeSlash(Option... options) {
        return format ("eye-slash", options);
    }

    public static String flaskVial(Option... options) {
        return format ("flask-vial", options);
    }

    public static String hand(Option... options) {
        return format ("hand", options);
    }

    public static String handPaper(Option... options) {
        return format ("hand-paper", options);
    }

    public static String om(Option... options) {
        return format ("om", options);
    }

    public static String worm(Option... options) {
        return format ("worm", options);
    }

    public static String houseCircleXmark(Option... options) {
        return format ("house-circle-xmark", options);
    }

    public static String plug(Option... options) {
        return format ("plug", options);
    }

    public static String chevronUp(Option... options) {
        return format ("chevron-up", options);
    }

    public static String handSpock(Option... options) {
        return format ("hand-spock", options);
    }

    public static String stopwatch(Option... options) {
        return format ("stopwatch", options);
    }

    public static String faceKiss(Option... options) {
        return format ("face-kiss", options);
    }

    public static String kiss(Option... options) {
        return format ("kiss", options);
    }

    public static String bridgeCircleXmark(Option... options) {
        return format ("bridge-circle-xmark", options);
    }

    public static String faceGrinTongue(Option... options) {
        return format ("face-grin-tongue", options);
    }

    public static String grinTongue(Option... options) {
        return format ("grin-tongue", options);
    }

    public static String chessBishop(Option... options) {
        return format ("chess-bishop", options);
    }

    public static String faceGrinWink(Option... options) {
        return format ("face-grin-wink", options);
    }

    public static String grinWink(Option... options) {
        return format ("grin-wink", options);
    }

    public static String earDeaf(Option... options) {
        return format ("ear-deaf", options);
    }

    public static String deaf(Option... options) {
        return format ("deaf", options);
    }

    public static String deafness(Option... options) {
        return format ("deafness", options);
    }

    public static String hardOfHearing(Option... options) {
        return format ("hard-of-hearing", options);
    }

    public static String roadCircleCheck(Option... options) {
        return format ("road-circle-check", options);
    }

    public static String diceFive(Option... options) {
        return format ("dice-five", options);
    }

    public static String squareRss(Option... options) {
        return format ("square-rss", options);
    }

    public static String rssSquare(Option... options) {
        return format ("rss-square", options);
    }

    public static String landMineOn(Option... options) {
        return format ("land-mine-on", options);
    }

    public static String iCursor(Option... options) {
        return format ("i-cursor", options);
    }

    public static String stamp(Option... options) {
        return format ("stamp", options);
    }

    public static String stairs(Option... options) {
        return format ("stairs", options);
    }

    public static String hryvniaSign(Option... options) {
        return format ("hryvnia-sign", options);
    }

    public static String hryvnia(Option... options) {
        return format ("hryvnia", options);
    }

    public static String pills(Option... options) {
        return format ("pills", options);
    }

    public static String faceGrinWide(Option... options) {
        return format ("face-grin-wide", options);
    }

    public static String grinAlt(Option... options) {
        return format ("grin-alt", options);
    }

    public static String tooth(Option... options) {
        return format ("tooth", options);
    }

    public static String bangladeshiTakaSign(Option... options) {
        return format ("bangladeshi-taka-sign", options);
    }

    public static String bicycle(Option... options) {
        return format ("bicycle", options);
    }

    public static String staffSnake(Option... options) {
        return format ("staff-snake", options);
    }

    public static String rodAsclepius(Option... options) {
        return format ("rod-asclepius", options);
    }

    public static String rodSnake(Option... options) {
        return format ("rod-snake", options);
    }

    public static String staffAesculapius(Option... options) {
        return format ("staff-aesculapius", options);
    }

    public static String headSideCoughSlash(Option... options) {
        return format ("head-side-cough-slash", options);
    }

    public static String truckMedical(Option... options) {
        return format ("truck-medical", options);
    }

    public static String ambulance(Option... options) {
        return format ("ambulance", options);
    }

    public static String wheatAwnCircleExclamation(Option... options) {
        return format ("wheat-awn-circle-exclamation", options);
    }

    public static String snowman(Option... options) {
        return format ("snowman", options);
    }

    public static String mortarPestle(Option... options) {
        return format ("mortar-pestle", options);
    }

    public static String roadBarrier(Option... options) {
        return format ("road-barrier", options);
    }

    public static String school(Option... options) {
        return format ("school", options);
    }

    public static String igloo(Option... options) {
        return format ("igloo", options);
    }

    public static String joint(Option... options) {
        return format ("joint", options);
    }

    public static String angleRight(Option... options) {
        return format ("angle-right", options);
    }

    public static String horse(Option... options) {
        return format ("horse", options);
    }

    public static String notesMedical(Option... options) {
        return format ("notes-medical", options);
    }

    public static String temperatureHalf(Option... options) {
        return format ("temperature-half", options);
    }

    public static String temperature2(Option... options) {
        return format ("temperature-2", options);
    }

    public static String thermometer2(Option... options) {
        return format ("thermometer-2", options);
    }

    public static String thermometerHalf(Option... options) {
        return format ("thermometer-half", options);
    }

    public static String dongSign(Option... options) {
        return format ("dong-sign", options);
    }

    public static String capsules(Option... options) {
        return format ("capsules", options);
    }

    public static String pooStorm(Option... options) {
        return format ("poo-storm", options);
    }

    public static String pooBolt(Option... options) {
        return format ("poo-bolt", options);
    }

    public static String faceFrownOpen(Option... options) {
        return format ("face-frown-open", options);
    }

    public static String frownOpen(Option... options) {
        return format ("frown-open", options);
    }

    public static String handPointUp(Option... options) {
        return format ("hand-point-up", options);
    }

    public static String moneyBill(Option... options) {
        return format ("money-bill", options);
    }

    public static String bookmark(Option... options) {
        return format ("bookmark", options);
    }

    public static String alignJustify(Option... options) {
        return format ("align-justify", options);
    }

    public static String umbrellaBeach(Option... options) {
        return format ("umbrella-beach", options);
    }

    public static String helmetUn(Option... options) {
        return format ("helmet-un", options);
    }

    public static String bullseye(Option... options) {
        return format ("bullseye", options);
    }

    public static String bacon(Option... options) {
        return format ("bacon", options);
    }

    public static String handPointDown(Option... options) {
        return format ("hand-point-down", options);
    }

    public static String arrowUpFromBracket(Option... options) {
        return format ("arrow-up-from-bracket", options);
    }

    public static String folder(Option... options) {
        return format ("folder", options);
    }

    public static String folderBlank(Option... options) {
        return format ("folder-blank", options);
    }

    public static String fileWaveform(Option... options) {
        return format ("file-waveform", options);
    }

    public static String fileMedicalAlt(Option... options) {
        return format ("file-medical-alt", options);
    }

    public static String radiation(Option... options) {
        return format ("radiation", options);
    }

    public static String chartSimple(Option... options) {
        return format ("chart-simple", options);
    }

    public static String marsStroke(Option... options) {
        return format ("mars-stroke", options);
    }

    public static String vial(Option... options) {
        return format ("vial", options);
    }

    public static String gauge(Option... options) {
        return format ("gauge", options);
    }

    public static String dashboard(Option... options) {
        return format ("dashboard", options);
    }

    public static String gaugeMed(Option... options) {
        return format ("gauge-med", options);
    }

    public static String tachometerAltAverage(Option... options) {
        return format ("tachometer-alt-average", options);
    }

    public static String wandMagicSparkles(Option... options) {
        return format ("wand-magic-sparkles", options);
    }

    public static String magicWandSparkles(Option... options) {
        return format ("magic-wand-sparkles", options);
    }

    public static String penClip(Option... options) {
        return format ("pen-clip", options);
    }

    public static String penAlt(Option... options) {
        return format ("pen-alt", options);
    }

    public static String bridgeCircleExclamation(Option... options) {
        return format ("bridge-circle-exclamation", options);
    }

    public static String user(Option... options) {
        return format ("user", options);
    }

    public static String schoolCircleCheck(Option... options) {
        return format ("school-circle-check", options);
    }

    public static String dumpster(Option... options) {
        return format ("dumpster", options);
    }

    public static String vanShuttle(Option... options) {
        return format ("van-shuttle", options);
    }

    public static String shuttleVan(Option... options) {
        return format ("shuttle-van", options);
    }

    public static String buildingUser(Option... options) {
        return format ("building-user", options);
    }

    public static String squareCaretLeft(Option... options) {
        return format ("square-caret-left", options);
    }

    public static String caretSquareLeft(Option... options) {
        return format ("caret-square-left", options);
    }

    public static String highlighter(Option... options) {
        return format ("highlighter", options);
    }

    public static String key(Option... options) {
        return format ("key", options);
    }

    public static String bullhorn(Option... options) {
        return format ("bullhorn", options);
    }

    public static String globe(Option... options) {
        return format ("globe", options);
    }

    public static String synagogue(Option... options) {
        return format ("synagogue", options);
    }

    public static String personHalfDress(Option... options) {
        return format ("person-half-dress", options);
    }

    public static String roadBridge(Option... options) {
        return format ("road-bridge", options);
    }

    public static String locationArrow(Option... options) {
        return format ("location-arrow", options);
    }

    public static String tabletButton(Option... options) {
        return format ("tablet-button", options);
    }

    public static String buildingLock(Option... options) {
        return format ("building-lock", options);
    }

    public static String pizzaSlice(Option... options) {
        return format ("pizza-slice", options);
    }

    public static String moneyBillWave(Option... options) {
        return format ("money-bill-wave", options);
    }

    public static String chartArea(Option... options) {
        return format ("chart-area", options);
    }

    public static String areaChart(Option... options) {
        return format ("area-chart", options);
    }

    public static String houseFlag(Option... options) {
        return format ("house-flag", options);
    }

    public static String personCircleMinus(Option... options) {
        return format ("person-circle-minus", options);
    }

    public static String ban(Option... options) {
        return format ("ban", options);
    }

    public static String cancel(Option... options) {
        return format ("cancel", options);
    }

    public static String cameraRotate(Option... options) {
        return format ("camera-rotate", options);
    }

    public static String sprayCanSparkles(Option... options) {
        return format ("spray-can-sparkles", options);
    }

    public static String airFreshener(Option... options) {
        return format ("air-freshener", options);
    }

    public static String star(Option... options) {
        return format ("star", options);
    }

    public static String repeat(Option... options) {
        return format ("repeat", options);
    }

    public static String cross(Option... options) {
        return format ("cross", options);
    }

    public static String box(Option... options) {
        return format ("box", options);
    }

    public static String venusMars(Option... options) {
        return format ("venus-mars", options);
    }

    public static String arrowPointer(Option... options) {
        return format ("arrow-pointer", options);
    }

    public static String mousePointer(Option... options) {
        return format ("mouse-pointer", options);
    }

    public static String maximize(Option... options) {
        return format ("maximize", options);
    }

    public static String expandArrowsAlt(Option... options) {
        return format ("expand-arrows-alt", options);
    }

    public static String chargingStation(Option... options) {
        return format ("charging-station", options);
    }

    public static String shapes(Option... options) {
        return format ("shapes", options);
    }

    public static String triangleCircleSquare(Option... options) {
        return format ("triangle-circle-square", options);
    }

    public static String shuffle(Option... options) {
        return format ("shuffle", options);
    }

    public static String random(Option... options) {
        return format ("random", options);
    }

    public static String personRunning(Option... options) {
        return format ("person-running", options);
    }

    public static String running(Option... options) {
        return format ("running", options);
    }

    public static String mobileRetro(Option... options) {
        return format ("mobile-retro", options);
    }

    public static String gripLinesVertical(Option... options) {
        return format ("grip-lines-vertical", options);
    }

    public static String spider(Option... options) {
        return format ("spider", options);
    }

    public static String handsBound(Option... options) {
        return format ("hands-bound", options);
    }

    public static String fileInvoiceDollar(Option... options) {
        return format ("file-invoice-dollar", options);
    }

    public static String planeCircleExclamation(Option... options) {
        return format ("plane-circle-exclamation", options);
    }

    public static String xRay(Option... options) {
        return format ("x-ray", options);
    }

    public static String spellCheck(Option... options) {
        return format ("spell-check", options);
    }

    public static String slash(Option... options) {
        return format ("slash", options);
    }

    public static String computerMouse(Option... options) {
        return format ("computer-mouse", options);
    }

    public static String mouse(Option... options) {
        return format ("mouse", options);
    }

    public static String arrowRightToBracket(Option... options) {
        return format ("arrow-right-to-bracket", options);
    }

    public static String signIn(Option... options) {
        return format ("sign-in", options);
    }

    public static String shopSlash(Option... options) {
        return format ("shop-slash", options);
    }

    public static String storeAltSlash(Option... options) {
        return format ("store-alt-slash", options);
    }

    public static String server(Option... options) {
        return format ("server", options);
    }

    public static String virusCovidSlash(Option... options) {
        return format ("virus-covid-slash", options);
    }

    public static String shopLock(Option... options) {
        return format ("shop-lock", options);
    }

    public static String hourglassStart(Option... options) {
        return format ("hourglass-start", options);
    }

    public static String hourglass1(Option... options) {
        return format ("hourglass-1", options);
    }

    public static String blenderPhone(Option... options) {
        return format ("blender-phone", options);
    }

    public static String buildingWheat(Option... options) {
        return format ("building-wheat", options);
    }

    public static String personBreastfeeding(Option... options) {
        return format ("person-breastfeeding", options);
    }

    public static String rightToBracket(Option... options) {
        return format ("right-to-bracket", options);
    }

    public static String signInAlt(Option... options) {
        return format ("sign-in-alt", options);
    }

    public static String venus(Option... options) {
        return format ("venus", options);
    }

    public static String passport(Option... options) {
        return format ("passport", options);
    }

    public static String heartPulse(Option... options) {
        return format ("heart-pulse", options);
    }

    public static String heartbeat(Option... options) {
        return format ("heartbeat", options);
    }

    public static String peopleCarryBox(Option... options) {
        return format ("people-carry-box", options);
    }

    public static String peopleCarry(Option... options) {
        return format ("people-carry", options);
    }

    public static String temperatureHigh(Option... options) {
        return format ("temperature-high", options);
    }

    public static String microchip(Option... options) {
        return format ("microchip", options);
    }

    public static String crown(Option... options) {
        return format ("crown", options);
    }

    public static String weightHanging(Option... options) {
        return format ("weight-hanging", options);
    }

    public static String xmarksLines(Option... options) {
        return format ("xmarks-lines", options);
    }

    public static String filePrescription(Option... options) {
        return format ("file-prescription", options);
    }

    public static String weightScale(Option... options) {
        return format ("weight-scale", options);
    }

    public static String weight(Option... options) {
        return format ("weight", options);
    }

    public static String userGroup(Option... options) {
        return format ("user-group", options);
    }

    public static String userFriends(Option... options) {
        return format ("user-friends", options);
    }

    public static String arrowUpAZ(Option... options) {
        return format ("arrow-up-a-z", options);
    }

    public static String sortAlphaUp(Option... options) {
        return format ("sort-alpha-up", options);
    }

    public static String chessKnight(Option... options) {
        return format ("chess-knight", options);
    }

    public static String faceLaughSquint(Option... options) {
        return format ("face-laugh-squint", options);
    }

    public static String laughSquint(Option... options) {
        return format ("laugh-squint", options);
    }

    public static String wheelchair(Option... options) {
        return format ("wheelchair", options);
    }

    public static String circleArrowUp(Option... options) {
        return format ("circle-arrow-up", options);
    }

    public static String arrowCircleUp(Option... options) {
        return format ("arrow-circle-up", options);
    }

    public static String toggleOn(Option... options) {
        return format ("toggle-on", options);
    }

    public static String personWalking(Option... options) {
        return format ("person-walking", options);
    }

    public static String walking(Option... options) {
        return format ("walking", options);
    }

    public static String fire(Option... options) {
        return format ("fire", options);
    }

    public static String bedPulse(Option... options) {
        return format ("bed-pulse", options);
    }

    public static String procedures(Option... options) {
        return format ("procedures", options);
    }

    public static String shuttleSpace(Option... options) {
        return format ("shuttle-space", options);
    }

    public static String spaceShuttle(Option... options) {
        return format ("space-shuttle", options);
    }

    public static String faceLaugh(Option... options) {
        return format ("face-laugh", options);
    }

    public static String laugh(Option... options) {
        return format ("laugh", options);
    }

    public static String folderOpen(Option... options) {
        return format ("folder-open", options);
    }

    public static String heartCirclePlus(Option... options) {
        return format ("heart-circle-plus", options);
    }

    public static String codeFork(Option... options) {
        return format ("code-fork", options);
    }

    public static String city(Option... options) {
        return format ("city", options);
    }

    public static String microphoneLines(Option... options) {
        return format ("microphone-lines", options);
    }

    public static String microphoneAlt(Option... options) {
        return format ("microphone-alt", options);
    }

    public static String pepperHot(Option... options) {
        return format ("pepper-hot", options);
    }

    public static String unlock(Option... options) {
        return format ("unlock", options);
    }

    public static String colonSign(Option... options) {
        return format ("colon-sign", options);
    }

    public static String headset(Option... options) {
        return format ("headset", options);
    }

    public static String storeSlash(Option... options) {
        return format ("store-slash", options);
    }

    public static String roadCircleXmark(Option... options) {
        return format ("road-circle-xmark", options);
    }

    public static String userMinus(Option... options) {
        return format ("user-minus", options);
    }

    public static String marsStrokeUp(Option... options) {
        return format ("mars-stroke-up", options);
    }

    public static String marsStrokeV(Option... options) {
        return format ("mars-stroke-v", options);
    }

    public static String champagneGlasses(Option... options) {
        return format ("champagne-glasses", options);
    }

    public static String glassCheers(Option... options) {
        return format ("glass-cheers", options);
    }

    public static String clipboard(Option... options) {
        return format ("clipboard", options);
    }

    public static String houseCircleExclamation(Option... options) {
        return format ("house-circle-exclamation", options);
    }

    public static String fileArrowUp(Option... options) {
        return format ("file-arrow-up", options);
    }

    public static String fileUpload(Option... options) {
        return format ("file-upload", options);
    }

    public static String wifi(Option... options) {
        return format ("wifi", options);
    }

    public static String wifi3(Option... options) {
        return format ("wifi-3", options);
    }

    public static String wifiStrong(Option... options) {
        return format ("wifi-strong", options);
    }

    public static String bath(Option... options) {
        return format ("bath", options);
    }

    public static String bathtub(Option... options) {
        return format ("bathtub", options);
    }

    public static String underline(Option... options) {
        return format ("underline", options);
    }

    public static String userPen(Option... options) {
        return format ("user-pen", options);
    }

    public static String userEdit(Option... options) {
        return format ("user-edit", options);
    }

    public static String signature(Option... options) {
        return format ("signature", options);
    }

    public static String stroopwafel(Option... options) {
        return format ("stroopwafel", options);
    }

    public static String bold(Option... options) {
        return format ("bold", options);
    }

    public static String anchorLock(Option... options) {
        return format ("anchor-lock", options);
    }

    public static String buildingNgo(Option... options) {
        return format ("building-ngo", options);
    }

    public static String manatSign(Option... options) {
        return format ("manat-sign", options);
    }

    public static String notEqual(Option... options) {
        return format ("not-equal", options);
    }

    public static String borderTopLeft(Option... options) {
        return format ("border-top-left", options);
    }

    public static String borderStyle(Option... options) {
        return format ("border-style", options);
    }

    public static String mapLocationDot(Option... options) {
        return format ("map-location-dot", options);
    }

    public static String mapMarkedAlt(Option... options) {
        return format ("map-marked-alt", options);
    }

    public static String jedi(Option... options) {
        return format ("jedi", options);
    }

    public static String squarePollVertical(Option... options) {
        return format ("square-poll-vertical", options);
    }

    public static String poll(Option... options) {
        return format ("poll", options);
    }

    public static String mugHot(Option... options) {
        return format ("mug-hot", options);
    }

    public static String carBattery(Option... options) {
        return format ("car-battery", options);
    }

    public static String batteryCar(Option... options) {
        return format ("battery-car", options);
    }

    public static String gift(Option... options) {
        return format ("gift", options);
    }

    public static String diceTwo(Option... options) {
        return format ("dice-two", options);
    }

    public static String chessQueen(Option... options) {
        return format ("chess-queen", options);
    }

    public static String glasses(Option... options) {
        return format ("glasses", options);
    }

    public static String chessBoard(Option... options) {
        return format ("chess-board", options);
    }

    public static String buildingCircleCheck(Option... options) {
        return format ("building-circle-check", options);
    }

    public static String personChalkboard(Option... options) {
        return format ("person-chalkboard", options);
    }

    public static String marsStrokeRight(Option... options) {
        return format ("mars-stroke-right", options);
    }

    public static String marsStrokeH(Option... options) {
        return format ("mars-stroke-h", options);
    }

    public static String handBackFist(Option... options) {
        return format ("hand-back-fist", options);
    }

    public static String handRock(Option... options) {
        return format ("hand-rock", options);
    }

    public static String squareCaretUp(Option... options) {
        return format ("square-caret-up", options);
    }

    public static String caretSquareUp(Option... options) {
        return format ("caret-square-up", options);
    }

    public static String cloudShowersWater(Option... options) {
        return format ("cloud-showers-water", options);
    }

    public static String chartBar(Option... options) {
        return format ("chart-bar", options);
    }

    public static String barChart(Option... options) {
        return format ("bar-chart", options);
    }

    public static String handsBubbles(Option... options) {
        return format ("hands-bubbles", options);
    }

    public static String handsWash(Option... options) {
        return format ("hands-wash", options);
    }

    public static String lessThanEqual(Option... options) {
        return format ("less-than-equal", options);
    }

    public static String train(Option... options) {
        return format ("train", options);
    }

    public static String eyeLowVision(Option... options) {
        return format ("eye-low-vision", options);
    }

    public static String lowVision(Option... options) {
        return format ("low-vision", options);
    }

    public static String crow(Option... options) {
        return format ("crow", options);
    }

    public static String sailboat(Option... options) {
        return format ("sailboat", options);
    }

    public static String windowRestore(Option... options) {
        return format ("window-restore", options);
    }

    public static String squarePlus(Option... options) {
        return format ("square-plus", options);
    }

    public static String plusSquare(Option... options) {
        return format ("plus-square", options);
    }

    public static String toriiGate(Option... options) {
        return format ("torii-gate", options);
    }

    public static String frog(Option... options) {
        return format ("frog", options);
    }

    public static String bucket(Option... options) {
        return format ("bucket", options);
    }

    public static String image(Option... options) {
        return format ("image", options);
    }

    public static String microphone(Option... options) {
        return format ("microphone", options);
    }

    public static String cow(Option... options) {
        return format ("cow", options);
    }

    public static String caretUp(Option... options) {
        return format ("caret-up", options);
    }

    public static String screwdriver(Option... options) {
        return format ("screwdriver", options);
    }

    public static String folderClosed(Option... options) {
        return format ("folder-closed", options);
    }

    public static String houseTsunami(Option... options) {
        return format ("house-tsunami", options);
    }

    public static String squareNfi(Option... options) {
        return format ("square-nfi", options);
    }

    public static String arrowUpFromGroundWater(Option... options) {
        return format ("arrow-up-from-ground-water", options);
    }

    public static String martiniGlass(Option... options) {
        return format ("martini-glass", options);
    }

    public static String glassMartiniAlt(Option... options) {
        return format ("glass-martini-alt", options);
    }

    public static String rotateLeft(Option... options) {
        return format ("rotate-left", options);
    }

    public static String rotateBack(Option... options) {
        return format ("rotate-back", options);
    }

    public static String rotateBackward(Option... options) {
        return format ("rotate-backward", options);
    }

    public static String undoAlt(Option... options) {
        return format ("undo-alt", options);
    }

    public static String tableColumns(Option... options) {
        return format ("table-columns", options);
    }

    public static String columns(Option... options) {
        return format ("columns", options);
    }

    public static String lemon(Option... options) {
        return format ("lemon", options);
    }

    public static String headSideMask(Option... options) {
        return format ("head-side-mask", options);
    }

    public static String handshake(Option... options) {
        return format ("handshake", options);
    }

    public static String gem(Option... options) {
        return format ("gem", options);
    }

    public static String dolly(Option... options) {
        return format ("dolly", options);
    }

    public static String dollyBox(Option... options) {
        return format ("dolly-box", options);
    }

    public static String smoking(Option... options) {
        return format ("smoking", options);
    }

    public static String minimize(Option... options) {
        return format ("minimize", options);
    }

    public static String compressArrowsAlt(Option... options) {
        return format ("compress-arrows-alt", options);
    }

    public static String monument(Option... options) {
        return format ("monument", options);
    }

    public static String snowplow(Option... options) {
        return format ("snowplow", options);
    }

    public static String anglesRight(Option... options) {
        return format ("angles-right", options);
    }

    public static String angleDoubleRight(Option... options) {
        return format ("angle-double-right", options);
    }

    public static String cannabis(Option... options) {
        return format ("cannabis", options);
    }

    public static String circlePlay(Option... options) {
        return format ("circle-play", options);
    }

    public static String playCircle(Option... options) {
        return format ("play-circle", options);
    }

    public static String tablets(Option... options) {
        return format ("tablets", options);
    }

    public static String ethernet(Option... options) {
        return format ("ethernet", options);
    }

    public static String euroSign(Option... options) {
        return format ("euro-sign", options);
    }

    public static String eur(Option... options) {
        return format ("eur", options);
    }

    public static String euro(Option... options) {
        return format ("euro", options);
    }

    public static String chair(Option... options) {
        return format ("chair", options);
    }

    public static String circleCheck(Option... options) {
        return format ("circle-check", options);
    }

    public static String checkCircle(Option... options) {
        return format ("check-circle", options);
    }

    public static String circleStop(Option... options) {
        return format ("circle-stop", options);
    }

    public static String stopCircle(Option... options) {
        return format ("stop-circle", options);
    }

    public static String compassDrafting(Option... options) {
        return format ("compass-drafting", options);
    }

    public static String draftingCompass(Option... options) {
        return format ("drafting-compass", options);
    }

    public static String plateWheat(Option... options) {
        return format ("plate-wheat", options);
    }

    public static String icicles(Option... options) {
        return format ("icicles", options);
    }

    public static String personShelter(Option... options) {
        return format ("person-shelter", options);
    }

    public static String neuter(Option... options) {
        return format ("neuter", options);
    }

    public static String idBadge(Option... options) {
        return format ("id-badge", options);
    }

    public static String marker(Option... options) {
        return format ("marker", options);
    }

    public static String faceLaughBeam(Option... options) {
        return format ("face-laugh-beam", options);
    }

    public static String laughBeam(Option... options) {
        return format ("laugh-beam", options);
    }

    public static String helicopterSymbol(Option... options) {
        return format ("helicopter-symbol", options);
    }

    public static String universalAccess(Option... options) {
        return format ("universal-access", options);
    }

    public static String circleChevronUp(Option... options) {
        return format ("circle-chevron-up", options);
    }

    public static String chevronCircleUp(Option... options) {
        return format ("chevron-circle-up", options);
    }

    public static String lariSign(Option... options) {
        return format ("lari-sign", options);
    }

    public static String volcano(Option... options) {
        return format ("volcano", options);
    }

    public static String personWalkingDashedLineArrowRight(Option... options) {
        return format ("person-walking-dashed-line-arrow-right", options);
    }

    public static String sterlingSign(Option... options) {
        return format ("sterling-sign", options);
    }

    public static String gbp(Option... options) {
        return format ("gbp", options);
    }

    public static String poundSign(Option... options) {
        return format ("pound-sign", options);
    }

    public static String viruses(Option... options) {
        return format ("viruses", options);
    }

    public static String squarePersonConfined(Option... options) {
        return format ("square-person-confined", options);
    }

    public static String userTie(Option... options) {
        return format ("user-tie", options);
    }

    public static String arrowDownLong(Option... options) {
        return format ("arrow-down-long", options);
    }

    public static String longArrowDown(Option... options) {
        return format ("long-arrow-down", options);
    }

    public static String tentArrowDownToLine(Option... options) {
        return format ("tent-arrow-down-to-line", options);
    }

    public static String certificate(Option... options) {
        return format ("certificate", options);
    }

    public static String replyAll(Option... options) {
        return format ("reply-all", options);
    }

    public static String mailReplyAll(Option... options) {
        return format ("mail-reply-all", options);
    }

    public static String suitcase(Option... options) {
        return format ("suitcase", options);
    }

    public static String personSkating(Option... options) {
        return format ("person-skating", options);
    }

    public static String skating(Option... options) {
        return format ("skating", options);
    }

    public static String filterCircleDollar(Option... options) {
        return format ("filter-circle-dollar", options);
    }

    public static String funnelDollar(Option... options) {
        return format ("funnel-dollar", options);
    }

    public static String cameraRetro(Option... options) {
        return format ("camera-retro", options);
    }

    public static String circleArrowDown(Option... options) {
        return format ("circle-arrow-down", options);
    }

    public static String arrowCircleDown(Option... options) {
        return format ("arrow-circle-down", options);
    }

    public static String fileImport(Option... options) {
        return format ("file-import", options);
    }

    public static String arrowRightToFile(Option... options) {
        return format ("arrow-right-to-file", options);
    }

    public static String squareArrowUpRight(Option... options) {
        return format ("square-arrow-up-right", options);
    }

    public static String externalLinkSquare(Option... options) {
        return format ("external-link-square", options);
    }

    public static String boxOpen(Option... options) {
        return format ("box-open", options);
    }

    public static String scroll(Option... options) {
        return format ("scroll", options);
    }

    public static String spa(Option... options) {
        return format ("spa", options);
    }

    public static String locationPinLock(Option... options) {
        return format ("location-pin-lock", options);
    }

    public static String pause(Option... options) {
        return format ("pause", options);
    }

    public static String hillAvalanche(Option... options) {
        return format ("hill-avalanche", options);
    }

    public static String temperatureEmpty(Option... options) {
        return format ("temperature-empty", options);
    }

    public static String temperature0(Option... options) {
        return format ("temperature-0", options);
    }

    public static String thermometer0(Option... options) {
        return format ("thermometer-0", options);
    }

    public static String thermometerEmpty(Option... options) {
        return format ("thermometer-empty", options);
    }

    public static String bomb(Option... options) {
        return format ("bomb", options);
    }

    public static String registered(Option... options) {
        return format ("registered", options);
    }

    public static String addressCard(Option... options) {
        return format ("address-card", options);
    }

    public static String contactCard(Option... options) {
        return format ("contact-card", options);
    }

    public static String vcard(Option... options) {
        return format ("vcard", options);
    }

    public static String scaleUnbalancedFlip(Option... options) {
        return format ("scale-unbalanced-flip", options);
    }

    public static String balanceScaleRight(Option... options) {
        return format ("balance-scale-right", options);
    }

    public static String subscript(Option... options) {
        return format ("subscript", options);
    }

    public static String diamondTurnRight(Option... options) {
        return format ("diamond-turn-right", options);
    }

    public static String directions(Option... options) {
        return format ("directions", options);
    }

    public static String burst(Option... options) {
        return format ("burst", options);
    }

    public static String houseLaptop(Option... options) {
        return format ("house-laptop", options);
    }

    public static String laptopHouse(Option... options) {
        return format ("laptop-house", options);
    }

    public static String faceTired(Option... options) {
        return format ("face-tired", options);
    }

    public static String tired(Option... options) {
        return format ("tired", options);
    }

    public static String moneyBills(Option... options) {
        return format ("money-bills", options);
    }

    public static String smog(Option... options) {
        return format ("smog", options);
    }

    public static String crutch(Option... options) {
        return format ("crutch", options);
    }

    public static String cloudArrowUp(Option... options) {
        return format ("cloud-arrow-up", options);
    }

    public static String cloudUpload(Option... options) {
        return format ("cloud-upload", options);
    }

    public static String cloudUploadAlt(Option... options) {
        return format ("cloud-upload-alt", options);
    }

    public static String palette(Option... options) {
        return format ("palette", options);
    }

    public static String arrowsTurnRight(Option... options) {
        return format ("arrows-turn-right", options);
    }

    public static String vest(Option... options) {
        return format ("vest", options);
    }

    public static String ferry(Option... options) {
        return format ("ferry", options);
    }

    public static String arrowsDownToPeople(Option... options) {
        return format ("arrows-down-to-people", options);
    }

    public static String seedling(Option... options) {
        return format ("seedling", options);
    }

    public static String sprout(Option... options) {
        return format ("sprout", options);
    }

    public static String leftRight(Option... options) {
        return format ("left-right", options);
    }

    public static String arrowsAltH(Option... options) {
        return format ("arrows-alt-h", options);
    }

    public static String boxesPacking(Option... options) {
        return format ("boxes-packing", options);
    }

    public static String circleArrowLeft(Option... options) {
        return format ("circle-arrow-left", options);
    }

    public static String arrowCircleLeft(Option... options) {
        return format ("arrow-circle-left", options);
    }

    public static String groupArrowsRotate(Option... options) {
        return format ("group-arrows-rotate", options);
    }

    public static String bowlFood(Option... options) {
        return format ("bowl-food", options);
    }

    public static String candyCane(Option... options) {
        return format ("candy-cane", options);
    }

    public static String arrowDownWideShort(Option... options) {
        return format ("arrow-down-wide-short", options);
    }

    public static String sortAmountAsc(Option... options) {
        return format ("sort-amount-asc", options);
    }

    public static String sortAmountDown(Option... options) {
        return format ("sort-amount-down", options);
    }

    public static String cloudBolt(Option... options) {
        return format ("cloud-bolt", options);
    }

    public static String thunderstorm(Option... options) {
        return format ("thunderstorm", options);
    }

    public static String textSlash(Option... options) {
        return format ("text-slash", options);
    }

    public static String removeFormat(Option... options) {
        return format ("remove-format", options);
    }

    public static String faceSmileWink(Option... options) {
        return format ("face-smile-wink", options);
    }

    public static String smileWink(Option... options) {
        return format ("smile-wink", options);
    }

    public static String fileWord(Option... options) {
        return format ("file-word", options);
    }

    public static String filePowerpoint(Option... options) {
        return format ("file-powerpoint", options);
    }

    public static String arrowsLeftRight(Option... options) {
        return format ("arrows-left-right", options);
    }

    public static String arrowsH(Option... options) {
        return format ("arrows-h", options);
    }

    public static String houseLock(Option... options) {
        return format ("house-lock", options);
    }

    public static String cloudArrowDown(Option... options) {
        return format ("cloud-arrow-down", options);
    }

    public static String cloudDownload(Option... options) {
        return format ("cloud-download", options);
    }

    public static String cloudDownloadAlt(Option... options) {
        return format ("cloud-download-alt", options);
    }

    public static String children(Option... options) {
        return format ("children", options);
    }

    public static String chalkboard(Option... options) {
        return format ("chalkboard", options);
    }

    public static String blackboard(Option... options) {
        return format ("blackboard", options);
    }

    public static String userLargeSlash(Option... options) {
        return format ("user-large-slash", options);
    }

    public static String userAltSlash(Option... options) {
        return format ("user-alt-slash", options);
    }

    public static String envelopeOpen(Option... options) {
        return format ("envelope-open", options);
    }

    public static String handshakeSimpleSlash(Option... options) {
        return format ("handshake-simple-slash", options);
    }

    public static String handshakeAltSlash(Option... options) {
        return format ("handshake-alt-slash", options);
    }

    public static String mattressPillow(Option... options) {
        return format ("mattress-pillow", options);
    }

    public static String guaraniSign(Option... options) {
        return format ("guarani-sign", options);
    }

    public static String arrowsRotate(Option... options) {
        return format ("arrows-rotate", options);
    }

    public static String refresh(Option... options) {
        return format ("refresh", options);
    }

    public static String sync(Option... options) {
        return format ("sync", options);
    }

    public static String fireExtinguisher(Option... options) {
        return format ("fire-extinguisher", options);
    }

    public static String cruzeiroSign(Option... options) {
        return format ("cruzeiro-sign", options);
    }

    public static String greaterThanEqual(Option... options) {
        return format ("greater-than-equal", options);
    }

    public static String shieldHalved(Option... options) {
        return format ("shield-halved", options);
    }

    public static String shieldAlt(Option... options) {
        return format ("shield-alt", options);
    }

    public static String bookAtlas(Option... options) {
        return format ("book-atlas", options);
    }

    public static String atlas(Option... options) {
        return format ("atlas", options);
    }

    public static String virus(Option... options) {
        return format ("virus", options);
    }

    public static String envelopeCircleCheck(Option... options) {
        return format ("envelope-circle-check", options);
    }

    public static String layerGroup(Option... options) {
        return format ("layer-group", options);
    }

    public static String arrowsToDot(Option... options) {
        return format ("arrows-to-dot", options);
    }

    public static String archway(Option... options) {
        return format ("archway", options);
    }

    public static String heartCircleCheck(Option... options) {
        return format ("heart-circle-check", options);
    }

    public static String houseChimneyCrack(Option... options) {
        return format ("house-chimney-crack", options);
    }

    public static String houseDamage(Option... options) {
        return format ("house-damage", options);
    }

    public static String fileZipper(Option... options) {
        return format ("file-zipper", options);
    }

    public static String fileArchive(Option... options) {
        return format ("file-archive", options);
    }

    public static String square(Option... options) {
        return format ("square", options);
    }

    public static String martiniGlassEmpty(Option... options) {
        return format ("martini-glass-empty", options);
    }

    public static String glassMartini(Option... options) {
        return format ("glass-martini", options);
    }

    public static String couch(Option... options) {
        return format ("couch", options);
    }

    public static String cediSign(Option... options) {
        return format ("cedi-sign", options);
    }

    public static String italic(Option... options) {
        return format ("italic", options);
    }

    public static String church(Option... options) {
        return format ("church", options);
    }

    public static String commentsDollar(Option... options) {
        return format ("comments-dollar", options);
    }

    public static String democrat(Option... options) {
        return format ("democrat", options);
    }

    public static String personSkiing(Option... options) {
        return format ("person-skiing", options);
    }

    public static String skiing(Option... options) {
        return format ("skiing", options);
    }

    public static String roadLock(Option... options) {
        return format ("road-lock", options);
    }

    public static String temperatureArrowDown(Option... options) {
        return format ("temperature-arrow-down", options);
    }

    public static String temperatureDown(Option... options) {
        return format ("temperature-down", options);
    }

    public static String featherPointed(Option... options) {
        return format ("feather-pointed", options);
    }

    public static String featherAlt(Option... options) {
        return format ("feather-alt", options);
    }

    public static String snowflake(Option... options) {
        return format ("snowflake", options);
    }

    public static String newspaper(Option... options) {
        return format ("newspaper", options);
    }

    public static String rectangleAd(Option... options) {
        return format ("rectangle-ad", options);
    }

    public static String ad(Option... options) {
        return format ("ad", options);
    }

    public static String circleArrowRight(Option... options) {
        return format ("circle-arrow-right", options);
    }

    public static String arrowCircleRight(Option... options) {
        return format ("arrow-circle-right", options);
    }

    public static String filterCircleXmark(Option... options) {
        return format ("filter-circle-xmark", options);
    }

    public static String locust(Option... options) {
        return format ("locust", options);
    }

    public static String sort(Option... options) {
        return format ("sort", options);
    }

    public static String unsorted(Option... options) {
        return format ("unsorted", options);
    }

    public static String listOl(Option... options) {
        return format ("list-ol", options);
    }

    public static String list12(Option... options) {
        return format ("list-1-2", options);
    }

    public static String listNumeric(Option... options) {
        return format ("list-numeric", options);
    }

    public static String personDressBurst(Option... options) {
        return format ("person-dress-burst", options);
    }

    public static String moneyCheckDollar(Option... options) {
        return format ("money-check-dollar", options);
    }

    public static String moneyCheckAlt(Option... options) {
        return format ("money-check-alt", options);
    }

    public static String vectorSquare(Option... options) {
        return format ("vector-square", options);
    }

    public static String breadSlice(Option... options) {
        return format ("bread-slice", options);
    }

    public static String language(Option... options) {
        return format ("language", options);
    }

    public static String faceKissWinkHeart(Option... options) {
        return format ("face-kiss-wink-heart", options);
    }

    public static String kissWinkHeart(Option... options) {
        return format ("kiss-wink-heart", options);
    }

    public static String filter(Option... options) {
        return format ("filter", options);
    }

    public static String question(Option... options) {
        return format ("question", options);
    }

    public static String fileSignature(Option... options) {
        return format ("file-signature", options);
    }

    public static String upDownLeftRight(Option... options) {
        return format ("up-down-left-right", options);
    }

    public static String arrowsAlt(Option... options) {
        return format ("arrows-alt", options);
    }

    public static String houseChimneyUser(Option... options) {
        return format ("house-chimney-user", options);
    }

    public static String handHoldingHeart(Option... options) {
        return format ("hand-holding-heart", options);
    }

    public static String puzzlePiece(Option... options) {
        return format ("puzzle-piece", options);
    }

    public static String moneyCheck(Option... options) {
        return format ("money-check", options);
    }

    public static String starHalfStroke(Option... options) {
        return format ("star-half-stroke", options);
    }

    public static String starHalfAlt(Option... options) {
        return format ("star-half-alt", options);
    }

    public static String code(Option... options) {
        return format ("code", options);
    }

    public static String whiskeyGlass(Option... options) {
        return format ("whiskey-glass", options);
    }

    public static String glassWhiskey(Option... options) {
        return format ("glass-whiskey", options);
    }

    public static String buildingCircleExclamation(Option... options) {
        return format ("building-circle-exclamation", options);
    }

    public static String magnifyingGlassChart(Option... options) {
        return format ("magnifying-glass-chart", options);
    }

    public static String arrowUpRightFromSquare(Option... options) {
        return format ("arrow-up-right-from-square", options);
    }

    public static String externalLink(Option... options) {
        return format ("external-link", options);
    }

    public static String cubesStacked(Option... options) {
        return format ("cubes-stacked", options);
    }

    public static String wonSign(Option... options) {
        return format ("won-sign", options);
    }

    public static String krw(Option... options) {
        return format ("krw", options);
    }

    public static String won(Option... options) {
        return format ("won", options);
    }

    public static String virusCovid(Option... options) {
        return format ("virus-covid", options);
    }

    public static String australSign(Option... options) {
        return format ("austral-sign", options);
    }

    public static String leaf(Option... options) {
        return format ("leaf", options);
    }

    public static String road(Option... options) {
        return format ("road", options);
    }

    public static String taxi(Option... options) {
        return format ("taxi", options);
    }

    public static String cab(Option... options) {
        return format ("cab", options);
    }

    public static String personCirclePlus(Option... options) {
        return format ("person-circle-plus", options);
    }

    public static String chartPie(Option... options) {
        return format ("chart-pie", options);
    }

    public static String pieChart(Option... options) {
        return format ("pie-chart", options);
    }

    public static String boltLightning(Option... options) {
        return format ("bolt-lightning", options);
    }

    public static String sackXmark(Option... options) {
        return format ("sack-xmark", options);
    }

    public static String fileExcel(Option... options) {
        return format ("file-excel", options);
    }

    public static String fileContract(Option... options) {
        return format ("file-contract", options);
    }

    public static String fishFins(Option... options) {
        return format ("fish-fins", options);
    }

    public static String buildingFlag(Option... options) {
        return format ("building-flag", options);
    }

    public static String faceGrinBeam(Option... options) {
        return format ("face-grin-beam", options);
    }

    public static String grinBeam(Option... options) {
        return format ("grin-beam", options);
    }

    public static String objectUngroup(Option... options) {
        return format ("object-ungroup", options);
    }

    public static String poop(Option... options) {
        return format ("poop", options);
    }

    public static String locationPin(Option... options) {
        return format ("location-pin", options);
    }

    public static String mapMarker(Option... options) {
        return format ("map-marker", options);
    }

    public static String kaaba(Option... options) {
        return format ("kaaba", options);
    }

    public static String toiletPaper(Option... options) {
        return format ("toilet-paper", options);
    }

    public static String helmetSafety(Option... options) {
        return format ("helmet-safety", options);
    }

    public static String hardHat(Option... options) {
        return format ("hard-hat", options);
    }

    public static String hatHard(Option... options) {
        return format ("hat-hard", options);
    }

    public static String eject(Option... options) {
        return format ("eject", options);
    }

    public static String circleRight(Option... options) {
        return format ("circle-right", options);
    }

    public static String arrowAltCircleRight(Option... options) {
        return format ("arrow-alt-circle-right", options);
    }

    public static String planeCircleCheck(Option... options) {
        return format ("plane-circle-check", options);
    }

    public static String faceRollingEyes(Option... options) {
        return format ("face-rolling-eyes", options);
    }

    public static String mehRollingEyes(Option... options) {
        return format ("meh-rolling-eyes", options);
    }

    public static String objectGroup(Option... options) {
        return format ("object-group", options);
    }

    public static String chartLine(Option... options) {
        return format ("chart-line", options);
    }

    public static String lineChart(Option... options) {
        return format ("line-chart", options);
    }

    public static String maskVentilator(Option... options) {
        return format ("mask-ventilator", options);
    }

    public static String arrowRight(Option... options) {
        return format ("arrow-right", options);
    }

    public static String signsPost(Option... options) {
        return format ("signs-post", options);
    }

    public static String mapSigns(Option... options) {
        return format ("map-signs", options);
    }

    public static String cashRegister(Option... options) {
        return format ("cash-register", options);
    }

    public static String personCircleQuestion(Option... options) {
        return format ("person-circle-question", options);
    }

    public static String tarp(Option... options) {
        return format ("tarp", options);
    }

    public static String screwdriverWrench(Option... options) {
        return format ("screwdriver-wrench", options);
    }

    public static String tools(Option... options) {
        return format ("tools", options);
    }

    public static String arrowsToEye(Option... options) {
        return format ("arrows-to-eye", options);
    }

    public static String plugCircleBolt(Option... options) {
        return format ("plug-circle-bolt", options);
    }

    public static String heart(Option... options) {
        return format ("heart", options);
    }

    public static String marsAndVenus(Option... options) {
        return format ("mars-and-venus", options);
    }

    public static String houseUser(Option... options) {
        return format ("house-user", options);
    }

    public static String homeUser(Option... options) {
        return format ("home-user", options);
    }

    public static String dumpsterFire(Option... options) {
        return format ("dumpster-fire", options);
    }

    public static String houseCrack(Option... options) {
        return format ("house-crack", options);
    }

    public static String martiniGlassCitrus(Option... options) {
        return format ("martini-glass-citrus", options);
    }

    public static String cocktail(Option... options) {
        return format ("cocktail", options);
    }

    public static String faceSurprise(Option... options) {
        return format ("face-surprise", options);
    }

    public static String surprise(Option... options) {
        return format ("surprise", options);
    }

    public static String bottleWater(Option... options) {
        return format ("bottle-water", options);
    }

    public static String circlePause(Option... options) {
        return format ("circle-pause", options);
    }

    public static String pauseCircle(Option... options) {
        return format ("pause-circle", options);
    }

    public static String toiletPaperSlash(Option... options) {
        return format ("toilet-paper-slash", options);
    }

    public static String appleWhole(Option... options) {
        return format ("apple-whole", options);
    }

    public static String appleAlt(Option... options) {
        return format ("apple-alt", options);
    }

    public static String kitchenSet(Option... options) {
        return format ("kitchen-set", options);
    }

    public static String temperatureQuarter(Option... options) {
        return format ("temperature-quarter", options);
    }

    public static String temperature1(Option... options) {
        return format ("temperature-1", options);
    }

    public static String thermometer1(Option... options) {
        return format ("thermometer-1", options);
    }

    public static String thermometerQuarter(Option... options) {
        return format ("thermometer-quarter", options);
    }

    public static String cube(Option... options) {
        return format ("cube", options);
    }

    public static String bitcoinSign(Option... options) {
        return format ("bitcoin-sign", options);
    }

    public static String shieldDog(Option... options) {
        return format ("shield-dog", options);
    }

    public static String solarPanel(Option... options) {
        return format ("solar-panel", options);
    }

    public static String lockOpen(Option... options) {
        return format ("lock-open", options);
    }

    public static String elevator(Option... options) {
        return format ("elevator", options);
    }

    public static String moneyBillTransfer(Option... options) {
        return format ("money-bill-transfer", options);
    }

    public static String moneyBillTrendUp(Option... options) {
        return format ("money-bill-trend-up", options);
    }

    public static String houseFloodWaterCircleArrowRight(Option... options) {
        return format ("house-flood-water-circle-arrow-right", options);
    }

    public static String squarePollHorizontal(Option... options) {
        return format ("square-poll-horizontal", options);
    }

    public static String pollH(Option... options) {
        return format ("poll-h", options);
    }

    public static String circle(Option... options) {
        return format ("circle", options);
    }

    public static String backwardFast(Option... options) {
        return format ("backward-fast", options);
    }

    public static String fastBackward(Option... options) {
        return format ("fast-backward", options);
    }

    public static String recycle(Option... options) {
        return format ("recycle", options);
    }

    public static String userAstronaut(Option... options) {
        return format ("user-astronaut", options);
    }

    public static String planeSlash(Option... options) {
        return format ("plane-slash", options);
    }

    public static String trademark(Option... options) {
        return format ("trademark", options);
    }

    public static String basketball(Option... options) {
        return format ("basketball", options);
    }

    public static String basketballBall(Option... options) {
        return format ("basketball-ball", options);
    }

    public static String satelliteDish(Option... options) {
        return format ("satellite-dish", options);
    }

    public static String circleUp(Option... options) {
        return format ("circle-up", options);
    }

    public static String arrowAltCircleUp(Option... options) {
        return format ("arrow-alt-circle-up", options);
    }

    public static String mobileScreenButton(Option... options) {
        return format ("mobile-screen-button", options);
    }

    public static String mobileAlt(Option... options) {
        return format ("mobile-alt", options);
    }

    public static String volumeHigh(Option... options) {
        return format ("volume-high", options);
    }

    public static String volumeUp(Option... options) {
        return format ("volume-up", options);
    }

    public static String usersRays(Option... options) {
        return format ("users-rays", options);
    }

    public static String wallet(Option... options) {
        return format ("wallet", options);
    }

    public static String clipboardCheck(Option... options) {
        return format ("clipboard-check", options);
    }

    public static String fileAudio(Option... options) {
        return format ("file-audio", options);
    }

    public static String burger(Option... options) {
        return format ("burger", options);
    }

    public static String hamburger(Option... options) {
        return format ("hamburger", options);
    }

    public static String wrench(Option... options) {
        return format ("wrench", options);
    }

    public static String bugs(Option... options) {
        return format ("bugs", options);
    }

    public static String rupeeSign(Option... options) {
        return format ("rupee-sign", options);
    }

    public static String rupee(Option... options) {
        return format ("rupee", options);
    }

    public static String fileImage(Option... options) {
        return format ("file-image", options);
    }

    public static String circleQuestion(Option... options) {
        return format ("circle-question", options);
    }

    public static String questionCircle(Option... options) {
        return format ("question-circle", options);
    }

    public static String planeDeparture(Option... options) {
        return format ("plane-departure", options);
    }

    public static String handshakeSlash(Option... options) {
        return format ("handshake-slash", options);
    }

    public static String bookBookmark(Option... options) {
        return format ("book-bookmark", options);
    }

    public static String codeBranch(Option... options) {
        return format ("code-branch", options);
    }

    public static String hatCowboy(Option... options) {
        return format ("hat-cowboy", options);
    }

    public static String bridge(Option... options) {
        return format ("bridge", options);
    }

    public static String phoneFlip(Option... options) {
        return format ("phone-flip", options);
    }

    public static String phoneAlt(Option... options) {
        return format ("phone-alt", options);
    }

    public static String truckFront(Option... options) {
        return format ("truck-front", options);
    }

    public static String cat(Option... options) {
        return format ("cat", options);
    }

    public static String anchorCircleExclamation(Option... options) {
        return format ("anchor-circle-exclamation", options);
    }

    public static String truckField(Option... options) {
        return format ("truck-field", options);
    }

    public static String route(Option... options) {
        return format ("route", options);
    }

    public static String clipboardQuestion(Option... options) {
        return format ("clipboard-question", options);
    }

    public static String panorama(Option... options) {
        return format ("panorama", options);
    }

    public static String commentMedical(Option... options) {
        return format ("comment-medical", options);
    }

    public static String teethOpen(Option... options) {
        return format ("teeth-open", options);
    }

    public static String fileCircleMinus(Option... options) {
        return format ("file-circle-minus", options);
    }

    public static String tags(Option... options) {
        return format ("tags", options);
    }

    public static String wineGlass(Option... options) {
        return format ("wine-glass", options);
    }

    public static String forwardFast(Option... options) {
        return format ("forward-fast", options);
    }

    public static String fastForward(Option... options) {
        return format ("fast-forward", options);
    }

    public static String faceMehBlank(Option... options) {
        return format ("face-meh-blank", options);
    }

    public static String mehBlank(Option... options) {
        return format ("meh-blank", options);
    }

    public static String squareParking(Option... options) {
        return format ("square-parking", options);
    }

    public static String parking(Option... options) {
        return format ("parking", options);
    }

    public static String houseSignal(Option... options) {
        return format ("house-signal", options);
    }

    public static String barsProgress(Option... options) {
        return format ("bars-progress", options);
    }

    public static String tasksAlt(Option... options) {
        return format ("tasks-alt", options);
    }

    public static String faucetDrip(Option... options) {
        return format ("faucet-drip", options);
    }

    public static String cartFlatbed(Option... options) {
        return format ("cart-flatbed", options);
    }

    public static String dollyFlatbed(Option... options) {
        return format ("dolly-flatbed", options);
    }

    public static String banSmoking(Option... options) {
        return format ("ban-smoking", options);
    }

    public static String smokingBan(Option... options) {
        return format ("smoking-ban", options);
    }

    public static String terminal(Option... options) {
        return format ("terminal", options);
    }

    public static String mobileButton(Option... options) {
        return format ("mobile-button", options);
    }

    public static String houseMedicalFlag(Option... options) {
        return format ("house-medical-flag", options);
    }

    public static String basketShopping(Option... options) {
        return format ("basket-shopping", options);
    }

    public static String shoppingBasket(Option... options) {
        return format ("shopping-basket", options);
    }

    public static String tape(Option... options) {
        return format ("tape", options);
    }

    public static String busSimple(Option... options) {
        return format ("bus-simple", options);
    }

    public static String busAlt(Option... options) {
        return format ("bus-alt", options);
    }

    public static String eye(Option... options) {
        return format ("eye", options);
    }

    public static String faceSadCry(Option... options) {
        return format ("face-sad-cry", options);
    }

    public static String sadCry(Option... options) {
        return format ("sad-cry", options);
    }

    public static String audioDescription(Option... options) {
        return format ("audio-description", options);
    }

    public static String personMilitaryToPerson(Option... options) {
        return format ("person-military-to-person", options);
    }

    public static String fileShield(Option... options) {
        return format ("file-shield", options);
    }

    public static String userSlash(Option... options) {
        return format ("user-slash", options);
    }

    public static String pen(Option... options) {
        return format ("pen", options);
    }

    public static String towerObservation(Option... options) {
        return format ("tower-observation", options);
    }

    public static String fileCode(Option... options) {
        return format ("file-code", options);
    }

    public static String signal(Option... options) {
        return format ("signal", options);
    }

    public static String signal5(Option... options) {
        return format ("signal-5", options);
    }

    public static String signalPerfect(Option... options) {
        return format ("signal-perfect", options);
    }

    public static String bus(Option... options) {
        return format ("bus", options);
    }

    public static String heartCircleXmark(Option... options) {
        return format ("heart-circle-xmark", options);
    }

    public static String houseChimney(Option... options) {
        return format ("house-chimney", options);
    }

    public static String homeLg(Option... options) {
        return format ("home-lg", options);
    }

    public static String windowMaximize(Option... options) {
        return format ("window-maximize", options);
    }

    public static String faceFrown(Option... options) {
        return format ("face-frown", options);
    }

    public static String frown(Option... options) {
        return format ("frown", options);
    }

    public static String prescription(Option... options) {
        return format ("prescription", options);
    }

    public static String shop(Option... options) {
        return format ("shop", options);
    }

    public static String storeAlt(Option... options) {
        return format ("store-alt", options);
    }

    public static String floppyDisk(Option... options) {
        return format ("floppy-disk", options);
    }

    public static String save(Option... options) {
        return format ("save", options);
    }

    public static String vihara(Option... options) {
        return format ("vihara", options);
    }

    public static String scaleUnbalanced(Option... options) {
        return format ("scale-unbalanced", options);
    }

    public static String balanceScaleLeft(Option... options) {
        return format ("balance-scale-left", options);
    }

    public static String sortUp(Option... options) {
        return format ("sort-up", options);
    }

    public static String sortAsc(Option... options) {
        return format ("sort-asc", options);
    }

    public static String commentDots(Option... options) {
        return format ("comment-dots", options);
    }

    public static String commenting(Option... options) {
        return format ("commenting", options);
    }

    public static String plantWilt(Option... options) {
        return format ("plant-wilt", options);
    }

    public static String diamond(Option... options) {
        return format ("diamond", options);
    }

    public static String faceGrinSquint(Option... options) {
        return format ("face-grin-squint", options);
    }

    public static String grinSquint(Option... options) {
        return format ("grin-squint", options);
    }

    public static String handHoldingDollar(Option... options) {
        return format ("hand-holding-dollar", options);
    }

    public static String handHoldingUsd(Option... options) {
        return format ("hand-holding-usd", options);
    }

    public static String bacterium(Option... options) {
        return format ("bacterium", options);
    }

    public static String handPointer(Option... options) {
        return format ("hand-pointer", options);
    }

    public static String drumSteelpan(Option... options) {
        return format ("drum-steelpan", options);
    }

    public static String handScissors(Option... options) {
        return format ("hand-scissors", options);
    }

    public static String handsPraying(Option... options) {
        return format ("hands-praying", options);
    }

    public static String prayingHands(Option... options) {
        return format ("praying-hands", options);
    }

    public static String arrowRotateRight(Option... options) {
        return format ("arrow-rotate-right", options);
    }

    public static String arrowRightRotate(Option... options) {
        return format ("arrow-right-rotate", options);
    }

    public static String arrowRotateForward(Option... options) {
        return format ("arrow-rotate-forward", options);
    }

    public static String redo(Option... options) {
        return format ("redo", options);
    }

    public static String biohazard(Option... options) {
        return format ("biohazard", options);
    }

    public static String locationCrosshairs(Option... options) {
        return format ("location-crosshairs", options);
    }

    public static String location(Option... options) {
        return format ("location", options);
    }

    public static String marsDouble(Option... options) {
        return format ("mars-double", options);
    }

    public static String childDress(Option... options) {
        return format ("child-dress", options);
    }

    public static String usersBetweenLines(Option... options) {
        return format ("users-between-lines", options);
    }

    public static String lungsVirus(Option... options) {
        return format ("lungs-virus", options);
    }

    public static String faceGrinTears(Option... options) {
        return format ("face-grin-tears", options);
    }

    public static String grinTears(Option... options) {
        return format ("grin-tears", options);
    }

    public static String phone(Option... options) {
        return format ("phone", options);
    }

    public static String calendarXmark(Option... options) {
        return format ("calendar-xmark", options);
    }

    public static String calendarTimes(Option... options) {
        return format ("calendar-times", options);
    }

    public static String childReaching(Option... options) {
        return format ("child-reaching", options);
    }

    public static String headSideVirus(Option... options) {
        return format ("head-side-virus", options);
    }

    public static String userGear(Option... options) {
        return format ("user-gear", options);
    }

    public static String userCog(Option... options) {
        return format ("user-cog", options);
    }

    public static String arrowUp19(Option... options) {
        return format ("arrow-up-1-9", options);
    }

    public static String sortNumericUp(Option... options) {
        return format ("sort-numeric-up", options);
    }

    public static String doorClosed(Option... options) {
        return format ("door-closed", options);
    }

    public static String shieldVirus(Option... options) {
        return format ("shield-virus", options);
    }

    public static String diceSix(Option... options) {
        return format ("dice-six", options);
    }

    public static String mosquitoNet(Option... options) {
        return format ("mosquito-net", options);
    }

    public static String bridgeWater(Option... options) {
        return format ("bridge-water", options);
    }

    public static String personBooth(Option... options) {
        return format ("person-booth", options);
    }

    public static String textWidth(Option... options) {
        return format ("text-width", options);
    }

    public static String hatWizard(Option... options) {
        return format ("hat-wizard", options);
    }

    public static String penFancy(Option... options) {
        return format ("pen-fancy", options);
    }

    public static String personDigging(Option... options) {
        return format ("person-digging", options);
    }

    public static String digging(Option... options) {
        return format ("digging", options);
    }

    public static String trash(Option... options) {
        return format ("trash", options);
    }

    public static String gaugeSimple(Option... options) {
        return format ("gauge-simple", options);
    }

    public static String gaugeSimpleMed(Option... options) {
        return format ("gauge-simple-med", options);
    }

    public static String tachometerAverage(Option... options) {
        return format ("tachometer-average", options);
    }

    public static String bookMedical(Option... options) {
        return format ("book-medical", options);
    }

    public static String poo(Option... options) {
        return format ("poo", options);
    }

    public static String quoteRight(Option... options) {
        return format ("quote-right", options);
    }

    public static String quoteRightAlt(Option... options) {
        return format ("quote-right-alt", options);
    }

    public static String shirt(Option... options) {
        return format ("shirt", options);
    }

    public static String tShirt(Option... options) {
        return format ("t-shirt", options);
    }

    public static String tshirt(Option... options) {
        return format ("tshirt", options);
    }

    public static String cubes(Option... options) {
        return format ("cubes", options);
    }

    public static String divide(Option... options) {
        return format ("divide", options);
    }

    public static String tengeSign(Option... options) {
        return format ("tenge-sign", options);
    }

    public static String tenge(Option... options) {
        return format ("tenge", options);
    }

    public static String headphones(Option... options) {
        return format ("headphones", options);
    }

    public static String handsHolding(Option... options) {
        return format ("hands-holding", options);
    }

    public static String handsClapping(Option... options) {
        return format ("hands-clapping", options);
    }

    public static String republican(Option... options) {
        return format ("republican", options);
    }

    public static String arrowLeft(Option... options) {
        return format ("arrow-left", options);
    }

    public static String personCircleXmark(Option... options) {
        return format ("person-circle-xmark", options);
    }

    public static String ruler(Option... options) {
        return format ("ruler", options);
    }

    public static String alignLeft(Option... options) {
        return format ("align-left", options);
    }

    public static String diceD6(Option... options) {
        return format ("dice-d6", options);
    }

    public static String restroom(Option... options) {
        return format ("restroom", options);
    }

    public static String usersViewfinder(Option... options) {
        return format ("users-viewfinder", options);
    }

    public static String fileVideo(Option... options) {
        return format ("file-video", options);
    }

    public static String upRightFromSquare(Option... options) {
        return format ("up-right-from-square", options);
    }

    public static String externalLinkAlt(Option... options) {
        return format ("external-link-alt", options);
    }

    public static String tableCells(Option... options) {
        return format ("table-cells", options);
    }

    public static String th(Option... options) {
        return format ("th", options);
    }

    public static String filePdf(Option... options) {
        return format ("file-pdf", options);
    }

    public static String bookBible(Option... options) {
        return format ("book-bible", options);
    }

    public static String bible(Option... options) {
        return format ("bible", options);
    }

    public static String suitcaseMedical(Option... options) {
        return format ("suitcase-medical", options);
    }

    public static String medkit(Option... options) {
        return format ("medkit", options);
    }

    public static String userSecret(Option... options) {
        return format ("user-secret", options);
    }

    public static String otter(Option... options) {
        return format ("otter", options);
    }

    public static String personDress(Option... options) {
        return format ("person-dress", options);
    }

    public static String female(Option... options) {
        return format ("female", options);
    }

    public static String commentDollar(Option... options) {
        return format ("comment-dollar", options);
    }

    public static String businessTime(Option... options) {
        return format ("business-time", options);
    }

    public static String briefcaseClock(Option... options) {
        return format ("briefcase-clock", options);
    }

    public static String tableCellsLarge(Option... options) {
        return format ("table-cells-large", options);
    }

    public static String thLarge(Option... options) {
        return format ("th-large", options);
    }

    public static String bookTanakh(Option... options) {
        return format ("book-tanakh", options);
    }

    public static String tanakh(Option... options) {
        return format ("tanakh", options);
    }

    public static String phoneVolume(Option... options) {
        return format ("phone-volume", options);
    }

    public static String volumeControlPhone(Option... options) {
        return format ("volume-control-phone", options);
    }

    public static String hatCowboySide(Option... options) {
        return format ("hat-cowboy-side", options);
    }

    public static String clipboardUser(Option... options) {
        return format ("clipboard-user", options);
    }

    public static String child(Option... options) {
        return format ("child", options);
    }

    public static String liraSign(Option... options) {
        return format ("lira-sign", options);
    }

    public static String satellite(Option... options) {
        return format ("satellite", options);
    }

    public static String planeLock(Option... options) {
        return format ("plane-lock", options);
    }

    public static String tag(Option... options) {
        return format ("tag", options);
    }

    public static String comment(Option... options) {
        return format ("comment", options);
    }

    public static String cakeCandles(Option... options) {
        return format ("cake-candles", options);
    }

    public static String birthdayCake(Option... options) {
        return format ("birthday-cake", options);
    }

    public static String cake(Option... options) {
        return format ("cake", options);
    }

    public static String envelope(Option... options) {
        return format ("envelope", options);
    }

    public static String anglesUp(Option... options) {
        return format ("angles-up", options);
    }

    public static String angleDoubleUp(Option... options) {
        return format ("angle-double-up", options);
    }

    public static String paperclip(Option... options) {
        return format ("paperclip", options);
    }

    public static String arrowRightToCity(Option... options) {
        return format ("arrow-right-to-city", options);
    }

    public static String ribbon(Option... options) {
        return format ("ribbon", options);
    }

    public static String lungs(Option... options) {
        return format ("lungs", options);
    }

    public static String arrowUp91(Option... options) {
        return format ("arrow-up-9-1", options);
    }

    public static String sortNumericUpAlt(Option... options) {
        return format ("sort-numeric-up-alt", options);
    }

    public static String litecoinSign(Option... options) {
        return format ("litecoin-sign", options);
    }

    public static String borderNone(Option... options) {
        return format ("border-none", options);
    }

    public static String circleNodes(Option... options) {
        return format ("circle-nodes", options);
    }

    public static String parachuteBox(Option... options) {
        return format ("parachute-box", options);
    }

    public static String indent(Option... options) {
        return format ("indent", options);
    }

    public static String truckFieldUn(Option... options) {
        return format ("truck-field-un", options);
    }

    public static String hourglass(Option... options) {
        return format ("hourglass", options);
    }

    public static String hourglassEmpty(Option... options) {
        return format ("hourglass-empty", options);
    }

    public static String mountain(Option... options) {
        return format ("mountain", options);
    }

    public static String userDoctor(Option... options) {
        return format ("user-doctor", options);
    }

    public static String userMd(Option... options) {
        return format ("user-md", options);
    }

    public static String circleInfo(Option... options) {
        return format ("circle-info", options);
    }

    public static String infoCircle(Option... options) {
        return format ("info-circle", options);
    }

    public static String cloudMeatball(Option... options) {
        return format ("cloud-meatball", options);
    }

    public static String camera(Option... options) {
        return format ("camera", options);
    }

    public static String cameraAlt(Option... options) {
        return format ("camera-alt", options);
    }

    public static String squareVirus(Option... options) {
        return format ("square-virus", options);
    }

    public static String meteor(Option... options) {
        return format ("meteor", options);
    }

    public static String carOn(Option... options) {
        return format ("car-on", options);
    }

    public static String sleigh(Option... options) {
        return format ("sleigh", options);
    }

    public static String arrowDown19(Option... options) {
        return format ("arrow-down-1-9", options);
    }

    public static String sortNumericAsc(Option... options) {
        return format ("sort-numeric-asc", options);
    }

    public static String sortNumericDown(Option... options) {
        return format ("sort-numeric-down", options);
    }

    public static String handHoldingDroplet(Option... options) {
        return format ("hand-holding-droplet", options);
    }

    public static String handHoldingWater(Option... options) {
        return format ("hand-holding-water", options);
    }

    public static String water(Option... options) {
        return format ("water", options);
    }

    public static String calendarCheck(Option... options) {
        return format ("calendar-check", options);
    }

    public static String braille(Option... options) {
        return format ("braille", options);
    }

    public static String prescriptionBottleMedical(Option... options) {
        return format ("prescription-bottle-medical", options);
    }

    public static String prescriptionBottleAlt(Option... options) {
        return format ("prescription-bottle-alt", options);
    }

    public static String landmark(Option... options) {
        return format ("landmark", options);
    }

    public static String truck(Option... options) {
        return format ("truck", options);
    }

    public static String crosshairs(Option... options) {
        return format ("crosshairs", options);
    }

    public static String personCane(Option... options) {
        return format ("person-cane", options);
    }

    public static String tent(Option... options) {
        return format ("tent", options);
    }

    public static String vestPatches(Option... options) {
        return format ("vest-patches", options);
    }

    public static String checkDouble(Option... options) {
        return format ("check-double", options);
    }

    public static String arrowDownAZ(Option... options) {
        return format ("arrow-down-a-z", options);
    }

    public static String sortAlphaAsc(Option... options) {
        return format ("sort-alpha-asc", options);
    }

    public static String sortAlphaDown(Option... options) {
        return format ("sort-alpha-down", options);
    }

    public static String moneyBillWheat(Option... options) {
        return format ("money-bill-wheat", options);
    }

    public static String cookie(Option... options) {
        return format ("cookie", options);
    }

    public static String arrowRotateLeft(Option... options) {
        return format ("arrow-rotate-left", options);
    }

    public static String arrowLeftRotate(Option... options) {
        return format ("arrow-left-rotate", options);
    }

    public static String arrowRotateBack(Option... options) {
        return format ("arrow-rotate-back", options);
    }

    public static String arrowRotateBackward(Option... options) {
        return format ("arrow-rotate-backward", options);
    }

    public static String undo(Option... options) {
        return format ("undo", options);
    }

    public static String hardDrive(Option... options) {
        return format ("hard-drive", options);
    }

    public static String hdd(Option... options) {
        return format ("hdd", options);
    }

    public static String faceGrinSquintTears(Option... options) {
        return format ("face-grin-squint-tears", options);
    }

    public static String grinSquintTears(Option... options) {
        return format ("grin-squint-tears", options);
    }

    public static String dumbbell(Option... options) {
        return format ("dumbbell", options);
    }

    public static String rectangleList(Option... options) {
        return format ("rectangle-list", options);
    }

    public static String listAlt(Option... options) {
        return format ("list-alt", options);
    }

    public static String tarpDroplet(Option... options) {
        return format ("tarp-droplet", options);
    }

    public static String houseMedicalCircleCheck(Option... options) {
        return format ("house-medical-circle-check", options);
    }

    public static String personSkiingNordic(Option... options) {
        return format ("person-skiing-nordic", options);
    }

    public static String skiingNordic(Option... options) {
        return format ("skiing-nordic", options);
    }

    public static String calendarPlus(Option... options) {
        return format ("calendar-plus", options);
    }

    public static String planeArrival(Option... options) {
        return format ("plane-arrival", options);
    }

    public static String circleLeft(Option... options) {
        return format ("circle-left", options);
    }

    public static String arrowAltCircleLeft(Option... options) {
        return format ("arrow-alt-circle-left", options);
    }

    public static String trainSubway(Option... options) {
        return format ("train-subway", options);
    }

    public static String subway(Option... options) {
        return format ("subway", options);
    }

    public static String chartGantt(Option... options) {
        return format ("chart-gantt", options);
    }

    public static String indianRupeeSign(Option... options) {
        return format ("indian-rupee-sign", options);
    }

    public static String indianRupee(Option... options) {
        return format ("indian-rupee", options);
    }

    public static String inr(Option... options) {
        return format ("inr", options);
    }

    public static String cropSimple(Option... options) {
        return format ("crop-simple", options);
    }

    public static String cropAlt(Option... options) {
        return format ("crop-alt", options);
    }

    public static String moneyBill1(Option... options) {
        return format ("money-bill-1", options);
    }

    public static String moneyBillAlt(Option... options) {
        return format ("money-bill-alt", options);
    }

    public static String leftLong(Option... options) {
        return format ("left-long", options);
    }

    public static String longArrowAltLeft(Option... options) {
        return format ("long-arrow-alt-left", options);
    }

    public static String dna(Option... options) {
        return format ("dna", options);
    }

    public static String virusSlash(Option... options) {
        return format ("virus-slash", options);
    }

    public static String minus(Option... options) {
        return format ("minus", options);
    }

    public static String subtract(Option... options) {
        return format ("subtract", options);
    }

    public static String chess(Option... options) {
        return format ("chess", options);
    }

    public static String arrowLeftLong(Option... options) {
        return format ("arrow-left-long", options);
    }

    public static String longArrowLeft(Option... options) {
        return format ("long-arrow-left", options);
    }

    public static String plugCircleCheck(Option... options) {
        return format ("plug-circle-check", options);
    }

    public static String streetView(Option... options) {
        return format ("street-view", options);
    }

    public static String francSign(Option... options) {
        return format ("franc-sign", options);
    }

    public static String volumeOff(Option... options) {
        return format ("volume-off", options);
    }

    public static String handsAslInterpreting(Option... options) {
        return format ("hands-asl-interpreting", options);
    }

    public static String americanSignLanguageInterpreting(Option... options) {
        return format ("american-sign-language-interpreting", options);
    }

    public static String aslInterpreting(Option... options) {
        return format ("asl-interpreting", options);
    }

    public static String handsAmericanSignLanguageInterpreting(Option... options) {
        return format ("hands-american-sign-language-interpreting", options);
    }

    public static String gear(Option... options) {
        return format ("gear", options);
    }

    public static String cog(Option... options) {
        return format ("cog", options);
    }

    public static String dropletSlash(Option... options) {
        return format ("droplet-slash", options);
    }

    public static String tintSlash(Option... options) {
        return format ("tint-slash", options);
    }

    public static String mosque(Option... options) {
        return format ("mosque", options);
    }

    public static String mosquito(Option... options) {
        return format ("mosquito", options);
    }

    public static String starOfDavid(Option... options) {
        return format ("star-of-david", options);
    }

    public static String personMilitaryRifle(Option... options) {
        return format ("person-military-rifle", options);
    }

    public static String cartShopping(Option... options) {
        return format ("cart-shopping", options);
    }

    public static String shoppingCart(Option... options) {
        return format ("shopping-cart", options);
    }

    public static String vials(Option... options) {
        return format ("vials", options);
    }

    public static String plugCirclePlus(Option... options) {
        return format ("plug-circle-plus", options);
    }

    public static String placeOfWorship(Option... options) {
        return format ("place-of-worship", options);
    }

    public static String gripVertical(Option... options) {
        return format ("grip-vertical", options);
    }

    public static String arrowTurnUp(Option... options) {
        return format ("arrow-turn-up", options);
    }

    public static String levelUp(Option... options) {
        return format ("level-up", options);
    }

    public static String squareRootVariable(Option... options) {
        return format ("square-root-variable", options);
    }

    public static String squareRootAlt(Option... options) {
        return format ("square-root-alt", options);
    }

    public static String clock(Option... options) {
        return format ("clock", options);
    }

    public static String clockFour(Option... options) {
        return format ("clock-four", options);
    }

    public static String backwardStep(Option... options) {
        return format ("backward-step", options);
    }

    public static String stepBackward(Option... options) {
        return format ("step-backward", options);
    }

    public static String pallet(Option... options) {
        return format ("pallet", options);
    }

    public static String faucet(Option... options) {
        return format ("faucet", options);
    }

    public static String baseballBatBall(Option... options) {
        return format ("baseball-bat-ball", options);
    }

    public static String timeline(Option... options) {
        return format ("timeline", options);
    }

    public static String keyboard(Option... options) {
        return format ("keyboard", options);
    }

    public static String caretDown(Option... options) {
        return format ("caret-down", options);
    }

    public static String houseChimneyMedical(Option... options) {
        return format ("house-chimney-medical", options);
    }

    public static String clinicMedical(Option... options) {
        return format ("clinic-medical", options);
    }

    public static String temperatureThreeQuarters(Option... options) {
        return format ("temperature-three-quarters", options);
    }

    public static String temperature3(Option... options) {
        return format ("temperature-3", options);
    }

    public static String thermometer3(Option... options) {
        return format ("thermometer-3", options);
    }

    public static String thermometerThreeQuarters(Option... options) {
        return format ("thermometer-three-quarters", options);
    }

    public static String mobileScreen(Option... options) {
        return format ("mobile-screen", options);
    }

    public static String mobileAndroidAlt(Option... options) {
        return format ("mobile-android-alt", options);
    }

    public static String planeUp(Option... options) {
        return format ("plane-up", options);
    }

    public static String piggyBank(Option... options) {
        return format ("piggy-bank", options);
    }

    public static String batteryHalf(Option... options) {
        return format ("battery-half", options);
    }

    public static String battery3(Option... options) {
        return format ("battery-3", options);
    }

    public static String mountainCity(Option... options) {
        return format ("mountain-city", options);
    }

    public static String coins(Option... options) {
        return format ("coins", options);
    }

    public static String khanda(Option... options) {
        return format ("khanda", options);
    }

    public static String sliders(Option... options) {
        return format ("sliders", options);
    }

    public static String slidersH(Option... options) {
        return format ("sliders-h", options);
    }

    public static String folderTree(Option... options) {
        return format ("folder-tree", options);
    }

    public static String networkWired(Option... options) {
        return format ("network-wired", options);
    }

    public static String mapPin(Option... options) {
        return format ("map-pin", options);
    }

    public static String hamsa(Option... options) {
        return format ("hamsa", options);
    }

    public static String centSign(Option... options) {
        return format ("cent-sign", options);
    }

    public static String flask(Option... options) {
        return format ("flask", options);
    }

    public static String personPregnant(Option... options) {
        return format ("person-pregnant", options);
    }

    public static String wandSparkles(Option... options) {
        return format ("wand-sparkles", options);
    }

    public static String ellipsisVertical(Option... options) {
        return format ("ellipsis-vertical", options);
    }

    public static String ellipsisV(Option... options) {
        return format ("ellipsis-v", options);
    }

    public static String ticket(Option... options) {
        return format ("ticket", options);
    }

    public static String powerOff(Option... options) {
        return format ("power-off", options);
    }

    public static String rightLong(Option... options) {
        return format ("right-long", options);
    }

    public static String longArrowAltRight(Option... options) {
        return format ("long-arrow-alt-right", options);
    }

    public static String flagUsa(Option... options) {
        return format ("flag-usa", options);
    }

    public static String laptopFile(Option... options) {
        return format ("laptop-file", options);
    }

    public static String tty(Option... options) {
        return format ("tty", options);
    }

    public static String teletype(Option... options) {
        return format ("teletype", options);
    }

    public static String diagramNext(Option... options) {
        return format ("diagram-next", options);
    }

    public static String personRifle(Option... options) {
        return format ("person-rifle", options);
    }

    public static String houseMedicalCircleExclamation(Option... options) {
        return format ("house-medical-circle-exclamation", options);
    }

    public static String closedCaptioning(Option... options) {
        return format ("closed-captioning", options);
    }

    public static String personHiking(Option... options) {
        return format ("person-hiking", options);
    }

    public static String hiking(Option... options) {
        return format ("hiking", options);
    }

    public static String venusDouble(Option... options) {
        return format ("venus-double", options);
    }

    public static String images(Option... options) {
        return format ("images", options);
    }

    public static String calculator(Option... options) {
        return format ("calculator", options);
    }

    public static String peoplePulling(Option... options) {
        return format ("people-pulling", options);
    }

    public static String cableCar(Option... options) {
        return format ("cable-car", options);
    }

    public static String tram(Option... options) {
        return format ("tram", options);
    }

    public static String cloudRain(Option... options) {
        return format ("cloud-rain", options);
    }

    public static String buildingCircleXmark(Option... options) {
        return format ("building-circle-xmark", options);
    }

    public static String ship(Option... options) {
        return format ("ship", options);
    }

    public static String arrowsDownToLine(Option... options) {
        return format ("arrows-down-to-line", options);
    }

    public static String download(Option... options) {
        return format ("download", options);
    }

    public static String faceGrin(Option... options) {
        return format ("face-grin", options);
    }

    public static String grin(Option... options) {
        return format ("grin", options);
    }

    public static String deleteLeft(Option... options) {
        return format ("delete-left", options);
    }

    public static String backspace(Option... options) {
        return format ("backspace", options);
    }

    public static String eyeDropper(Option... options) {
        return format ("eye-dropper", options);
    }

    public static String eyeDropperEmpty(Option... options) {
        return format ("eye-dropper-empty", options);
    }

    public static String eyedropper(Option... options) {
        return format ("eyedropper", options);
    }

    public static String fileCircleCheck(Option... options) {
        return format ("file-circle-check", options);
    }

    public static String forward(Option... options) {
        return format ("forward", options);
    }

    public static String mobile(Option... options) {
        return format ("mobile", options);
    }

    public static String mobileAndroid(Option... options) {
        return format ("mobile-android", options);
    }

    public static String mobilePhone(Option... options) {
        return format ("mobile-phone", options);
    }

    public static String faceMeh(Option... options) {
        return format ("face-meh", options);
    }

    public static String meh(Option... options) {
        return format ("meh", options);
    }

    public static String alignCenter(Option... options) {
        return format ("align-center", options);
    }

    public static String bookSkull(Option... options) {
        return format ("book-skull", options);
    }

    public static String bookDead(Option... options) {
        return format ("book-dead", options);
    }

    public static String idCard(Option... options) {
        return format ("id-card", options);
    }

    public static String driversLicense(Option... options) {
        return format ("drivers-license", options);
    }

    public static String outdent(Option... options) {
        return format ("outdent", options);
    }

    public static String dedent(Option... options) {
        return format ("dedent", options);
    }

    public static String heartCircleExclamation(Option... options) {
        return format ("heart-circle-exclamation", options);
    }

    public static String house(Option... options) {
        return format ("house", options);
    }

    public static String home(Option... options) {
        return format ("home", options);
    }

    public static String homeAlt(Option... options) {
        return format ("home-alt", options);
    }

    public static String homeLgAlt(Option... options) {
        return format ("home-lg-alt", options);
    }

    public static String calendarWeek(Option... options) {
        return format ("calendar-week", options);
    }

    public static String laptopMedical(Option... options) {
        return format ("laptop-medical", options);
    }

    public static String fileMedical(Option... options) {
        return format ("file-medical", options);
    }

    public static String diceOne(Option... options) {
        return format ("dice-one", options);
    }

    public static String kiwiBird(Option... options) {
        return format ("kiwi-bird", options);
    }

    public static String arrowRightArrowLeft(Option... options) {
        return format ("arrow-right-arrow-left", options);
    }

    public static String exchange(Option... options) {
        return format ("exchange", options);
    }

    public static String rotateRight(Option... options) {
        return format ("rotate-right", options);
    }

    public static String redoAlt(Option... options) {
        return format ("redo-alt", options);
    }

    public static String rotateForward(Option... options) {
        return format ("rotate-forward", options);
    }

    public static String utensils(Option... options) {
        return format ("utensils", options);
    }

    public static String cutlery(Option... options) {
        return format ("cutlery", options);
    }

    public static String arrowUpWideShort(Option... options) {
        return format ("arrow-up-wide-short", options);
    }

    public static String sortAmountUp(Option... options) {
        return format ("sort-amount-up", options);
    }

    public static String millSign(Option... options) {
        return format ("mill-sign", options);
    }

    public static String bowlRice(Option... options) {
        return format ("bowl-rice", options);
    }

    public static String skull(Option... options) {
        return format ("skull", options);
    }

    public static String towerBroadcast(Option... options) {
        return format ("tower-broadcast", options);
    }

    public static String broadcastTower(Option... options) {
        return format ("broadcast-tower", options);
    }

    public static String truckPickup(Option... options) {
        return format ("truck-pickup", options);
    }

    public static String upLong(Option... options) {
        return format ("up-long", options);
    }

    public static String longArrowAltUp(Option... options) {
        return format ("long-arrow-alt-up", options);
    }

    public static String stop(Option... options) {
        return format ("stop", options);
    }

    public static String codeMerge(Option... options) {
        return format ("code-merge", options);
    }

    public static String upload(Option... options) {
        return format ("upload", options);
    }

    public static String hurricane(Option... options) {
        return format ("hurricane", options);
    }

    public static String mound(Option... options) {
        return format ("mound", options);
    }

    public static String toiletPortable(Option... options) {
        return format ("toilet-portable", options);
    }

    public static String compactDisc(Option... options) {
        return format ("compact-disc", options);
    }

    public static String fileArrowDown(Option... options) {
        return format ("file-arrow-down", options);
    }

    public static String fileDownload(Option... options) {
        return format ("file-download", options);
    }

    public static String caravan(Option... options) {
        return format ("caravan", options);
    }

    public static String shieldCat(Option... options) {
        return format ("shield-cat", options);
    }

    public static String bolt(Option... options) {
        return format ("bolt", options);
    }

    public static String zap(Option... options) {
        return format ("zap", options);
    }

    public static String glassWater(Option... options) {
        return format ("glass-water", options);
    }

    public static String oilWell(Option... options) {
        return format ("oil-well", options);
    }

    public static String vault(Option... options) {
        return format ("vault", options);
    }

    public static String mars(Option... options) {
        return format ("mars", options);
    }

    public static String toilet(Option... options) {
        return format ("toilet", options);
    }

    public static String planeCircleXmark(Option... options) {
        return format ("plane-circle-xmark", options);
    }

    public static String yenSign(Option... options) {
        return format ("yen-sign", options);
    }

    public static String cny(Option... options) {
        return format ("cny", options);
    }

    public static String jpy(Option... options) {
        return format ("jpy", options);
    }

    public static String rmb(Option... options) {
        return format ("rmb", options);
    }

    public static String yen(Option... options) {
        return format ("yen", options);
    }

    public static String rubleSign(Option... options) {
        return format ("ruble-sign", options);
    }

    public static String rouble(Option... options) {
        return format ("rouble", options);
    }

    public static String rub(Option... options) {
        return format ("rub", options);
    }

    public static String ruble(Option... options) {
        return format ("ruble", options);
    }

    public static String sun(Option... options) {
        return format ("sun", options);
    }

    public static String guitar(Option... options) {
        return format ("guitar", options);
    }

    public static String faceLaughWink(Option... options) {
        return format ("face-laugh-wink", options);
    }

    public static String laughWink(Option... options) {
        return format ("laugh-wink", options);
    }

    public static String horseHead(Option... options) {
        return format ("horse-head", options);
    }

    public static String boreHole(Option... options) {
        return format ("bore-hole", options);
    }

    public static String industry(Option... options) {
        return format ("industry", options);
    }

    public static String circleDown(Option... options) {
        return format ("circle-down", options);
    }

    public static String arrowAltCircleDown(Option... options) {
        return format ("arrow-alt-circle-down", options);
    }

    public static String arrowsTurnToDots(Option... options) {
        return format ("arrows-turn-to-dots", options);
    }

    public static String florinSign(Option... options) {
        return format ("florin-sign", options);
    }

    public static String arrowDownShortWide(Option... options) {
        return format ("arrow-down-short-wide", options);
    }

    public static String sortAmountDesc(Option... options) {
        return format ("sort-amount-desc", options);
    }

    public static String sortAmountDownAlt(Option... options) {
        return format ("sort-amount-down-alt", options);
    }

    public static String lessThan(Option... options) {
        return format ("less-than", options);
    }

    public static String angleDown(Option... options) {
        return format ("angle-down", options);
    }

    public static String carTunnel(Option... options) {
        return format ("car-tunnel", options);
    }

    public static String headSideCough(Option... options) {
        return format ("head-side-cough", options);
    }

    public static String gripLines(Option... options) {
        return format ("grip-lines", options);
    }

    public static String thumbsDown(Option... options) {
        return format ("thumbs-down", options);
    }

    public static String userLock(Option... options) {
        return format ("user-lock", options);
    }

    public static String arrowRightLong(Option... options) {
        return format ("arrow-right-long", options);
    }

    public static String longArrowRight(Option... options) {
        return format ("long-arrow-right", options);
    }

    public static String anchorCircleXmark(Option... options) {
        return format ("anchor-circle-xmark", options);
    }

    public static String ellipsis(Option... options) {
        return format ("ellipsis", options);
    }

    public static String ellipsisH(Option... options) {
        return format ("ellipsis-h", options);
    }

    public static String chessPawn(Option... options) {
        return format ("chess-pawn", options);
    }

    public static String kitMedical(Option... options) {
        return format ("kit-medical", options);
    }

    public static String firstAid(Option... options) {
        return format ("first-aid", options);
    }

    public static String personThroughWindow(Option... options) {
        return format ("person-through-window", options);
    }

    public static String toolbox(Option... options) {
        return format ("toolbox", options);
    }

    public static String handsHoldingCircle(Option... options) {
        return format ("hands-holding-circle", options);
    }

    public static String bug(Option... options) {
        return format ("bug", options);
    }

    public static String creditCard(Option... options) {
        return format ("credit-card", options);
    }

    public static String creditCardAlt(Option... options) {
        return format ("credit-card-alt", options);
    }

    public static String car(Option... options) {
        return format ("car", options);
    }

    public static String automobile(Option... options) {
        return format ("automobile", options);
    }

    public static String handHoldingHand(Option... options) {
        return format ("hand-holding-hand", options);
    }

    public static String bookOpenReader(Option... options) {
        return format ("book-open-reader", options);
    }

    public static String bookReader(Option... options) {
        return format ("book-reader", options);
    }

    public static String mountainSun(Option... options) {
        return format ("mountain-sun", options);
    }

    public static String arrowsLeftRightToLine(Option... options) {
        return format ("arrows-left-right-to-line", options);
    }

    public static String diceD20(Option... options) {
        return format ("dice-d20", options);
    }

    public static String truckDroplet(Option... options) {
        return format ("truck-droplet", options);
    }

    public static String fileCircleXmark(Option... options) {
        return format ("file-circle-xmark", options);
    }

    public static String temperatureArrowUp(Option... options) {
        return format ("temperature-arrow-up", options);
    }

    public static String temperatureUp(Option... options) {
        return format ("temperature-up", options);
    }

    public static String medal(Option... options) {
        return format ("medal", options);
    }

    public static String bed(Option... options) {
        return format ("bed", options);
    }

    public static String squareH(Option... options) {
        return format ("square-h", options);
    }

    public static String hSquare(Option... options) {
        return format ("h-square", options);
    }

    public static String podcast(Option... options) {
        return format ("podcast", options);
    }

    public static String temperatureFull(Option... options) {
        return format ("temperature-full", options);
    }

    public static String temperature4(Option... options) {
        return format ("temperature-4", options);
    }

    public static String thermometer4(Option... options) {
        return format ("thermometer-4", options);
    }

    public static String thermometerFull(Option... options) {
        return format ("thermometer-full", options);
    }

    public static String bell(Option... options) {
        return format ("bell", options);
    }

    public static String superscript(Option... options) {
        return format ("superscript", options);
    }

    public static String plugCircleXmark(Option... options) {
        return format ("plug-circle-xmark", options);
    }

    public static String starOfLife(Option... options) {
        return format ("star-of-life", options);
    }

    public static String phoneSlash(Option... options) {
        return format ("phone-slash", options);
    }

    public static String paintRoller(Option... options) {
        return format ("paint-roller", options);
    }

    public static String handshakeAngle(Option... options) {
        return format ("handshake-angle", options);
    }

    public static String handsHelping(Option... options) {
        return format ("hands-helping", options);
    }

    public static String locationDot(Option... options) {
        return format ("location-dot", options);
    }

    public static String mapMarkerAlt(Option... options) {
        return format ("map-marker-alt", options);
    }

    public static String file(Option... options) {
        return format ("file", options);
    }

    public static String greaterThan(Option... options) {
        return format ("greater-than", options);
    }

    public static String personSwimming(Option... options) {
        return format ("person-swimming", options);
    }

    public static String swimmer(Option... options) {
        return format ("swimmer", options);
    }

    public static String arrowDown(Option... options) {
        return format ("arrow-down", options);
    }

    public static String droplet(Option... options) {
        return format ("droplet", options);
    }

    public static String tint(Option... options) {
        return format ("tint", options);
    }

    public static String eraser(Option... options) {
        return format ("eraser", options);
    }

    public static String earthAmericas(Option... options) {
        return format ("earth-americas", options);
    }

    public static String earth(Option... options) {
        return format ("earth", options);
    }

    public static String earthAmerica(Option... options) {
        return format ("earth-america", options);
    }

    public static String globeAmericas(Option... options) {
        return format ("globe-americas", options);
    }

    public static String personBurst(Option... options) {
        return format ("person-burst", options);
    }

    public static String dove(Option... options) {
        return format ("dove", options);
    }

    public static String batteryEmpty(Option... options) {
        return format ("battery-empty", options);
    }

    public static String battery0(Option... options) {
        return format ("battery-0", options);
    }

    public static String socks(Option... options) {
        return format ("socks", options);
    }

    public static String inbox(Option... options) {
        return format ("inbox", options);
    }

    public static String section(Option... options) {
        return format ("section", options);
    }

    public static String gaugeHigh(Option... options) {
        return format ("gauge-high", options);
    }

    public static String tachometerAlt(Option... options) {
        return format ("tachometer-alt", options);
    }

    public static String tachometerAltFast(Option... options) {
        return format ("tachometer-alt-fast", options);
    }

    public static String envelopeOpenText(Option... options) {
        return format ("envelope-open-text", options);
    }

    public static String hospital(Option... options) {
        return format ("hospital", options);
    }

    public static String hospitalAlt(Option... options) {
        return format ("hospital-alt", options);
    }

    public static String hospitalWide(Option... options) {
        return format ("hospital-wide", options);
    }

    public static String wineBottle(Option... options) {
        return format ("wine-bottle", options);
    }

    public static String chessRook(Option... options) {
        return format ("chess-rook", options);
    }

    public static String barsStaggered(Option... options) {
        return format ("bars-staggered", options);
    }

    public static String reorder(Option... options) {
        return format ("reorder", options);
    }

    public static String stream(Option... options) {
        return format ("stream", options);
    }

    public static String dharmachakra(Option... options) {
        return format ("dharmachakra", options);
    }

    public static String hotdog(Option... options) {
        return format ("hotdog", options);
    }

    public static String personWalkingWithCane(Option... options) {
        return format ("person-walking-with-cane", options);
    }

    public static String blind(Option... options) {
        return format ("blind", options);
    }

    public static String drum(Option... options) {
        return format ("drum", options);
    }

    public static String iceCream(Option... options) {
        return format ("ice-cream", options);
    }

    public static String heartCircleBolt(Option... options) {
        return format ("heart-circle-bolt", options);
    }

    public static String fax(Option... options) {
        return format ("fax", options);
    }

    public static String paragraph(Option... options) {
        return format ("paragraph", options);
    }

    public static String checkToSlot(Option... options) {
        return format ("check-to-slot", options);
    }

    public static String voteYea(Option... options) {
        return format ("vote-yea", options);
    }

    public static String starHalf(Option... options) {
        return format ("star-half", options);
    }

    public static String boxesStacked(Option... options) {
        return format ("boxes-stacked", options);
    }

    public static String boxes(Option... options) {
        return format ("boxes", options);
    }

    public static String boxesAlt(Option... options) {
        return format ("boxes-alt", options);
    }

    public static String link(Option... options) {
        return format ("link", options);
    }

    public static String chain(Option... options) {
        return format ("chain", options);
    }

    public static String earListen(Option... options) {
        return format ("ear-listen", options);
    }

    public static String assistiveListeningSystems(Option... options) {
        return format ("assistive-listening-systems", options);
    }

    public static String treeCity(Option... options) {
        return format ("tree-city", options);
    }

    public static String play(Option... options) {
        return format ("play", options);
    }

    public static String font(Option... options) {
        return format ("font", options);
    }

    public static String rupiahSign(Option... options) {
        return format ("rupiah-sign", options);
    }

    public static String magnifyingGlass(Option... options) {
        return format ("magnifying-glass", options);
    }

    public static String search(Option... options) {
        return format ("search", options);
    }

    public static String tableTennisPaddleBall(Option... options) {
        return format ("table-tennis-paddle-ball", options);
    }

    public static String pingPongPaddleBall(Option... options) {
        return format ("ping-pong-paddle-ball", options);
    }

    public static String tableTennis(Option... options) {
        return format ("table-tennis", options);
    }

    public static String personDotsFromLine(Option... options) {
        return format ("person-dots-from-line", options);
    }

    public static String diagnoses(Option... options) {
        return format ("diagnoses", options);
    }

    public static String trashCanArrowUp(Option... options) {
        return format ("trash-can-arrow-up", options);
    }

    public static String trashRestoreAlt(Option... options) {
        return format ("trash-restore-alt", options);
    }

    public static String nairaSign(Option... options) {
        return format ("naira-sign", options);
    }

    public static String cartArrowDown(Option... options) {
        return format ("cart-arrow-down", options);
    }

    public static String walkieTalkie(Option... options) {
        return format ("walkie-talkie", options);
    }

    public static String filePen(Option... options) {
        return format ("file-pen", options);
    }

    public static String fileEdit(Option... options) {
        return format ("file-edit", options);
    }

    public static String receipt(Option... options) {
        return format ("receipt", options);
    }

    public static String squarePen(Option... options) {
        return format ("square-pen", options);
    }

    public static String penSquare(Option... options) {
        return format ("pen-square", options);
    }

    public static String pencilSquare(Option... options) {
        return format ("pencil-square", options);
    }

    public static String suitcaseRolling(Option... options) {
        return format ("suitcase-rolling", options);
    }

    public static String personCircleExclamation(Option... options) {
        return format ("person-circle-exclamation", options);
    }

    public static String chevronDown(Option... options) {
        return format ("chevron-down", options);
    }

    public static String batteryFull(Option... options) {
        return format ("battery-full", options);
    }

    public static String battery(Option... options) {
        return format ("battery", options);
    }

    public static String battery5(Option... options) {
        return format ("battery-5", options);
    }

    public static String skullCrossbones(Option... options) {
        return format ("skull-crossbones", options);
    }

    public static String codeCompare(Option... options) {
        return format ("code-compare", options);
    }

    public static String listUl(Option... options) {
        return format ("list-ul", options);
    }

    public static String listDots(Option... options) {
        return format ("list-dots", options);
    }

    public static String schoolLock(Option... options) {
        return format ("school-lock", options);
    }

    public static String towerCell(Option... options) {
        return format ("tower-cell", options);
    }

    public static String downLong(Option... options) {
        return format ("down-long", options);
    }

    public static String longArrowAltDown(Option... options) {
        return format ("long-arrow-alt-down", options);
    }

    public static String rankingStar(Option... options) {
        return format ("ranking-star", options);
    }

    public static String chessKing(Option... options) {
        return format ("chess-king", options);
    }

    public static String personHarassing(Option... options) {
        return format ("person-harassing", options);
    }

    public static String brazilianRealSign(Option... options) {
        return format ("brazilian-real-sign", options);
    }

    public static String landmarkDome(Option... options) {
        return format ("landmark-dome", options);
    }

    public static String landmarkAlt(Option... options) {
        return format ("landmark-alt", options);
    }

    public static String arrowUp(Option... options) {
        return format ("arrow-up", options);
    }

    public static String tv(Option... options) {
        return format ("tv", options);
    }

    public static String television(Option... options) {
        return format ("television", options);
    }

    public static String tvAlt(Option... options) {
        return format ("tv-alt", options);
    }

    public static String shrimp(Option... options) {
        return format ("shrimp", options);
    }

    public static String listCheck(Option... options) {
        return format ("list-check", options);
    }

    public static String tasks(Option... options) {
        return format ("tasks", options);
    }

    public static String jugDetergent(Option... options) {
        return format ("jug-detergent", options);
    }

    public static String circleUser(Option... options) {
        return format ("circle-user", options);
    }

    public static String userCircle(Option... options) {
        return format ("user-circle", options);
    }

    public static String userShield(Option... options) {
        return format ("user-shield", options);
    }

    public static String wind(Option... options) {
        return format ("wind", options);
    }

    public static String carBurst(Option... options) {
        return format ("car-burst", options);
    }

    public static String carCrash(Option... options) {
        return format ("car-crash", options);
    }

    public static String personSnowboarding(Option... options) {
        return format ("person-snowboarding", options);
    }

    public static String snowboarding(Option... options) {
        return format ("snowboarding", options);
    }

    public static String truckFast(Option... options) {
        return format ("truck-fast", options);
    }

    public static String shippingFast(Option... options) {
        return format ("shipping-fast", options);
    }

    public static String fish(Option... options) {
        return format ("fish", options);
    }

    public static String userGraduate(Option... options) {
        return format ("user-graduate", options);
    }

    public static String circleHalfStroke(Option... options) {
        return format ("circle-half-stroke", options);
    }

    public static String adjust(Option... options) {
        return format ("adjust", options);
    }

    public static String clapperboard(Option... options) {
        return format ("clapperboard", options);
    }

    public static String circleRadiation(Option... options) {
        return format ("circle-radiation", options);
    }

    public static String radiationAlt(Option... options) {
        return format ("radiation-alt", options);
    }

    public static String baseball(Option... options) {
        return format ("baseball", options);
    }

    public static String baseballBall(Option... options) {
        return format ("baseball-ball", options);
    }

    public static String jetFighterUp(Option... options) {
        return format ("jet-fighter-up", options);
    }

    public static String diagramProject(Option... options) {
        return format ("diagram-project", options);
    }

    public static String projectDiagram(Option... options) {
        return format ("project-diagram", options);
    }

    public static String copy(Option... options) {
        return format ("copy", options);
    }

    public static String volumeXmark(Option... options) {
        return format ("volume-xmark", options);
    }

    public static String volumeMute(Option... options) {
        return format ("volume-mute", options);
    }

    public static String volumeTimes(Option... options) {
        return format ("volume-times", options);
    }

    public static String handSparkles(Option... options) {
        return format ("hand-sparkles", options);
    }

    public static String grip(Option... options) {
        return format ("grip", options);
    }

    public static String gripHorizontal(Option... options) {
        return format ("grip-horizontal", options);
    }

    public static String shareFromSquare(Option... options) {
        return format ("share-from-square", options);
    }

    public static String shareSquare(Option... options) {
        return format ("share-square", options);
    }

    public static String childCombatant(Option... options) {
        return format ("child-combatant", options);
    }

    public static String childRifle(Option... options) {
        return format ("child-rifle", options);
    }

    public static String gun(Option... options) {
        return format ("gun", options);
    }

    public static String squarePhone(Option... options) {
        return format ("square-phone", options);
    }

    public static String phoneSquare(Option... options) {
        return format ("phone-square", options);
    }

    public static String plus(Option... options) {
        return format ("plus", options);
    }

    public static String add(Option... options) {
        return format ("add", options);
    }

    public static String expand(Option... options) {
        return format ("expand", options);
    }

    public static String computer(Option... options) {
        return format ("computer", options);
    }

    public static String xmark(Option... options) {
        return format ("xmark", options);
    }

    public static String close(Option... options) {
        return format ("close", options);
    }

    public static String multiply(Option... options) {
        return format ("multiply", options);
    }

    public static String remove(Option... options) {
        return format ("remove", options);
    }

    public static String times(Option... options) {
        return format ("times", options);
    }

    public static String arrowsUpDownLeftRight(Option... options) {
        return format ("arrows-up-down-left-right", options);
    }

    public static String arrows(Option... options) {
        return format ("arrows", options);
    }

    public static String chalkboardUser(Option... options) {
        return format ("chalkboard-user", options);
    }

    public static String chalkboardTeacher(Option... options) {
        return format ("chalkboard-teacher", options);
    }

    public static String pesoSign(Option... options) {
        return format ("peso-sign", options);
    }

    public static String buildingShield(Option... options) {
        return format ("building-shield", options);
    }

    public static String baby(Option... options) {
        return format ("baby", options);
    }

    public static String usersLine(Option... options) {
        return format ("users-line", options);
    }

    public static String quoteLeft(Option... options) {
        return format ("quote-left", options);
    }

    public static String quoteLeftAlt(Option... options) {
        return format ("quote-left-alt", options);
    }

    public static String tractor(Option... options) {
        return format ("tractor", options);
    }

    public static String trashArrowUp(Option... options) {
        return format ("trash-arrow-up", options);
    }

    public static String trashRestore(Option... options) {
        return format ("trash-restore", options);
    }

    public static String arrowDownUpLock(Option... options) {
        return format ("arrow-down-up-lock", options);
    }

    public static String linesLeaning(Option... options) {
        return format ("lines-leaning", options);
    }

    public static String rulerCombined(Option... options) {
        return format ("ruler-combined", options);
    }

    public static String copyright(Option... options) {
        return format ("copyright", options);
    }

    public static String equals(Option... options) {
        return format ("equals", options);
    }

    public static String blender(Option... options) {
        return format ("blender", options);
    }

    public static String teeth(Option... options) {
        return format ("teeth", options);
    }

    public static String shekelSign(Option... options) {
        return format ("shekel-sign", options);
    }

    public static String ils(Option... options) {
        return format ("ils", options);
    }

    public static String shekel(Option... options) {
        return format ("shekel", options);
    }

    public static String sheqel(Option... options) {
        return format ("sheqel", options);
    }

    public static String sheqelSign(Option... options) {
        return format ("sheqel-sign", options);
    }

    public static String map(Option... options) {
        return format ("map", options);
    }

    public static String rocket(Option... options) {
        return format ("rocket", options);
    }

    public static String photoFilm(Option... options) {
        return format ("photo-film", options);
    }

    public static String photoVideo(Option... options) {
        return format ("photo-video", options);
    }

    public static String folderMinus(Option... options) {
        return format ("folder-minus", options);
    }

    public static String store(Option... options) {
        return format ("store", options);
    }

    public static String arrowTrendUp(Option... options) {
        return format ("arrow-trend-up", options);
    }

    public static String plugCircleMinus(Option... options) {
        return format ("plug-circle-minus", options);
    }

    public static String signHanging(Option... options) {
        return format ("sign-hanging", options);
    }

    public static String sign(Option... options) {
        return format ("sign", options);
    }

    public static String bezierCurve(Option... options) {
        return format ("bezier-curve", options);
    }

    public static String bellSlash(Option... options) {
        return format ("bell-slash", options);
    }

    public static String tablet(Option... options) {
        return format ("tablet", options);
    }

    public static String tabletAndroid(Option... options) {
        return format ("tablet-android", options);
    }

    public static String schoolFlag(Option... options) {
        return format ("school-flag", options);
    }

    public static String fill(Option... options) {
        return format ("fill", options);
    }

    public static String angleUp(Option... options) {
        return format ("angle-up", options);
    }

    public static String drumstickBite(Option... options) {
        return format ("drumstick-bite", options);
    }

    public static String hollyBerry(Option... options) {
        return format ("holly-berry", options);
    }

    public static String chevronLeft(Option... options) {
        return format ("chevron-left", options);
    }

    public static String bacteria(Option... options) {
        return format ("bacteria", options);
    }

    public static String handLizard(Option... options) {
        return format ("hand-lizard", options);
    }

    public static String notdef(Option... options) {
        return format ("notdef", options);
    }

    public static String disease(Option... options) {
        return format ("disease", options);
    }

    public static String briefcaseMedical(Option... options) {
        return format ("briefcase-medical", options);
    }

    public static String genderless(Option... options) {
        return format ("genderless", options);
    }

    public static String chevronRight(Option... options) {
        return format ("chevron-right", options);
    }

    public static String retweet(Option... options) {
        return format ("retweet", options);
    }

    public static String carRear(Option... options) {
        return format ("car-rear", options);
    }

    public static String carAlt(Option... options) {
        return format ("car-alt", options);
    }

    public static String pumpSoap(Option... options) {
        return format ("pump-soap", options);
    }

    public static String videoSlash(Option... options) {
        return format ("video-slash", options);
    }

    public static String batteryQuarter(Option... options) {
        return format ("battery-quarter", options);
    }

    public static String battery2(Option... options) {
        return format ("battery-2", options);
    }

    public static String radio(Option... options) {
        return format ("radio", options);
    }

    public static String babyCarriage(Option... options) {
        return format ("baby-carriage", options);
    }

    public static String carriageBaby(Option... options) {
        return format ("carriage-baby", options);
    }

    public static String trafficLight(Option... options) {
        return format ("traffic-light", options);
    }

    public static String thermometer(Option... options) {
        return format ("thermometer", options);
    }

    public static String vrCardboard(Option... options) {
        return format ("vr-cardboard", options);
    }

    public static String handMiddleFinger(Option... options) {
        return format ("hand-middle-finger", options);
    }

    public static String percent(Option... options) {
        return format ("percent", options);
    }

    public static String percentage(Option... options) {
        return format ("percentage", options);
    }

    public static String truckMoving(Option... options) {
        return format ("truck-moving", options);
    }

    public static String glassWaterDroplet(Option... options) {
        return format ("glass-water-droplet", options);
    }

    public static String display(Option... options) {
        return format ("display", options);
    }

    public static String faceSmile(Option... options) {
        return format ("face-smile", options);
    }

    public static String smile(Option... options) {
        return format ("smile", options);
    }

    public static String thumbtack(Option... options) {
        return format ("thumbtack", options);
    }

    public static String thumbTack(Option... options) {
        return format ("thumb-tack", options);
    }

    public static String trophy(Option... options) {
        return format ("trophy", options);
    }

    public static String personPraying(Option... options) {
        return format ("person-praying", options);
    }

    public static String pray(Option... options) {
        return format ("pray", options);
    }

    public static String hammer(Option... options) {
        return format ("hammer", options);
    }

    public static String handPeace(Option... options) {
        return format ("hand-peace", options);
    }

    public static String rotate(Option... options) {
        return format ("rotate", options);
    }

    public static String syncAlt(Option... options) {
        return format ("sync-alt", options);
    }

    public static String spinner(Option... options) {
        return format ("spinner", options);
    }

    public static String robot(Option... options) {
        return format ("robot", options);
    }

    public static String peace(Option... options) {
        return format ("peace", options);
    }

    public static String gears(Option... options) {
        return format ("gears", options);
    }

    public static String cogs(Option... options) {
        return format ("cogs", options);
    }

    public static String warehouse(Option... options) {
        return format ("warehouse", options);
    }

    public static String arrowUpRightDots(Option... options) {
        return format ("arrow-up-right-dots", options);
    }

    public static String splotch(Option... options) {
        return format ("splotch", options);
    }

    public static String faceGrinHearts(Option... options) {
        return format ("face-grin-hearts", options);
    }

    public static String grinHearts(Option... options) {
        return format ("grin-hearts", options);
    }

    public static String diceFour(Option... options) {
        return format ("dice-four", options);
    }

    public static String simCard(Option... options) {
        return format ("sim-card", options);
    }

    public static String transgender(Option... options) {
        return format ("transgender", options);
    }

    public static String transgenderAlt(Option... options) {
        return format ("transgender-alt", options);
    }

    public static String mercury(Option... options) {
        return format ("mercury", options);
    }

    public static String arrowTurnDown(Option... options) {
        return format ("arrow-turn-down", options);
    }

    public static String levelDown(Option... options) {
        return format ("level-down", options);
    }

    public static String personFallingBurst(Option... options) {
        return format ("person-falling-burst", options);
    }

    public static String award(Option... options) {
        return format ("award", options);
    }

    public static String ticketSimple(Option... options) {
        return format ("ticket-simple", options);
    }

    public static String ticketAlt(Option... options) {
        return format ("ticket-alt", options);
    }

    public static String building(Option... options) {
        return format ("building", options);
    }

    public static String anglesLeft(Option... options) {
        return format ("angles-left", options);
    }

    public static String angleDoubleLeft(Option... options) {
        return format ("angle-double-left", options);
    }

    public static String qrcode(Option... options) {
        return format ("qrcode", options);
    }

    public static String clockRotateLeft(Option... options) {
        return format ("clock-rotate-left", options);
    }

    public static String history(Option... options) {
        return format ("history", options);
    }

    public static String faceGrinBeamSweat(Option... options) {
        return format ("face-grin-beam-sweat", options);
    }

    public static String grinBeamSweat(Option... options) {
        return format ("grin-beam-sweat", options);
    }

    public static String fileExport(Option... options) {
        return format ("file-export", options);
    }

    public static String arrowRightFromFile(Option... options) {
        return format ("arrow-right-from-file", options);
    }

    public static String shield(Option... options) {
        return format ("shield", options);
    }

    public static String shieldBlank(Option... options) {
        return format ("shield-blank", options);
    }

    public static String arrowUpShortWide(Option... options) {
        return format ("arrow-up-short-wide", options);
    }

    public static String sortAmountUpAlt(Option... options) {
        return format ("sort-amount-up-alt", options);
    }

    public static String houseMedical(Option... options) {
        return format ("house-medical", options);
    }

    public static String golfBallTee(Option... options) {
        return format ("golf-ball-tee", options);
    }

    public static String golfBall(Option... options) {
        return format ("golf-ball", options);
    }

    public static String circleChevronLeft(Option... options) {
        return format ("circle-chevron-left", options);
    }

    public static String chevronCircleLeft(Option... options) {
        return format ("chevron-circle-left", options);
    }

    public static String houseChimneyWindow(Option... options) {
        return format ("house-chimney-window", options);
    }

    public static String penNib(Option... options) {
        return format ("pen-nib", options);
    }

    public static String tentArrowTurnLeft(Option... options) {
        return format ("tent-arrow-turn-left", options);
    }

    public static String tents(Option... options) {
        return format ("tents", options);
    }

    public static String wandMagic(Option... options) {
        return format ("wand-magic", options);
    }

    public static String magic(Option... options) {
        return format ("magic", options);
    }

    public static String dog(Option... options) {
        return format ("dog", options);
    }

    public static String carrot(Option... options) {
        return format ("carrot", options);
    }

    public static String moon(Option... options) {
        return format ("moon", options);
    }

    public static String wineGlassEmpty(Option... options) {
        return format ("wine-glass-empty", options);
    }

    public static String wineGlassAlt(Option... options) {
        return format ("wine-glass-alt", options);
    }

    public static String cheese(Option... options) {
        return format ("cheese", options);
    }

    public static String yinYang(Option... options) {
        return format ("yin-yang", options);
    }

    public static String music(Option... options) {
        return format ("music", options);
    }

    public static String codeCommit(Option... options) {
        return format ("code-commit", options);
    }

    public static String temperatureLow(Option... options) {
        return format ("temperature-low", options);
    }

    public static String personBiking(Option... options) {
        return format ("person-biking", options);
    }

    public static String biking(Option... options) {
        return format ("biking", options);
    }

    public static String broom(Option... options) {
        return format ("broom", options);
    }

    public static String shieldHeart(Option... options) {
        return format ("shield-heart", options);
    }

    public static String gopuram(Option... options) {
        return format ("gopuram", options);
    }

    public static String earthOceania(Option... options) {
        return format ("earth-oceania", options);
    }

    public static String globeOceania(Option... options) {
        return format ("globe-oceania", options);
    }

    public static String squareXmark(Option... options) {
        return format ("square-xmark", options);
    }

    public static String timesSquare(Option... options) {
        return format ("times-square", options);
    }

    public static String xmarkSquare(Option... options) {
        return format ("xmark-square", options);
    }

    public static String hashtag(Option... options) {
        return format ("hashtag", options);
    }

    public static String upRightAndDownLeftFromCenter(Option... options) {
        return format ("up-right-and-down-left-from-center", options);
    }

    public static String expandAlt(Option... options) {
        return format ("expand-alt", options);
    }

    public static String oilCan(Option... options) {
        return format ("oil-can", options);
    }

    public static String hippo(Option... options) {
        return format ("hippo", options);
    }

    public static String chartColumn(Option... options) {
        return format ("chart-column", options);
    }

    public static String infinity(Option... options) {
        return format ("infinity", options);
    }

    public static String vialCircleCheck(Option... options) {
        return format ("vial-circle-check", options);
    }

    public static String personArrowDownToLine(Option... options) {
        return format ("person-arrow-down-to-line", options);
    }

    public static String voicemail(Option... options) {
        return format ("voicemail", options);
    }

    public static String fan(Option... options) {
        return format ("fan", options);
    }

    public static String personWalkingLuggage(Option... options) {
        return format ("person-walking-luggage", options);
    }

    public static String upDown(Option... options) {
        return format ("up-down", options);
    }

    public static String arrowsAltV(Option... options) {
        return format ("arrows-alt-v", options);
    }

    public static String cloudMoonRain(Option... options) {
        return format ("cloud-moon-rain", options);
    }

    public static String calendar(Option... options) {
        return format ("calendar", options);
    }

    public static String trailer(Option... options) {
        return format ("trailer", options);
    }

    public static String bahai(Option... options) {
        return format ("bahai", options);
    }

    public static String haykal(Option... options) {
        return format ("haykal", options);
    }

    public static String sdCard(Option... options) {
        return format ("sd-card", options);
    }

    public static String dragon(Option... options) {
        return format ("dragon", options);
    }

    public static String shoePrints(Option... options) {
        return format ("shoe-prints", options);
    }

    public static String circlePlus(Option... options) {
        return format ("circle-plus", options);
    }

    public static String plusCircle(Option... options) {
        return format ("plus-circle", options);
    }

    public static String faceGrinTongueWink(Option... options) {
        return format ("face-grin-tongue-wink", options);
    }

    public static String grinTongueWink(Option... options) {
        return format ("grin-tongue-wink", options);
    }

    public static String handHolding(Option... options) {
        return format ("hand-holding", options);
    }

    public static String plugCircleExclamation(Option... options) {
        return format ("plug-circle-exclamation", options);
    }

    public static String linkSlash(Option... options) {
        return format ("link-slash", options);
    }

    public static String chainBroken(Option... options) {
        return format ("chain-broken", options);
    }

    public static String chainSlash(Option... options) {
        return format ("chain-slash", options);
    }

    public static String unlink(Option... options) {
        return format ("unlink", options);
    }

    public static String clone(Option... options) {
        return format ("clone", options);
    }

    public static String personWalkingArrowLoopLeft(Option... options) {
        return format ("person-walking-arrow-loop-left", options);
    }

    public static String arrowUpZA(Option... options) {
        return format ("arrow-up-z-a", options);
    }

    public static String sortAlphaUpAlt(Option... options) {
        return format ("sort-alpha-up-alt", options);
    }

    public static String fireFlameCurved(Option... options) {
        return format ("fire-flame-curved", options);
    }

    public static String fireAlt(Option... options) {
        return format ("fire-alt", options);
    }

    public static String tornado(Option... options) {
        return format ("tornado", options);
    }

    public static String fileCirclePlus(Option... options) {
        return format ("file-circle-plus", options);
    }

    public static String bookQuran(Option... options) {
        return format ("book-quran", options);
    }

    public static String quran(Option... options) {
        return format ("quran", options);
    }

    public static String anchor(Option... options) {
        return format ("anchor", options);
    }

    public static String borderAll(Option... options) {
        return format ("border-all", options);
    }

    public static String faceAngry(Option... options) {
        return format ("face-angry", options);
    }

    public static String angry(Option... options) {
        return format ("angry", options);
    }

    public static String cookieBite(Option... options) {
        return format ("cookie-bite", options);
    }

    public static String arrowTrendDown(Option... options) {
        return format ("arrow-trend-down", options);
    }

    public static String rss(Option... options) {
        return format ("rss", options);
    }

    public static String feed(Option... options) {
        return format ("feed", options);
    }

    public static String drawPolygon(Option... options) {
        return format ("draw-polygon", options);
    }

    public static String scaleBalanced(Option... options) {
        return format ("scale-balanced", options);
    }

    public static String balanceScale(Option... options) {
        return format ("balance-scale", options);
    }

    public static String gaugeSimpleHigh(Option... options) {
        return format ("gauge-simple-high", options);
    }

    public static String tachometer(Option... options) {
        return format ("tachometer", options);
    }

    public static String tachometerFast(Option... options) {
        return format ("tachometer-fast", options);
    }

    public static String shower(Option... options) {
        return format ("shower", options);
    }

    public static String desktop(Option... options) {
        return format ("desktop", options);
    }

    public static String desktopAlt(Option... options) {
        return format ("desktop-alt", options);
    }

    public static String tableList(Option... options) {
        return format ("table-list", options);
    }

    public static String thList(Option... options) {
        return format ("th-list", options);
    }

    public static String commentSms(Option... options) {
        return format ("comment-sms", options);
    }

    public static String sms(Option... options) {
        return format ("sms", options);
    }

    public static String book(Option... options) {
        return format ("book", options);
    }

    public static String userPlus(Option... options) {
        return format ("user-plus", options);
    }

    public static String check(Option... options) {
        return format ("check", options);
    }

    public static String batteryThreeQuarters(Option... options) {
        return format ("battery-three-quarters", options);
    }

    public static String battery4(Option... options) {
        return format ("battery-4", options);
    }

    public static String houseCircleCheck(Option... options) {
        return format ("house-circle-check", options);
    }

    public static String angleLeft(Option... options) {
        return format ("angle-left", options);
    }

    public static String diagramSuccessor(Option... options) {
        return format ("diagram-successor", options);
    }

    public static String truckArrowRight(Option... options) {
        return format ("truck-arrow-right", options);
    }

    public static String arrowsSplitUpAndLeft(Option... options) {
        return format ("arrows-split-up-and-left", options);
    }

    public static String handFist(Option... options) {
        return format ("hand-fist", options);
    }

    public static String fistRaised(Option... options) {
        return format ("fist-raised", options);
    }

    public static String cloudMoon(Option... options) {
        return format ("cloud-moon", options);
    }

    public static String briefcase(Option... options) {
        return format ("briefcase", options);
    }

    public static String personFalling(Option... options) {
        return format ("person-falling", options);
    }

    public static String imagePortrait(Option... options) {
        return format ("image-portrait", options);
    }

    public static String portrait(Option... options) {
        return format ("portrait", options);
    }

    public static String userTag(Option... options) {
        return format ("user-tag", options);
    }

    public static String rug(Option... options) {
        return format ("rug", options);
    }

    public static String earthEurope(Option... options) {
        return format ("earth-europe", options);
    }

    public static String globeEurope(Option... options) {
        return format ("globe-europe", options);
    }

    public static String cartFlatbedSuitcase(Option... options) {
        return format ("cart-flatbed-suitcase", options);
    }

    public static String luggageCart(Option... options) {
        return format ("luggage-cart", options);
    }

    public static String rectangleXmark(Option... options) {
        return format ("rectangle-xmark", options);
    }

    public static String rectangleTimes(Option... options) {
        return format ("rectangle-times", options);
    }

    public static String timesRectangle(Option... options) {
        return format ("times-rectangle", options);
    }

    public static String windowClose(Option... options) {
        return format ("window-close", options);
    }

    public static String bahtSign(Option... options) {
        return format ("baht-sign", options);
    }

    public static String bookOpen(Option... options) {
        return format ("book-open", options);
    }

    public static String bookJournalWhills(Option... options) {
        return format ("book-journal-whills", options);
    }

    public static String journalWhills(Option... options) {
        return format ("journal-whills", options);
    }

    public static String handcuffs(Option... options) {
        return format ("handcuffs", options);
    }

    public static String triangleExclamation(Option... options) {
        return format ("triangle-exclamation", options);
    }

    public static String exclamationTriangle(Option... options) {
        return format ("exclamation-triangle", options);
    }

    public static String warning(Option... options) {
        return format ("warning", options);
    }

    public static String database(Option... options) {
        return format ("database", options);
    }

    public static String share(Option... options) {
        return format ("share", options);
    }

    public static String arrowTurnRight(Option... options) {
        return format ("arrow-turn-right", options);
    }

    public static String mailForward(Option... options) {
        return format ("mail-forward", options);
    }

    public static String bottleDroplet(Option... options) {
        return format ("bottle-droplet", options);
    }

    public static String maskFace(Option... options) {
        return format ("mask-face", options);
    }

    public static String hillRockslide(Option... options) {
        return format ("hill-rockslide", options);
    }

    public static String rightLeft(Option... options) {
        return format ("right-left", options);
    }

    public static String exchangeAlt(Option... options) {
        return format ("exchange-alt", options);
    }

    public static String paperPlane(Option... options) {
        return format ("paper-plane", options);
    }

    public static String roadCircleExclamation(Option... options) {
        return format ("road-circle-exclamation", options);
    }

    public static String dungeon(Option... options) {
        return format ("dungeon", options);
    }

    public static String alignRight(Option... options) {
        return format ("align-right", options);
    }

    public static String moneyBill1Wave(Option... options) {
        return format ("money-bill-1-wave", options);
    }

    public static String moneyBillWaveAlt(Option... options) {
        return format ("money-bill-wave-alt", options);
    }

    public static String lifeRing(Option... options) {
        return format ("life-ring", options);
    }

    public static String hands(Option... options) {
        return format ("hands", options);
    }

    public static String signLanguage(Option... options) {
        return format ("sign-language", options);
    }

    public static String signing(Option... options) {
        return format ("signing", options);
    }

    public static String calendarDay(Option... options) {
        return format ("calendar-day", options);
    }

    public static String waterLadder(Option... options) {
        return format ("water-ladder", options);
    }

    public static String ladderWater(Option... options) {
        return format ("ladder-water", options);
    }

    public static String swimmingPool(Option... options) {
        return format ("swimming-pool", options);
    }

    public static String arrowsUpDown(Option... options) {
        return format ("arrows-up-down", options);
    }

    public static String arrowsV(Option... options) {
        return format ("arrows-v", options);
    }

    public static String faceGrimace(Option... options) {
        return format ("face-grimace", options);
    }

    public static String grimace(Option... options) {
        return format ("grimace", options);
    }

    public static String wheelchairMove(Option... options) {
        return format ("wheelchair-move", options);
    }

    public static String wheelchairAlt(Option... options) {
        return format ("wheelchair-alt", options);
    }

    public static String turnDown(Option... options) {
        return format ("turn-down", options);
    }

    public static String levelDownAlt(Option... options) {
        return format ("level-down-alt", options);
    }

    public static String personWalkingArrowRight(Option... options) {
        return format ("person-walking-arrow-right", options);
    }

    public static String squareEnvelope(Option... options) {
        return format ("square-envelope", options);
    }

    public static String envelopeSquare(Option... options) {
        return format ("envelope-square", options);
    }

    public static String dice(Option... options) {
        return format ("dice", options);
    }

    public static String bowlingBall(Option... options) {
        return format ("bowling-ball", options);
    }

    public static String brain(Option... options) {
        return format ("brain", options);
    }

    public static String bandage(Option... options) {
        return format ("bandage", options);
    }

    public static String bandAid(Option... options) {
        return format ("band-aid", options);
    }

    public static String calendarMinus(Option... options) {
        return format ("calendar-minus", options);
    }

    public static String circleXmark(Option... options) {
        return format ("circle-xmark", options);
    }

    public static String timesCircle(Option... options) {
        return format ("times-circle", options);
    }

    public static String xmarkCircle(Option... options) {
        return format ("xmark-circle", options);
    }

    public static String gifts(Option... options) {
        return format ("gifts", options);
    }

    public static String hotel(Option... options) {
        return format ("hotel", options);
    }

    public static String earthAsia(Option... options) {
        return format ("earth-asia", options);
    }

    public static String globeAsia(Option... options) {
        return format ("globe-asia", options);
    }

    public static String idCardClip(Option... options) {
        return format ("id-card-clip", options);
    }

    public static String idCardAlt(Option... options) {
        return format ("id-card-alt", options);
    }

    public static String magnifyingGlassPlus(Option... options) {
        return format ("magnifying-glass-plus", options);
    }

    public static String searchPlus(Option... options) {
        return format ("search-plus", options);
    }

    public static String thumbsUp(Option... options) {
        return format ("thumbs-up", options);
    }

    public static String userClock(Option... options) {
        return format ("user-clock", options);
    }

    public static String handDots(Option... options) {
        return format ("hand-dots", options);
    }

    public static String allergies(Option... options) {
        return format ("allergies", options);
    }

    public static String fileInvoice(Option... options) {
        return format ("file-invoice", options);
    }

    public static String windowMinimize(Option... options) {
        return format ("window-minimize", options);
    }

    public static String mugSaucer(Option... options) {
        return format ("mug-saucer", options);
    }

    public static String coffee(Option... options) {
        return format ("coffee", options);
    }

    public static String brush(Option... options) {
        return format ("brush", options);
    }

    public static String mask(Option... options) {
        return format ("mask", options);
    }

    public static String magnifyingGlassMinus(Option... options) {
        return format ("magnifying-glass-minus", options);
    }

    public static String searchMinus(Option... options) {
        return format ("search-minus", options);
    }

    public static String rulerVertical(Option... options) {
        return format ("ruler-vertical", options);
    }

    public static String userLarge(Option... options) {
        return format ("user-large", options);
    }

    public static String userAlt(Option... options) {
        return format ("user-alt", options);
    }

    public static String trainTram(Option... options) {
        return format ("train-tram", options);
    }

    public static String userNurse(Option... options) {
        return format ("user-nurse", options);
    }

    public static String syringe(Option... options) {
        return format ("syringe", options);
    }

    public static String cloudSun(Option... options) {
        return format ("cloud-sun", options);
    }

    public static String stopwatch20(Option... options) {
        return format ("stopwatch-20", options);
    }

    public static String squareFull(Option... options) {
        return format ("square-full", options);
    }

    public static String magnet(Option... options) {
        return format ("magnet", options);
    }

    public static String jar(Option... options) {
        return format ("jar", options);
    }

    public static String noteSticky(Option... options) {
        return format ("note-sticky", options);
    }

    public static String stickyNote(Option... options) {
        return format ("sticky-note", options);
    }

    public static String bugSlash(Option... options) {
        return format ("bug-slash", options);
    }

    public static String arrowUpFromWaterPump(Option... options) {
        return format ("arrow-up-from-water-pump", options);
    }

    public static String bone(Option... options) {
        return format ("bone", options);
    }

    public static String userInjured(Option... options) {
        return format ("user-injured", options);
    }

    public static String faceSadTear(Option... options) {
        return format ("face-sad-tear", options);
    }

    public static String sadTear(Option... options) {
        return format ("sad-tear", options);
    }

    public static String plane(Option... options) {
        return format ("plane", options);
    }

    public static String tentArrowsDown(Option... options) {
        return format ("tent-arrows-down", options);
    }

    public static String exclamation(Option... options) {
        return format ("exclamation", options);
    }

    public static String arrowsSpin(Option... options) {
        return format ("arrows-spin", options);
    }

    public static String print(Option... options) {
        return format ("print", options);
    }

    public static String turkishLiraSign(Option... options) {
        return format ("turkish-lira-sign", options);
    }

    public static String try_(Option... options) {
        return format ("try", options);
    }

    public static String turkishLira(Option... options) {
        return format ("turkish-lira", options);
    }

    public static String dollarSign(Option... options) {
        return format ("dollar-sign", options);
    }

    public static String dollar(Option... options) {
        return format ("dollar", options);
    }

    public static String usd(Option... options) {
        return format ("usd", options);
    }

    public static String magnifyingGlassDollar(Option... options) {
        return format ("magnifying-glass-dollar", options);
    }

    public static String searchDollar(Option... options) {
        return format ("search-dollar", options);
    }

    public static String usersGear(Option... options) {
        return format ("users-gear", options);
    }

    public static String usersCog(Option... options) {
        return format ("users-cog", options);
    }

    public static String personMilitaryPointing(Option... options) {
        return format ("person-military-pointing", options);
    }

    public static String buildingColumns(Option... options) {
        return format ("building-columns", options);
    }

    public static String bank(Option... options) {
        return format ("bank", options);
    }

    public static String institution(Option... options) {
        return format ("institution", options);
    }

    public static String museum(Option... options) {
        return format ("museum", options);
    }

    public static String university(Option... options) {
        return format ("university", options);
    }

    public static String umbrella(Option... options) {
        return format ("umbrella", options);
    }

    public static String trowel(Option... options) {
        return format ("trowel", options);
    }

    public static String stapler(Option... options) {
        return format ("stapler", options);
    }

    public static String masksTheater(Option... options) {
        return format ("masks-theater", options);
    }

    public static String theaterMasks(Option... options) {
        return format ("theater-masks", options);
    }

    public static String kipSign(Option... options) {
        return format ("kip-sign", options);
    }

    public static String handPointLeft(Option... options) {
        return format ("hand-point-left", options);
    }

    public static String handshakeSimple(Option... options) {
        return format ("handshake-simple", options);
    }

    public static String handshakeAlt(Option... options) {
        return format ("handshake-alt", options);
    }

    public static String jetFighter(Option... options) {
        return format ("jet-fighter", options);
    }

    public static String fighterJet(Option... options) {
        return format ("fighter-jet", options);
    }

    public static String squareShareNodes(Option... options) {
        return format ("square-share-nodes", options);
    }

    public static String shareAltSquare(Option... options) {
        return format ("share-alt-square", options);
    }

    public static String barcode(Option... options) {
        return format ("barcode", options);
    }

    public static String plusMinus(Option... options) {
        return format ("plus-minus", options);
    }

    public static String video(Option... options) {
        return format ("video", options);
    }

    public static String videoCamera(Option... options) {
        return format ("video-camera", options);
    }

    public static String graduationCap(Option... options) {
        return format ("graduation-cap", options);
    }

    public static String mortarBoard(Option... options) {
        return format ("mortar-board", options);
    }

    public static String handHoldingMedical(Option... options) {
        return format ("hand-holding-medical", options);
    }

    public static String personCircleCheck(Option... options) {
        return format ("person-circle-check", options);
    }

    public static String turnUp(Option... options) {
        return format ("turn-up", options);
    }

    public static String levelUpAlt(Option... options) {
        return format ("level-up-alt", options);
    }

    /************************************************************************
     * Support methods.
     ************************************************************************/

    /**
     * Formats an icon using the supplied options.
     * <p>
     * Note that if no options are passed {@link Option#BOLD} is assumed (this is
     * the version that is most compatible with the free version of FontAwesome as
     * not all regular icons are included).
     * 
     * @param name
     *                the icon name (as from the FA icon browser).
     * @param options
     *                the options to apply.
     * @return the fully formatted css style application.
     */
    public static String format(String name, Option... options) {
        if (options.length == 0)
            return "fas fa-" + name;
        Set<Option> optionSet = new HashSet<Option> ();
        for (Option option : options)
            optionSet.add (option);
        String str = "fa";
        if (optionSet.contains (Option.THIN))
            str += "t";
        if (optionSet.contains (Option.LIGHT))
            str += "l";
        else if (optionSet.contains (Option.REGULAR))
            str += "r";
        else
            str += "s";
        if (optionSet.contains (Option.SPIN))
            str += " fa-spin";
        if (optionSet.contains (Option.SPIN_REVERSE))
            str += " fa-spin-reverse";
        if (optionSet.contains (Option.SPIN_PULSE))
            str += " fa-spin-pulse";
        if (optionSet.contains (Option.FLIP_HORIZONTAL))
            str += " fa-flip-horizontal";
        if (optionSet.contains (Option.FLIP_VERTICAL))
            str += " fa-flip-vertical";
        if (optionSet.contains (Option.FADE))
            str += " fa-fade";
        if (optionSet.contains (Option.BEAT))
            str += " fa-beat";
        if (optionSet.contains (Option.BEAT_FADE))
            str += " fa-beat-fade";
        if (optionSet.contains (Option.FLIP))
            str += " fa-flip";
        if (optionSet.contains (Option.SHAKE))
            str += " fa-shake";
        if (optionSet.contains (Option.BOUNCE))
            str += " fa-bounce";
        if (optionSet.contains (Option.BORDER))
            str += " fa-border";
        str += " fa-" + name;
        return str;
    }

    /************************************************************************
     * Method builder
     ************************************************************************/

    /**
     * Mechanism to interrogate the FontAwesome CSS file(s) to create methods for
     * each of the icons.
     * <p>
     * Run as a Java Application and it will print to standard out methods that call
     * {@link FontAwesome#format(String, Option...)} to generate the CSS styles for
     * a given icon. Copy these into the {@link FontAwesome} class (replacing the
     * existing ones).
     */
    @GwtIncompatible
    public static class FontAwesomeGenerator {

        /**
         * Generates methods from the FA CSS file.
         */
        public static void main(String... args) throws Exception {
            try (InputStream is = FontAwesomeGenerator.class.getResourceAsStream ("fontawesome.css")) {
                BufferedReader br = new BufferedReader (new InputStreamReader (is));
                String line = null;
                while ((line = br.readLine ()) != null) {
                    if (!line.startsWith (".fa-"))
                        continue;
                    if (!line.endsWith ("{"))
                        continue;
                    int idx = line.indexOf ("::before");
                    if (idx < 0)
                        continue;
                    line = line.substring (0, idx);
                    line = line.substring (4);
                    if (line.length () <= 1)
                        continue;
                    String cssStyle = line;
                    String methodName = null;
                    for (String part : line.split ("-")) {
                        if (methodName == null)
                            methodName = part;
                        else
                            methodName += StringUtils.capitalize (part);
                    }
                    System.out.println ("public static String " + methodName + "(Option... options) {");
                    System.out.println ("    return format(\"" + cssStyle + "\", options);");
                    System.out.println ("}");
                }
            }
        }
    }
}

