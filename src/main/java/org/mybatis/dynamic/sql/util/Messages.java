/*
 *    Copyright 2016-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class Messages {
    private static final String BUNDLE_NAME = "org.mybatis.dynamic.sql.util.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() {}

    public static String getString(String key) {
        return RESOURCE_BUNDLE.getString(key);
    }

    public static String getString(String key, String p1) {
        return MessageFormat.format(getString(key), p1);
    }

    public static String getString(String key, String p1, String p2, String p3) {
        return MessageFormat.format(getString(key), p1, p2, p3);
    }

    public static String getInternalErrorString(int internalErrorNumber) {
        return MessageFormat.format(getString("INTERNAL.ERROR"), internalErrorNumber); //$NON-NLS-1$
    }
}
