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

import java.util.function.Consumer;

import elemental2.dom.File;

/**
 * Used to upload a file from client to a remote endpoint (implementation
 * specific).
 * <p>
 * Instances are expected to be multi-user and concurrent. That is, more than
 * one file can be sent at one time or over a length of time.
 * <p>
 * 
 */
public interface IFileUploader {

    /**
     * Callback listener for file upload lifecycle events.
     */
    public interface IFileUploaderListener {

        /**
         * Enumerates various failure conditions.
         */
        public enum FailureType {
            ABORT, TIMEOUT, ERROR;
        }

        /**
         * Invoked on a successful upload.
         * <p>
         * A reference is passed through which provides an (implementation specific)
         * reference to the file that was uploaded. This allows the file to be
         * referenced later on (i.e. passed back to the server so it know where to get
         * the file from).
         * 
         * @param ref
         *            a reference to refer to the uploaded file.
         */
        public void onSuccess(String ref);

        /**
         * Invoked when the upload has failed.
         * 
         * @param type
         *             the reason for failure.
         */
        public void onFailure(FailureType type);

        /**
         * Progress indicator.
         * 
         * @param percentage
         *                   the percentage progress (if not able to be calculated this
         *                   is -1).
         */
        public void onProgress(int percentage);

        /**
         * Convenience to create a listener using lambda-expressions for the various
         * callback methods.
         * 
         * @param success
         *                 (optional) invoked on success with the file reference.
         * @param fail
         *                 (optional) invoked on failure with the failure type.
         * @param progress
         *                 (optional) invoked on progress with the percentage.
         * @return the constructed listener.
         */
        public static IFileUploaderListener create(Consumer<String> success, Consumer<FailureType> fail, Consumer<Integer> progress) {
            return new IFileUploaderListener() {

                @Override
                public void onSuccess(String ref) {
                    if (success != null)
                        success.accept (ref);
                }

                @Override
                public void onFailure(FailureType type) {
                    if (fail != null)
                        fail.accept(type);
                }

                @Override
                public void onProgress(int percentage) {
                    if (progress != null)
                        progress.accept(percentage);
                }

            };
        }
    }

    /**
     * Sends the passed file with the given callback listener.
     * 
     * @param file
     *                 the file to send.
     * @param listener
     *                 the listener to capture various lifecycle events of the
     *                 upload process.
     */
    public void send(File file, IFileUploaderListener listener);
}
