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
package org.mybatis.dynamic.sql.delete;

import java.util.function.Function;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.VisitableCondition;
import org.mybatis.dynamic.sql.delete.render.DeleteStatement;
import org.mybatis.dynamic.sql.where.AbstractWhereDSL;

public class DeleteDSL<R> {

    private Function<DeleteModel, R> decoratorFunction;
    private SqlTable table;
    
    private DeleteDSL(SqlTable table, Function<DeleteModel, R> decoratorFunction) {
        this.table = table;
        this.decoratorFunction = decoratorFunction;
    }
    
    public <T> DeleteWhereBuilder where(BindableColumn<T> column, VisitableCondition<T> condition) {
        return new DeleteWhereBuilder(column, condition);
    }
    
    public <T> DeleteWhereBuilder where(BindableColumn<T> column, VisitableCondition<T> condition,
            SqlCriterion<?>...subCriteria) {
        return new DeleteWhereBuilder(column, condition, subCriteria);
    }
    
    /**
     * WARNING! Calling this method could result in an delete statement that deletes
     * all rows in a table.
     * 
     * @return the model class
     */
    public R build() {
        DeleteModel deleteModel = new DeleteModel.Builder()
                .withTable(table)
                .build();
        return decoratorFunction.apply(deleteModel);
    }
    
    public static <R> DeleteDSL<R> genericDeleteFrom(SqlTable table, Function<DeleteModel, R> decoratorFunction) {
        return new DeleteDSL<>(table, decoratorFunction);
    }
    
    public static DeleteDSL<DeleteModel> deleteFrom(SqlTable table) {
        return genericDeleteFrom(table, Function.identity());
    }
    
    public static DeleteDSL<MyBatis3DeleteModel> deleteFrom(SqlTable table, Function<DeleteStatement, Integer> mapperMethod) {
        return genericDeleteFrom(table, decorate(mapperMethod));
    }
    
    private static Function<DeleteModel, MyBatis3DeleteModel> decorate(Function<DeleteStatement, Integer> mapperMethod) {
        return deleteModel -> MyBatis3DeleteModel.of(deleteModel, mapperMethod);
    }
    
    public class DeleteWhereBuilder extends AbstractWhereDSL<DeleteWhereBuilder> {
        
        private <T> DeleteWhereBuilder(BindableColumn<T> column, VisitableCondition<T> condition) {
            super(column, condition);
        }
        
        private <T> DeleteWhereBuilder(BindableColumn<T> column, VisitableCondition<T> condition,
                SqlCriterion<?>...subCriteria) {
            super(column, condition, subCriteria);
        }
        
        public R build() {
            DeleteModel deleteModel = new DeleteModel.Builder()
                    .withTable(table)
                    .withWhereModel(buildWhereModel())
                    .build();
            return decoratorFunction.apply(deleteModel);
        }
        
        @Override
        protected DeleteWhereBuilder getThis() {
            return this;
        }
    }
}
