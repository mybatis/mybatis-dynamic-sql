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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.insert.InsertSelectModel;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectProvider;

public class InsertSelectRenderer {

    private InsertSelectModel model;
    private RenderingStrategy renderingStrategy;
    
    private InsertSelectRenderer(Builder builder) {
        model = Objects.requireNonNull(builder.model);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
    }
    
    public InsertSelectProvider render() {
        SelectProvider selectProvider = model.selectModel().render(renderingStrategy);
        
        return new InsertSelectProvider.Builder()
                .withTableName(model.table().name())
                .withColumnsPhrase(calculateColumnsPhrase())
                .withSelectStatement(selectProvider.getFullSelectStatement())
                .withParameters(selectProvider.getParameters())
                .build();
    }
    
    private Optional<String> calculateColumnsPhrase() {
        return model.mapColumns(SqlColumn::name)
                .map(this::calculateColumnsPhrase);
    }
    
    private String calculateColumnsPhrase(Stream<String> ss) {
        return ss.collect(Collectors.joining(", ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    public static class Builder {
        private InsertSelectModel model;
        private RenderingStrategy renderingStrategy;
        
        public Builder withInsertSelectModel(InsertSelectModel model) {
            this.model = model;
            return this;
        }
        
        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }
        
        public InsertSelectRenderer build() {
            return new InsertSelectRenderer(this);
        }
    }
}
