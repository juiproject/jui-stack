/*******************************************************************************
 * Copyright 2026 Jeremy Buckley
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
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import com.effacy.jui.core.client.Debug;
import com.effacy.jui.platform.core.JuiIncompatible;
import com.effacy.jui.platform.css.client.CssDeclaration;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.core.shared.GwtIncompatible;

import elemental2.dom.Element;

/**
 * Injects the Remix Icon (v4) font family and exposes every icon as a typed
 * static method.
 * <p>
 * Remix Icon is an open source set of ~1,500 icons (each with a -line and
 * -fill variant) by Remix Design — see {@link https://remixicon.com}.
 * <p>
 * <b>Licensing.</b> Remix Icon is licensed under the <i>Remix Icon License
 * v1.0</i> (a custom license, not Apache 2.0 — that changed in Jan 2026). The
 * license permits commercial use inside applications, UI kits and design
 * systems, but prohibits resale of the icons as a standalone icon pack or as
 * a competing icon library with only cosmetic modifications. See
 * {@code REMIX_LICENSE.txt} in this package's resources, the README.md next
 * to this file, and {@link https://github.com/Remix-Design/RemixIcon} for the
 * canonical terms. The complete license text ships alongside the font in the
 * resource package so consuming applications receive it automatically.
 * <p>
 * See the {@code README.md} file in this package for upgrade instructions and
 * a summary of how methods are generated via {@link RemixIconGenerator}.
 *
 * @author Jeremy Buckley
 */
public class RemixIcon {

    /**
     * Remix Icon version.
     */
    public static final String VERSION = "4.6.0";

    /**
     * Determines if it has been injected.
     */
    private static boolean INJECTED = false;

    /**
     * Declares the font resources. JUI rewrites the {@code @CssResource.Font}
     * annotation into a real {@code @font-face} block at compile time — the
     * upstream {@code remixicon.css} has had its {@code @font-face} stripped
     * so there is only one source of truth for the font declaration.
     */
    @CssResource ({ "remixicon.css" })
    @CssResource.Font (name = "remixicon", noinline = true, weight = "400", sources = { "remixicon.woff2" })
    public static abstract class FontCSS implements CssDeclaration {}

    /**
     * Instance of the font family.
     */
    private static FontCSS INSTANCE;

    /**
     * Private constructor.
     */
    private RemixIcon() {
        // Nothing.
    }

    /**
     * Inject the font to make it available.
     */
    public static final void inject() {
        if (!INJECTED) {
            if ((INSTANCE == null) && !Debug.isUnitTestMode()) {
                INSTANCE = (FontCSS) GWT.create (FontCSS.class);
                INSTANCE.ensureInjected ();
            }
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
     * Variants to apply when resolving an icon class name.
     * <p>
     * Remix Icon ships two visual variants of each icon — an outline
     * ({@code -line}) and a solid ({@code -fill}). {@link #LINE} is the
     * default; pass {@link #FILL} to use the filled form.
     */
    public enum Option {
        /**
         * The outline (line) variant — this is the default and does not need
         * to be passed explicitly.
         */
        LINE,

        /**
         * The solid (fill) variant.
         */
        FILL;
    }

    /************************************************************************
     * Support
     ************************************************************************/

    /**
     * Convenience to apply a collection of classes, separated by spaces, to a
     * raw element.
     *
     * @param el
     *                the element to apply to.
     * @param classes
     *                the classes to apply.
     * @return the passed element.
     */
    public static Element apply(Element el, String classes) {
        if ((el != null) && !StringSupport.empty (classes)) {
            for (String css : classes.split (" ")) {
                css = css.trim ();
                if (!StringSupport.empty (css))
                    el.classList.add (css);
            }
        }
        return el;
    }

    /**
     * Formats a Remix Icon CSS class string for the given icon name and
     * options. Used internally by every typed icon method; also available for
     * dynamic cases where the icon name is not known at compile time.
     *
     * @param name
     *                the icon name without the {@code ri-} prefix and without
     *                the {@code -line}/{@code -fill} suffix (e.g. {@code
     *                "home-4"} or {@code "account-box"}).
     * @param options
     *                variant options. Pass {@link Option#FILL} for the solid
     *                variant; otherwise the line variant is used.
     * @return the fully-formed CSS class string (e.g. {@code "ri-home-4-line"}).
     */
    public static String format(String name, Option... options) {
        Set<Option> opts = new HashSet<> ();
        for (Option o : options)
            opts.add (o);
        String suffix = opts.contains (Option.FILL) ? "-fill" : "-line";
        return "ri-" + name + suffix;
    }

    /************************************************************************
     * Icons — generated by {@link RemixIconGenerator}.
     ************************************************************************/

    public static String _24Hours(Option... options) {
        return format("24-hours", options);
    }

    public static String _4k(Option... options) {
        return format("4k", options);
    }

    public static String accessibility(Option... options) {
        return format("accessibility", options);
    }

    public static String accountBox(Option... options) {
        return format("account-box", options);
    }

    public static String accountBox2(Option... options) {
        return format("account-box-2", options);
    }

    public static String accountCircle(Option... options) {
        return format("account-circle", options);
    }

    public static String accountCircle2(Option... options) {
        return format("account-circle-2", options);
    }

    public static String accountPinBox(Option... options) {
        return format("account-pin-box", options);
    }

    public static String accountPinCircle(Option... options) {
        return format("account-pin-circle", options);
    }

    public static String add(Option... options) {
        return format("add", options);
    }

    public static String addBox(Option... options) {
        return format("add-box", options);
    }

    public static String addCircle(Option... options) {
        return format("add-circle", options);
    }

    public static String addLarge(Option... options) {
        return format("add-large", options);
    }

    public static String admin(Option... options) {
        return format("admin", options);
    }

    public static String advertisement(Option... options) {
        return format("advertisement", options);
    }

    public static String aed(Option... options) {
        return format("aed", options);
    }

    public static String aedElectrodes(Option... options) {
        return format("aed-electrodes", options);
    }

    public static String airplay(Option... options) {
        return format("airplay", options);
    }

    public static String alarm(Option... options) {
        return format("alarm", options);
    }

    public static String alarmAdd(Option... options) {
        return format("alarm-add", options);
    }

    public static String alarmSnooze(Option... options) {
        return format("alarm-snooze", options);
    }

    public static String alarmWarning(Option... options) {
        return format("alarm-warning", options);
    }

    public static String album(Option... options) {
        return format("album", options);
    }

    public static String alert(Option... options) {
        return format("alert", options);
    }

    public static String alibabaCloud(Option... options) {
        return format("alibaba-cloud", options);
    }

    public static String aliens(Option... options) {
        return format("aliens", options);
    }

    public static String alignItemBottom(Option... options) {
        return format("align-item-bottom", options);
    }

    public static String alignItemHorizontalCenter(Option... options) {
        return format("align-item-horizontal-center", options);
    }

    public static String alignItemLeft(Option... options) {
        return format("align-item-left", options);
    }

    public static String alignItemRight(Option... options) {
        return format("align-item-right", options);
    }

    public static String alignItemTop(Option... options) {
        return format("align-item-top", options);
    }

    public static String alignItemVerticalCenter(Option... options) {
        return format("align-item-vertical-center", options);
    }

    public static String alipay(Option... options) {
        return format("alipay", options);
    }

    public static String amazon(Option... options) {
        return format("amazon", options);
    }

    public static String anchor(Option... options) {
        return format("anchor", options);
    }

    public static String ancientGate(Option... options) {
        return format("ancient-gate", options);
    }

    public static String ancientPavilion(Option... options) {
        return format("ancient-pavilion", options);
    }

    public static String android(Option... options) {
        return format("android", options);
    }

    public static String angularjs(Option... options) {
        return format("angularjs", options);
    }

    public static String anthropic(Option... options) {
        return format("anthropic", options);
    }

    public static String anticlockwise(Option... options) {
        return format("anticlockwise", options);
    }

    public static String anticlockwise2(Option... options) {
        return format("anticlockwise-2", options);
    }

    public static String appStore(Option... options) {
        return format("app-store", options);
    }

    public static String apple(Option... options) {
        return format("apple", options);
    }

    public static String apps(Option... options) {
        return format("apps", options);
    }

    public static String apps2(Option... options) {
        return format("apps-2", options);
    }

    public static String apps2Add(Option... options) {
        return format("apps-2-add", options);
    }

    public static String apps2Ai(Option... options) {
        return format("apps-2-ai", options);
    }

    public static String archive(Option... options) {
        return format("archive", options);
    }

    public static String archive2(Option... options) {
        return format("archive-2", options);
    }

    public static String archiveDrawer(Option... options) {
        return format("archive-drawer", options);
    }

    public static String archiveStack(Option... options) {
        return format("archive-stack", options);
    }

    public static String armchair(Option... options) {
        return format("armchair", options);
    }

    public static String arrowDown(Option... options) {
        return format("arrow-down", options);
    }

    public static String arrowDownBox(Option... options) {
        return format("arrow-down-box", options);
    }

    public static String arrowDownCircle(Option... options) {
        return format("arrow-down-circle", options);
    }

    public static String arrowDownDouble(Option... options) {
        return format("arrow-down-double", options);
    }

    public static String arrowDownLong(Option... options) {
        return format("arrow-down-long", options);
    }

    public static String arrowDownS(Option... options) {
        return format("arrow-down-s", options);
    }

    public static String arrowDownWide(Option... options) {
        return format("arrow-down-wide", options);
    }

    public static String arrowDropDown(Option... options) {
        return format("arrow-drop-down", options);
    }

    public static String arrowDropLeft(Option... options) {
        return format("arrow-drop-left", options);
    }

    public static String arrowDropRight(Option... options) {
        return format("arrow-drop-right", options);
    }

    public static String arrowDropUp(Option... options) {
        return format("arrow-drop-up", options);
    }

    public static String arrowGoBack(Option... options) {
        return format("arrow-go-back", options);
    }

    public static String arrowGoForward(Option... options) {
        return format("arrow-go-forward", options);
    }

    public static String arrowLeft(Option... options) {
        return format("arrow-left", options);
    }

    public static String arrowLeftBox(Option... options) {
        return format("arrow-left-box", options);
    }

    public static String arrowLeftCircle(Option... options) {
        return format("arrow-left-circle", options);
    }

    public static String arrowLeftDouble(Option... options) {
        return format("arrow-left-double", options);
    }

    public static String arrowLeftDown(Option... options) {
        return format("arrow-left-down", options);
    }

    public static String arrowLeftDownBox(Option... options) {
        return format("arrow-left-down-box", options);
    }

    public static String arrowLeftDownLong(Option... options) {
        return format("arrow-left-down-long", options);
    }

    public static String arrowLeftLong(Option... options) {
        return format("arrow-left-long", options);
    }

    public static String arrowLeftRight(Option... options) {
        return format("arrow-left-right", options);
    }

    public static String arrowLeftS(Option... options) {
        return format("arrow-left-s", options);
    }

    public static String arrowLeftUp(Option... options) {
        return format("arrow-left-up", options);
    }

    public static String arrowLeftUpBox(Option... options) {
        return format("arrow-left-up-box", options);
    }

    public static String arrowLeftUpLong(Option... options) {
        return format("arrow-left-up-long", options);
    }

    public static String arrowLeftWide(Option... options) {
        return format("arrow-left-wide", options);
    }

    public static String arrowRight(Option... options) {
        return format("arrow-right", options);
    }

    public static String arrowRightBox(Option... options) {
        return format("arrow-right-box", options);
    }

    public static String arrowRightCircle(Option... options) {
        return format("arrow-right-circle", options);
    }

    public static String arrowRightDouble(Option... options) {
        return format("arrow-right-double", options);
    }

    public static String arrowRightDown(Option... options) {
        return format("arrow-right-down", options);
    }

    public static String arrowRightDownBox(Option... options) {
        return format("arrow-right-down-box", options);
    }

    public static String arrowRightDownLong(Option... options) {
        return format("arrow-right-down-long", options);
    }

    public static String arrowRightLong(Option... options) {
        return format("arrow-right-long", options);
    }

    public static String arrowRightS(Option... options) {
        return format("arrow-right-s", options);
    }

    public static String arrowRightUp(Option... options) {
        return format("arrow-right-up", options);
    }

    public static String arrowRightUpBox(Option... options) {
        return format("arrow-right-up-box", options);
    }

    public static String arrowRightUpLong(Option... options) {
        return format("arrow-right-up-long", options);
    }

    public static String arrowRightWide(Option... options) {
        return format("arrow-right-wide", options);
    }

    public static String arrowTurnBack(Option... options) {
        return format("arrow-turn-back", options);
    }

    public static String arrowTurnForward(Option... options) {
        return format("arrow-turn-forward", options);
    }

    public static String arrowUp(Option... options) {
        return format("arrow-up", options);
    }

    public static String arrowUpBox(Option... options) {
        return format("arrow-up-box", options);
    }

    public static String arrowUpCircle(Option... options) {
        return format("arrow-up-circle", options);
    }

    public static String arrowUpDouble(Option... options) {
        return format("arrow-up-double", options);
    }

    public static String arrowUpDown(Option... options) {
        return format("arrow-up-down", options);
    }

    public static String arrowUpLong(Option... options) {
        return format("arrow-up-long", options);
    }

    public static String arrowUpS(Option... options) {
        return format("arrow-up-s", options);
    }

    public static String arrowUpWide(Option... options) {
        return format("arrow-up-wide", options);
    }

    public static String artboard(Option... options) {
        return format("artboard", options);
    }

    public static String artboard2(Option... options) {
        return format("artboard-2", options);
    }

    public static String article(Option... options) {
        return format("article", options);
    }

    public static String aspectRatio(Option... options) {
        return format("aspect-ratio", options);
    }

    public static String at(Option... options) {
        return format("at", options);
    }

    public static String attachment(Option... options) {
        return format("attachment", options);
    }

    public static String auction(Option... options) {
        return format("auction", options);
    }

    public static String award(Option... options) {
        return format("award", options);
    }

    public static String baidu(Option... options) {
        return format("baidu", options);
    }

    public static String ballPen(Option... options) {
        return format("ball-pen", options);
    }

    public static String bank(Option... options) {
        return format("bank", options);
    }

    public static String bankCard(Option... options) {
        return format("bank-card", options);
    }

    public static String bankCard2(Option... options) {
        return format("bank-card-2", options);
    }

    public static String barChart(Option... options) {
        return format("bar-chart", options);
    }

    public static String barChart2(Option... options) {
        return format("bar-chart-2", options);
    }

    public static String barChartBox(Option... options) {
        return format("bar-chart-box", options);
    }

    public static String barChartBoxAi(Option... options) {
        return format("bar-chart-box-ai", options);
    }

    public static String barChartGrouped(Option... options) {
        return format("bar-chart-grouped", options);
    }

    public static String barChartHorizontal(Option... options) {
        return format("bar-chart-horizontal", options);
    }

    public static String barcode(Option... options) {
        return format("barcode", options);
    }

    public static String barcodeBox(Option... options) {
        return format("barcode-box", options);
    }

    public static String bard(Option... options) {
        return format("bard", options);
    }

    public static String barricade(Option... options) {
        return format("barricade", options);
    }

    public static String baseStation(Option... options) {
        return format("base-station", options);
    }

    public static String basketball(Option... options) {
        return format("basketball", options);
    }

    public static String battery(Option... options) {
        return format("battery", options);
    }

    public static String battery2(Option... options) {
        return format("battery-2", options);
    }

    public static String battery2Charge(Option... options) {
        return format("battery-2-charge", options);
    }

    public static String batteryCharge(Option... options) {
        return format("battery-charge", options);
    }

    public static String batteryLow(Option... options) {
        return format("battery-low", options);
    }

    public static String batterySaver(Option... options) {
        return format("battery-saver", options);
    }

    public static String batteryShare(Option... options) {
        return format("battery-share", options);
    }

    public static String bearSmile(Option... options) {
        return format("bear-smile", options);
    }

    public static String beer(Option... options) {
        return format("beer", options);
    }

    public static String behance(Option... options) {
        return format("behance", options);
    }

    public static String bell(Option... options) {
        return format("bell", options);
    }

    public static String bike(Option... options) {
        return format("bike", options);
    }

    public static String bilibili(Option... options) {
        return format("bilibili", options);
    }

    public static String bill(Option... options) {
        return format("bill", options);
    }

    public static String billiards(Option... options) {
        return format("billiards", options);
    }

    public static String bitCoin(Option... options) {
        return format("bit-coin", options);
    }

    public static String blaze(Option... options) {
        return format("blaze", options);
    }

    public static String blender(Option... options) {
        return format("blender", options);
    }

    public static String blogger(Option... options) {
        return format("blogger", options);
    }

    public static String bluesky(Option... options) {
        return format("bluesky", options);
    }

    public static String bluetooth(Option... options) {
        return format("bluetooth", options);
    }

    public static String bluetoothConnect(Option... options) {
        return format("bluetooth-connect", options);
    }

    public static String blurOff(Option... options) {
        return format("blur-off", options);
    }

    public static String bnb(Option... options) {
        return format("bnb", options);
    }

    public static String bodyScan(Option... options) {
        return format("body-scan", options);
    }

    public static String book(Option... options) {
        return format("book", options);
    }

    public static String book2(Option... options) {
        return format("book-2", options);
    }

    public static String book3(Option... options) {
        return format("book-3", options);
    }

    public static String bookMarked(Option... options) {
        return format("book-marked", options);
    }

    public static String bookOpen(Option... options) {
        return format("book-open", options);
    }

    public static String bookRead(Option... options) {
        return format("book-read", options);
    }

    public static String bookShelf(Option... options) {
        return format("book-shelf", options);
    }

    public static String booklet(Option... options) {
        return format("booklet", options);
    }

    public static String bookmark(Option... options) {
        return format("bookmark", options);
    }

    public static String bookmark2(Option... options) {
        return format("bookmark-2", options);
    }

    public static String bookmark3(Option... options) {
        return format("bookmark-3", options);
    }

    public static String bootstrap(Option... options) {
        return format("bootstrap", options);
    }

    public static String bowl(Option... options) {
        return format("bowl", options);
    }

    public static String box1(Option... options) {
        return format("box-1", options);
    }

    public static String box2(Option... options) {
        return format("box-2", options);
    }

    public static String box3(Option... options) {
        return format("box-3", options);
    }

    public static String boxing(Option... options) {
        return format("boxing", options);
    }

    public static String braces(Option... options) {
        return format("braces", options);
    }

    public static String brackets(Option... options) {
        return format("brackets", options);
    }

    public static String brain(Option... options) {
        return format("brain", options);
    }

    public static String brain2(Option... options) {
        return format("brain-2", options);
    }

    public static String bread(Option... options) {
        return format("bread", options);
    }

    public static String briefcase(Option... options) {
        return format("briefcase", options);
    }

    public static String briefcase2(Option... options) {
        return format("briefcase-2", options);
    }

    public static String briefcase3(Option... options) {
        return format("briefcase-3", options);
    }

    public static String briefcase4(Option... options) {
        return format("briefcase-4", options);
    }

    public static String briefcase5(Option... options) {
        return format("briefcase-5", options);
    }

    public static String broadcast(Option... options) {
        return format("broadcast", options);
    }

    public static String brush(Option... options) {
        return format("brush", options);
    }

    public static String brush2(Option... options) {
        return format("brush-2", options);
    }

    public static String brush3(Option... options) {
        return format("brush-3", options);
    }

    public static String brush4(Option... options) {
        return format("brush-4", options);
    }

    public static String brushAi(Option... options) {
        return format("brush-ai", options);
    }

    public static String btc(Option... options) {
        return format("btc", options);
    }

    public static String bubbleChart(Option... options) {
        return format("bubble-chart", options);
    }

    public static String bug(Option... options) {
        return format("bug", options);
    }

    public static String bug2(Option... options) {
        return format("bug-2", options);
    }

    public static String building(Option... options) {
        return format("building", options);
    }

    public static String building2(Option... options) {
        return format("building-2", options);
    }

    public static String building3(Option... options) {
        return format("building-3", options);
    }

    public static String building4(Option... options) {
        return format("building-4", options);
    }

    public static String bus(Option... options) {
        return format("bus", options);
    }

    public static String bus2(Option... options) {
        return format("bus-2", options);
    }

    public static String busWifi(Option... options) {
        return format("bus-wifi", options);
    }

    public static String cactus(Option... options) {
        return format("cactus", options);
    }

    public static String cake(Option... options) {
        return format("cake", options);
    }

    public static String cake2(Option... options) {
        return format("cake-2", options);
    }

    public static String cake3(Option... options) {
        return format("cake-3", options);
    }

    public static String calculator(Option... options) {
        return format("calculator", options);
    }

    public static String calendar(Option... options) {
        return format("calendar", options);
    }

    public static String calendar2(Option... options) {
        return format("calendar-2", options);
    }

    public static String calendarCheck(Option... options) {
        return format("calendar-check", options);
    }

    public static String calendarClose(Option... options) {
        return format("calendar-close", options);
    }

    public static String calendarEvent(Option... options) {
        return format("calendar-event", options);
    }

    public static String calendarSchedule(Option... options) {
        return format("calendar-schedule", options);
    }

    public static String calendarTodo(Option... options) {
        return format("calendar-todo", options);
    }

    public static String camera(Option... options) {
        return format("camera", options);
    }

    public static String camera2(Option... options) {
        return format("camera-2", options);
    }

    public static String camera3(Option... options) {
        return format("camera-3", options);
    }

    public static String cameraAi(Option... options) {
        return format("camera-ai", options);
    }

    public static String cameraLens(Option... options) {
        return format("camera-lens", options);
    }

    public static String cameraLensAi(Option... options) {
        return format("camera-lens-ai", options);
    }

    public static String cameraOff(Option... options) {
        return format("camera-off", options);
    }

    public static String cameraSwitch(Option... options) {
        return format("camera-switch", options);
    }

    public static String candle(Option... options) {
        return format("candle", options);
    }

    public static String capsule(Option... options) {
        return format("capsule", options);
    }

    public static String car(Option... options) {
        return format("car", options);
    }

    public static String carWashing(Option... options) {
        return format("car-washing", options);
    }

    public static String caravan(Option... options) {
        return format("caravan", options);
    }

    public static String cash(Option... options) {
        return format("cash", options);
    }

    public static String cast(Option... options) {
        return format("cast", options);
    }

    public static String cellphone(Option... options) {
        return format("cellphone", options);
    }

    public static String celsius(Option... options) {
        return format("celsius", options);
    }

    public static String centos(Option... options) {
        return format("centos", options);
    }

    public static String characterRecognition(Option... options) {
        return format("character-recognition", options);
    }

    public static String chargingPile(Option... options) {
        return format("charging-pile", options);
    }

    public static String chargingPile2(Option... options) {
        return format("charging-pile-2", options);
    }

    public static String chat1(Option... options) {
        return format("chat-1", options);
    }

    public static String chat2(Option... options) {
        return format("chat-2", options);
    }

    public static String chat3(Option... options) {
        return format("chat-3", options);
    }

    public static String chat4(Option... options) {
        return format("chat-4", options);
    }

    public static String chatAi(Option... options) {
        return format("chat-ai", options);
    }

    public static String chatCheck(Option... options) {
        return format("chat-check", options);
    }

    public static String chatDelete(Option... options) {
        return format("chat-delete", options);
    }

    public static String chatDownload(Option... options) {
        return format("chat-download", options);
    }

    public static String chatFollowUp(Option... options) {
        return format("chat-follow-up", options);
    }

    public static String chatForward(Option... options) {
        return format("chat-forward", options);
    }

    public static String chatHeart(Option... options) {
        return format("chat-heart", options);
    }

    public static String chatHistory(Option... options) {
        return format("chat-history", options);
    }

    public static String chatNew(Option... options) {
        return format("chat-new", options);
    }

    public static String chatOff(Option... options) {
        return format("chat-off", options);
    }

    public static String chatPoll(Option... options) {
        return format("chat-poll", options);
    }

    public static String chatPrivate(Option... options) {
        return format("chat-private", options);
    }

    public static String chatQuote(Option... options) {
        return format("chat-quote", options);
    }

    public static String chatSearch(Option... options) {
        return format("chat-search", options);
    }

    public static String chatSettings(Option... options) {
        return format("chat-settings", options);
    }

    public static String chatSmile(Option... options) {
        return format("chat-smile", options);
    }

    public static String chatSmile2(Option... options) {
        return format("chat-smile-2", options);
    }

    public static String chatSmile3(Option... options) {
        return format("chat-smile-3", options);
    }

    public static String chatSmileAi(Option... options) {
        return format("chat-smile-ai", options);
    }

    public static String chatThread(Option... options) {
        return format("chat-thread", options);
    }

    public static String chatUnread(Option... options) {
        return format("chat-unread", options);
    }

    public static String chatUpload(Option... options) {
        return format("chat-upload", options);
    }

    public static String chatVoice(Option... options) {
        return format("chat-voice", options);
    }

    public static String chatVoiceAi(Option... options) {
        return format("chat-voice-ai", options);
    }

    public static String check(Option... options) {
        return format("check", options);
    }

    public static String checkDouble(Option... options) {
        return format("check-double", options);
    }

    public static String checkbox(Option... options) {
        return format("checkbox", options);
    }

    public static String checkboxBlank(Option... options) {
        return format("checkbox-blank", options);
    }

    public static String checkboxBlankCircle(Option... options) {
        return format("checkbox-blank-circle", options);
    }

    public static String checkboxCircle(Option... options) {
        return format("checkbox-circle", options);
    }

    public static String checkboxIndeterminate(Option... options) {
        return format("checkbox-indeterminate", options);
    }

    public static String checkboxMultiple(Option... options) {
        return format("checkbox-multiple", options);
    }

    public static String checkboxMultipleBlank(Option... options) {
        return format("checkbox-multiple-blank", options);
    }

    public static String chess(Option... options) {
        return format("chess", options);
    }

    public static String chinaRailway(Option... options) {
        return format("china-railway", options);
    }

    public static String chrome(Option... options) {
        return format("chrome", options);
    }

    public static String circle(Option... options) {
        return format("circle", options);
    }

    public static String clapperboard(Option... options) {
        return format("clapperboard", options);
    }

    public static String clapperboardAi(Option... options) {
        return format("clapperboard-ai", options);
    }

    public static String claude(Option... options) {
        return format("claude", options);
    }

    public static String clipboard(Option... options) {
        return format("clipboard", options);
    }

    public static String clockwise(Option... options) {
        return format("clockwise", options);
    }

    public static String clockwise2(Option... options) {
        return format("clockwise-2", options);
    }

    public static String close(Option... options) {
        return format("close", options);
    }

    public static String closeCircle(Option... options) {
        return format("close-circle", options);
    }

    public static String closeLarge(Option... options) {
        return format("close-large", options);
    }

    public static String closedCaptioning(Option... options) {
        return format("closed-captioning", options);
    }

    public static String closedCaptioningAi(Option... options) {
        return format("closed-captioning-ai", options);
    }

    public static String cloud(Option... options) {
        return format("cloud", options);
    }

    public static String cloudOff(Option... options) {
        return format("cloud-off", options);
    }

    public static String cloudWindy(Option... options) {
        return format("cloud-windy", options);
    }

    public static String cloudy(Option... options) {
        return format("cloudy", options);
    }

    public static String cloudy2(Option... options) {
        return format("cloudy-2", options);
    }

    public static String code(Option... options) {
        return format("code", options);
    }

    public static String codeAi(Option... options) {
        return format("code-ai", options);
    }

    public static String codeBox(Option... options) {
        return format("code-box", options);
    }

    public static String codeS(Option... options) {
        return format("code-s", options);
    }

    public static String codeSSlash(Option... options) {
        return format("code-s-slash", options);
    }

    public static String codepen(Option... options) {
        return format("codepen", options);
    }

    public static String coin(Option... options) {
        return format("coin", options);
    }

    public static String coins(Option... options) {
        return format("coins", options);
    }

    public static String collage(Option... options) {
        return format("collage", options);
    }

    public static String collapseDiagonal(Option... options) {
        return format("collapse-diagonal", options);
    }

    public static String collapseDiagonal2(Option... options) {
        return format("collapse-diagonal-2", options);
    }

    public static String collapseHorizontal(Option... options) {
        return format("collapse-horizontal", options);
    }

    public static String collapseVertical(Option... options) {
        return format("collapse-vertical", options);
    }

    public static String colorFilter(Option... options) {
        return format("color-filter", options);
    }

    public static String colorFilterAi(Option... options) {
        return format("color-filter-ai", options);
    }

    public static String command(Option... options) {
        return format("command", options);
    }

    public static String community(Option... options) {
        return format("community", options);
    }

    public static String compass(Option... options) {
        return format("compass", options);
    }

    public static String compass2(Option... options) {
        return format("compass-2", options);
    }

    public static String compass3(Option... options) {
        return format("compass-3", options);
    }

    public static String compass4(Option... options) {
        return format("compass-4", options);
    }

    public static String compassDiscover(Option... options) {
        return format("compass-discover", options);
    }

    public static String compasses(Option... options) {
        return format("compasses", options);
    }

    public static String compasses2(Option... options) {
        return format("compasses-2", options);
    }

    public static String computer(Option... options) {
        return format("computer", options);
    }

    public static String contacts(Option... options) {
        return format("contacts", options);
    }

    public static String contactsBook(Option... options) {
        return format("contacts-book", options);
    }

    public static String contactsBook2(Option... options) {
        return format("contacts-book-2", options);
    }

    public static String contactsBook3(Option... options) {
        return format("contacts-book-3", options);
    }

    public static String contactsBookUpload(Option... options) {
        return format("contacts-book-upload", options);
    }

    public static String contract(Option... options) {
        return format("contract", options);
    }

    public static String contractLeft(Option... options) {
        return format("contract-left", options);
    }

    public static String contractLeftRight(Option... options) {
        return format("contract-left-right", options);
    }

    public static String contractRight(Option... options) {
        return format("contract-right", options);
    }

    public static String contractUpDown(Option... options) {
        return format("contract-up-down", options);
    }

    public static String contrast(Option... options) {
        return format("contrast", options);
    }

    public static String contrast2(Option... options) {
        return format("contrast-2", options);
    }

    public static String contrastDrop(Option... options) {
        return format("contrast-drop", options);
    }

    public static String contrastDrop2(Option... options) {
        return format("contrast-drop-2", options);
    }

    public static String copilot(Option... options) {
        return format("copilot", options);
    }

    public static String copperCoin(Option... options) {
        return format("copper-coin", options);
    }

    public static String copperDiamond(Option... options) {
        return format("copper-diamond", options);
    }

    public static String copyleft(Option... options) {
        return format("copyleft", options);
    }

    public static String copyright(Option... options) {
        return format("copyright", options);
    }

    public static String coreos(Option... options) {
        return format("coreos", options);
    }

    public static String cornerDownLeft(Option... options) {
        return format("corner-down-left", options);
    }

    public static String cornerDownRight(Option... options) {
        return format("corner-down-right", options);
    }

    public static String cornerLeftDown(Option... options) {
        return format("corner-left-down", options);
    }

    public static String cornerLeftUp(Option... options) {
        return format("corner-left-up", options);
    }

    public static String cornerRightDown(Option... options) {
        return format("corner-right-down", options);
    }

    public static String cornerRightUp(Option... options) {
        return format("corner-right-up", options);
    }

    public static String cornerUpLeft(Option... options) {
        return format("corner-up-left", options);
    }

    public static String cornerUpLeftDouble(Option... options) {
        return format("corner-up-left-double", options);
    }

    public static String cornerUpRight(Option... options) {
        return format("corner-up-right", options);
    }

    public static String cornerUpRightDouble(Option... options) {
        return format("corner-up-right-double", options);
    }

    public static String coupon(Option... options) {
        return format("coupon", options);
    }

    public static String coupon2(Option... options) {
        return format("coupon-2", options);
    }

    public static String coupon3(Option... options) {
        return format("coupon-3", options);
    }

    public static String coupon4(Option... options) {
        return format("coupon-4", options);
    }

    public static String coupon5(Option... options) {
        return format("coupon-5", options);
    }

    public static String cpu(Option... options) {
        return format("cpu", options);
    }

    public static String creativeCommons(Option... options) {
        return format("creative-commons", options);
    }

    public static String creativeCommonsBy(Option... options) {
        return format("creative-commons-by", options);
    }

    public static String creativeCommonsNc(Option... options) {
        return format("creative-commons-nc", options);
    }

    public static String creativeCommonsNd(Option... options) {
        return format("creative-commons-nd", options);
    }

    public static String creativeCommonsSa(Option... options) {
        return format("creative-commons-sa", options);
    }

    public static String creativeCommonsZero(Option... options) {
        return format("creative-commons-zero", options);
    }

    public static String criminal(Option... options) {
        return format("criminal", options);
    }

    public static String crop(Option... options) {
        return format("crop", options);
    }

    public static String crop2(Option... options) {
        return format("crop-2", options);
    }

    public static String cross(Option... options) {
        return format("cross", options);
    }

    public static String crosshair(Option... options) {
        return format("crosshair", options);
    }

    public static String crosshair2(Option... options) {
        return format("crosshair-2", options);
    }

    public static String css3(Option... options) {
        return format("css3", options);
    }

    public static String cup(Option... options) {
        return format("cup", options);
    }

    public static String currency(Option... options) {
        return format("currency", options);
    }

    public static String cursor(Option... options) {
        return format("cursor", options);
    }

    public static String customerService(Option... options) {
        return format("customer-service", options);
    }

    public static String customerService2(Option... options) {
        return format("customer-service-2", options);
    }

    public static String dashboard(Option... options) {
        return format("dashboard", options);
    }

    public static String dashboard2(Option... options) {
        return format("dashboard-2", options);
    }

    public static String dashboard3(Option... options) {
        return format("dashboard-3", options);
    }

    public static String dashboardHorizontal(Option... options) {
        return format("dashboard-horizontal", options);
    }

    public static String database(Option... options) {
        return format("database", options);
    }

    public static String database2(Option... options) {
        return format("database-2", options);
    }

    public static String deleteBack(Option... options) {
        return format("delete-back", options);
    }

    public static String deleteBack2(Option... options) {
        return format("delete-back-2", options);
    }

    public static String deleteBin(Option... options) {
        return format("delete-bin", options);
    }

    public static String deleteBin2(Option... options) {
        return format("delete-bin-2", options);
    }

    public static String deleteBin3(Option... options) {
        return format("delete-bin-3", options);
    }

    public static String deleteBin4(Option... options) {
        return format("delete-bin-4", options);
    }

    public static String deleteBin5(Option... options) {
        return format("delete-bin-5", options);
    }

    public static String deleteBin6(Option... options) {
        return format("delete-bin-6", options);
    }

    public static String deleteBin7(Option... options) {
        return format("delete-bin-7", options);
    }

    public static String device(Option... options) {
        return format("device", options);
    }

    public static String deviceRecover(Option... options) {
        return format("device-recover", options);
    }

    public static String diamond(Option... options) {
        return format("diamond", options);
    }

    public static String diamondRing(Option... options) {
        return format("diamond-ring", options);
    }

    public static String dice(Option... options) {
        return format("dice", options);
    }

    public static String dice1(Option... options) {
        return format("dice-1", options);
    }

    public static String dice2(Option... options) {
        return format("dice-2", options);
    }

    public static String dice3(Option... options) {
        return format("dice-3", options);
    }

    public static String dice4(Option... options) {
        return format("dice-4", options);
    }

    public static String dice5(Option... options) {
        return format("dice-5", options);
    }

    public static String dice6(Option... options) {
        return format("dice-6", options);
    }

    public static String dingding(Option... options) {
        return format("dingding", options);
    }

    public static String direction(Option... options) {
        return format("direction", options);
    }

    public static String disc(Option... options) {
        return format("disc", options);
    }

    public static String discord(Option... options) {
        return format("discord", options);
    }

    public static String discountPercent(Option... options) {
        return format("discount-percent", options);
    }

    public static String discuss(Option... options) {
        return format("discuss", options);
    }

    public static String dislike(Option... options) {
        return format("dislike", options);
    }

    public static String disqus(Option... options) {
        return format("disqus", options);
    }

    public static String divide(Option... options) {
        return format("divide", options);
    }

    public static String dna(Option... options) {
        return format("dna", options);
    }

    public static String donutChart(Option... options) {
        return format("donut-chart", options);
    }

    public static String door(Option... options) {
        return format("door", options);
    }

    public static String doorClosed(Option... options) {
        return format("door-closed", options);
    }

    public static String doorLock(Option... options) {
        return format("door-lock", options);
    }

    public static String doorLockBox(Option... options) {
        return format("door-lock-box", options);
    }

    public static String doorOpen(Option... options) {
        return format("door-open", options);
    }

    public static String dossier(Option... options) {
        return format("dossier", options);
    }

    public static String douban(Option... options) {
        return format("douban", options);
    }

    public static String download(Option... options) {
        return format("download", options);
    }

    public static String download2(Option... options) {
        return format("download-2", options);
    }

    public static String downloadCloud(Option... options) {
        return format("download-cloud", options);
    }

    public static String downloadCloud2(Option... options) {
        return format("download-cloud-2", options);
    }

    public static String draft(Option... options) {
        return format("draft", options);
    }

    public static String dragDrop(Option... options) {
        return format("drag-drop", options);
    }

    public static String dragMove(Option... options) {
        return format("drag-move", options);
    }

    public static String dragMove2(Option... options) {
        return format("drag-move-2", options);
    }

    public static String dribbble(Option... options) {
        return format("dribbble", options);
    }

    public static String drinks(Option... options) {
        return format("drinks", options);
    }

    public static String drinks2(Option... options) {
        return format("drinks-2", options);
    }

    public static String drive(Option... options) {
        return format("drive", options);
    }

    public static String drizzle(Option... options) {
        return format("drizzle", options);
    }

    public static String drop(Option... options) {
        return format("drop", options);
    }

    public static String dropbox(Option... options) {
        return format("dropbox", options);
    }

    public static String dropper(Option... options) {
        return format("dropper", options);
    }

    public static String dualSim1(Option... options) {
        return format("dual-sim-1", options);
    }

    public static String dualSim2(Option... options) {
        return format("dual-sim-2", options);
    }

    public static String dv(Option... options) {
        return format("dv", options);
    }

    public static String dvd(Option... options) {
        return format("dvd", options);
    }

    public static String dvdAi(Option... options) {
        return format("dvd-ai", options);
    }

    public static String eBike(Option... options) {
        return format("e-bike", options);
    }

    public static String eBike2(Option... options) {
        return format("e-bike-2", options);
    }

    public static String earth(Option... options) {
        return format("earth", options);
    }

    public static String earthquake(Option... options) {
        return format("earthquake", options);
    }

    public static String edge(Option... options) {
        return format("edge", options);
    }

    public static String edgeNew(Option... options) {
        return format("edge-new", options);
    }

    public static String edit(Option... options) {
        return format("edit", options);
    }

    public static String edit2(Option... options) {
        return format("edit-2", options);
    }

    public static String editBox(Option... options) {
        return format("edit-box", options);
    }

    public static String editCircle(Option... options) {
        return format("edit-circle", options);
    }

    public static String eject(Option... options) {
        return format("eject", options);
    }

    public static String emojiSticker(Option... options) {
        return format("emoji-sticker", options);
    }

    public static String emotion(Option... options) {
        return format("emotion", options);
    }

    public static String emotion2(Option... options) {
        return format("emotion-2", options);
    }

    public static String emotionHappy(Option... options) {
        return format("emotion-happy", options);
    }

    public static String emotionLaugh(Option... options) {
        return format("emotion-laugh", options);
    }

    public static String emotionNormal(Option... options) {
        return format("emotion-normal", options);
    }

    public static String emotionSad(Option... options) {
        return format("emotion-sad", options);
    }

    public static String emotionUnhappy(Option... options) {
        return format("emotion-unhappy", options);
    }

    public static String empathize(Option... options) {
        return format("empathize", options);
    }

    public static String equal(Option... options) {
        return format("equal", options);
    }

    public static String equalizer(Option... options) {
        return format("equalizer", options);
    }

    public static String equalizer2(Option... options) {
        return format("equalizer-2", options);
    }

    public static String equalizer3(Option... options) {
        return format("equalizer-3", options);
    }

    public static String eraser(Option... options) {
        return format("eraser", options);
    }

    public static String errorWarning(Option... options) {
        return format("error-warning", options);
    }

    public static String eth(Option... options) {
        return format("eth", options);
    }

    public static String evernote(Option... options) {
        return format("evernote", options);
    }

    public static String exchange(Option... options) {
        return format("exchange", options);
    }

    public static String exchange2(Option... options) {
        return format("exchange-2", options);
    }

    public static String exchangeBox(Option... options) {
        return format("exchange-box", options);
    }

    public static String exchangeCny(Option... options) {
        return format("exchange-cny", options);
    }

    public static String exchangeDollar(Option... options) {
        return format("exchange-dollar", options);
    }

    public static String exchangeFunds(Option... options) {
        return format("exchange-funds", options);
    }

    public static String expandDiagonal(Option... options) {
        return format("expand-diagonal", options);
    }

    public static String expandDiagonal2(Option... options) {
        return format("expand-diagonal-2", options);
    }

    public static String expandDiagonalS(Option... options) {
        return format("expand-diagonal-s", options);
    }

    public static String expandDiagonalS2(Option... options) {
        return format("expand-diagonal-s-2", options);
    }

    public static String expandHeight(Option... options) {
        return format("expand-height", options);
    }

    public static String expandHorizontal(Option... options) {
        return format("expand-horizontal", options);
    }

    public static String expandHorizontalS(Option... options) {
        return format("expand-horizontal-s", options);
    }

    public static String expandLeft(Option... options) {
        return format("expand-left", options);
    }

    public static String expandLeftRight(Option... options) {
        return format("expand-left-right", options);
    }

    public static String expandRight(Option... options) {
        return format("expand-right", options);
    }

    public static String expandUpDown(Option... options) {
        return format("expand-up-down", options);
    }

    public static String expandVertical(Option... options) {
        return format("expand-vertical", options);
    }

    public static String expandVerticalS(Option... options) {
        return format("expand-vertical-s", options);
    }

    public static String expandWidth(Option... options) {
        return format("expand-width", options);
    }

    public static String export(Option... options) {
        return format("export", options);
    }

    public static String externalLink(Option... options) {
        return format("external-link", options);
    }

    public static String eye(Option... options) {
        return format("eye", options);
    }

    public static String eye2(Option... options) {
        return format("eye-2", options);
    }

    public static String eyeClose(Option... options) {
        return format("eye-close", options);
    }

    public static String eyeOff(Option... options) {
        return format("eye-off", options);
    }

    public static String facebook(Option... options) {
        return format("facebook", options);
    }

    public static String facebookBox(Option... options) {
        return format("facebook-box", options);
    }

    public static String facebookCircle(Option... options) {
        return format("facebook-circle", options);
    }

    public static String fahrenheit(Option... options) {
        return format("fahrenheit", options);
    }

    public static String fediverse(Option... options) {
        return format("fediverse", options);
    }

    public static String feedback(Option... options) {
        return format("feedback", options);
    }

    public static String figma(Option... options) {
        return format("figma", options);
    }

    public static String file(Option... options) {
        return format("file", options);
    }

    public static String file2(Option... options) {
        return format("file-2", options);
    }

    public static String file3(Option... options) {
        return format("file-3", options);
    }

    public static String file4(Option... options) {
        return format("file-4", options);
    }

    public static String fileAdd(Option... options) {
        return format("file-add", options);
    }

    public static String fileChart(Option... options) {
        return format("file-chart", options);
    }

    public static String fileChart2(Option... options) {
        return format("file-chart-2", options);
    }

    public static String fileCheck(Option... options) {
        return format("file-check", options);
    }

    public static String fileClose(Option... options) {
        return format("file-close", options);
    }

    public static String fileCloud(Option... options) {
        return format("file-cloud", options);
    }

    public static String fileCode(Option... options) {
        return format("file-code", options);
    }

    public static String fileCopy(Option... options) {
        return format("file-copy", options);
    }

    public static String fileCopy2(Option... options) {
        return format("file-copy-2", options);
    }

    public static String fileDamage(Option... options) {
        return format("file-damage", options);
    }

    public static String fileDownload(Option... options) {
        return format("file-download", options);
    }

    public static String fileEdit(Option... options) {
        return format("file-edit", options);
    }

    public static String fileExcel(Option... options) {
        return format("file-excel", options);
    }

    public static String fileExcel2(Option... options) {
        return format("file-excel-2", options);
    }

    public static String fileForbid(Option... options) {
        return format("file-forbid", options);
    }

    public static String fileGif(Option... options) {
        return format("file-gif", options);
    }

    public static String fileHistory(Option... options) {
        return format("file-history", options);
    }

    public static String fileHwp(Option... options) {
        return format("file-hwp", options);
    }

    public static String fileImage(Option... options) {
        return format("file-image", options);
    }

    public static String fileInfo(Option... options) {
        return format("file-info", options);
    }

    public static String fileList(Option... options) {
        return format("file-list", options);
    }

    public static String fileList2(Option... options) {
        return format("file-list-2", options);
    }

    public static String fileList3(Option... options) {
        return format("file-list-3", options);
    }

    public static String fileLock(Option... options) {
        return format("file-lock", options);
    }

    public static String fileMarked(Option... options) {
        return format("file-marked", options);
    }

    public static String fileMusic(Option... options) {
        return format("file-music", options);
    }

    public static String filePaper(Option... options) {
        return format("file-paper", options);
    }

    public static String filePaper2(Option... options) {
        return format("file-paper-2", options);
    }

    public static String filePdf(Option... options) {
        return format("file-pdf", options);
    }

    public static String filePdf2(Option... options) {
        return format("file-pdf-2", options);
    }

    public static String filePpt(Option... options) {
        return format("file-ppt", options);
    }

    public static String filePpt2(Option... options) {
        return format("file-ppt-2", options);
    }

    public static String fileReduce(Option... options) {
        return format("file-reduce", options);
    }

    public static String fileSearch(Option... options) {
        return format("file-search", options);
    }

    public static String fileSettings(Option... options) {
        return format("file-settings", options);
    }

    public static String fileShield(Option... options) {
        return format("file-shield", options);
    }

    public static String fileShield2(Option... options) {
        return format("file-shield-2", options);
    }

    public static String fileShred(Option... options) {
        return format("file-shred", options);
    }

    public static String fileText(Option... options) {
        return format("file-text", options);
    }

    public static String fileTransfer(Option... options) {
        return format("file-transfer", options);
    }

    public static String fileUnknow(Option... options) {
        return format("file-unknow", options);
    }

    public static String fileUpload(Option... options) {
        return format("file-upload", options);
    }

    public static String fileUser(Option... options) {
        return format("file-user", options);
    }

    public static String fileVideo(Option... options) {
        return format("file-video", options);
    }

    public static String fileWarning(Option... options) {
        return format("file-warning", options);
    }

    public static String fileWord(Option... options) {
        return format("file-word", options);
    }

    public static String fileWord2(Option... options) {
        return format("file-word-2", options);
    }

    public static String fileZip(Option... options) {
        return format("file-zip", options);
    }

    public static String film(Option... options) {
        return format("film", options);
    }

    public static String filmAi(Option... options) {
        return format("film-ai", options);
    }

    public static String filter(Option... options) {
        return format("filter", options);
    }

    public static String filter2(Option... options) {
        return format("filter-2", options);
    }

    public static String filter3(Option... options) {
        return format("filter-3", options);
    }

    public static String filterOff(Option... options) {
        return format("filter-off", options);
    }

    public static String findReplace(Option... options) {
        return format("find-replace", options);
    }

    public static String finder(Option... options) {
        return format("finder", options);
    }

    public static String fingerprint(Option... options) {
        return format("fingerprint", options);
    }

    public static String fingerprint2(Option... options) {
        return format("fingerprint-2", options);
    }

    public static String fire(Option... options) {
        return format("fire", options);
    }

    public static String firebase(Option... options) {
        return format("firebase", options);
    }

    public static String firefox(Option... options) {
        return format("firefox", options);
    }

    public static String firefoxBrowser(Option... options) {
        return format("firefox-browser", options);
    }

    public static String firstAidKit(Option... options) {
        return format("first-aid-kit", options);
    }

    public static String flag(Option... options) {
        return format("flag", options);
    }

    public static String flag2(Option... options) {
        return format("flag-2", options);
    }

    public static String flagOff(Option... options) {
        return format("flag-off", options);
    }

    public static String flashlight(Option... options) {
        return format("flashlight", options);
    }

    public static String flask(Option... options) {
        return format("flask", options);
    }

    public static String flickr(Option... options) {
        return format("flickr", options);
    }

    public static String flightLand(Option... options) {
        return format("flight-land", options);
    }

    public static String flightTakeoff(Option... options) {
        return format("flight-takeoff", options);
    }

    public static String flipHorizontal(Option... options) {
        return format("flip-horizontal", options);
    }

    public static String flipHorizontal2(Option... options) {
        return format("flip-horizontal-2", options);
    }

    public static String flipVertical(Option... options) {
        return format("flip-vertical", options);
    }

    public static String flipVertical2(Option... options) {
        return format("flip-vertical-2", options);
    }

    public static String flood(Option... options) {
        return format("flood", options);
    }

    public static String flower(Option... options) {
        return format("flower", options);
    }

    public static String flutter(Option... options) {
        return format("flutter", options);
    }

    public static String focus(Option... options) {
        return format("focus", options);
    }

    public static String focus2(Option... options) {
        return format("focus-2", options);
    }

    public static String focus3(Option... options) {
        return format("focus-3", options);
    }

    public static String foggy(Option... options) {
        return format("foggy", options);
    }

    public static String folder(Option... options) {
        return format("folder", options);
    }

    public static String folder2(Option... options) {
        return format("folder-2", options);
    }

    public static String folder3(Option... options) {
        return format("folder-3", options);
    }

    public static String folder4(Option... options) {
        return format("folder-4", options);
    }

    public static String folder5(Option... options) {
        return format("folder-5", options);
    }

    public static String folder6(Option... options) {
        return format("folder-6", options);
    }

    public static String folderAdd(Option... options) {
        return format("folder-add", options);
    }

    public static String folderChart(Option... options) {
        return format("folder-chart", options);
    }

    public static String folderChart2(Option... options) {
        return format("folder-chart-2", options);
    }

    public static String folderCheck(Option... options) {
        return format("folder-check", options);
    }

    public static String folderClose(Option... options) {
        return format("folder-close", options);
    }

    public static String folderCloud(Option... options) {
        return format("folder-cloud", options);
    }

    public static String folderDownload(Option... options) {
        return format("folder-download", options);
    }

    public static String folderForbid(Option... options) {
        return format("folder-forbid", options);
    }

    public static String folderHistory(Option... options) {
        return format("folder-history", options);
    }

    public static String folderImage(Option... options) {
        return format("folder-image", options);
    }

    public static String folderInfo(Option... options) {
        return format("folder-info", options);
    }

    public static String folderKeyhole(Option... options) {
        return format("folder-keyhole", options);
    }

    public static String folderLock(Option... options) {
        return format("folder-lock", options);
    }

    public static String folderMusic(Option... options) {
        return format("folder-music", options);
    }

    public static String folderOpen(Option... options) {
        return format("folder-open", options);
    }

    public static String folderReceived(Option... options) {
        return format("folder-received", options);
    }

    public static String folderReduce(Option... options) {
        return format("folder-reduce", options);
    }

    public static String folderSettings(Option... options) {
        return format("folder-settings", options);
    }

    public static String folderShared(Option... options) {
        return format("folder-shared", options);
    }

    public static String folderShield(Option... options) {
        return format("folder-shield", options);
    }

    public static String folderShield2(Option... options) {
        return format("folder-shield-2", options);
    }

    public static String folderTransfer(Option... options) {
        return format("folder-transfer", options);
    }

    public static String folderUnknow(Option... options) {
        return format("folder-unknow", options);
    }

    public static String folderUpload(Option... options) {
        return format("folder-upload", options);
    }

    public static String folderUser(Option... options) {
        return format("folder-user", options);
    }

    public static String folderVideo(Option... options) {
        return format("folder-video", options);
    }

    public static String folderWarning(Option... options) {
        return format("folder-warning", options);
    }

    public static String folderZip(Option... options) {
        return format("folder-zip", options);
    }

    public static String folders(Option... options) {
        return format("folders", options);
    }

    public static String football(Option... options) {
        return format("football", options);
    }

    public static String footprint(Option... options) {
        return format("footprint", options);
    }

    public static String forbid(Option... options) {
        return format("forbid", options);
    }

    public static String forbid2(Option... options) {
        return format("forbid-2", options);
    }

    public static String forward10(Option... options) {
        return format("forward-10", options);
    }

    public static String forward15(Option... options) {
        return format("forward-15", options);
    }

    public static String forward30(Option... options) {
        return format("forward-30", options);
    }

    public static String forward5(Option... options) {
        return format("forward-5", options);
    }

    public static String forwardEnd(Option... options) {
        return format("forward-end", options);
    }

    public static String forwardEndMini(Option... options) {
        return format("forward-end-mini", options);
    }

    public static String fridge(Option... options) {
        return format("fridge", options);
    }

    public static String friendica(Option... options) {
        return format("friendica", options);
    }

    public static String fullscreen(Option... options) {
        return format("fullscreen", options);
    }

    public static String fullscreenExit(Option... options) {
        return format("fullscreen-exit", options);
    }

    public static String function(Option... options) {
        return format("function", options);
    }

    public static String functionAdd(Option... options) {
        return format("function-add", options);
    }

    public static String funds(Option... options) {
        return format("funds", options);
    }

    public static String fundsBox(Option... options) {
        return format("funds-box", options);
    }

    public static String gallery(Option... options) {
        return format("gallery", options);
    }

    public static String galleryUpload(Option... options) {
        return format("gallery-upload", options);
    }

    public static String game(Option... options) {
        return format("game", options);
    }

    public static String gamepad(Option... options) {
        return format("gamepad", options);
    }

    public static String gasStation(Option... options) {
        return format("gas-station", options);
    }

    public static String gatsby(Option... options) {
        return format("gatsby", options);
    }

    public static String gemini(Option... options) {
        return format("gemini", options);
    }

    public static String genderless(Option... options) {
        return format("genderless", options);
    }

    public static String ghost(Option... options) {
        return format("ghost", options);
    }

    public static String ghost2(Option... options) {
        return format("ghost-2", options);
    }

    public static String ghostSmile(Option... options) {
        return format("ghost-smile", options);
    }

    public static String gift(Option... options) {
        return format("gift", options);
    }

    public static String gift2(Option... options) {
        return format("gift-2", options);
    }

    public static String gitBranch(Option... options) {
        return format("git-branch", options);
    }

    public static String gitClosePullRequest(Option... options) {
        return format("git-close-pull-request", options);
    }

    public static String gitCommit(Option... options) {
        return format("git-commit", options);
    }

    public static String gitFork(Option... options) {
        return format("git-fork", options);
    }

    public static String gitMerge(Option... options) {
        return format("git-merge", options);
    }

    public static String gitPrDraft(Option... options) {
        return format("git-pr-draft", options);
    }

    public static String gitPullRequest(Option... options) {
        return format("git-pull-request", options);
    }

    public static String gitRepository(Option... options) {
        return format("git-repository", options);
    }

    public static String gitRepositoryCommits(Option... options) {
        return format("git-repository-commits", options);
    }

    public static String gitRepositoryPrivate(Option... options) {
        return format("git-repository-private", options);
    }

    public static String github(Option... options) {
        return format("github", options);
    }

    public static String gitlab(Option... options) {
        return format("gitlab", options);
    }

    public static String glasses(Option... options) {
        return format("glasses", options);
    }

    public static String glasses2(Option... options) {
        return format("glasses-2", options);
    }

    public static String global(Option... options) {
        return format("global", options);
    }

    public static String globe(Option... options) {
        return format("globe", options);
    }

    public static String goblet(Option... options) {
        return format("goblet", options);
    }

    public static String goblet2(Option... options) {
        return format("goblet-2", options);
    }

    public static String goggles(Option... options) {
        return format("goggles", options);
    }

    public static String golfBall(Option... options) {
        return format("golf-ball", options);
    }

    public static String google(Option... options) {
        return format("google", options);
    }

    public static String googlePlay(Option... options) {
        return format("google-play", options);
    }

    public static String government(Option... options) {
        return format("government", options);
    }

    public static String gps(Option... options) {
        return format("gps", options);
    }

    public static String gradienter(Option... options) {
        return format("gradienter", options);
    }

    public static String graduationCap(Option... options) {
        return format("graduation-cap", options);
    }

    public static String grid(Option... options) {
        return format("grid", options);
    }

    public static String group(Option... options) {
        return format("group", options);
    }

    public static String group2(Option... options) {
        return format("group-2", options);
    }

    public static String group3(Option... options) {
        return format("group-3", options);
    }

    public static String guide(Option... options) {
        return format("guide", options);
    }

    public static String hail(Option... options) {
        return format("hail", options);
    }

    public static String hammer(Option... options) {
        return format("hammer", options);
    }

    public static String handCoin(Option... options) {
        return format("hand-coin", options);
    }

    public static String handHeart(Option... options) {
        return format("hand-heart", options);
    }

    public static String handSanitizer(Option... options) {
        return format("hand-sanitizer", options);
    }

    public static String handbag(Option... options) {
        return format("handbag", options);
    }

    public static String hardDrive(Option... options) {
        return format("hard-drive", options);
    }

    public static String hardDrive2(Option... options) {
        return format("hard-drive-2", options);
    }

    public static String hardDrive3(Option... options) {
        return format("hard-drive-3", options);
    }

    public static String haze(Option... options) {
        return format("haze", options);
    }

    public static String haze2(Option... options) {
        return format("haze-2", options);
    }

    public static String hd(Option... options) {
        return format("hd", options);
    }

    public static String headphone(Option... options) {
        return format("headphone", options);
    }

    public static String healthBook(Option... options) {
        return format("health-book", options);
    }

    public static String heart(Option... options) {
        return format("heart", options);
    }

    public static String heart2(Option... options) {
        return format("heart-2", options);
    }

    public static String heart3(Option... options) {
        return format("heart-3", options);
    }

    public static String heartAdd(Option... options) {
        return format("heart-add", options);
    }

    public static String heartAdd2(Option... options) {
        return format("heart-add-2", options);
    }

    public static String heartPulse(Option... options) {
        return format("heart-pulse", options);
    }

    public static String hearts(Option... options) {
        return format("hearts", options);
    }

    public static String heavyShowers(Option... options) {
        return format("heavy-showers", options);
    }

    public static String hexagon(Option... options) {
        return format("hexagon", options);
    }

    public static String history(Option... options) {
        return format("history", options);
    }

    public static String home(Option... options) {
        return format("home", options);
    }

    public static String home2(Option... options) {
        return format("home-2", options);
    }

    public static String home3(Option... options) {
        return format("home-3", options);
    }

    public static String home4(Option... options) {
        return format("home-4", options);
    }

    public static String home5(Option... options) {
        return format("home-5", options);
    }

    public static String home6(Option... options) {
        return format("home-6", options);
    }

    public static String home7(Option... options) {
        return format("home-7", options);
    }

    public static String home8(Option... options) {
        return format("home-8", options);
    }

    public static String home9(Option... options) {
        return format("home-9", options);
    }

    public static String homeGear(Option... options) {
        return format("home-gear", options);
    }

    public static String homeHeart(Option... options) {
        return format("home-heart", options);
    }

    public static String homeOffice(Option... options) {
        return format("home-office", options);
    }

    public static String homeSmile(Option... options) {
        return format("home-smile", options);
    }

    public static String homeSmile2(Option... options) {
        return format("home-smile-2", options);
    }

    public static String homeWifi(Option... options) {
        return format("home-wifi", options);
    }

    public static String honorOfKings(Option... options) {
        return format("honor-of-kings", options);
    }

    public static String honour(Option... options) {
        return format("honour", options);
    }

    public static String hospital(Option... options) {
        return format("hospital", options);
    }

    public static String hotel(Option... options) {
        return format("hotel", options);
    }

    public static String hotelBed(Option... options) {
        return format("hotel-bed", options);
    }

    public static String hotspot(Option... options) {
        return format("hotspot", options);
    }

    public static String hourglass(Option... options) {
        return format("hourglass", options);
    }

    public static String hourglass2(Option... options) {
        return format("hourglass-2", options);
    }

    public static String hq(Option... options) {
        return format("hq", options);
    }

    public static String html5(Option... options) {
        return format("html5", options);
    }

    public static String idCard(Option... options) {
        return format("id-card", options);
    }

    public static String ie(Option... options) {
        return format("ie", options);
    }

    public static String image(Option... options) {
        return format("image", options);
    }

    public static String image2(Option... options) {
        return format("image-2", options);
    }

    public static String imageAdd(Option... options) {
        return format("image-add", options);
    }

    public static String imageAi(Option... options) {
        return format("image-ai", options);
    }

    public static String imageCircle(Option... options) {
        return format("image-circle", options);
    }

    public static String imageCircleAi(Option... options) {
        return format("image-circle-ai", options);
    }

    public static String imageEdit(Option... options) {
        return format("image-edit", options);
    }

    public static String import_(Option... options) {
        return format("import", options);
    }

    public static String inbox(Option... options) {
        return format("inbox", options);
    }

    public static String inbox2(Option... options) {
        return format("inbox-2", options);
    }

    public static String inboxArchive(Option... options) {
        return format("inbox-archive", options);
    }

    public static String inboxUnarchive(Option... options) {
        return format("inbox-unarchive", options);
    }

    public static String increaseDecrease(Option... options) {
        return format("increase-decrease", options);
    }

    public static String indeterminateCircle(Option... options) {
        return format("indeterminate-circle", options);
    }

    public static String infinity(Option... options) {
        return format("infinity", options);
    }

    public static String infoCard(Option... options) {
        return format("info-card", options);
    }

    public static String information(Option... options) {
        return format("information", options);
    }

    public static String information2(Option... options) {
        return format("information-2", options);
    }

    public static String informationOff(Option... options) {
        return format("information-off", options);
    }

    public static String infraredThermometer(Option... options) {
        return format("infrared-thermometer", options);
    }

    public static String inkBottle(Option... options) {
        return format("ink-bottle", options);
    }

    public static String inputMethod(Option... options) {
        return format("input-method", options);
    }

    public static String instagram(Option... options) {
        return format("instagram", options);
    }

    public static String install(Option... options) {
        return format("install", options);
    }

    public static String instance(Option... options) {
        return format("instance", options);
    }

    public static String invision(Option... options) {
        return format("invision", options);
    }

    public static String java(Option... options) {
        return format("java", options);
    }

    public static String javascript(Option... options) {
        return format("javascript", options);
    }

    public static String jewelry(Option... options) {
        return format("jewelry", options);
    }

    public static String kakaoTalk(Option... options) {
        return format("kakao-talk", options);
    }

    public static String key(Option... options) {
        return format("key", options);
    }

    public static String key2(Option... options) {
        return format("key-2", options);
    }

    public static String keyboard(Option... options) {
        return format("keyboard", options);
    }

    public static String keyboardBox(Option... options) {
        return format("keyboard-box", options);
    }

    public static String keynote(Option... options) {
        return format("keynote", options);
    }

    public static String kick(Option... options) {
        return format("kick", options);
    }

    public static String knife(Option... options) {
        return format("knife", options);
    }

    public static String knifeBlood(Option... options) {
        return format("knife-blood", options);
    }

    public static String landscape(Option... options) {
        return format("landscape", options);
    }

    public static String landscapeAi(Option... options) {
        return format("landscape-ai", options);
    }

    public static String layout(Option... options) {
        return format("layout", options);
    }

    public static String layout2(Option... options) {
        return format("layout-2", options);
    }

    public static String layout3(Option... options) {
        return format("layout-3", options);
    }

    public static String layout4(Option... options) {
        return format("layout-4", options);
    }

    public static String layout5(Option... options) {
        return format("layout-5", options);
    }

    public static String layout6(Option... options) {
        return format("layout-6", options);
    }

    public static String layoutBottom(Option... options) {
        return format("layout-bottom", options);
    }

    public static String layoutBottom2(Option... options) {
        return format("layout-bottom-2", options);
    }

    public static String layoutColumn(Option... options) {
        return format("layout-column", options);
    }

    public static String layoutGrid(Option... options) {
        return format("layout-grid", options);
    }

    public static String layoutGrid2(Option... options) {
        return format("layout-grid-2", options);
    }

    public static String layoutHorizontal(Option... options) {
        return format("layout-horizontal", options);
    }

    public static String layoutLeft(Option... options) {
        return format("layout-left", options);
    }

    public static String layoutLeft2(Option... options) {
        return format("layout-left-2", options);
    }

    public static String layoutMasonry(Option... options) {
        return format("layout-masonry", options);
    }

    public static String layoutRight(Option... options) {
        return format("layout-right", options);
    }

    public static String layoutRight2(Option... options) {
        return format("layout-right-2", options);
    }

    public static String layoutRow(Option... options) {
        return format("layout-row", options);
    }

    public static String layoutTop(Option... options) {
        return format("layout-top", options);
    }

    public static String layoutTop2(Option... options) {
        return format("layout-top-2", options);
    }

    public static String layoutVertical(Option... options) {
        return format("layout-vertical", options);
    }

    public static String leaf(Option... options) {
        return format("leaf", options);
    }

    public static String lifebuoy(Option... options) {
        return format("lifebuoy", options);
    }

    public static String lightbulb(Option... options) {
        return format("lightbulb", options);
    }

    public static String lightbulbFlash(Option... options) {
        return format("lightbulb-flash", options);
    }

    public static String line(Option... options) {
        return format("line", options);
    }

    public static String lineChart(Option... options) {
        return format("line-chart", options);
    }

    public static String linkedin(Option... options) {
        return format("linkedin", options);
    }

    public static String linkedinBox(Option... options) {
        return format("linkedin-box", options);
    }

    public static String links(Option... options) {
        return format("links", options);
    }

    public static String listSettings(Option... options) {
        return format("list-settings", options);
    }

    public static String live(Option... options) {
        return format("live", options);
    }

    public static String loader(Option... options) {
        return format("loader", options);
    }

    public static String loader2(Option... options) {
        return format("loader-2", options);
    }

    public static String loader3(Option... options) {
        return format("loader-3", options);
    }

    public static String loader4(Option... options) {
        return format("loader-4", options);
    }

    public static String loader5(Option... options) {
        return format("loader-5", options);
    }

    public static String lock(Option... options) {
        return format("lock", options);
    }

    public static String lock2(Option... options) {
        return format("lock-2", options);
    }

    public static String lockPassword(Option... options) {
        return format("lock-password", options);
    }

    public static String lockStar(Option... options) {
        return format("lock-star", options);
    }

    public static String lockUnlock(Option... options) {
        return format("lock-unlock", options);
    }

    public static String loginBox(Option... options) {
        return format("login-box", options);
    }

    public static String loginCircle(Option... options) {
        return format("login-circle", options);
    }

    public static String logoutBox(Option... options) {
        return format("logout-box", options);
    }

    public static String logoutBoxR(Option... options) {
        return format("logout-box-r", options);
    }

    public static String logoutCircle(Option... options) {
        return format("logout-circle", options);
    }

    public static String logoutCircleR(Option... options) {
        return format("logout-circle-r", options);
    }

    public static String loopLeft(Option... options) {
        return format("loop-left", options);
    }

    public static String loopRight(Option... options) {
        return format("loop-right", options);
    }

    public static String luggageCart(Option... options) {
        return format("luggage-cart", options);
    }

    public static String luggageDeposit(Option... options) {
        return format("luggage-deposit", options);
    }

    public static String lungs(Option... options) {
        return format("lungs", options);
    }

    public static String mac(Option... options) {
        return format("mac", options);
    }

    public static String macbook(Option... options) {
        return format("macbook", options);
    }

    public static String magic(Option... options) {
        return format("magic", options);
    }

    public static String mail(Option... options) {
        return format("mail", options);
    }

    public static String mailAdd(Option... options) {
        return format("mail-add", options);
    }

    public static String mailAi(Option... options) {
        return format("mail-ai", options);
    }

    public static String mailCheck(Option... options) {
        return format("mail-check", options);
    }

    public static String mailClose(Option... options) {
        return format("mail-close", options);
    }

    public static String mailDownload(Option... options) {
        return format("mail-download", options);
    }

    public static String mailForbid(Option... options) {
        return format("mail-forbid", options);
    }

    public static String mailLock(Option... options) {
        return format("mail-lock", options);
    }

    public static String mailOpen(Option... options) {
        return format("mail-open", options);
    }

    public static String mailSend(Option... options) {
        return format("mail-send", options);
    }

    public static String mailSettings(Option... options) {
        return format("mail-settings", options);
    }

    public static String mailStar(Option... options) {
        return format("mail-star", options);
    }

    public static String mailUnread(Option... options) {
        return format("mail-unread", options);
    }

    public static String mailVolume(Option... options) {
        return format("mail-volume", options);
    }

    public static String map(Option... options) {
        return format("map", options);
    }

    public static String map2(Option... options) {
        return format("map-2", options);
    }

    public static String mapPin(Option... options) {
        return format("map-pin", options);
    }

    public static String mapPin2(Option... options) {
        return format("map-pin-2", options);
    }

    public static String mapPin3(Option... options) {
        return format("map-pin-3", options);
    }

    public static String mapPin4(Option... options) {
        return format("map-pin-4", options);
    }

    public static String mapPin5(Option... options) {
        return format("map-pin-5", options);
    }

    public static String mapPinAdd(Option... options) {
        return format("map-pin-add", options);
    }

    public static String mapPinRange(Option... options) {
        return format("map-pin-range", options);
    }

    public static String mapPinTime(Option... options) {
        return format("map-pin-time", options);
    }

    public static String mapPinUser(Option... options) {
        return format("map-pin-user", options);
    }

    public static String markPen(Option... options) {
        return format("mark-pen", options);
    }

    public static String markdown(Option... options) {
        return format("markdown", options);
    }

    public static String markup(Option... options) {
        return format("markup", options);
    }

    public static String mastercard(Option... options) {
        return format("mastercard", options);
    }

    public static String mastodon(Option... options) {
        return format("mastodon", options);
    }

    public static String medal(Option... options) {
        return format("medal", options);
    }

    public static String medal2(Option... options) {
        return format("medal-2", options);
    }

    public static String medicineBottle(Option... options) {
        return format("medicine-bottle", options);
    }

    public static String medium(Option... options) {
        return format("medium", options);
    }

    public static String megaphone(Option... options) {
        return format("megaphone", options);
    }

    public static String memories(Option... options) {
        return format("memories", options);
    }

    public static String men(Option... options) {
        return format("men", options);
    }

    public static String mentalHealth(Option... options) {
        return format("mental-health", options);
    }

    public static String menu(Option... options) {
        return format("menu", options);
    }

    public static String menu2(Option... options) {
        return format("menu-2", options);
    }

    public static String menu3(Option... options) {
        return format("menu-3", options);
    }

    public static String menu4(Option... options) {
        return format("menu-4", options);
    }

    public static String menu5(Option... options) {
        return format("menu-5", options);
    }

    public static String menuAdd(Option... options) {
        return format("menu-add", options);
    }

    public static String menuFold(Option... options) {
        return format("menu-fold", options);
    }

    public static String menuFold2(Option... options) {
        return format("menu-fold-2", options);
    }

    public static String menuFold3(Option... options) {
        return format("menu-fold-3", options);
    }

    public static String menuFold4(Option... options) {
        return format("menu-fold-4", options);
    }

    public static String menuSearch(Option... options) {
        return format("menu-search", options);
    }

    public static String menuUnfold(Option... options) {
        return format("menu-unfold", options);
    }

    public static String menuUnfold2(Option... options) {
        return format("menu-unfold-2", options);
    }

    public static String menuUnfold3(Option... options) {
        return format("menu-unfold-3", options);
    }

    public static String menuUnfold4(Option... options) {
        return format("menu-unfold-4", options);
    }

    public static String message(Option... options) {
        return format("message", options);
    }

    public static String message2(Option... options) {
        return format("message-2", options);
    }

    public static String message3(Option... options) {
        return format("message-3", options);
    }

    public static String messenger(Option... options) {
        return format("messenger", options);
    }

    public static String meta(Option... options) {
        return format("meta", options);
    }

    public static String meteor(Option... options) {
        return format("meteor", options);
    }

    public static String mic(Option... options) {
        return format("mic", options);
    }

    public static String mic2(Option... options) {
        return format("mic-2", options);
    }

    public static String mic2Ai(Option... options) {
        return format("mic-2-ai", options);
    }

    public static String micAi(Option... options) {
        return format("mic-ai", options);
    }

    public static String micOff(Option... options) {
        return format("mic-off", options);
    }

    public static String mickey(Option... options) {
        return format("mickey", options);
    }

    public static String microscope(Option... options) {
        return format("microscope", options);
    }

    public static String microsoft(Option... options) {
        return format("microsoft", options);
    }

    public static String microsoftLoop(Option... options) {
        return format("microsoft-loop", options);
    }

    public static String miniProgram(Option... options) {
        return format("mini-program", options);
    }

    public static String mist(Option... options) {
        return format("mist", options);
    }

    public static String mixtral(Option... options) {
        return format("mixtral", options);
    }

    public static String mobileDownload(Option... options) {
        return format("mobile-download", options);
    }

    public static String moneyCnyBox(Option... options) {
        return format("money-cny-box", options);
    }

    public static String moneyCnyCircle(Option... options) {
        return format("money-cny-circle", options);
    }

    public static String moneyDollarBox(Option... options) {
        return format("money-dollar-box", options);
    }

    public static String moneyDollarCircle(Option... options) {
        return format("money-dollar-circle", options);
    }

    public static String moneyEuroBox(Option... options) {
        return format("money-euro-box", options);
    }

    public static String moneyEuroCircle(Option... options) {
        return format("money-euro-circle", options);
    }

    public static String moneyPoundBox(Option... options) {
        return format("money-pound-box", options);
    }

    public static String moneyPoundCircle(Option... options) {
        return format("money-pound-circle", options);
    }

    public static String moneyRupeeCircle(Option... options) {
        return format("money-rupee-circle", options);
    }

    public static String moon(Option... options) {
        return format("moon", options);
    }

    public static String moonClear(Option... options) {
        return format("moon-clear", options);
    }

    public static String moonCloudy(Option... options) {
        return format("moon-cloudy", options);
    }

    public static String moonFoggy(Option... options) {
        return format("moon-foggy", options);
    }

    public static String more(Option... options) {
        return format("more", options);
    }

    public static String more2(Option... options) {
        return format("more-2", options);
    }

    public static String motorbike(Option... options) {
        return format("motorbike", options);
    }

    public static String mouse(Option... options) {
        return format("mouse", options);
    }

    public static String movie(Option... options) {
        return format("movie", options);
    }

    public static String movie2(Option... options) {
        return format("movie-2", options);
    }

    public static String movie2Ai(Option... options) {
        return format("movie-2-ai", options);
    }

    public static String movieAi(Option... options) {
        return format("movie-ai", options);
    }

    public static String multiImage(Option... options) {
        return format("multi-image", options);
    }

    public static String music(Option... options) {
        return format("music", options);
    }

    public static String music2(Option... options) {
        return format("music-2", options);
    }

    public static String musicAi(Option... options) {
        return format("music-ai", options);
    }

    public static String mv(Option... options) {
        return format("mv", options);
    }

    public static String mvAi(Option... options) {
        return format("mv-ai", options);
    }

    public static String navigation(Option... options) {
        return format("navigation", options);
    }

    public static String neteaseCloudMusic(Option... options) {
        return format("netease-cloud-music", options);
    }

    public static String netflix(Option... options) {
        return format("netflix", options);
    }

    public static String news(Option... options) {
        return format("news", options);
    }

    public static String newspaper(Option... options) {
        return format("newspaper", options);
    }

    public static String nextjs(Option... options) {
        return format("nextjs", options);
    }

    public static String nft(Option... options) {
        return format("nft", options);
    }

    public static String noCreditCard(Option... options) {
        return format("no-credit-card", options);
    }

    public static String nodejs(Option... options) {
        return format("nodejs", options);
    }

    public static String notification(Option... options) {
        return format("notification", options);
    }

    public static String notification2(Option... options) {
        return format("notification-2", options);
    }

    public static String notification3(Option... options) {
        return format("notification-3", options);
    }

    public static String notification4(Option... options) {
        return format("notification-4", options);
    }

    public static String notificationBadge(Option... options) {
        return format("notification-badge", options);
    }

    public static String notificationOff(Option... options) {
        return format("notification-off", options);
    }

    public static String notificationSnooze(Option... options) {
        return format("notification-snooze", options);
    }

    public static String notion(Option... options) {
        return format("notion", options);
    }

    public static String npmjs(Option... options) {
        return format("npmjs", options);
    }

    public static String numbers(Option... options) {
        return format("numbers", options);
    }

    public static String nurse(Option... options) {
        return format("nurse", options);
    }

    public static String octagon(Option... options) {
        return format("octagon", options);
    }

    public static String oil(Option... options) {
        return format("oil", options);
    }

    public static String openArm(Option... options) {
        return format("open-arm", options);
    }

    public static String openSource(Option... options) {
        return format("open-source", options);
    }

    public static String openai(Option... options) {
        return format("openai", options);
    }

    public static String openbase(Option... options) {
        return format("openbase", options);
    }

    public static String opera(Option... options) {
        return format("opera", options);
    }

    public static String orderPlay(Option... options) {
        return format("order-play", options);
    }

    public static String outlet(Option... options) {
        return format("outlet", options);
    }

    public static String outlet2(Option... options) {
        return format("outlet-2", options);
    }

    public static String p2p(Option... options) {
        return format("p2p", options);
    }

    public static String pages(Option... options) {
        return format("pages", options);
    }

    public static String paint(Option... options) {
        return format("paint", options);
    }

    public static String paintBrush(Option... options) {
        return format("paint-brush", options);
    }

    public static String palette(Option... options) {
        return format("palette", options);
    }

    public static String pantone(Option... options) {
        return format("pantone", options);
    }

    public static String parent(Option... options) {
        return format("parent", options);
    }

    public static String parentheses(Option... options) {
        return format("parentheses", options);
    }

    public static String parking(Option... options) {
        return format("parking", options);
    }

    public static String parkingBox(Option... options) {
        return format("parking-box", options);
    }

    public static String passExpired(Option... options) {
        return format("pass-expired", options);
    }

    public static String passPending(Option... options) {
        return format("pass-pending", options);
    }

    public static String passValid(Option... options) {
        return format("pass-valid", options);
    }

    public static String passport(Option... options) {
        return format("passport", options);
    }

    public static String patreon(Option... options) {
        return format("patreon", options);
    }

    public static String pause(Option... options) {
        return format("pause", options);
    }

    public static String pauseCircle(Option... options) {
        return format("pause-circle", options);
    }

    public static String pauseLarge(Option... options) {
        return format("pause-large", options);
    }

    public static String pauseMini(Option... options) {
        return format("pause-mini", options);
    }

    public static String paypal(Option... options) {
        return format("paypal", options);
    }

    public static String penNib(Option... options) {
        return format("pen-nib", options);
    }

    public static String pencil(Option... options) {
        return format("pencil", options);
    }

    public static String pencilRuler(Option... options) {
        return format("pencil-ruler", options);
    }

    public static String pencilRuler2(Option... options) {
        return format("pencil-ruler-2", options);
    }

    public static String pentagon(Option... options) {
        return format("pentagon", options);
    }

    public static String percent(Option... options) {
        return format("percent", options);
    }

    public static String perplexity(Option... options) {
        return format("perplexity", options);
    }

    public static String phone(Option... options) {
        return format("phone", options);
    }

    public static String phoneCamera(Option... options) {
        return format("phone-camera", options);
    }

    public static String phoneFind(Option... options) {
        return format("phone-find", options);
    }

    public static String phoneLock(Option... options) {
        return format("phone-lock", options);
    }

    public static String php(Option... options) {
        return format("php", options);
    }

    public static String pictureInPicture(Option... options) {
        return format("picture-in-picture", options);
    }

    public static String pictureInPicture2(Option... options) {
        return format("picture-in-picture-2", options);
    }

    public static String pictureInPictureExit(Option... options) {
        return format("picture-in-picture-exit", options);
    }

    public static String pieChart(Option... options) {
        return format("pie-chart", options);
    }

    public static String pieChart2(Option... options) {
        return format("pie-chart-2", options);
    }

    public static String pieChartBox(Option... options) {
        return format("pie-chart-box", options);
    }

    public static String pinDistance(Option... options) {
        return format("pin-distance", options);
    }

    public static String pingPong(Option... options) {
        return format("ping-pong", options);
    }

    public static String pinterest(Option... options) {
        return format("pinterest", options);
    }

    public static String pix(Option... options) {
        return format("pix", options);
    }

    public static String pixelfed(Option... options) {
        return format("pixelfed", options);
    }

    public static String plane(Option... options) {
        return format("plane", options);
    }

    public static String planet(Option... options) {
        return format("planet", options);
    }

    public static String plant(Option... options) {
        return format("plant", options);
    }

    public static String play(Option... options) {
        return format("play", options);
    }

    public static String playCircle(Option... options) {
        return format("play-circle", options);
    }

    public static String playLarge(Option... options) {
        return format("play-large", options);
    }

    public static String playList(Option... options) {
        return format("play-list", options);
    }

    public static String playList2(Option... options) {
        return format("play-list-2", options);
    }

    public static String playListAdd(Option... options) {
        return format("play-list-add", options);
    }

    public static String playMini(Option... options) {
        return format("play-mini", options);
    }

    public static String playReverse(Option... options) {
        return format("play-reverse", options);
    }

    public static String playReverseLarge(Option... options) {
        return format("play-reverse-large", options);
    }

    public static String playReverseMini(Option... options) {
        return format("play-reverse-mini", options);
    }

    public static String playstation(Option... options) {
        return format("playstation", options);
    }

    public static String plug(Option... options) {
        return format("plug", options);
    }

    public static String plug2(Option... options) {
        return format("plug-2", options);
    }

    public static String pokerClubs(Option... options) {
        return format("poker-clubs", options);
    }

    public static String pokerDiamonds(Option... options) {
        return format("poker-diamonds", options);
    }

    public static String pokerHearts(Option... options) {
        return format("poker-hearts", options);
    }

    public static String pokerSpades(Option... options) {
        return format("poker-spades", options);
    }

    public static String polaroid(Option... options) {
        return format("polaroid", options);
    }

    public static String polaroid2(Option... options) {
        return format("polaroid-2", options);
    }

    public static String policeBadge(Option... options) {
        return format("police-badge", options);
    }

    public static String policeCar(Option... options) {
        return format("police-car", options);
    }

    public static String presentation(Option... options) {
        return format("presentation", options);
    }

    public static String priceTag(Option... options) {
        return format("price-tag", options);
    }

    public static String priceTag2(Option... options) {
        return format("price-tag-2", options);
    }

    public static String priceTag3(Option... options) {
        return format("price-tag-3", options);
    }

    public static String printer(Option... options) {
        return format("printer", options);
    }

    public static String printerCloud(Option... options) {
        return format("printer-cloud", options);
    }

    public static String productHunt(Option... options) {
        return format("product-hunt", options);
    }

    public static String profile(Option... options) {
        return format("profile", options);
    }

    public static String progress1(Option... options) {
        return format("progress-1", options);
    }

    public static String progress2(Option... options) {
        return format("progress-2", options);
    }

    public static String progress3(Option... options) {
        return format("progress-3", options);
    }

    public static String progress4(Option... options) {
        return format("progress-4", options);
    }

    public static String progress5(Option... options) {
        return format("progress-5", options);
    }

    public static String progress6(Option... options) {
        return format("progress-6", options);
    }

    public static String progress7(Option... options) {
        return format("progress-7", options);
    }

    public static String progress8(Option... options) {
        return format("progress-8", options);
    }

    public static String prohibited(Option... options) {
        return format("prohibited", options);
    }

    public static String prohibited2(Option... options) {
        return format("prohibited-2", options);
    }

    public static String projector(Option... options) {
        return format("projector", options);
    }

    public static String projector2(Option... options) {
        return format("projector-2", options);
    }

    public static String psychotherapy(Option... options) {
        return format("psychotherapy", options);
    }

    public static String pulse(Option... options) {
        return format("pulse", options);
    }

    public static String pulseAi(Option... options) {
        return format("pulse-ai", options);
    }

    public static String pushpin(Option... options) {
        return format("pushpin", options);
    }

    public static String pushpin2(Option... options) {
        return format("pushpin-2", options);
    }

    public static String puzzle(Option... options) {
        return format("puzzle", options);
    }

    public static String puzzle2(Option... options) {
        return format("puzzle-2", options);
    }

    public static String qq(Option... options) {
        return format("qq", options);
    }

    public static String qrCode(Option... options) {
        return format("qr-code", options);
    }

    public static String qrScan(Option... options) {
        return format("qr-scan", options);
    }

    public static String qrScan2(Option... options) {
        return format("qr-scan-2", options);
    }

    public static String question(Option... options) {
        return format("question", options);
    }

    public static String questionAnswer(Option... options) {
        return format("question-answer", options);
    }

    public static String questionnaire(Option... options) {
        return format("questionnaire", options);
    }

    public static String quillPen(Option... options) {
        return format("quill-pen", options);
    }

    public static String quillPenAi(Option... options) {
        return format("quill-pen-ai", options);
    }

    public static String radar(Option... options) {
        return format("radar", options);
    }

    public static String radio(Option... options) {
        return format("radio", options);
    }

    public static String radio2(Option... options) {
        return format("radio-2", options);
    }

    public static String radioButton(Option... options) {
        return format("radio-button", options);
    }

    public static String rainbow(Option... options) {
        return format("rainbow", options);
    }

    public static String rainy(Option... options) {
        return format("rainy", options);
    }

    public static String ram(Option... options) {
        return format("ram", options);
    }

    public static String ram2(Option... options) {
        return format("ram-2", options);
    }

    public static String reactjs(Option... options) {
        return format("reactjs", options);
    }

    public static String receipt(Option... options) {
        return format("receipt", options);
    }

    public static String recordCircle(Option... options) {
        return format("record-circle", options);
    }

    public static String recordMail(Option... options) {
        return format("record-mail", options);
    }

    public static String rectangle(Option... options) {
        return format("rectangle", options);
    }

    public static String recycle(Option... options) {
        return format("recycle", options);
    }

    public static String redPacket(Option... options) {
        return format("red-packet", options);
    }

    public static String reddit(Option... options) {
        return format("reddit", options);
    }

    public static String refresh(Option... options) {
        return format("refresh", options);
    }

    public static String refund(Option... options) {
        return format("refund", options);
    }

    public static String refund2(Option... options) {
        return format("refund-2", options);
    }

    public static String registered(Option... options) {
        return format("registered", options);
    }

    public static String remixRun(Option... options) {
        return format("remix-run", options);
    }

    public static String remixicon(Option... options) {
        return format("remixicon", options);
    }

    public static String remoteControl(Option... options) {
        return format("remote-control", options);
    }

    public static String remoteControl2(Option... options) {
        return format("remote-control-2", options);
    }

    public static String repeat(Option... options) {
        return format("repeat", options);
    }

    public static String repeat2(Option... options) {
        return format("repeat-2", options);
    }

    public static String repeatOne(Option... options) {
        return format("repeat-one", options);
    }

    public static String replay10(Option... options) {
        return format("replay-10", options);
    }

    public static String replay15(Option... options) {
        return format("replay-15", options);
    }

    public static String replay30(Option... options) {
        return format("replay-30", options);
    }

    public static String replay5(Option... options) {
        return format("replay-5", options);
    }

    public static String reply(Option... options) {
        return format("reply", options);
    }

    public static String replyAll(Option... options) {
        return format("reply-all", options);
    }

    public static String reserved(Option... options) {
        return format("reserved", options);
    }

    public static String resetLeft(Option... options) {
        return format("reset-left", options);
    }

    public static String resetRight(Option... options) {
        return format("reset-right", options);
    }

    public static String restTime(Option... options) {
        return format("rest-time", options);
    }

    public static String restart(Option... options) {
        return format("restart", options);
    }

    public static String restaurant(Option... options) {
        return format("restaurant", options);
    }

    public static String restaurant2(Option... options) {
        return format("restaurant-2", options);
    }

    public static String rewind(Option... options) {
        return format("rewind", options);
    }

    public static String rewindMini(Option... options) {
        return format("rewind-mini", options);
    }

    public static String rewindStart(Option... options) {
        return format("rewind-start", options);
    }

    public static String rewindStartMini(Option... options) {
        return format("rewind-start-mini", options);
    }

    public static String rfid(Option... options) {
        return format("rfid", options);
    }

    public static String rhythm(Option... options) {
        return format("rhythm", options);
    }

    public static String riding(Option... options) {
        return format("riding", options);
    }

    public static String roadMap(Option... options) {
        return format("road-map", options);
    }

    public static String roadster(Option... options) {
        return format("roadster", options);
    }

    public static String robot(Option... options) {
        return format("robot", options);
    }

    public static String robot2(Option... options) {
        return format("robot-2", options);
    }

    public static String robot3(Option... options) {
        return format("robot-3", options);
    }

    public static String rocket(Option... options) {
        return format("rocket", options);
    }

    public static String rocket2(Option... options) {
        return format("rocket-2", options);
    }

    public static String rotateLock(Option... options) {
        return format("rotate-lock", options);
    }

    public static String route(Option... options) {
        return format("route", options);
    }

    public static String router(Option... options) {
        return format("router", options);
    }

    public static String rss(Option... options) {
        return format("rss", options);
    }

    public static String ruler(Option... options) {
        return format("ruler", options);
    }

    public static String ruler2(Option... options) {
        return format("ruler-2", options);
    }

    public static String run(Option... options) {
        return format("run", options);
    }

    public static String safari(Option... options) {
        return format("safari", options);
    }

    public static String safe(Option... options) {
        return format("safe", options);
    }

    public static String safe2(Option... options) {
        return format("safe-2", options);
    }

    public static String safe3(Option... options) {
        return format("safe-3", options);
    }

    public static String sailboat(Option... options) {
        return format("sailboat", options);
    }

    public static String save(Option... options) {
        return format("save", options);
    }

    public static String save2(Option... options) {
        return format("save-2", options);
    }

    public static String save3(Option... options) {
        return format("save-3", options);
    }

    public static String scales(Option... options) {
        return format("scales", options);
    }

    public static String scales2(Option... options) {
        return format("scales-2", options);
    }

    public static String scales3(Option... options) {
        return format("scales-3", options);
    }

    public static String scan(Option... options) {
        return format("scan", options);
    }

    public static String scan2(Option... options) {
        return format("scan-2", options);
    }

    public static String school(Option... options) {
        return format("school", options);
    }

    public static String scissors(Option... options) {
        return format("scissors", options);
    }

    public static String scissors2(Option... options) {
        return format("scissors-2", options);
    }

    public static String scissorsCut(Option... options) {
        return format("scissors-cut", options);
    }

    public static String screenshot(Option... options) {
        return format("screenshot", options);
    }

    public static String screenshot2(Option... options) {
        return format("screenshot-2", options);
    }

    public static String scrollToBottom(Option... options) {
        return format("scroll-to-bottom", options);
    }

    public static String sdCard(Option... options) {
        return format("sd-card", options);
    }

    public static String sdCardMini(Option... options) {
        return format("sd-card-mini", options);
    }

    public static String search(Option... options) {
        return format("search", options);
    }

    public static String search2(Option... options) {
        return format("search-2", options);
    }

    public static String searchEye(Option... options) {
        return format("search-eye", options);
    }

    public static String securePayment(Option... options) {
        return format("secure-payment", options);
    }

    public static String seedling(Option... options) {
        return format("seedling", options);
    }

    public static String sendPlane(Option... options) {
        return format("send-plane", options);
    }

    public static String sendPlane2(Option... options) {
        return format("send-plane-2", options);
    }

    public static String sensor(Option... options) {
        return format("sensor", options);
    }

    public static String seo(Option... options) {
        return format("seo", options);
    }

    public static String server(Option... options) {
        return format("server", options);
    }

    public static String service(Option... options) {
        return format("service", options);
    }

    public static String serviceBell(Option... options) {
        return format("service-bell", options);
    }

    public static String settings(Option... options) {
        return format("settings", options);
    }

    public static String settings2(Option... options) {
        return format("settings-2", options);
    }

    public static String settings3(Option... options) {
        return format("settings-3", options);
    }

    public static String settings4(Option... options) {
        return format("settings-4", options);
    }

    public static String settings5(Option... options) {
        return format("settings-5", options);
    }

    public static String settings6(Option... options) {
        return format("settings-6", options);
    }

    public static String shadow(Option... options) {
        return format("shadow", options);
    }

    public static String shakeHands(Option... options) {
        return format("shake-hands", options);
    }

    public static String shape(Option... options) {
        return format("shape", options);
    }

    public static String shape2(Option... options) {
        return format("shape-2", options);
    }

    public static String shapes(Option... options) {
        return format("shapes", options);
    }

    public static String share(Option... options) {
        return format("share", options);
    }

    public static String share2(Option... options) {
        return format("share-2", options);
    }

    public static String shareBox(Option... options) {
        return format("share-box", options);
    }

    public static String shareCircle(Option... options) {
        return format("share-circle", options);
    }

    public static String shareForward(Option... options) {
        return format("share-forward", options);
    }

    public static String shareForward2(Option... options) {
        return format("share-forward-2", options);
    }

    public static String shareForwardBox(Option... options) {
        return format("share-forward-box", options);
    }

    public static String shield(Option... options) {
        return format("shield", options);
    }

    public static String shieldCheck(Option... options) {
        return format("shield-check", options);
    }

    public static String shieldCross(Option... options) {
        return format("shield-cross", options);
    }

    public static String shieldFlash(Option... options) {
        return format("shield-flash", options);
    }

    public static String shieldKeyhole(Option... options) {
        return format("shield-keyhole", options);
    }

    public static String shieldStar(Option... options) {
        return format("shield-star", options);
    }

    public static String shieldUser(Option... options) {
        return format("shield-user", options);
    }

    public static String shining(Option... options) {
        return format("shining", options);
    }

    public static String shining2(Option... options) {
        return format("shining-2", options);
    }

    public static String ship(Option... options) {
        return format("ship", options);
    }

    public static String ship2(Option... options) {
        return format("ship-2", options);
    }

    public static String shirt(Option... options) {
        return format("shirt", options);
    }

    public static String shoppingBag(Option... options) {
        return format("shopping-bag", options);
    }

    public static String shoppingBag2(Option... options) {
        return format("shopping-bag-2", options);
    }

    public static String shoppingBag3(Option... options) {
        return format("shopping-bag-3", options);
    }

    public static String shoppingBag4(Option... options) {
        return format("shopping-bag-4", options);
    }

    public static String shoppingBasket(Option... options) {
        return format("shopping-basket", options);
    }

    public static String shoppingBasket2(Option... options) {
        return format("shopping-basket-2", options);
    }

    public static String shoppingCart(Option... options) {
        return format("shopping-cart", options);
    }

    public static String shoppingCart2(Option... options) {
        return format("shopping-cart-2", options);
    }

    public static String showers(Option... options) {
        return format("showers", options);
    }

    public static String shuffle(Option... options) {
        return format("shuffle", options);
    }

    public static String shutDown(Option... options) {
        return format("shut-down", options);
    }

    public static String sideBar(Option... options) {
        return format("side-bar", options);
    }

    public static String sidebarFold(Option... options) {
        return format("sidebar-fold", options);
    }

    public static String sidebarUnfold(Option... options) {
        return format("sidebar-unfold", options);
    }

    public static String signalTower(Option... options) {
        return format("signal-tower", options);
    }

    public static String signalWifi(Option... options) {
        return format("signal-wifi", options);
    }

    public static String signalWifi1(Option... options) {
        return format("signal-wifi-1", options);
    }

    public static String signalWifi2(Option... options) {
        return format("signal-wifi-2", options);
    }

    public static String signalWifi3(Option... options) {
        return format("signal-wifi-3", options);
    }

    public static String signalWifiError(Option... options) {
        return format("signal-wifi-error", options);
    }

    public static String signalWifiOff(Option... options) {
        return format("signal-wifi-off", options);
    }

    public static String signpost(Option... options) {
        return format("signpost", options);
    }

    public static String simCard(Option... options) {
        return format("sim-card", options);
    }

    public static String simCard2(Option... options) {
        return format("sim-card-2", options);
    }

    public static String sip(Option... options) {
        return format("sip", options);
    }

    public static String skipBack(Option... options) {
        return format("skip-back", options);
    }

    public static String skipBackMini(Option... options) {
        return format("skip-back-mini", options);
    }

    public static String skipDown(Option... options) {
        return format("skip-down", options);
    }

    public static String skipForward(Option... options) {
        return format("skip-forward", options);
    }

    public static String skipForwardMini(Option... options) {
        return format("skip-forward-mini", options);
    }

    public static String skipLeft(Option... options) {
        return format("skip-left", options);
    }

    public static String skipRight(Option... options) {
        return format("skip-right", options);
    }

    public static String skipUp(Option... options) {
        return format("skip-up", options);
    }

    public static String skull(Option... options) {
        return format("skull", options);
    }

    public static String skull2(Option... options) {
        return format("skull-2", options);
    }

    public static String skype(Option... options) {
        return format("skype", options);
    }

    public static String slack(Option... options) {
        return format("slack", options);
    }

    public static String slice(Option... options) {
        return format("slice", options);
    }

    public static String slideshow(Option... options) {
        return format("slideshow", options);
    }

    public static String slideshow2(Option... options) {
        return format("slideshow-2", options);
    }

    public static String slideshow3(Option... options) {
        return format("slideshow-3", options);
    }

    public static String slideshow4(Option... options) {
        return format("slideshow-4", options);
    }

    public static String slowDown(Option... options) {
        return format("slow-down", options);
    }

    public static String smartphone(Option... options) {
        return format("smartphone", options);
    }

    public static String snapchat(Option... options) {
        return format("snapchat", options);
    }

    public static String snowflake(Option... options) {
        return format("snowflake", options);
    }

    public static String snowy(Option... options) {
        return format("snowy", options);
    }

    public static String sofa(Option... options) {
        return format("sofa", options);
    }

    public static String soundModule(Option... options) {
        return format("sound-module", options);
    }

    public static String soundcloud(Option... options) {
        return format("soundcloud", options);
    }

    public static String spaceShip(Option... options) {
        return format("space-ship", options);
    }

    public static String spam(Option... options) {
        return format("spam", options);
    }

    public static String spam2(Option... options) {
        return format("spam-2", options);
    }

    public static String spam3(Option... options) {
        return format("spam-3", options);
    }

    public static String sparkling(Option... options) {
        return format("sparkling", options);
    }

    public static String sparkling2(Option... options) {
        return format("sparkling-2", options);
    }

    public static String speak(Option... options) {
        return format("speak", options);
    }

    public static String speakAi(Option... options) {
        return format("speak-ai", options);
    }

    public static String speaker(Option... options) {
        return format("speaker", options);
    }

    public static String speaker2(Option... options) {
        return format("speaker-2", options);
    }

    public static String speaker3(Option... options) {
        return format("speaker-3", options);
    }

    public static String spectrum(Option... options) {
        return format("spectrum", options);
    }

    public static String speed(Option... options) {
        return format("speed", options);
    }

    public static String speedMini(Option... options) {
        return format("speed-mini", options);
    }

    public static String speedUp(Option... options) {
        return format("speed-up", options);
    }

    public static String spotify(Option... options) {
        return format("spotify", options);
    }

    public static String spy(Option... options) {
        return format("spy", options);
    }

    public static String square(Option... options) {
        return format("square", options);
    }

    public static String stack(Option... options) {
        return format("stack", options);
    }

    public static String stackOverflow(Option... options) {
        return format("stack-overflow", options);
    }

    public static String stackshare(Option... options) {
        return format("stackshare", options);
    }

    public static String stairs(Option... options) {
        return format("stairs", options);
    }

    public static String star(Option... options) {
        return format("star", options);
    }

    public static String starHalf(Option... options) {
        return format("star-half", options);
    }

    public static String starHalfS(Option... options) {
        return format("star-half-s", options);
    }

    public static String starOff(Option... options) {
        return format("star-off", options);
    }

    public static String starS(Option... options) {
        return format("star-s", options);
    }

    public static String starSmile(Option... options) {
        return format("star-smile", options);
    }

    public static String steam(Option... options) {
        return format("steam", options);
    }

    public static String steering(Option... options) {
        return format("steering", options);
    }

    public static String steering2(Option... options) {
        return format("steering-2", options);
    }

    public static String stethoscope(Option... options) {
        return format("stethoscope", options);
    }

    public static String stickyNote(Option... options) {
        return format("sticky-note", options);
    }

    public static String stickyNote2(Option... options) {
        return format("sticky-note-2", options);
    }

    public static String stickyNoteAdd(Option... options) {
        return format("sticky-note-add", options);
    }

    public static String stock(Option... options) {
        return format("stock", options);
    }

    public static String stop(Option... options) {
        return format("stop", options);
    }

    public static String stopCircle(Option... options) {
        return format("stop-circle", options);
    }

    public static String stopLarge(Option... options) {
        return format("stop-large", options);
    }

    public static String stopMini(Option... options) {
        return format("stop-mini", options);
    }

    public static String store(Option... options) {
        return format("store", options);
    }

    public static String store2(Option... options) {
        return format("store-2", options);
    }

    public static String store3(Option... options) {
        return format("store-3", options);
    }

    public static String subtract(Option... options) {
        return format("subtract", options);
    }

    public static String subway(Option... options) {
        return format("subway", options);
    }

    public static String subwayWifi(Option... options) {
        return format("subway-wifi", options);
    }

    public static String suitcase(Option... options) {
        return format("suitcase", options);
    }

    public static String suitcase2(Option... options) {
        return format("suitcase-2", options);
    }

    public static String suitcase3(Option... options) {
        return format("suitcase-3", options);
    }

    public static String sun(Option... options) {
        return format("sun", options);
    }

    public static String sunCloudy(Option... options) {
        return format("sun-cloudy", options);
    }

    public static String sunFoggy(Option... options) {
        return format("sun-foggy", options);
    }

    public static String supabase(Option... options) {
        return format("supabase", options);
    }

    public static String surgicalMask(Option... options) {
        return format("surgical-mask", options);
    }

    public static String surroundSound(Option... options) {
        return format("surround-sound", options);
    }

    public static String survey(Option... options) {
        return format("survey", options);
    }

    public static String svelte(Option... options) {
        return format("svelte", options);
    }

    public static String swap(Option... options) {
        return format("swap", options);
    }

    public static String swap2(Option... options) {
        return format("swap-2", options);
    }

    public static String swap3(Option... options) {
        return format("swap-3", options);
    }

    public static String swapBox(Option... options) {
        return format("swap-box", options);
    }

    public static String switch_(Option... options) {
        return format("switch", options);
    }

    public static String sword(Option... options) {
        return format("sword", options);
    }

    public static String syringe(Option... options) {
        return format("syringe", options);
    }

    public static String tBox(Option... options) {
        return format("t-box", options);
    }

    public static String tShirt(Option... options) {
        return format("t-shirt", options);
    }

    public static String tShirt2(Option... options) {
        return format("t-shirt-2", options);
    }

    public static String tShirtAir(Option... options) {
        return format("t-shirt-air", options);
    }

    public static String table(Option... options) {
        return format("table", options);
    }

    public static String tableAlt(Option... options) {
        return format("table-alt", options);
    }

    public static String tablet(Option... options) {
        return format("tablet", options);
    }

    public static String tailwindCss(Option... options) {
        return format("tailwind-css", options);
    }

    public static String takeaway(Option... options) {
        return format("takeaway", options);
    }

    public static String taobao(Option... options) {
        return format("taobao", options);
    }

    public static String tape(Option... options) {
        return format("tape", options);
    }

    public static String task(Option... options) {
        return format("task", options);
    }

    public static String taxi(Option... options) {
        return format("taxi", options);
    }

    public static String taxiWifi(Option... options) {
        return format("taxi-wifi", options);
    }

    public static String team(Option... options) {
        return format("team", options);
    }

    public static String telegram(Option... options) {
        return format("telegram", options);
    }

    public static String telegram2(Option... options) {
        return format("telegram-2", options);
    }

    public static String tempCold(Option... options) {
        return format("temp-cold", options);
    }

    public static String tempHot(Option... options) {
        return format("temp-hot", options);
    }

    public static String tent(Option... options) {
        return format("tent", options);
    }

    public static String terminal(Option... options) {
        return format("terminal", options);
    }

    public static String terminalBox(Option... options) {
        return format("terminal-box", options);
    }

    public static String terminalWindow(Option... options) {
        return format("terminal-window", options);
    }

    public static String testTube(Option... options) {
        return format("test-tube", options);
    }

    public static String thermometer(Option... options) {
        return format("thermometer", options);
    }

    public static String threads(Option... options) {
        return format("threads", options);
    }

    public static String thumbDown(Option... options) {
        return format("thumb-down", options);
    }

    public static String thumbUp(Option... options) {
        return format("thumb-up", options);
    }

    public static String thunderstorms(Option... options) {
        return format("thunderstorms", options);
    }

    public static String ticket(Option... options) {
        return format("ticket", options);
    }

    public static String ticket2(Option... options) {
        return format("ticket-2", options);
    }

    public static String tiktok(Option... options) {
        return format("tiktok", options);
    }

    public static String time(Option... options) {
        return format("time", options);
    }

    public static String timeZone(Option... options) {
        return format("time-zone", options);
    }

    public static String timer(Option... options) {
        return format("timer", options);
    }

    public static String timer2(Option... options) {
        return format("timer-2", options);
    }

    public static String timerFlash(Option... options) {
        return format("timer-flash", options);
    }

    public static String todo(Option... options) {
        return format("todo", options);
    }

    public static String toggle(Option... options) {
        return format("toggle", options);
    }

    public static String tokenSwap(Option... options) {
        return format("token-swap", options);
    }

    public static String tools(Option... options) {
        return format("tools", options);
    }

    public static String tooth(Option... options) {
        return format("tooth", options);
    }

    public static String tornado(Option... options) {
        return format("tornado", options);
    }

    public static String trademark(Option... options) {
        return format("trademark", options);
    }

    public static String trafficLight(Option... options) {
        return format("traffic-light", options);
    }

    public static String train(Option... options) {
        return format("train", options);
    }

    public static String trainWifi(Option... options) {
        return format("train-wifi", options);
    }

    public static String travesti(Option... options) {
        return format("travesti", options);
    }

    public static String treasureMap(Option... options) {
        return format("treasure-map", options);
    }

    public static String tree(Option... options) {
        return format("tree", options);
    }

    public static String trello(Option... options) {
        return format("trello", options);
    }

    public static String triangle(Option... options) {
        return format("triangle", options);
    }

    public static String triangularFlag(Option... options) {
        return format("triangular-flag", options);
    }

    public static String trophy(Option... options) {
        return format("trophy", options);
    }

    public static String truck(Option... options) {
        return format("truck", options);
    }

    public static String tumblr(Option... options) {
        return format("tumblr", options);
    }

    public static String tv(Option... options) {
        return format("tv", options);
    }

    public static String tv2(Option... options) {
        return format("tv-2", options);
    }

    public static String twitch(Option... options) {
        return format("twitch", options);
    }

    public static String twitter(Option... options) {
        return format("twitter", options);
    }

    public static String twitterX(Option... options) {
        return format("twitter-x", options);
    }

    public static String typhoon(Option... options) {
        return format("typhoon", options);
    }

    public static String uDisk(Option... options) {
        return format("u-disk", options);
    }

    public static String ubuntu(Option... options) {
        return format("ubuntu", options);
    }

    public static String umbrella(Option... options) {
        return format("umbrella", options);
    }

    public static String uninstall(Option... options) {
        return format("uninstall", options);
    }

    public static String unpin(Option... options) {
        return format("unpin", options);
    }

    public static String unsplash(Option... options) {
        return format("unsplash", options);
    }

    public static String upload(Option... options) {
        return format("upload", options);
    }

    public static String upload2(Option... options) {
        return format("upload-2", options);
    }

    public static String uploadCloud(Option... options) {
        return format("upload-cloud", options);
    }

    public static String uploadCloud2(Option... options) {
        return format("upload-cloud-2", options);
    }

    public static String usb(Option... options) {
        return format("usb", options);
    }

    public static String user(Option... options) {
        return format("user", options);
    }

    public static String user2(Option... options) {
        return format("user-2", options);
    }

    public static String user3(Option... options) {
        return format("user-3", options);
    }

    public static String user4(Option... options) {
        return format("user-4", options);
    }

    public static String user5(Option... options) {
        return format("user-5", options);
    }

    public static String user6(Option... options) {
        return format("user-6", options);
    }

    public static String userAdd(Option... options) {
        return format("user-add", options);
    }

    public static String userCommunity(Option... options) {
        return format("user-community", options);
    }

    public static String userFollow(Option... options) {
        return format("user-follow", options);
    }

    public static String userForbid(Option... options) {
        return format("user-forbid", options);
    }

    public static String userHeart(Option... options) {
        return format("user-heart", options);
    }

    public static String userLocation(Option... options) {
        return format("user-location", options);
    }

    public static String userMinus(Option... options) {
        return format("user-minus", options);
    }

    public static String userReceived(Option... options) {
        return format("user-received", options);
    }

    public static String userReceived2(Option... options) {
        return format("user-received-2", options);
    }

    public static String userSearch(Option... options) {
        return format("user-search", options);
    }

    public static String userSettings(Option... options) {
        return format("user-settings", options);
    }

    public static String userShared(Option... options) {
        return format("user-shared", options);
    }

    public static String userShared2(Option... options) {
        return format("user-shared-2", options);
    }

    public static String userSmile(Option... options) {
        return format("user-smile", options);
    }

    public static String userStar(Option... options) {
        return format("user-star", options);
    }

    public static String userUnfollow(Option... options) {
        return format("user-unfollow", options);
    }

    public static String userVoice(Option... options) {
        return format("user-voice", options);
    }

    public static String vercel(Option... options) {
        return format("vercel", options);
    }

    public static String verifiedBadge(Option... options) {
        return format("verified-badge", options);
    }

    public static String video(Option... options) {
        return format("video", options);
    }

    public static String videoAdd(Option... options) {
        return format("video-add", options);
    }

    public static String videoAi(Option... options) {
        return format("video-ai", options);
    }

    public static String videoChat(Option... options) {
        return format("video-chat", options);
    }

    public static String videoDownload(Option... options) {
        return format("video-download", options);
    }

    public static String videoOff(Option... options) {
        return format("video-off", options);
    }

    public static String videoOn(Option... options) {
        return format("video-on", options);
    }

    public static String videoOnAi(Option... options) {
        return format("video-on-ai", options);
    }

    public static String videoUpload(Option... options) {
        return format("video-upload", options);
    }

    public static String vidicon(Option... options) {
        return format("vidicon", options);
    }

    public static String vidicon2(Option... options) {
        return format("vidicon-2", options);
    }

    public static String vimeo(Option... options) {
        return format("vimeo", options);
    }

    public static String vip(Option... options) {
        return format("vip", options);
    }

    public static String vipCrown(Option... options) {
        return format("vip-crown", options);
    }

    public static String vipCrown2(Option... options) {
        return format("vip-crown-2", options);
    }

    public static String vipDiamond(Option... options) {
        return format("vip-diamond", options);
    }

    public static String virus(Option... options) {
        return format("virus", options);
    }

    public static String visa(Option... options) {
        return format("visa", options);
    }

    public static String vk(Option... options) {
        return format("vk", options);
    }

    public static String voiceAi(Option... options) {
        return format("voice-ai", options);
    }

    public static String voiceRecognition(Option... options) {
        return format("voice-recognition", options);
    }

    public static String voiceprint(Option... options) {
        return format("voiceprint", options);
    }

    public static String volumeDown(Option... options) {
        return format("volume-down", options);
    }

    public static String volumeMute(Option... options) {
        return format("volume-mute", options);
    }

    public static String volumeOffVibrate(Option... options) {
        return format("volume-off-vibrate", options);
    }

    public static String volumeUp(Option... options) {
        return format("volume-up", options);
    }

    public static String volumeVibrate(Option... options) {
        return format("volume-vibrate", options);
    }

    public static String vuejs(Option... options) {
        return format("vuejs", options);
    }

    public static String walk(Option... options) {
        return format("walk", options);
    }

    public static String wallet(Option... options) {
        return format("wallet", options);
    }

    public static String wallet2(Option... options) {
        return format("wallet-2", options);
    }

    public static String wallet3(Option... options) {
        return format("wallet-3", options);
    }

    public static String waterFlash(Option... options) {
        return format("water-flash", options);
    }

    public static String waterPercent(Option... options) {
        return format("water-percent", options);
    }

    public static String webcam(Option... options) {
        return format("webcam", options);
    }

    public static String webhook(Option... options) {
        return format("webhook", options);
    }

    public static String wechat(Option... options) {
        return format("wechat", options);
    }

    public static String wechat2(Option... options) {
        return format("wechat-2", options);
    }

    public static String wechatChannels(Option... options) {
        return format("wechat-channels", options);
    }

    public static String wechatPay(Option... options) {
        return format("wechat-pay", options);
    }

    public static String weibo(Option... options) {
        return format("weibo", options);
    }

    public static String weight(Option... options) {
        return format("weight", options);
    }

    public static String whatsapp(Option... options) {
        return format("whatsapp", options);
    }

    public static String wheelchair(Option... options) {
        return format("wheelchair", options);
    }

    public static String wifi(Option... options) {
        return format("wifi", options);
    }

    public static String wifiOff(Option... options) {
        return format("wifi-off", options);
    }

    public static String window(Option... options) {
        return format("window", options);
    }

    public static String window2(Option... options) {
        return format("window-2", options);
    }

    public static String windows(Option... options) {
        return format("windows", options);
    }

    public static String windy(Option... options) {
        return format("windy", options);
    }

    public static String wirelessCharging(Option... options) {
        return format("wireless-charging", options);
    }

    public static String women(Option... options) {
        return format("women", options);
    }

    public static String wordpress(Option... options) {
        return format("wordpress", options);
    }

    public static String xbox(Option... options) {
        return format("xbox", options);
    }

    public static String xing(Option... options) {
        return format("xing", options);
    }

    public static String xrp(Option... options) {
        return format("xrp", options);
    }

    public static String xtz(Option... options) {
        return format("xtz", options);
    }

    public static String youtube(Option... options) {
        return format("youtube", options);
    }

    public static String yuque(Option... options) {
        return format("yuque", options);
    }

    public static String zcool(Option... options) {
        return format("zcool", options);
    }

    public static String zhihu(Option... options) {
        return format("zhihu", options);
    }

    public static String zoomIn(Option... options) {
        return format("zoom-in", options);
    }

    public static String zoomOut(Option... options) {
        return format("zoom-out", options);
    }

    public static String zzz(Option... options) {
        return format("zzz", options);
    }

    /************************************************************************
     * Method builder
     ************************************************************************/

    /**
     * Mechanism to interrogate the Remix Icon CSS file to regenerate the
     * typed icon methods. Run as a Java application — it prints method
     * declarations to {@code stdout} which can be pasted into this class
     * (replacing the existing block between the {@code Icons} and
     * {@code Method builder} markers).
     * <p>
     * Mirrors {@link FontAwesome.FontAwesomeGenerator} in structure.
     */
    @JuiIncompatible
    @GwtIncompatible
    public static class RemixIconGenerator {

        /**
         * Generates methods from {@code remixicon.css}.
         */
        public static void main(String... args) throws Exception {
            // Java reserved words that must not be used as method names.
            Set<String> reserved = new HashSet<> ();
            for (String w : ("abstract assert boolean break byte case catch char class const continue default "
                    + "do double else enum extends final finally float for goto if implements import "
                    + "instanceof int interface long native new package private protected public return short "
                    + "static strictfp super switch synchronized this throw throws transient try void volatile "
                    + "while true false null").split (" "))
                reserved.add (w);

            // Collect unique icon base names (deduplicated across -line / -fill variants).
            Set<String> names = new TreeSet<> ();
            try (InputStream is = RemixIconGenerator.class.getResourceAsStream ("remixicon.css")) {
                BufferedReader br = new BufferedReader (new InputStreamReader (is));
                String line;
                while ((line = br.readLine ()) != null) {
                    line = line.trim ();
                    if (!line.startsWith (".ri-"))
                        continue;
                    int beforeIdx = line.indexOf (":before");
                    if (beforeIdx < 0)
                        continue;
                    String cls = line.substring (1, beforeIdx); // strip leading '.'
                    String base;
                    if (cls.endsWith ("-line"))
                        base = cls.substring (3, cls.length () - 5);
                    else if (cls.endsWith ("-fill"))
                        base = cls.substring (3, cls.length () - 5);
                    else
                        continue; // skip utility classes like .ri-lg, .ri-fw, etc.
                    if (base.isEmpty ())
                        continue;
                    names.add (base);
                }
            }

            for (String name : names) {
                String method = toCamelCase (name);
                if (Character.isDigit (method.charAt (0)))
                    method = "_" + method;
                if (reserved.contains (method))
                    method = method + "_";
                System.out.println ("    public static String " + method + "(Option... options) {");
                System.out.println ("        return format(\"" + name + "\", options);");
                System.out.println ("    }");
                System.out.println ();
            }
        }

        private static String toCamelCase(String name) {
            StringBuilder out = new StringBuilder ();
            boolean first = true;
            for (String part : name.split ("-")) {
                if (part.isEmpty ())
                    continue;
                if (first) {
                    out.append (part);
                    first = false;
                } else {
                    out.append (StringUtils.capitalize (part));
                }
            }
            return out.toString ();
        }
    }
}

