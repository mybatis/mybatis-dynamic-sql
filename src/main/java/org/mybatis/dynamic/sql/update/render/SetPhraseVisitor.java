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

import org.mybatis.dynamic.sql.util.ColumnAndValueVisitor;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.ValueMapping;

public class SetPhraseVisitor implements ColumnAndValueVisitor<FragmentAndParameters> {
    
    private AtomicInteger sequence = new AtomicInteger(1);

    @Override
    public FragmentAndParameters visit(NullMapping mapping) {
        return new FragmentAndParameters.Builder(mapping.column().name() + " = null").build();
    }

    @Override
    public FragmentAndParameters visit(ConstantMapping mapping) {
        return new FragmentAndParameters.Builder(mapping.column().name() + " = " + mapping.constant()).build();
    }

    @Override
    public <T> FragmentAndParameters visit(ValueMapping<T> mapping) {
        String mapKey = "up" + sequence.getAndIncrement(); //$NON-NLS-1$
        String jdbcPlaceholder = mapping.column().getFormattedJdbcPlaceholder("parameters", mapKey); //$NON-NLS-1$
        String setPhrase = mapping.column().name() + " = " + jdbcPlaceholder; //$NON-NLS-1$
        
        return new FragmentAndParameters.Builder(setPhrase) //$NON-NLS-1$)
                .withParameter(mapKey, mapping.value())
                .build();
    }
}
