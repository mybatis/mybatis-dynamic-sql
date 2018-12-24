/**
 *    Copyright 2016-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.util;

import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public interface StringUtilities {

    static String spaceAfter(Optional<String> in) {
        return in.map(s -> s + " ") //$NON-NLS-1$
                .orElse(""); //$NON-NLS-1$
    }

    static String spaceAfter(String in) {
        return in + " "; //$NON-NLS-1$
    }
    
    static String spaceBefore(Optional<String> in) {
        return in.map(s -> " " + s) //$NON-NLS-1$
                .orElse(""); //$NON-NLS-1$
    }

    static String spaceBefore(String in) {
        return " " + in; //$NON-NLS-1$
    }
    
    static String safelyUpperCase(String s) {
        return s == null ? null : s.toUpperCase();
    }

    static UnaryOperator<Stream<String>> upperCaseAfter(UnaryOperator<Stream<String>> valueModifier) {
        UnaryOperator<Stream<String>> ua = s -> s.map(StringUtilities::safelyUpperCase);
        return t -> ua.apply(valueModifier.apply(t));
    }
}
