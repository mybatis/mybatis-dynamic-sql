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
package org.mybatis.dynamic.sql.insert.render;

import java.util.Objects;
import java.util.function.Function;

import org.mybatis.dynamic.sql.insert.InsertModel;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.InsertMapping;

public class InsertRenderer<T> {

    private InsertModel<T> model;
    
    private InsertRenderer(InsertModel<T> model) {
        this.model = Objects.requireNonNull(model);
    }
    
    public InsertSupport<T> render(RenderingStrategy renderingStrategy) {
        ValuePhraseVisitor visitor = new ValuePhraseVisitor(renderingStrategy);
        return model.mapColumnMappings(toFieldAndValue(visitor))
                .collect(FieldAndValueCollector.toInsertSupport(model.record(), model.table()));
    }

    private Function<InsertMapping, FieldAndValue> toFieldAndValue(ValuePhraseVisitor visitor) {
        return insertMapping -> toFieldAndValue(visitor, insertMapping);
    }
    
    private FieldAndValue toFieldAndValue(ValuePhraseVisitor visitor, InsertMapping insertMapping) {
        return insertMapping.accept(visitor);
    }
    
    public static <T> InsertRenderer<T> of(InsertModel<T> model) {
        return new InsertRenderer<>(model);
    }
}
