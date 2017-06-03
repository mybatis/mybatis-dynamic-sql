/**
 *    Copyright 2016-2017 the original author or authors.
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
package org.mybatis.dynamic.sql.update.render;

import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.StringConstantMapping;
import org.mybatis.dynamic.sql.util.UpdateMappingVisitor;
import org.mybatis.dynamic.sql.util.ValueMapping;

public class SetPhraseVisitor implements UpdateMappingVisitor<FragmentAndParameters> {
    
    private AtomicInteger sequence = new AtomicInteger(1);
    private RenderingStrategy renderingStrategy;
    
    public SetPhraseVisitor(RenderingStrategy renderingStrategy) {
        this.renderingStrategy = renderingStrategy;
    }

    @Override
    public FragmentAndParameters visit(NullMapping mapping) {
        return new FragmentAndParameters.Builder(mapping.column().name() + " = null").build(); //$NON-NLS-1$
    }

    @Override
    public FragmentAndParameters visit(ConstantMapping mapping) {
        String fragment = mapping.column().name() + " = " + mapping.constant(); //$NON-NLS-1$
        return new FragmentAndParameters.Builder(fragment).build();
    }

    @Override
    public FragmentAndParameters visit(StringConstantMapping mapping) {
        String fragment = mapping.column().name() + " = " //$NON-NLS-1$
                + "'" + mapping.constant() + "'"; //$NON-NLS-1$ //$NON-NLS-2$
        return new FragmentAndParameters.Builder(fragment).build();
    }
    
    @Override
    public <T> FragmentAndParameters visit(ValueMapping<T> mapping) {
        String mapKey = "up" + sequence.getAndIncrement(); //$NON-NLS-1$
        String jdbcPlaceholder =
                renderingStrategy.getFormattedJdbcPlaceholder(mapping.column(), "parameters", mapKey); //$NON-NLS-1$
        String setPhrase = mapping.column().name() + " = " + jdbcPlaceholder; //$NON-NLS-1$
        
        return new FragmentAndParameters.Builder(setPhrase) //$NON-NLS-1$)
                .withParameter(mapKey, mapping.value())
                .build();
    }
}
