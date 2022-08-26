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
package org.mybatis.dynamic.sql.render;

import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.dynamic.sql.BindableColumn;

public abstract class RenderingStrategy {
    public static final String DEFAULT_PARAMETER_PREFIX = "parameters"; //$NON-NLS-1$

    public static String formatParameterMapKey(AtomicInteger sequence) {
        return "p" + sequence.getAndIncrement(); //$NON-NLS-1$
    }

    public abstract String getFormattedJdbcPlaceholder(BindableColumn<?> column, String prefix, String parameterName);

    public abstract String getFormattedJdbcPlaceholder(String prefix, String parameterName);

    public String getMultiRowFormattedJdbcPlaceholder(BindableColumn<?> column, String prefix, String parameterName) {
        return getFormattedJdbcPlaceholder(column, prefix, parameterName);
    }
}
