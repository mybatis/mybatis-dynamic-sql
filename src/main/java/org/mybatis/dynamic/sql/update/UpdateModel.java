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
package org.mybatis.dynamic.sql.update;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.update.render.UpdateRenderer;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.UpdateMapping;
import org.mybatis.dynamic.sql.where.WhereModel;

public class UpdateModel {
    private SqlTable table;
    private WhereModel whereModel;
    private List<UpdateMapping> columnMappings;
    
    private UpdateModel(Builder builder) {
        table = Objects.requireNonNull(builder.table);
        whereModel = builder.whereModel;
        columnMappings = Objects.requireNonNull(builder.columnMappings);
    }
    
    public SqlTable table() {
        return table;
    }
    
    public Optional<WhereModel> whereModel() {
        return Optional.ofNullable(whereModel);
    }
    
    public <R> Stream<R> mapColumnMappings(Function<UpdateMapping, R> mapper) {
        return columnMappings.stream().map(mapper);
    }

    @NotNull
    public UpdateStatementProvider render(RenderingStrategy renderingStrategy) {
        return UpdateRenderer.withUpdateModel(this)
                .withRenderingStrategy(renderingStrategy)
                .build()
                .render();
    }
    
    public static Builder withTable(SqlTable table) {
        return new Builder().withTable(table);
    }
    
    public static class Builder {
        private SqlTable table;
        private WhereModel whereModel;
        private List<UpdateMapping> columnMappings = new ArrayList<>();
        
        public Builder withTable(SqlTable table) {
            this.table = table;
            return this;
        }
        
        public Builder withColumnMappings(List<UpdateMapping> columnMappings) {
            this.columnMappings.addAll(columnMappings);
            return this;
        }
        
        public Builder withWhereModel(WhereModel whereModel) {
            this.whereModel = whereModel;
            return this;
        }
        
        public UpdateModel build() {
            return new UpdateModel(this);
        }
    }
}
