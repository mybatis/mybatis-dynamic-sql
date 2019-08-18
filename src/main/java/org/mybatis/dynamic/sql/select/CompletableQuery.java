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
package org.mybatis.dynamic.sql.select;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.VisitableCondition;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3SelectCompleter;

/**
 * This interface describes operations allowed for a select statement after the from and join clauses. This is
 * primarily to support {@link MyBatis3SelectCompleter}.
 * 
 * @author Jeff Butler
 *
 * @param <R> the model type created by these operations
 * 
 */
public interface CompletableQuery<R> extends Buildable<R> {
    QueryExpressionDSL<R>.QueryExpressionWhereBuilder where();

    <T> QueryExpressionDSL<R>.QueryExpressionWhereBuilder where(BindableColumn<T> column,
            VisitableCondition<T> condition);

    <T> QueryExpressionDSL<R>.QueryExpressionWhereBuilder where(BindableColumn<T> column,
            VisitableCondition<T> condition, SqlCriterion<?>... subCriteria);

    QueryExpressionDSL<R>.GroupByFinisher groupBy(BasicColumn...columns);
    
    SelectDSL<R> orderBy(SortSpecification...columns);

    SelectDSL<R>.LimitFinisher limit(long limit);

    SelectDSL<R>.OffsetFirstFinisher offset(long offset);

    SelectDSL<R>.FetchFirstFinisher fetchFirst(long fetchFirstRows);
}
