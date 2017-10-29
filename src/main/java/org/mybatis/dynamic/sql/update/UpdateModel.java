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
package org.mybatis.dynamic.sql.update;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.update.render.UpdateRenderer;
import org.mybatis.dynamic.sql.update.render.UpdateSupport;
import org.mybatis.dynamic.sql.util.UpdateMapping;
import org.mybatis.dynamic.sql.where.WhereModel;

public class UpdateModel {
    private SqlTable table;
    private Optional<WhereModel> whereModel;
    private List<UpdateMapping> columnValues = new ArrayList<>();
    
    private UpdateModel(Builder builder) {
        table = builder.table;
        whereModel = Optional.ofNullable(builder.whereModel);
        columnValues.addAll(builder.columnValues);
    }
    
    public SqlTable table() {
        return table;
    }
    
    public Optional<WhereModel> whereModel() {
        return whereModel;
    }
    
    public <R> Stream<R> mapColumnValues(Function<UpdateMapping, R> mapper) {
        return columnValues.stream().map(mapper);
    }
    
    public UpdateSupport render(RenderingStrategy renderingStrategy) {
        return UpdateRenderer.of(this).render(renderingStrategy);
    }
    
    public static class Builder {
        private SqlTable table;
        private WhereModel whereModel;
        private List<UpdateMapping> columnValues = new ArrayList<>();
        
        public Builder(SqlTable table) {
            this.table = table;
        }
        
        public Builder withColumnValues(List<UpdateMapping> columnValues) {
            this.columnValues.addAll(columnValues);
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
