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
package com.effacy.jui.ui.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.effacy.jui.core.client.component.IBindable;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.json.client.Serializer;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.effacy.jui.ui.client.FileDownloader.FileDownloaderConfig.ProblemType;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLFrameElement;
import elemental2.dom.XMLHttpRequest;

/**
 * Provides a mechansim to faciliate downloading files (more specifically, ones
 * that require generation which could be long running) with visual notification
 * of downloads in progress (and without opening a separate window).
 * <p>
 */
public final class FileDownloader {

    /************************************************************************
     * Configuration
     ************************************************************************/

    /**
     * Callback used by {@link IFileDownloaderNotifier} to interact with the
     * download mechanism.
     */
    public interface  IFileDownloader {
    
        /**
         * Cancels all downloads in progress (contingent on {@link #canCancel()} being
         * {@code true}).
         */
        public void cancel();
    }

    /**
     * Contract expected for interacting with a visual indicator of downloads in
     * progress.
     */
    public interface IFileDownloaderNotifier extends IBindable {
        
        /**
         * Registers the callback handler.
         * 
         * @param downloader
         *                   the handler.
         */
        public void register(IFileDownloader downloader);

        /**
         * Refresh the display for the passed downloads in progress.
         * 
         * @param downloads
         *                  the downloads.
         */
        public void refresh(List<IFileDownloadHandler> downloads);
    }

    /**
     * Configuration for the downloading mechanism.
     */
    public static class FileDownloaderConfig {

        public enum ProblemType {
            GENERAL, SAVE, NOT_FOUND, LOSS_OF_SESSION;
        }

        @FunctionalInterface
        public interface IProblemMapper {

            public String generate(ProblemType problem, String type);
        }

        /**
         * See {@link #createNotifier(Supplier)}.
         */
        private Supplier<IFileDownloaderNotifier> createNotifier;

        /**
         * See {@link #errorMessage(Function)}.
         */
        private IProblemMapper problemMapper;

        /**
         * See {@link #configureContainer(JQuery).
         */
        private Consumer<JQuery> configureContainer;

        /**
         * See {@link #messageHeader(String)}.
         */
        private String messageHeader = "Message";

        /**
         * See {@link #retrieveHeader(String)}.
         */
        private String retrieveHeader = "RetrieveLocation";

        /**
         * See {@link #pollingStatus(int)}.
         */
        private int pollingStatus = 202;

        /**
         * See {@link #pollingHeader(String)}.
         */
        private String pollingHeader = "RetrieveLocation";

        /**
         * See {@link #lossOfSessionStatus(int)}.
         */
        private int lossOfSessionStatus = 403;

        /**
         * See {@link lossOfSessionUrl(Function<String,Boolean>)}.
         */
        private Function<String,Boolean> lossOfSessionUrl;

        /**
         * Provide a notifier that provides a visual indicator when files are being
         * downloaded.
         * <p>
         * If not supplied a default is used.
         */
        public FileDownloaderConfig createNotifier(Supplier<IFileDownloaderNotifier> createNotifier) {
            this.createNotifier = createNotifier;
            return this;
        }

        /**
         * Given a problem description, generates a suitable message to display.
         * <p>
         * It is safe to return {@code null} in which case the default message for the
         * specified conditions will be used.
         * 
         * @param problemMapper
         *                      to map to a problem message.
         * @return this configuration instance.
         */
        public FileDownloaderConfig problemMapper(IProblemMapper problemMapper) {
            this.problemMapper = problemMapper;
            return this;
        }

        /**
         * Provide additional configuration to the container that holds the notifier.
         * 
         * @param configureContainer
         *                           the configuration to apply.
         * @return this configuration instance.
         */
        public FileDownloaderConfig configureContainer(Consumer<JQuery> configureContainer) {
            this.configureContainer = configureContainer;
            return this;
        }

        /**
         * Assigns the header to look for any error message.
         * <p>
         * The default is {@code Message}.
         * 
         * @param messageHeader
         *                      the header.
         * @return this configuration instance.
         */
        public FileDownloaderConfig messageHeader(String messageHeader) {
            if (messageHeader != null)
                this.messageHeader = messageHeader;
            return this;
        }

        /**
         * Assigns the header to look for any location to retrieve content from. This
         * acts like a redirect to allow such behaviours such as returning a signed URL.
         * <p>
         * The default is {@code RetrieveLocation}.
         * 
         * @param retrieveHeader
         *                       the header.
         * @return this configuration instance.
         */
        public FileDownloaderConfig retrieveHeader(String retrieveHeader) {
            if (retrieveHeader != null)
                this.retrieveHeader = retrieveHeader;
            return this;
        }

        /**
         * Assigns the status code to inform the mechanism that polling is required
         * (i.e. not ready).
         * <p>
         * The default is 202.
         * 
         * @param pollingStatus
         *                      the status code.
         * @return this configuration instance.
         */
        public FileDownloaderConfig pollingStatus(int pollingStatus) {
            this.pollingStatus = pollingStatus;
            return this;
        }

        /**
         * Assigns the header to look for the url to use when polling (i.e. passed when
         * the status code is {@link #pollingStatus(int)}).
         * <p>
         * The default is {@code RetrieveLocation}.
         * 
         * @param pollingHeader
         *                      the header.
         * @return this configuration instance.
         */
        public FileDownloaderConfig pollingHeader(String pollingHeader) {
            if (pollingHeader != null)
                this.pollingHeader = pollingHeader;
            return this;
        }

        /**
         * Assigns the status code to inform the mechanism that session has been lost.
         * <p>
         * The default is 403.
         * 
         * @param lossOfSessionStatus
         *                            the status code.
         * @return this configuration instance.
         */
        public FileDownloaderConfig lossOfSessionStatus(int lossOfSessionStatus) {
            this.lossOfSessionStatus = lossOfSessionStatus;
            return this;
        }

        /**
         * An additional check (other than {@link #lossOfSessionStatus(int)}) to
         * determine if a loss of session has occurred by inspecting the reponse URL
         * from the XHR session.
         * <p>
         * Often a loss of session is enacted by a redirection to a login page. This can
         * be used to detect that and abort.
         * 
         * @param lossOfSessionUrl
         *                         the interrogator (should return {@code true} if the
         *                         passed responnse url is a login page).
         * @return this configuration instance.
         */
        public FileDownloaderConfig lossOfSessionUrl(Function<String,Boolean> lossOfSessionUrl) {
            this.lossOfSessionUrl = lossOfSessionUrl;
            return this;
        }

        /**
         * Obtains a notifier instance.
         */
        IFileDownloaderNotifier createNotifier() {
            if (createNotifier != null)
                return createNotifier.get();
            return new FileDownloaderNotifier();
        }

        /**
         * Generates an error message based on the given type.
         */
        String problemMapper(ProblemType problem, String type, String response) {
            if (problemMapper != null) {
                String message = problemMapper.generate(problem, type);
                if (message != null)
                    return message;
            }
            if (ProblemType.NOT_FOUND == problem) {
                if (!StringSupport.empty(response))
                    return "The " + type + " is no longer available due to \"" + response + "\""; 
                return "It looks like the " + type + " may have failed or has taken too long to process.";
            }
            if (ProblemType.SAVE == problem)
                return "Seems that there was a problem.  Please try again or contact customer service.";
            if (ProblemType.LOSS_OF_SESSION == problem)
                return "It looks like you have been logged out after a period of inactivity.";
            if (!StringSupport.empty(response))
                return "There was a problem generating the " + type + " due to \"" + response + "\"";        
            return "It looks like there was a problem and the " + type + " was not able to be generated.  Please try again later and if the problem persists contact customer support.";
        }

        /**
         * Provides any additional configuration to the root element 
         */
        void configureContainer(JQuery el) {
            if (configureContainer != null)
                configureContainer.accept(el);
        }
     }

    /**
     * The confguration for the downloader.
     */
    private static FileDownloaderConfig CONFIG;

    /**
     * Provide configuration for the downloader. This can only be done once.
     * 
     * @param config
     *               the configuration to apply.
     */
    public static void config(FileDownloaderConfig config) {
        if (CONFIG == null)
            CONFIG = config;
    }

    /************************************************************************
     * Download methods (all static).
     ************************************************************************/

    /**
     * See {@link #pollable(String, String, Object)}.
     */
    public static void pollable(String url) {
        pollable(null, url, null);
    }

    /**
     * See {@link #pollable(String, String, Object)}.
     */
    public static void pollable(String type, String url) {
        pollable(type, url, null);
    }

    /**
     * Initiate a download that could be long running and is notified.
     * 
     * @param url
     *               the url to retrieve from.
     * @param config
     *               (optional) configuration data (must be serialisable).
     * @return a handler for the download.
     */
    public static void pollable(String url, Object config) {
        pollable (null, url, config);
    }

    /**
     * Initiate a download that could be long running and is notified.
     * <p>
     * If configuration data is passed then thuis can be a string (and will be
     * passed as is) or some object. If it is an object it must be serialisable to
     * JSON (using the JUI serialiser).
     * 
     * @param type
     *               the display type for the file being download (if {@code null}
     *               this will be treated as "file").
     * @param url
     *               the url to retrieve from.
     * @param config
     *               (optional) configuration data (must be serialisable).
     * @return a handler for the download.
     */
    public static void pollable(String type, String url, Object config) {
        if (type == null)
            type = "file";
        if (DOWNLOADER == null)
            DOWNLOADER = new FileDownloader((CONFIG != null) ? CONFIG : new FileDownloaderConfig());
        DOWNLOADER.start (type, url, config);
    }

    /**
     * See {@link #download(String, int)}. Default time is 60s.
     */
    public static void download(String url) {
        download (url, 60);
    }

    /**
     * Downloads a file without any notifier or polling ability. This is the
     * simplest approach without opening another window (it is down in dynamically
     * created iframe).
     * 
     * @param url
     *                the URL to download from.
     * @param seconds
     *                the number of seconds to keep the iframe around before
     *                removing it (so should well larger than the length of time it
     *                takes to download).
     */
    public static void download(String url, int seconds) {
        HTMLFrameElement frame = createFrame();
        CSS.WIDTH.apply(frame, Length.px (0));
        CSS.HEIGHT.apply(frame, Length.px (0));
        frame.src = url;
        DomGlobal.document.body.append(frame);
        TimerSupport.timer(() -> {
            frame.remove();
        }).schedule(seconds * 1000);
    }

    /************************************************************************
     * Implementation
     ************************************************************************/
    
    /**
     * Constructs an IFrame element (detached).
     * 
     * @return the element.
     */
    private native static HTMLFrameElement createFrame()
    /*-{
        return $wnd.document.createElement('iframe');
    }-*/;

    /**
     * The file downloader to use.
     */
    private static FileDownloader DOWNLOADER;

    /**
     * Collection of active downloaders (these are ones that are not using the
     * simple framework).
     */
    private List<IFileDownloadHandler> downloads = new ArrayList<IFileDownloadHandler> ();

    /**
     * Configuration for the downloader.
     */
    private FileDownloaderConfig config;

    /**
     * Construct instance with configuration.
     * 
     * @param config
     *               the configuration.
     */
    FileDownloader(FileDownloaderConfig config) {
        this.config = config;
    }

    /**
     * Starts a download. See {@link #pollable(String, String, Object)} for
     * arguments.
     */
    void start(String type, String url, Object config) {
        downloads.add (new FileDownloadHandler(type, url, config));
        show ();
        refresh ();
    }

    /**
     * Removes the download from the processing list (added by
     * {@link #start(String, String, Object)}).
     * 
     * @param download
     *                 the download to remove.
     */
    void remove(IFileDownloadHandler download) {
        if (downloads.isEmpty ())
            return;
        downloads.remove (download);
        refresh ();
        if (downloads.isEmpty ())
            hide ();
    }

    /**
     * Refreshes the list of downloads against the notifier.
     */
    void refresh() {
        if (notifier != null)
            notifier.refresh (downloads);
    }

    /**
     * Represents a single download in progress. Provides support for visual
     * notification as well as polling.
     */
    class FileDownloadHandler implements IFileDownloadHandler {

        /**
         * The number of times the downloader has polled.
         */
        private int pollCount;

        /**
         * Determines if the download can be cancelled.
         */
        private boolean canCancel;

        /**
         * Mark as cancelled (stops polling).
         */
        private boolean cancelled;

        /**
         * Display type for the what is being downloaded.
         */
        private String type;

        /**
         * Construct with retrieval data. See
         * {@link FileDownloader#pollable(String, String, Object)} for arguments.
         */
        FileDownloadHandler(String type, String url, Object generationConfig) {
            this.type = type;
            download (url, generationConfig);
        }

        /**
         * Performs a download.
         * 
         * @param url
         *                         the target URL.
         * @param generationConfig
         *                         any additional configuration that should be passed in
         *                         the body.
         */
        void download(String url, Object generationConfig) {
            XMLHttpRequest xhr = new XMLHttpRequest();
            xhr.responseType = "blob";
            xhr.onreadystatechange = e -> {
                if (xhr.readyState != 4)
                    return null;

                // Detect a loss of session. We do this here as the loss of session url check
                // may be perform on a successful outcome.
                if ((xhr.status == config.lossOfSessionStatus) || ((config.lossOfSessionUrl != null) && (xhr.responseURL != null) && config.lossOfSessionUrl.apply(xhr.responseURL))) {
                    alert ("Loss of session", config.problemMapper(ProblemType.LOSS_OF_SESSION, type, null));
                    remove ();
                    return null;
                }

                // Successful outcome. A location may be provided in the {@link
                // FileDownloaderConfig#retrieveHeader} header.
                if (xhr.status == 200) {
                    String retrieve = xhr.getResponseHeader (config.retrieveHeader);
                    if (!StringSupport.empty (retrieve))
                        download (retrieve, null);
                    else 
                        save (xhr, url);
                    remove ();
                    return null;
                }

                // Check if not ready and polling is required. A location to poll must be
                // provided in the {@link FileDownloaderConfig#pollingHeader} header.
                if (xhr.status == config.pollingStatus) {
                    String reference = xhr.getResponseHeader (config.pollingHeader);
                    if (!StringSupport.empty (reference)) {
                        poll (reference);
                        return null;
                    }
                }

                // Deal with not found or gone (404 and 410).
                if ((xhr.status == 404) || (xhr.status == 410)) {
                    String response = xhr.getResponseHeader (config.messageHeader);
                    alert ("There was a problem", config.problemMapper(ProblemType.NOT_FOUND, type, response));
                    remove ();
                    return null;
                }

                // Deal with a non-specific error condition. Attempt to resolve an error message
                // from the {@link FileDownloaderConfig#messageHeader} header.
                try {
                    String response = xhr.getResponseHeader (config.messageHeader);
                    alert ("There was a problem", config.problemMapper(ProblemType.GENERAL, type, response));
                } finally {
                    remove ();
                }
                return null;
            };
            if (generationConfig != null) {
                xhr.open ("POST", url, true);
                xhr.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
                xhr.setRequestHeader("Accept", "application/json");
                String data;
                if (generationConfig instanceof String)
                    data = (String) generationConfig;
                else
                    data = Serializer.getInstance().serialize(generationConfig);
                xhr.send (data);
            } else {
                xhr.open ("GET", url, true);
                xhr.send ();
            }
        }

        @Override
        public boolean canCancel() {
            return canCancel;
        }

        @Override
        public int pollCount() {
            return pollCount;
        }

        @Override
        public void cancel() {
            if (!canCancel)
                return;
            cancelled = true;
            remove ();
        }

        @Override
        public String type() {
            return type;
        }

        protected void remove() {
            FileDownloader.this.remove (this);
        }

        protected void poll(String url) {
            // If this has been cancelled, then we slip out of the polling flow.
            if (cancelled)
                return;

            // Used to keep track of the number of polling attempts.
            pollCount++;

            // Since we are polling then the downloader can be cancelled. Change
            // the cancellation state and refresh the notifier.
            canCancel = true;
            FileDownloader.this.refresh ();

            // 2 seconds is the default but after 10s extends to 4s (so as not
            // to load things down too much).
            int seconds = 2;
            if (pollCount > 5)
                seconds = 4;
            TimerSupport.timer(() -> {
                // If cancelled during the timer run, the fall out.
                if (cancelled)
                    return;
                download (url, null);
            }).schedule (seconds * 1000);
        }

        /**
         * Saves a file using the passed XHR response.
         * 
         * @param xhr
         *            the reponse to extract the file from.
         */
        protected void save(XMLHttpRequest xhr, String url) {
            if (cancelled)
                return;
            String filename = xhr.getResponseHeader ("Content-Disposition");
            try {
                if (filename == null) {
                    // Take the last component of the URL.
                    int i = url.lastIndexOf('/');
                    if (i >= 0)
                        filename = url.substring (url.lastIndexOf ('/') + 1);
                } else {
                    // Strip off any attachemnt part.
                    int i = filename.indexOf("filename=");
                    if (i >= 0)
                        filename = filename.substring(i + 9);
                    // Strip off any quotes.
                    if (filename.startsWith("\""))
                        filename = filename.substring(1);
                    if (filename.endsWith("\""))
                        filename = filename.substring(0, filename.length() - 1);
                }
                Element el = DomGlobal.document.createElement("a");
                try {
                    save (el, xhr, filename, xhr.getResponseHeader ("Content-Type"));
                } catch (Throwable e) {
                    alert ("Problem downloading " + type, config.problemMapper(ProblemType.SAVE, type, null));
                    Logger.reportUncaughtException(e);
                }
                TimerSupport.timer(() -> {
                    el.remove();
                }).schedule (20 * 1000);
            } catch (Throwable e) {
                alert ("Problem downloading " + type, config.problemMapper(ProblemType.SAVE, type, null));
                Logger.reportUncaughtException(e);
            }
        }

        protected native void save(Element anchor, XMLHttpRequest xhr, String filename, String contentType)
        /*-{
            var blob = new Blob([ xhr.response ], { type : contentType });
            anchor.download = filename;
            var url = null;
            try {
                var URL = $wnd.URL || $wnd.webkitURL;
                url = URL.createObjectURL(blob);
                anchor.href = url;
            } catch (er) {
                anchor.href = blob;
            }
            anchor.dispatchEvent(new MouseEvent('click'));
            if (url != null) {
                setTimeout(function() { URL.revokeObjectURL(url); }, 100);
            }
        }-*/;

        private void alert(String title, String message) {
            NotificationDialog.alert (title, message, e -> {});
        }
    }

    /************************************************************************
     * Visual notification.
     ************************************************************************/

    public interface IFileDownloadHandler {
        public boolean canCancel();
        public void cancel();
        public int pollCount();
        public String type();
    }

    /**
     * Registers an alternative notifier.
     * 
     * @param notifier
     *                 the notifier instance.
     */
    public static void notifier(IFileDownloaderNotifier notifier) {
        if (DOWNLOADER.notifier == null) {
            DOWNLOADER.notifier = notifier;
            DOWNLOADER.notifier.register(new IFileDownloader() {

                @Override
                public void cancel() {
                    List<IFileDownloadHandler> tmpLoaders = new ArrayList<> (DOWNLOADER.downloads);
                    DOWNLOADER.downloads.clear ();
                    DOWNLOADER.hide ();
                    tmpLoaders.forEach(loader -> loader.cancel());
                }

            });
        }
    }

    /**
     * The notifier component (generally a component).
     */
    private IFileDownloaderNotifier notifier;

    /**
     * The element attached to the document body that holds the notifier component
     * (see {@link #notifier}).
     */
    private Element notifierEl;
    
    /**
     * Hides the notifier.
     */
    private void hide() {
        if (notifierEl != null)
            JQuery.$ (notifierEl).css("visibility", "hidden").css("opacity", "0");
    }

    /**
     * Shows the notifier.
     */
    private void show() {
        if (notifierEl == null) {
            if (notifier == null)
                notifier (new FileDownloaderNotifier());
            notifierEl = DomGlobal.document.createElement("div");
            notifierEl.id = "id-file-downloader";
            JQuery.$ (notifierEl)
                .css("visibility", "hidden")
                .css("position", "fixed")
                .css("bottom", "10px")
                .css("left", "10px")
                .css("opacity", "0")
                .css ("transition", "opacity 0.5s ease")
                .css("z-index", "9000000");
            DomGlobal.document.body.append(notifierEl);
            notifier.bind("id-file-downloader");
        }
        JQuery.$(notifierEl).css("visibility", "visible").css("opacity", "1");
    }

    /************************************************************************
     * Default notification component.
     ************************************************************************/

    static class FileDownloaderNotifier extends SimpleComponent implements IFileDownloaderNotifier {
        private IFileDownloader downloader;
        private int lastDownloadCount = 0;
        private Element messageEl;

        public FileDownloaderNotifier() {
            renderer(root -> {
                P.$(root).$(
                    Em.$().style (FontAwesome.spinner(FontAwesome.Option.SPIN)),
                    Span.$().by ("message"),
                    Em.$().style (FontAwesome.times(), styles().cancel()).onclick(e -> {
                        downloader.cancel();
                    })
                );
            }, dom -> {
                messageEl = dom.first("message");
            });
        }
        public void register(IFileDownloader downloader) {
            this.downloader = downloader;
        }

        public void refresh(List<IFileDownloadHandler> downloads) {
            boolean reducing = (downloads.size() < lastDownloadCount);
            lastDownloadCount = downloads.size();

            // If not rendered then don't worry.
            if (!isRendered())
                return;

            // If empty we don't actually need to do anything (it will be hidden).
            if (downloads.isEmpty ())
                return;
        
            // Generate message.
            String type = null;
            for (IFileDownloadHandler download : downloads) {
                if (type == null) {
                    type = download.type();
                } else if (!type.equals (download.type())) {
                    type = "item";
                    break;
                }
            }
            String message = null;
            if (reducing) {
                message = "Downloading last " + type + " ...";
                if (lastDownloadCount > 0)
                    message = "Downloading remaining " + lastDownloadCount + " " + type + "s ...";
            } else {
                message = "Downloading " + type + " ...";
                if (lastDownloadCount > 1)
                    message = "Downloading " + lastDownloadCount + " " + type +  "s ...";
            }

            // Determine if the entire body can be cancelled.
            boolean canCancel = true;
            for (IFileDownloadHandler download : downloads) {
                if (!download.canCancel()) {
                    canCancel = false;
                    break;
                }
            }

            // Render content.
            messageEl.textContent = message;
            if (canCancel)
                getRoot().classList.add(styles().cancel());
            else
                getRoot().classList.remove(styles().cancel());
        }

        /********************************************************************
         * CSS
         ********************************************************************/

        @Override
        protected ILocalCSS styles() {
            return FileDownloaderNotifierCSS.instance();
        }

        public static interface ILocalCSS extends IComponentCSS {

            public String cancel();

        }

        /**
         * Component CSS (standard pattern).
         */
        @CssResource({
            IComponentCSS.COMPONENT_CSS,
            "com/effacy/jui/ui/client/FileDownloaderNotifier.css",
            "com/effacy/jui/ui/client/FileDownloaderNotifier_Override.css"
        })
        public static abstract class FileDownloaderNotifierCSS implements ILocalCSS {

            private static FileDownloaderNotifierCSS STYLES;

            public static ILocalCSS instance() {
                if (STYLES == null) {
                    STYLES = (FileDownloaderNotifierCSS) GWT.create (FileDownloaderNotifierCSS.class);
                    STYLES.ensureInjected ();
                }
                return STYLES;
            }
        }
    }
}
