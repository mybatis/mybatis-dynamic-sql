/**
 *    Copyright 2016-2019 the original author or authors.
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
package org.mybatis.dynamic.sql.where.condition;

import org.mybatis.dynamic.sql.Callback;
import org.mybatis.dynamic.sql.util.StringUtilities;

import java.util.Collection;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class IsInCaseInsensitiveWhenPresent extends IsInCaseInsensitive {

    protected IsInCaseInsensitiveWhenPresent(Collection<String> values) {
        super(values, s -> s.filter(Objects::nonNull).map(StringUtilities::safelyUpperCase));
    }

    protected IsInCaseInsensitiveWhenPresent(Collection<String> values, UnaryOperator<Stream<String>> valueStreamTransformer,
                                             Callback callback) {
        super(values, valueStreamTransformer, callback);
    }

    @Override
    public IsInCaseInsensitiveWhenPresent withListEmptyCallback(Callback callback) {
        return new IsInCaseInsensitiveWhenPresent(values, valueStreamTransformer, callback);
    }

    public static IsInCaseInsensitiveWhenPresent of(Collection<String> values) {
        return new IsInCaseInsensitiveWhenPresent(values);
    }
}
