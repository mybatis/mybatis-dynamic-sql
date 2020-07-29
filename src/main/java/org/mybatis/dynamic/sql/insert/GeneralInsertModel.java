/**
 *    Copyright 2016-2020 the original author or authors.
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
package org.mybatis.dynamic.sql.insert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertRenderer;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.AbstractColumnMapping;

public class GeneralInsertModel {

    private SqlTable table;
    private List<AbstractColumnMapping<?>> insertMappings;

    private GeneralInsertModel(Builder builder) {
        table = Objects.requireNonNull(builder.table);
        insertMappings = builder.insertMappings;
    }
    
    public <R> Stream<R> mapColumnMappings(Function<AbstractColumnMapping<?>, R> mapper) {
        return insertMappings.stream().map(mapper);
    }

    public SqlTable table() {
        return table;
    }

    @NotNull
    public GeneralInsertStatementProvider render(RenderingStrategy renderingStrategy) {
        return GeneralInsertRenderer.withInsertModel(this)
                .withRenderingStrategy(renderingStrategy)
                .build()
                .render();
    }

    public static class Builder {
        private SqlTable table;
        private List<AbstractColumnMapping<?>> insertMappings = new ArrayList<>();
        
        public Builder withTable(SqlTable table) {
            this.table = table;
            return this;
        }
        
        public Builder withInsertMappings(List<AbstractColumnMapping<?>> insertMappings) {
            this.insertMappings.addAll(insertMappings);
            return this;
        }
        
        public GeneralInsertModel build() {
            return new GeneralInsertModel(this);
        }
    }
}
