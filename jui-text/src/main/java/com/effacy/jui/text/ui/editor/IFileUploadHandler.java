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
package com.effacy.jui.text.ui.editor;

import java.util.function.Consumer;

import elemental2.dom.File;

/**
 * Handles a file introduced into the editor (for example pasted from the
 * clipboard or dropped in) by uploading it somewhere and yielding a URL to
 * reference.
 * <p>
 * Configured via {@link Editor.Config#fileUpload(IFileUploadHandler)}. The
 * editor invokes {@link #upload(File, Consumer, Consumer)} with the file and, on
 * success, embeds it at the current selection: an image file is inserted as an
 * inline image using the returned URL as its {@code src}; any other file is
 * inserted as a link (labelled with the file name) to the returned URL. The
 * handler is also the natural place to enforce upload policy (for example a size
 * limit): reject by reporting via {@code onError} (optionally surfacing a
 * message to the user) and the editor leaves the document unchanged.
 */
@FunctionalInterface
public interface IFileUploadHandler {

    /**
     * Uploads a file and reports the URL to reference.
     *
     * @param file
     *                  the file.
     * @param onSuccess
     *                  invoked with the URL to reference on success.
     * @param onError
     *                  invoked with a message if the upload fails (or is
     *                  rejected).
     */
    public void upload(File file, Consumer<String> onSuccess, Consumer<String> onError);
}
