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

import java.util.List;

import com.effacy.jui.validation.model.IErrorMessage;

public class NotificationBlockCreator {

    public static String MULTIPLE_HEADER = "There were some problems:";

    public static void apply(NotificationBlock notifier, String... errors) {
        if ((errors == null) || errors.length == 0) {
            notifier.hide();
            return;
        }
        notifier.builder ().notification(NotificationBlock.NotificationBuilder.Theme.ERROR, not -> {
            if (errors.length == 1) {
                not.content(errors[0]);
            } else {
                not.content (MULTIPLE_HEADER);
                for (String error : errors)
                    not.line (error);
            }
        });
        notifier.show();
    }

    /**
     * Convenience to build a notification from a collection of errors.
     * 
     * @param notifier
     *                              the notifier.
     * @param errors
     *                              the errors.
     * @param multipleMessageHeader
     *                              (optional) a header for when there are more than
     *                              one error.
     */
    public static void apply (NotificationBlock notifier, List<? extends IErrorMessage> errors) {
        if ((errors == null) || errors.isEmpty()) {
            notifier.hide();
            return;
        }
        notifier.builder ().notification(NotificationBlock.NotificationBuilder.Theme.ERROR, not -> {
            if (errors.size() == 1) {
                not.content(errors.get(0).getMessage ());
            } else {
                not.content (MULTIPLE_HEADER);
                errors.forEach(error -> {
                    not.line (error.getMessage());
                });
            }
        });
        notifier.show();
    }
}
