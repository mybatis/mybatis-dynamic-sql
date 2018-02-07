/**
 *    Copyright 2016-2018 the original author or authors.
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
import java.util.function.Function;
import java.util.function.Supplier;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.VisitableCondition;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.ColumnMapping;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.SelectMapping;
import org.mybatis.dynamic.sql.util.StringConstantMapping;
import org.mybatis.dynamic.sql.util.UpdateMapping;
import org.mybatis.dynamic.sql.util.ValueMapping;
import org.mybatis.dynamic.sql.where.AbstractWhereDSL;

public class UpdateDSL<R> {

    private Function<UpdateModel, R> adapterFunction;
    private List<UpdateMapping> columnMappings = new ArrayList<>();
    private SqlTable table;
    
    private UpdateDSL(SqlTable table, Function<UpdateModel, R> adapterFunction) {
        this.table = Objects.requireNonNull(table);
        this.adapterFunction = Objects.requireNonNull(adapterFunction);
    }
    
    public <T> SetClauseFinisher<T> set(SqlColumn<T> column) {
        return new SetClauseFinisher<>(column);
    }
    
    public <T> UpdateWhereBuilder where(BindableColumn<T> column, VisitableCondition<T> condition) {
        return new UpdateWhereBuilder(column, condition);
    }
    
    public <T> UpdateWhereBuilder where(BindableColumn<T> column, VisitableCondition<T> condition,
            SqlCriterion<?>...subCriteria) {
        return new UpdateWhereBuilder(column, condition, subCriteria);
    }
    
    /**
     * WARNING! Calling this method could result in an update statement that updates
     * all rows in a table.
     * 
     * @return the update model
     */
    public R build() {
        UpdateModel updateModel = UpdateModel.withTable(table)
                .withColumnMappings(columnMappings)
                .build();
        return adapterFunction.apply(updateModel);
    }
    
    public static <R> UpdateDSL<R> update(Function<UpdateModel, R> adapterFunction, SqlTable table) {
        return new UpdateDSL<>(table, adapterFunction);
    }
    
    public static UpdateDSL<UpdateModel> update(SqlTable table) {
        return update(Function.identity(), table);
    }
    
    public static <T> UpdateDSL<MyBatis3UpdateModelAdapter<T>> updateWithMapper(
            Function<UpdateStatementProvider, T> mapperMethod, SqlTable table) {
        return update(updateModel -> MyBatis3UpdateModelAdapter.of(updateModel, mapperMethod), table);
    }
    
    public class SetClauseFinisher<T> {
        
        private SqlColumn<T> column;
        
        public SetClauseFinisher(SqlColumn<T> column) {
            this.column = column;
        }
        
        public UpdateDSL<R> equalToNull() {
            columnMappings.add(NullMapping.of(column));
            return UpdateDSL.this;
        }

        public UpdateDSL<R> equalToConstant(String constant) {
            columnMappings.add(ConstantMapping.of(column, constant));
            return UpdateDSL.this;
        }
        
        public UpdateDSL<R> equalToStringConstant(String constant) {
            columnMappings.add(StringConstantMapping.of(column, constant));
            return UpdateDSL.this;
        }
        
        public UpdateDSL<R> equalTo(T value) {
            return equalTo(() -> value);
        }

        public UpdateDSL<R> equalTo(Supplier<T> valueSupplier) {
            columnMappings.add(ValueMapping.of(column, valueSupplier));
            return UpdateDSL.this;
        }

        public UpdateDSL<R> equalTo(Buildable<SelectModel> buildable) {
            columnMappings.add(SelectMapping.of(column, buildable));
            return UpdateDSL.this;
        }

        public UpdateDSL<R> equalTo(BasicColumn rightColumn) {
            columnMappings.add(ColumnMapping.of(column, rightColumn));
            return UpdateDSL.this;
        }

        public UpdateDSL<R> equalToWhenPresent(T value) {
            return equalToWhenPresent(() -> value);
        }

        public UpdateDSL<R> equalToWhenPresent(Supplier<T> valueSupplier) {
            if (valueSupplier.get() != null) {
                columnMappings.add(ValueMapping.of(column, valueSupplier));
            }
            return UpdateDSL.this;
        }
    }

    public class UpdateWhereBuilder extends AbstractWhereDSL<UpdateWhereBuilder> {
        
        public <T> UpdateWhereBuilder(BindableColumn<T> column, VisitableCondition<T> condition) {
            super(column, condition);
        }
        
        public <T> UpdateWhereBuilder(BindableColumn<T> column, VisitableCondition<T> condition,
                SqlCriterion<?>...subCriteria) {
            super(column, condition, subCriteria);
        }
        
        public R build() {
            UpdateModel updateModel = UpdateModel.withTable(table)
                    .withColumnMappings(columnMappings)
                    .withWhereModel(buildWhereModel())
                    .build();
            return adapterFunction.apply(updateModel);
        }
        
        @Override
        protected UpdateWhereBuilder getThis() {
            return this;
        }
    }
}
