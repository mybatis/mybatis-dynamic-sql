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
package org.mybatis.dynamic.sql.render;

import java.util.Optional;

import org.mybatis.dynamic.sql.BindableColumn;

public abstract class RenderingStrategy {
    @SuppressWarnings("squid:S2390")
    public static final RenderingStrategy MYBATIS3 = new MyBatis3RenderingStrategy();
    @SuppressWarnings("squid:S2390")
    public static final RenderingStrategy SPRING_NAMED_PARAMETER = new SpringNamedParameterRenderingStrategy();
    public static final String DEFAULT_PARAMETER_PREFIX = "parameters"; //$NON-NLS-1$
    
    public String getFormattedJdbcPlaceholder(BindableColumn<?> column, String prefix, String parameterName) {
        return getFormattedJdbcPlaceholder(Optional.of(column), prefix, parameterName);
    }

    public String getFormattedJdbcPlaceholder(String prefix, String parameterName) {
        return getFormattedJdbcPlaceholder(Optional.empty(), prefix, parameterName);
    }

    public abstract String getFormattedJdbcPlaceholder(Optional<BindableColumn<?>> column, String prefix,
            String parameterName);
}
