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
 * Handles an image that has been introduced into the editor (for example pasted
 * from the clipboard) by uploading it somewhere and yielding a {@code src} URL to
 * embed.
 * <p>
 * Configured via {@link Editor.Config#imageUpload(IImageUploadHandler)}. The
 * editor invokes {@link #upload(File, Consumer, Consumer)} with the image file
 * and, on success, inserts an inline image at the current selection using the
 * returned {@code src}.
 */
@FunctionalInterface
public interface IImageUploadHandler {

    /**
     * Uploads an image file and reports the URL to embed.
     *
     * @param file
     *                  the image file.
     * @param onSuccess
     *                  invoked with the {@code src} URL to embed on success.
     * @param onError
     *                  invoked with a message if the upload fails.
     */
    public void upload(File file, Consumer<String> onSuccess, Consumer<String> onError);
}
