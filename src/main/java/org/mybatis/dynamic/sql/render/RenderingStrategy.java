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

import org.mybatis.dynamic.sql.BindableColumn;

public abstract class RenderingStrategy {
    /**
     * Rendering strategy for MyBatis3.
     * 
     * @deprecated use {@link RenderingStrategies#MYBATIS3} instead
     */
    @Deprecated
    @SuppressWarnings("squid:S2390")
    public static final RenderingStrategy MYBATIS3 = new MyBatis3RenderingStrategy();

    /**
     * Rendering strategy for Spring JDBC Template Named Parameters.
     * 
     * @deprecated use {@link RenderingStrategies#SPRING_NAMED_PARAMETER} instead
     */
    @Deprecated
    @SuppressWarnings("squid:S2390")
    public static final RenderingStrategy SPRING_NAMED_PARAMETER = new SpringNamedParameterRenderingStrategy();

    public static final String DEFAULT_PARAMETER_PREFIX = "parameters"; //$NON-NLS-1$
    
    public abstract String getFormattedJdbcPlaceholder(BindableColumn<?> column, String prefix, String parameterName);

    public abstract String getFormattedJdbcPlaceholder(String prefix, String parameterName);
}
