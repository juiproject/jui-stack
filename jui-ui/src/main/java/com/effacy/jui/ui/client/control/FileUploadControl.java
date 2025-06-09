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
package com.effacy.jui.ui.client.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.control.Control;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.A;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.Input;
import com.effacy.jui.core.client.dom.builder.Text;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.file.IFileUploader;
import com.effacy.jui.core.client.file.IFileUploader.IFileUploaderListener;
import com.effacy.jui.core.client.file.IFileUploader.IFileUploaderListener.FailureType;
import com.effacy.jui.core.client.util.SizeSupport;
import com.effacy.jui.core.client.util.UID;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.ComparisonSupport;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.control.FileUploadControl.FileAttachment;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import elemental2.dom.File;
import elemental2.dom.HTMLInputElement;

public class FileUploadControl extends Control<List<FileAttachment>, FileUploadControl.Config> {

    /**
     * The default style to employ when one is not assign explicitly.
     */
    public static Config.Style DEFAULT_STYLE = Config.Style.STANDARD;

    /**
     * Models a file (attachment for an existing file).
     */
    public static class FileAttachment {

        public enum Status {
            CREATED, DELETED, EXISTING;
        }

        /**
         * See {@link #getStatus()}.
         * <p>
         * This is assigned on creation or transitioned internally.
         */
        private Status status;

        /**
         * When the attachment is supplied but is in error. This could occur due to a
         * previous upload working fine, creating an attachment, but later the
         * attachment is determined invalid (i.e does not pass an anti-virus check). The
         * record of it is retained and reported but the underlying attachment is no
         * longer accessible.
         */
        private boolean error;

        /**
         * The reason why it is in error.
         */
        private String errorMessage;

        /**
         * See {@link #getId()}.
         */
        private Object id;

        /**
         * See {@link #getName()}.
         */
        private String name;

        /**
         * See {@link #getSize()}.
         */
        private int size;

        /**
         * See {@link #getType()}.
         */
        private String type;
        
        /**
         * See {@link #getReference()}.
         * <p>
         * This is assigned only during creation.
         */
        private String reference;
        
        FileAttachment(FileAttachment copy) {
            this.id = copy.id;
            this.status = copy.status;
            this.error = copy.error;
            this.errorMessage = copy.errorMessage;
            this.id = copy.id;
            this.name = copy.name;
            this.size = copy.size;
            this.type = copy.type;
            this.reference = copy.reference;
        }

        /**
         * Construct a new (existing) file attachment.
         * <p>
         * The status is set to {@link Status#EXISTING}.
         * 
         * @param id
         *             the ID to refer to it by.
         * @param name
         *             the display name.
         * @param type
         *             (optional) the content type.
         * @param size
         *             the size in bytes (-1 if not known).
         */
        public FileAttachment(Object id, String name, String type, int size) {
            this.id = id;
            this.name = name;
            this.size = size;
            this.type = type;
            this.status = Status.EXISTING;
        }

        /**
         * Construct a new (existing) file attachment but one that is in error.
         * <p>
         * The status is set to {@link Status#EXISTING_ERROR}.
         * 
         * @param id
         *                     the ID to refer to it by.
         * @param name
         *                     the display name.
         * @param errorMessage
         *                     (optional) supporting message.
         */
        public FileAttachment(Object id, String name, String errorMessage) {
            this.id = id;
            this.name = name;
            this.error = true;
            this.errorMessage = errorMessage;
            this.status = Status.EXISTING;
        }

        /**
         * For internal use. Creates a new file attachment having been uploaded.
         */
        FileAttachment(File file, String reference) {
            this.name = file.name;
            this.size = file.size;
            this.type = file.type;
            this.reference = reference;
            this.status = Status.CREATED;
        }

        /**
         * The status of the file attachment.
         * <p>
         * When created externally and added this is always {@link Status#EXISTING}.
         * 
         * @return the status.
         */
        public Status getStatus() {
            return status;
        }

        /**
         * The ID of the file attachment.
         * <p>
         * Only valid for prior-existing attachments.
         * 
         * @return the ID.
         */
        public Object getId() {
            return id;
        }

        /**
         * The name of the file attachment.
         * 
         * @return the name.
         */
        public String getName() {
            return name;
        }

        /**
         * The size of the file attachment in bytes.
         * 
         * @return the size.
         */
        public long getSize() {
            return size;
        }

        /**
         * The content type.
         * 
         * @return the type in standard format.
         */
        public String getType() {
            return type;
        }

        /**
         * A reference to an uploaded (new) attachment.
         * <p>
         * This is the reference passed back from the {@link IFileUploader} assigned to
         * the control.
         * 
         * @return the reference.
         */
        public String getReference() {
            return reference;
        }

        @Override
        public int hashCode() {
            if (id != null)
                return id.hashCode ();
            if (reference != null)
                return reference.hashCode();
            if (name != null)
                return name.hashCode();
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (this == obj)
                return true;
            if (!(obj instanceof FileAttachment))
                return false;
            FileAttachment castObj = (FileAttachment) obj;
            // If the status' are not equals then they are deemed difference (even if the
            // ID's or references match). This is due to the fact that the status is the
            // only variable than can be changed on an attachment (i.e. when it is deleted).
            if (status != castObj.status)
                return false;
            // Only one of id or reference will be non-null. A match on any, then, is an
            // equality.
            if ((id != null) && ComparisonSupport.equal (id, castObj.id))
                return true;
            if ((reference != null) && ComparisonSupport.equal (reference, castObj.reference))
                return true;
            return false;
        }
    }

    public static class Config extends Control.Config<List<FileAttachment>, Config> {

        /********************************************************************
         * Styles for the tab set.
         ********************************************************************/

        /**
         * Style for the tab set (defines presentation configuration including CSS).
         */
        public interface Style {

            /**
             * The CSS styles.
             */
            public ILocalCSS styles();

            /**
             * The number of files to display full-size until rendered compactly.
             */
            public int smallLimit();

            /**
             * Convenience to create a style.
             * 
             * @param styles
             *                     the CSS styles.
             * @param selectorIcon
             *                     the CSS class to use for the selector icon.
             * @return the associated style.
             */
            public static Style create(final ILocalCSS styles, final int smallLimit) {
                return new Style () {

                    @Override
                    public ILocalCSS styles() {
                        return styles;
                    }

                    @Override
                    public int smallLimit() {
                        return smallLimit;
                    }

                };
            }

            /**
             * Standard style.
             */
            public static final Style STANDARD = Style.create (StandardLocalCSS.instance (), 5);

            /**
             * Compact style.
             */
            public static final Style COMPACT = Style.create (CompactLocalCSS.instance (), 1);

        }

        /**
         * The styles to apply to the tab set.
         */
        protected Style style = (DEFAULT_STYLE != null) ? DEFAULT_STYLE : Style.STANDARD;

        /**
         * See {@link #uploader(IFileUploader)}.
         */
        private IFileUploader uploader;

        /**
         * See {@link #limit(int)}.
         */
        private int limit = 0;

        /**
         * See {@link #includeRemoved(boolean)}.
         */
        private boolean includeRemoved;

        /**
         * See {@link #fileValidator(Function)}.
         */
        private Function<File,Optional<String>> fileValidator;

        private BiFunction<List<FileAttachmentItem>,List<FileAttachment>,String> messageProvider;

        /**
         * Assigns a different style.
         * 
         * @param style
         *              the style.
         * @return this configuration.
         */
        public Config style(Style style) {
            if (style != null)
                this.style = style;
            return this;
        }

        /**
         * Provides an uploader to employ.
         * 
         * @param uploader
         *                       the uploader to use.
         * @return this configuration.
         */
        public Config uploader(IFileUploader uploader) {
            this.uploader = uploader;
            return this;
        }

        /**
         * Provides a limit to the number of files that can be presented by the control
         * (included uploaded).
         * <p>
         * If any assignment exceeded the limit, it will be truncated.
         * 
         * @param limit
         *              the maximum number of files.
         * @return this configuration.
         */
        public Config limit(int limit) {
            this.limit = limit;
            return this;
        }

        /**
         * If removed (prior existing) items should be included in the returned value
         * (but marked as removed).
         * <p>
         * Generally this is easier to process.
         * 
         * @param includeRemoved
         *                       {@code true} if removed (prior existing) items should
         *                       be included in the value and not removed from it.
         * @return this configuration.
         */
        public Config includeRemoved(boolean includeRemoved) {
            this.includeRemoved = includeRemoved;
            return this;
        }

        /**
         * A mechanism to validate the passed file allowing the return of an error
         * message (no message passively accepts the file).
         * <p>
         * This is addative (they will chain).
         * 
         * @param fileValidator
         *                      the validator to use.
         * @return this configuration.
         */
        public Config fileValidator(Function<File,Optional<String>> fileValidator) {
            if (this.fileValidator == null) {
                this.fileValidator = fileValidator;
            } else {
                Function<File,Optional<String>> prior = this.fileValidator;
                this.fileValidator = f -> {
                    Optional<String> v = prior.apply(f);
                    if ((v != null) && v.isPresent())
                        return v;
                    return fileValidator.apply (f);
                };
            }
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
         */
        @Override
        @SuppressWarnings("unchecked")
        public FileUploadControl build(LayoutData... data) {
            return build (new FileUploadControl (this), data);
        }
    }

    /************************************************************************
     * Construction.
     ************************************************************************/

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    public FileUploadControl(Config config) {
        super (config);
    }

    /************************************************************************
     * Value management.
     ************************************************************************/

    @Override
    protected List<FileAttachment> prepareValueForAssignment(List<FileAttachment> value) {
        if (value == null)
            return new ArrayList<>();
        return value;
    }

    @Override
    protected List<FileAttachment> prepareValueForRetrieval(List<FileAttachment> value) {
        return prepareValueForAssignment (value);
    }

    @Override
    protected List<FileAttachment> valueFromSource() {
        List<FileAttachment> files = new ArrayList<>();
        attachments.forEach (item -> {
            // Add in those completed items or those that are pre-existing (i.e. assigned).
            if ((FileAttachmentItemStatus.COMPLETE == item.status) || (FileAttachmentItemStatus.EXISTS == item.status))
                files.add (item.data);
        });
        if (config().includeRemoved) {
            removed.forEach (item -> {
                // Here we need to report the file attachment as deleted, which means modifying
                // it. In this case we need to take a copy so that it does not affect any
                // previously retained version (i.e the reset value).
                FileAttachment fa = new FileAttachment (item);
                fa.status = FileAttachment.Status.DELETED;
                files.add (fa);
            });
        }
        return files;
    }

    @Override
    protected void valueToSource(List<FileAttachment> value) {
        attachments.clear();
        removed.clear();
        if (value != null) {
            value.forEach (file -> {
                attachments.add (new FileAttachmentItem (file));
            });
        }
        renderAttachments ();
    }

    @Override
    protected void onBeforeReset() {
        super.onBeforeReset();

        // Clear any error records.
        if (attachments.removeIf (v -> v.error()))
            renderAttachments ();
    }

    /************************************************************************
     * Rendering.
     ************************************************************************/

    /**
     * The status of a {@link FileAttachmentItem}.
     */
    enum FileAttachmentItemStatus {
        EXISTS, PENDING, UPLOADING, ERROR, COMPLETE;
    }

    /**
     * Wrapper to represent a file attachment item in the list of files. This
     * manages rendering and upload.
     */
    class FileAttachmentItem {

        /**
         * Unique ID for reference.
         */
        private String uid = UID.createUID();

        /**
         * Status of the item.
         */
        private FileAttachmentItemStatus status;

        /**
         * Any message associated with the status.
         */
        private String statusMessage;

        /**
         * File attachment data. This is only present when passed through initially or
         * when a file has been successfully uploaded.
         */
        private FileAttachment data;

        /**
         * When uploaded this is the file.
         */
        private File file;

        /**
         * During file upload this is the percentage progress (where available).
         */
        private int percentage = 0;

        /**
         * The element associated with the item in the item list. Used to re-render
         * into.
         */
        private Element element;

        /**
         * Construct from an existing attachment (external).
         * 
         * @param data
         *             the attachment data.
         */
        FileAttachmentItem(FileAttachment data) {
            // It's OK to reference this here.
            this.data = data;
            this.status = FileAttachmentItemStatus.EXISTS;
        }

        /**
         * Construct as a file being uploaded. Initial state is
         * {@link FileAttachmentItemStatus#PENDING.
         * 
         * @param file
         *             the file to upload.
         */
        FileAttachmentItem(File file) {
            this.file = file;
            this.status = FileAttachmentItemStatus.PENDING;
        }

        /**
         * The display name based on the file or passed file attachment.
         * 
         * @return the file name.
         */
        public String name() {
            return (file != null) ? file.name : data.name;
        }

        /**
         * The size of the file based on the file itself or passed file attachment.
         * 
         * @return the file size (in bytes).
         */
        public int size() {
            return (file != null) ? file.size : data.size;
        }

        /**
         * If the item is in error. This could be due to a failed upload for a new item
         * OR an existing item that is in error.
         * 
         * @return {@code true} if in error.
         */
        public boolean error() {
            if (status == FileAttachmentItemStatus.ERROR)
                return true;
            if ((data != null) && data.error)
                return true;
            return false;
        }

        /**
         * Determines if this is an exiting attachment.
         * 
         * @return {@code true} if it is.
         */
        public boolean existing() {
            return (status == FileAttachmentItemStatus.EXISTS);
        }

        /**
         * Determines if this is a newly added attachment (must be complete).
         * 
         * @return {@code true} if it is.
         */
        public boolean added() {
            return (status == FileAttachmentItemStatus.COMPLETE);
        }

        /**
         * Refreshes the presentation based on updated data.
         */
        void refresh() {
            if (element != null)
                element.classList.remove (styles ().error ());
            Wrap.buildInto (element, el -> render (el));

            // In the complete state we fire a modified to ensure that the completed file is
            // registered.
            if (status == FileAttachmentItemStatus.COMPLETE)
                modified();

            // Update the message.
            renderMessage ();
        }

        /**
         * Renders into the given element builder.
         */
        void render(ElementBuilder el) {
            // Delegate to enclosing class allowing for override.
            renderAttachment (el, this);
        }

        /**
         * Starts upload.
         */
        void start() {
            if (FileAttachmentItemStatus.PENDING != status)
                return;
            status = FileAttachmentItemStatus.UPLOADING;
            if (config().uploader == null)
                throw new RuntimeException("File upload control has no uploader configured for it!");
            config ().uploader.send (file, IFileUploaderListener.create (ref -> {
                status = FileAttachmentItemStatus.COMPLETE;
                data = new FileAttachment (file, ref);
                refresh ();
            }, fail -> {
                status = FileAttachmentItemStatus.ERROR;
                if (fail == FailureType.ABORT)
                    statusMessage = "Upload was aborted";
                else if (fail == FailureType.TIMEOUT)
                    statusMessage = "Upload timed out";
                else
                    statusMessage = "Failed to upload";
                refresh ();
            }, prog -> {
                percentage = prog;
                refresh ();
            }));
        }
        
    }

    /**
     * All the current attachments (these will excluded prior added attachments that
     * have been removed).
     */
    private List<FileAttachmentItem> attachments = new ArrayList<>();

    /**
     * All attachments that have been added prior (to the control) but have since
     * been removed.
     */
    private List<FileAttachment> removed = new ArrayList<>();

    protected HTMLInputElement fileEl;

    protected Element listEl;

    protected Element messageEl;

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (root -> {
            // The main drop region.
            Div.$ (root).style (styles ().dropRegion ()).$ (dr -> {
                if (config().limit == 1)
                    dr.style (styles ().small ());
                ElementBuilder input = Input.$ (dr, "file").by ("file").onchange (e -> {
                    add (fileEl.files.asList ());
                    fileEl.value = null;
                });
                if (config().limit != 1)
                    input.attr ("multiple","multiple");
                Div.$ (dr).style (styles ().icon ()).$ (
                    Em.$ ().style (FontAwesome.cloudArrowUp ())
                );
                Div.$ (dr).style (styles ().title ()).$ (
                    A.$ ().text ("Click to upload").onclick (e -> fileEl.click ()),
                    Text.$ (" or drag and drop")
                );
                dr.on (e -> {
                    if (e.isEvent (UIEventType.DRAGENTER, UIEventType.DRAGOVER))
                         getRoot().classList.add (styles ().drag());
                    else
                        getRoot().classList.remove (styles ().drag());
                    e.preventDefault();
                }, UIEventType.DRAGENTER, UIEventType.DRAGOVER, UIEventType.DRAGLEAVE);
                dr.on (e -> {
                    if (e.getDataTransfer().files.length > 0)
                        add (e.getDataTransfer().files.asList());
                    getRoot().classList.remove (styles ().drag());
                    e.preventDefault();
                }, UIEventType.DROP);
            });
            if (config().limit > 1) {
                // The is the alternative drop region that is displayed when the file limit has
                // been exceeded. It is activated by CSS.
                Div.$ (root).style (styles ().dropRegion (), styles().limit()).$ (dr -> {
                    Div.$ (dr).style (styles ().icon ()).$ (
                        Em.$ ().style (FontAwesome.cloudArrowUp ())
                    );
                    Div.$ (dr).style (styles ().title ()).$ (
                        Text.$ ("You have reached the limit of " + config().limit + " files")
                    );
                });
            }
            Div.$ (root).by ("message").style (styles ().message ());
            Div.$ (root).style (styles().list()).by ("list").onclick (e -> {
                Element removeEl = e.getTarget("#remove", 3);
                if (removeEl == null)
                    removeEl = e.getTarget("#delete", 3);
                if (removeEl != null) {
                    String uid = removeEl.getAttribute ("item");
                    if (uid != null) 
                        remove (uid);
                }
            });
        }).build (dom -> {
            fileEl = dom.first ("file");
            listEl = dom.first ("list");
            messageEl = dom.first ("message");
            JQuery.$ (messageEl).hide ();
        });
    }

    /**
     * Renders all the attachments (after a modification or change).
     */
    protected void renderAttachments() {
        if (config().limit > 0) {
            if (attachments.size() >= config().limit)
                getRoot().classList.add (styles ().limit ());
            else
                getRoot().classList.remove (styles ().limit ());
        }
        if (attachments.size() > config().style.smallLimit())
            listEl.classList.add (styles ().small ());
        else
            listEl.classList.remove (styles ().small ());
        Wrap.buildInto (listEl, list -> {
            attachments.forEach(attachment -> {
                Div.$ (list).$ (item -> {
                    item.use (n -> attachment.element = (Element) n);
                    attachment.render (item);
                });
            });
        });
        renderMessage ();
    }

    /**
     * Renders out the message line. Called by {@link #renderAttachments()} and
     * whenever an item is rendered (which occurs when it changes state, so could
     * impact the message).
     * <p>
     * The message is obtained from {@link #resolveMessage(List, List)}.
     */
    protected void renderMessage() {
        String msg = resolveMessage (attachments, removed);
        if (StringSupport.empty(msg))
            JQuery.$(messageEl).hide();
        else
            JQuery.$(messageEl).text (msg).show();
    }

    /**
     * Renders an attachment into its container (the list of files).
     * <p>
     * This can be overridden to provide a different rendition. If so then mark the
     * remove (remove added file) and delete (delete existing attachment) actions
     * with the ID <code>remove</code> and <code>delete</code> respectively. Include
     * the UID in the <code>item</code> attribute. The click handler on the
     * enclosing list will pick this up for processing.
     * 
     * @param el
     *             the element builder to build into.
     * @param item
     *             the item to build.
     */
    protected void renderAttachment(ElementBuilder el, FileAttachmentItem item) {
        Div.$ (el).style (styles ().icon ()).$ (
            Em.$ ().style (resolveIcon (item))
        );
        Div.$ (el).style (styles ().content ()).$ (content -> {
            Div.$ (content).style (styles ().title ()).text (item.name ());
            if ((item.status == FileAttachmentItemStatus.PENDING)) {
                Div.$ (content).style (styles ().info ()).$ (
                    Text.$ ("Preparing..."),
                    Div.$ ().css ("flex-grow", "1"),
                    Div.$ ().style (styles ().notice ()).text (item.percentage + "%")
                );
            } else if (item.status == FileAttachmentItemStatus.UPLOADING) {
                Div.$ (content).style (styles ().info ()).$ (
                    Div.$ ().style (styles ().bar ()).$ (
                        Div.$ ().css (CSS.WIDTH, Length.pct (item.percentage))
                    ),
                    Div.$ ().style (styles ().notice ()).text (item.percentage + "%")
                );
            } else if (item.status == FileAttachmentItemStatus.ERROR) {
                el.style (styles ().error ());
                Div.$ (content).style (styles ().info ()).$ (
                    Text.$ (StringSupport.empty (item.statusMessage) ? "Failed to upload" : item.statusMessage)
                );
            } else if (item.status == FileAttachmentItemStatus.COMPLETE) {
                Div.$ (content).style (styles ().info ()).$ (
                    Text.$ ("Processed " + SizeSupport.convertToDataSize (item.size(), 1, SizeSupport.MIXED)).iff (item.size() > 0),
                    Text.$ ("Processed").iff (item.size() <= 0),
                    Div.$ ().css ("flex-grow", "1")
                );
            } else if (item.status == FileAttachmentItemStatus.EXISTS) {
                if (item.error()) {
                    el.style (styles ().error ());
                    Div.$ (content).style (styles ().info ()).$ (
                        Text.$ (StringSupport.empty (item.data.errorMessage) ? "There is a problem with this file" : item.data.errorMessage)
                    );
                } else {
                    Div.$ (content).style (styles ().info ()).$ (
                        Text.$ ("Size: " + SizeSupport.convertToDataSize (item.size(), 1, SizeSupport.MIXED)).iff (item.size() > 0),
                        Text.$ ("No size abailable").iff (item.size() <= 0)
                    );
                }
            }
        });
        if (item.status == FileAttachmentItemStatus.UPLOADING)
            Div.$ (el).style (styles().notice()).text (item.percentage + "%");
        if ((item.status == FileAttachmentItemStatus.COMPLETE) || (item.status == FileAttachmentItemStatus.ERROR))
            Em.$ (el).style(FontAwesome.times()).id ("remove").attr ("item", item.uid);
        else if (item.status == FileAttachmentItemStatus.EXISTS)
            Em.$ (el).style(FontAwesome.trashAlt()).id ("delete").attr ("item", item.uid);
    }

    /**
     * Resolves the message line (as needed).
     * 
     * @param attachments
     *                    the attachments.
     * @param removals
     *                    any removals (of previously existing, aka assigned,
     *                    attachments).
     * @return the message to display (or empty).
     */
    protected String resolveMessage(List<FileAttachmentItem> attachments, List<FileAttachment> removals) {
        // Delegate to any configured message provider.
        if (config ().messageProvider != null)
            return config ().messageProvider.apply (attachments, removals);

        // No message if there were no prior existing attachments.
        boolean existing = !removals.isEmpty() || attachments.stream ().anyMatch(FileAttachmentItem::existing);
        if (!existing)
            return null;

        int added = (int) attachments.stream().filter(FileAttachmentItem::added).count ();
        int removed = removals.size ();

        // No changes then no message.
        if ((added == 0) && (removed == 0))
            return null;

        String msg = null;
        if (removed == 1)
            msg = "Removing one existing";
        else if (removed > 1)
            msg = "Removing " + removals.size () + " existing";
        if (added > 0) {
            if (removed == 0)
                msg = "A";
            else
                msg += " and a";
            if (added == 1)
                msg += "dding one new";
            else
                msg += "dding " + added + " new";
        }
        if ((added == 1) || ((added == 0) && (removed == 1)))
            msg += " attachment";
        else
            msg += " attachments";

        return msg;
    }

    /**
     * Resolves the icon CSS to use for the given item.
     * <p>
     * The must resolve based on (at least) the item status.
     * 
     * @param item
     *             the item.
     * @return the icon CSS.
     */
    protected String resolveIcon(FileAttachmentItem item) {
        if ((item == null) || (item.status == FileAttachmentItemStatus.PENDING) || (item.status == FileAttachmentItemStatus.UPLOADING))
            return FontAwesome.cog ();
        if (item.status == FileAttachmentItemStatus.COMPLETE)
            return FontAwesome.fileCircleCheck ();
        if (item.error ())
            return FontAwesome.bug ();
        // Here we are left with existing items.
        if ((item.data == null) || (item.data.type == null))
            return FontAwesome.fileAlt ();
        if ("application/pdf".equals (item.data.type))
            return FontAwesome.filePdf ();
        if ("application/vnd.ms-excel".equals (item.data.type))
            return FontAwesome.fileExcel ();
        if (item.data.type.startsWith ("image"))
            return FontAwesome.fileImage ();
        return FontAwesome.fileAlt ();
    }

    /**
     * Removes the attachment of the given UID.
     * 
     * @param uid
     *            the uid.
     */
    protected void remove(String uid) {
        attachments.stream ()
            .filter (v -> uid.equals (v.uid))
            .findFirst ().ifPresent (v -> {
                // If the file exists then we need to transition to the removed list.
                if (v.status == FileAttachmentItemStatus.EXISTS)
                    removed.add (v.data);
                attachments.remove (v);
                renderAttachments ();
                modified ();
            });
    }

    /**
     * Adds the given files to the list of attachments.
     * 
     * @param files
     *              the files to add.
     */
    protected void add(List<File> files) {
        if (files == null)
            return;
        for (File file : files) {
            // Ensure we don't exceed the limit.
            if ((config().limit > 0) && (attachments.size() >= config().limit))
                break;
            FileAttachmentItem item = new FileAttachmentItem (file);
            if (config().fileValidator != null) {
                Optional<String> error = config().fileValidator.apply (file);
                if ((error != null) && error.isPresent()) {
                    item.status = FileAttachmentItemStatus.ERROR;
                    item.statusMessage = error.get ();
                }
            }
            attachments.add (item);
        }
        renderAttachments ();
        modified ();
        attachments.forEach (attachment -> {
            attachment.start ();
        });
    }

    /********************************************************************
     * CSS with standard styles.
     ********************************************************************/

    @Override
    public ILocalCSS styles() {
        return config ().style.styles ();
    }

    public static interface ILocalCSS extends IControlCSS {

        public String dropRegion();

        public String icon();

        public String title();

        public String info();

        public String list();

        public String content();

        public String notice();

        public String bar();

        public String small();

        public String limit();

        public String error();

        public String drag();

        public String message();
    }

    /**
     * Component CSS (standard version).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS, IControlCSS.CONTROL_CSS,
        "com/effacy/jui/ui/client/control/FileUploadControl.css",
        "com/effacy/jui/ui/client/control/FileUploadControl_Override.css"
    })
    public static abstract class StandardLocalCSS implements ILocalCSS {

        private static StandardLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (StandardLocalCSS) GWT.create (StandardLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

    /**
     * Component CSS (compact version).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS, IControlCSS.CONTROL_CSS,
        "com/effacy/jui/ui/client/control/FileUploadControl.css",
        "com/effacy/jui/ui/client/control/FileUploadControl_Override.css",
        "com/effacy/jui/ui/client/control/FileUploadControl_Compact.css",
        "com/effacy/jui/ui/client/control/FileUploadControl_Compact_Override.css"
    })
    public static abstract class CompactLocalCSS implements ILocalCSS {

        private static CompactLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (CompactLocalCSS) GWT.create (CompactLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}
