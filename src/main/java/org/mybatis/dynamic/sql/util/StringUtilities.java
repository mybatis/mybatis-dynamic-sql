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

import java.util.Optional;

public interface StringUtilities {

    static String spaceAfter(Optional<String> in) {
        return in.map(StringUtilities::spaceAfter)
                .orElse(""); //$NON-NLS-1$
    }

    static String spaceAfter(String in) {
        return in + " "; //$NON-NLS-1$
    }

    static String spaceBefore(Optional<String> in) {
        return in.map(StringUtilities::spaceBefore)
                .orElse(""); //$NON-NLS-1$
    }

    static String spaceBefore(String in) {
        return " " + in; //$NON-NLS-1$
    }

    static String safelyUpperCase(String s) {
        return s == null ? null : s.toUpperCase();
    }

    static String toCamelCase(String inputString) {
        StringBuilder sb = new StringBuilder();

        boolean nextUpperCase = false;

        for (int i = 0; i < inputString.length(); i++) {
            char c = inputString.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                if (nextUpperCase) {
                    sb.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            } else {
                if (sb.length() > 0) {
                    nextUpperCase = true;
                }
            }
        }

        return sb.toString();
    }
}
