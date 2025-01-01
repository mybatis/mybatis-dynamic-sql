/*
 *    Copyright 2016-2025 the original author or authors.
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
package org.mybatis.dynamic.sql.util.springbatch;

import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.dynamic.sql.render.MyBatis3RenderingStrategy;

/**
 * This rendering strategy should be used for MyBatis3 statements using the
 * MyBatisPagingItemReader supplied by mybatis-spring integration
 * (<a href="http://www.mybatis.org/spring/">http://www.mybatis.org/spring/</a>).
 */
public class SpringBatchPagingItemReaderRenderingStrategy extends MyBatis3RenderingStrategy {

    @Override
    public String getFormattedJdbcPlaceholderForPagingParameters(String prefix, String parameterName) {
        return "#{" //$NON-NLS-1$
                + parameterName
                + "}"; //$NON-NLS-1$
    }

    @Override
    public String formatParameterMapKeyForFetchFirstRows(AtomicInteger sequence) {
        return "_pagesize"; //$NON-NLS-1$
    }

    @Override
    public String formatParameterMapKeyForLimit(AtomicInteger sequence) {
        return "_pagesize"; //$NON-NLS-1$
    }

    @Override
    public String formatParameterMapKeyForOffset(AtomicInteger sequence) {
        return "_skiprows"; //$NON-NLS-1$
    }
}
