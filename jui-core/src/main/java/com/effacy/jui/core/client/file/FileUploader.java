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
package com.effacy.jui.core.client.file;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.file.IFileUploader.IFileUploaderListener.FailureType;
import com.effacy.jui.platform.util.client.Logger;

import elemental2.dom.Blob;
import elemental2.dom.File;
import elemental2.dom.FormData;
import elemental2.dom.XMLHttpRequest;

/**
 * Abstract base class for implementing concrete {@link IFileUploader} instance
 * for specific endpoint types.
 */
public abstract class FileUploader implements IFileUploader {

    /**
     * See constructor.
     */
    protected int chunkSize = 0;

    /**
     * See constructor.
     */
    protected int maxConcurrent = 0;

    /**
     * Construct with configuration.
     * 
     * @param chunkSize
     *                      the maximum size of each chunk to upload (set to 0 if to
     *                      upload the file all at once).
     * @param maxConcurrent
     *                      the maximum number of concurrent chunks that should run
     *                      at once (set to 0 if there is no limit).
     */
    protected FileUploader(int chunkSize, int maxConcurrent) {
        this.chunkSize = chunkSize;
        this.maxConcurrent = maxConcurrent;
    }

    @Override
    public void send(File file, IFileUploaderListener listener) {
        createSender (file, listener).start ();
    }

    /**
     * Creates a suitably configured {@link FileSender} for initiating an upload.
     * 
     * @return the configured sender.
     */
    protected abstract FileSender createSender(File file, IFileUploaderListener listener);

    /**
     * Used in {@link FileChunk}.
     */
    enum FileChunkStatus {
        PENDING, RUNNING, STOPPED;
    }

    /**
     * Does the leg-work of performing an upload. Implementation should sub-class
     * this internally.
     * <p>
     * Note that this is expected to upload only a single file then will be
     * disposed.
     */
    abstract class FileSender {

        protected XMLHttpRequest xhr;
        
        protected File file;
        
        protected String ref;

        protected IFileUploaderListener listener;

        protected List<FileChunk> chunks = new ArrayList<>();

        /**
         * Construct with the file to upload, a reference for the file (as available,
         * though can be set directly as the member variable) and a listener.
         * 
         * @param file
         *                 the file to be uploaded.
         * @param ref
         *                 the reference to the file to be passed back to the caller.
         * @param listener
         *                 the listener to lifecycle events.
         */
        FileSender(File file, String ref, IFileUploaderListener listener) {
            this.file = file;
            this.ref = ref;
            this.listener = listener;

            // Break the file into chunks as needed.
            int fileSize = file.size;
            if (fileSize > 0) {
                if (chunkSize == 0) {
                    chunks.add (new FileChunk (0));
                } else {
                    int numberChunks = fileSize / chunkSize;
                    if (fileSize % chunkSize > 0)
                        numberChunks++;
                    for (int i = 0; i < numberChunks; i++)
                        chunks.add (new FileChunk (i));
                }
            }
        }

        /**
         * Invoked on success.
         * <p>
         * The delegates to the listener passing the reference stored in {@link #ref}.
         */
        protected void success() {
            // Kick off any remaining chunks and if all are done then we can notify the
            // listener.
            if (process()) {
                if (listener != null) {
                    try {
                        listener.onSuccess (ref);
                    } catch (Throwable e) {
                        Logger.reportUncaughtException (e, FileUploader.this);
                    }
                }
            }
        }

        /**
         * Invoked on failures.
         * <p>
         * Delegates through to the listener.
         * 
         * @param type
         *             the failure type.
         */
        protected void fail(FailureType type) {
            // Abort any other running chunks.
            chunks.forEach (chunk -> chunk.abort ());
            if (listener != null) {
                try {
                    listener.onFailure (type);
                } catch (Throwable e) {
                    Logger.reportUncaughtException (e, FileUploader.this);
                }
            }
        }

        /**
         * Invoked on progress updated.
         * <p>
         * Delegates through to the listener.
         */
        protected void progress() {
            if (listener != null) {
                try {
                    int completed = 0;
                    int total = 0;
                    int uploaded = 0;
                    for (FileChunk chunk : chunks) {
                        total += chunk.bytesTotal;
                        uploaded += chunk.bytesUploaded;
                        if (chunk.stopped ())
                            completed++;
                    }
                    int percentage = -1;
                    if (total > 0)
                        percentage = (int) ((uploaded * 100.0) / total);
                    else
                        percentage = (int) ((completed * 100.0) / chunks.size ());
                    if (percentage >= 0)
                        listener.onProgress (percentage);
                } catch (Throwable e) {
                    Logger.reportUncaughtException(e, FileUploader.this);
                }
            }
        }

        /**
         * Starts the upload.
         * <p>
         * The default implementation obtains an {@link XMLHttpRequest} from
         * {@link #createXMLHttpRequest()}, attaches listeners to it then sends the
         * file.
         * <p>
         * If your implementation cannot resolve a reference on construction then it is
         * recommended to override this with the override resolving the reference. Once
         * resolved then call this implementation.
         * <p>
         * Note that this takes care of any chunking that may have be configured (and if
         * so, rationalising the percentage progress).
         */
        public void start() {
            if (maxConcurrent <= 0) {
                // Start all chunks.
                chunks.forEach (chunk -> {
                    chunk.start ();
                });
            } else
                process ();
        }

        /**
         * Loops over the chunks are starts them.
         * 
         * @return {@code true} if all chunks have been completed (i.e. nothing is
         *         running).
         */
        private boolean process() {
            int running = 0;
            for (FileChunk chunk : chunks) {
                if (chunk.stopped ())
                    continue;
                running++;
                if (chunk.pending ())
                    chunk.start ();
                if (running >= maxConcurrent)
                    break;
            }

            // No running chunks then we are done.
            return (running == 0);
        }

        /**
         * Creates and returns a suitably configured {@link XMLHttpRequest} for the given chunk.
         * 
         * @param chunk
         *                    the chunk being sent.
         * @param totalChunks
         *                    the total number of chunks.
         * @param file
         *                    the file the chunk is part of.
         * @param ref
         *                    the reference for the file being uploaded.
         * @return the request.
         */
        protected abstract XMLHttpRequest createXMLHttpRequest(FileChunk chunk, int totalChunks, File file, String ref);

        /**
         * Creates form data to send for the given chunk. If this return {@code null}
         * then the chunk is sent directly.
         * 
         * @param chunk
         *                    the chunk being sent.
         * @param totalChunks
         *                    the total number of chunks.
         * @param file
         *                    the file the chunk is part of.
         * @param ref
         *                    the reference for the file being uploaded.
         * @return the form data to use (or {@code null}).
         */
        protected abstract FormData createFormData(FileChunk chunk, int totalChunks, File file, String ref);


        /**
         * Represents a single block of data that is being uploaded. This could be the
         * entire file or a slice of the file.
         */
        class FileChunk {

            /**
             * The lifecycle state of the chunk upload.
             */
            private FileChunkStatus state = FileChunkStatus.PENDING;
            
            /**
             * The chunk to upload.
             */
            private Blob chunk;

            /**
             * The part.
             */
            private int part;

            /**
             * The total size in number of bytes.
             */
            private int bytesTotal = 0;

            /**
             * The total bytes uploaded so far.
             */
            private int bytesUploaded = 0;
            
            /**
             * Construct a chunk given its position relative to all chunks.
             * 
             * @param part
             *                   the chunk index (from 0).
             */
            FileChunk (int part) {
                if (chunkSize > 0) {
                    int start = part * chunkSize;
                    int end = Math.min (file.size, start + chunkSize);
                    if (end > start)
                        chunk = file.slice (start, end);
                }
                if ((chunk == null) && (part == 0))
                    chunk = file;
                bytesTotal = chunk.size;
                this.part = part + 1;
            }

            /**
             * The data of the chunk.
             * 
             * @return the data.
             */
            public Blob data() {
                return chunk;
            }

            /**
             * The part number (from 1).
             * 
             * @return the part number.
             */
            public int part() {
                return part;
            }

            /**
             * The size of the chunk.
             * 
             * @return the size.
             */
            public int size() {
                return bytesTotal;
            }

            /**
             * Determines if the chunk processing has been completed.
             * 
             * @return {@code true} it has.
             */
            public boolean stopped() {
                return (state == FileChunkStatus.STOPPED);
            }

            /**
             * Determines if the chunk has not been started.
             * 
             * @return {@code true} it has.
             */
            boolean pending() {
                return (state == FileChunkStatus.PENDING);
            }

            void start() {
                state = FileChunkStatus.RUNNING;
                xhr = createXMLHttpRequest (this, chunks.size (), file, ref);
                xhr.onload = e -> {
                    state = FileChunkStatus.STOPPED;
                    if (xhr.status == 200)
                        success ();
                    else
                        fail (FailureType.ERROR);
                };
                xhr.onerror = e -> {
                    if (state != FileChunkStatus.STOPPED)
                        fail (FailureType.ERROR);
                    state = FileChunkStatus.STOPPED;
                    return null;
                };
                xhr.upload.onprogress = e -> {
                    state = FileChunkStatus.STOPPED;
                    if (e.lengthComputable && (e.total > 0)) {
                        // We make no assumptions about loaded and total, other than the ratio is the
                        // fractional progress so far.
                        double progress = e.loaded / e.total;
                        this.bytesUploaded = (int) (bytesTotal * progress);
                        progress ();
                    }
                };
                xhr.onabort = e -> {
                    if (state != FileChunkStatus.STOPPED)
                        fail (FailureType.ABORT);
                    state = FileChunkStatus.STOPPED;
                };
                xhr.ontimeout = e -> {
                    if (state != FileChunkStatus.STOPPED)
                        fail (FailureType.TIMEOUT);
                    state = FileChunkStatus.STOPPED;
                };

                // Create the form data, if not present the we just send the chunk.
                FormData data = createFormData (this, chunks.size (), file, ref);
                if (data != null)
                    xhr.send (data);
                else
                    xhr.send (chunk);
            }

            /**
             * Aborts the upload.
             */
            void abort() {
                if (state != FileChunkStatus.STOPPED) {
                    state = FileChunkStatus.STOPPED;
                    if (xhr != null) {
                        try {
                            xhr.abort ();
                        } catch (Throwable e) {
                            // Not to worry, if it fails it probably doesn't need to be aborted.
                        }
                    }
                }
            }
        }
    }

}
