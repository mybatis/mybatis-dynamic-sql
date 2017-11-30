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
package org.mybatis.dynamic.sql;

import java.util.Arrays;
import java.util.List;

import org.mybatis.dynamic.sql.delete.DeleteDSL;
import org.mybatis.dynamic.sql.delete.DeleteModel;
import org.mybatis.dynamic.sql.insert.BatchInsertDSL;
import org.mybatis.dynamic.sql.insert.InsertDSL;
import org.mybatis.dynamic.sql.insert.InsertSelectDSL;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL.FromGatherer;
import org.mybatis.dynamic.sql.select.SelectDSL;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.SimpleSortSpecification;
import org.mybatis.dynamic.sql.select.aggregate.Avg;
import org.mybatis.dynamic.sql.select.aggregate.Count;
import org.mybatis.dynamic.sql.select.aggregate.CountAll;
import org.mybatis.dynamic.sql.select.aggregate.Max;
import org.mybatis.dynamic.sql.select.aggregate.Min;
import org.mybatis.dynamic.sql.select.aggregate.Sum;
import org.mybatis.dynamic.sql.select.function.Add;
import org.mybatis.dynamic.sql.select.function.Lower;
import org.mybatis.dynamic.sql.select.function.Substring;
import org.mybatis.dynamic.sql.select.function.Upper;
import org.mybatis.dynamic.sql.select.join.EqualTo;
import org.mybatis.dynamic.sql.select.join.JoinCondition;
import org.mybatis.dynamic.sql.select.join.JoinCriterion;
import org.mybatis.dynamic.sql.update.UpdateDSL;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.where.condition.IsBetween;
import org.mybatis.dynamic.sql.where.condition.IsEqualTo;
import org.mybatis.dynamic.sql.where.condition.IsEqualToWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsGreaterThan;
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanOrEqualTo;
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanOrEqualToWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsIn;
import org.mybatis.dynamic.sql.where.condition.IsInCaseInsensitive;
import org.mybatis.dynamic.sql.where.condition.IsInWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsLessThan;
import org.mybatis.dynamic.sql.where.condition.IsLessThanOrEqualTo;
import org.mybatis.dynamic.sql.where.condition.IsLessThanOrEqualToWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsLessThanWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsLike;
import org.mybatis.dynamic.sql.where.condition.IsLikeCaseInsensitive;
import org.mybatis.dynamic.sql.where.condition.IsNotBetween;
import org.mybatis.dynamic.sql.where.condition.IsNotEqualTo;
import org.mybatis.dynamic.sql.where.condition.IsNotEqualToWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsNotIn;
import org.mybatis.dynamic.sql.where.condition.IsNotInCaseInsensitive;
import org.mybatis.dynamic.sql.where.condition.IsNotInWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsNotLike;
import org.mybatis.dynamic.sql.where.condition.IsNotLikeCaseInsensitive;
import org.mybatis.dynamic.sql.where.condition.IsNotNull;
import org.mybatis.dynamic.sql.where.condition.IsNull;

public interface SqlBuilder {

    // statements
    static DeleteDSL<DeleteModel> deleteFrom(SqlTable table) {
        return DeleteDSL.deleteFrom(table);
    }

    static <T> InsertDSL.IntoGatherer<T> insert(T record) {
        return InsertDSL.insert(record);
    }
    
    @SafeVarargs
    static <T> BatchInsertDSL.IntoGatherer<T> insert(T...records) {
        return BatchInsertDSL.insert(records);
    }
    
    static <T> BatchInsertDSL.IntoGatherer<T> insert(List<T> records) {
        return BatchInsertDSL.insert(records);
    }
    
    static InsertSelectDSL.InsertColumnGatherer insertInto(SqlTable table) {
        return InsertSelectDSL.insertInto(table);
    }
    
    static FromGatherer<SelectModel> select(BasicColumn...selectList) {
        return SelectDSL.select(selectList);
    }
    
    static FromGatherer<SelectModel> selectDistinct(BasicColumn...selectList) {
        return SelectDSL.selectDistinct(selectList);
    }
    
    static UpdateDSL<UpdateModel> update(SqlTable table) {
        return UpdateDSL.update(table);
    }

    // where condition connectors
    static <T> SqlCriterion<T> or(BindableColumn<T> column, VisitableCondition<T> condition) {
        return new SqlCriterion.Builder<T>()
                .withConnector("or") //$NON-NLS-1$
                .withColumn(column)
                .withCondition(condition)
                .build();
    }

    static <T> SqlCriterion<T> or(BindableColumn<T> column, VisitableCondition<T> condition,
            SqlCriterion<?>...subCriteria) {
        return new SqlCriterion.Builder<T>()
                .withConnector("or") //$NON-NLS-1$
                .withColumn(column)
                .withCondition(condition)
                .withSubCriteria(Arrays.asList(subCriteria))
                .build();
    }

    static <T> SqlCriterion<T> and(BindableColumn<T> column, VisitableCondition<T> condition) {
        return new SqlCriterion.Builder<T>()
                .withConnector("and") //$NON-NLS-1$
                .withColumn(column)
                .withCondition(condition)
                .build();
    }

    static <T> SqlCriterion<T> and(BindableColumn<T> column, VisitableCondition<T> condition,
            SqlCriterion<?>...subCriteria) {
        return new SqlCriterion.Builder<T>()
                .withConnector("and") //$NON-NLS-1$
                .withColumn(column)
                .withCondition(condition)
                .withSubCriteria(Arrays.asList(subCriteria))
                .build();
    }

    // join support
    static JoinCriterion and(BasicColumn joinColumn, JoinCondition joinCondition) {
        return new JoinCriterion.Builder()
                .withJoinColumn(joinColumn)
                .withJoinCondition(joinCondition)
                .withConnector("and") //$NON-NLS-1$
                .build();
    }
    
    static EqualTo equalTo(BasicColumn column) {
        return new EqualTo(column);
    }

    // aggregate support
    static CountAll count() {
        return new CountAll();
    }
    
    static Count count(BasicColumn column) {
        return Count.of(column);
    }
    
    static Max max(BasicColumn column) {
        return Max.of(column);
    }
    
    static Min min(BasicColumn column) {
        return Min.of(column);
    }

    static Avg avg(BasicColumn column) {
        return Avg.of(column);
    }

    static Sum sum(BasicColumn column) {
        return Sum.of(column);
    }

    // functions
    static <T extends Number> Add<T> add(BindableColumn<T> column1, BindableColumn<T> column2) {
        return Add.of(column1, column2);
    }
    
    static Lower lower(BindableColumn<String> column) {
        return Lower.of(column);
    }
    
    static Substring substring(BindableColumn<String> column, int offset, int length) {
        return Substring.of(column, offset, length);
    }
    
    static Upper upper(BindableColumn<String> column) {
        return Upper.of(column);
    }
    
    // conditions for all data types
    static <T> IsNull<T> isNull() {
        return new IsNull<>();
    }

    static <T> IsNotNull<T> isNotNull() {
        return new IsNotNull<>();
    }

    static <T> IsEqualTo<T> isEqualTo(T value) {
        return IsEqualTo.of(value);
    }

    static <T> IsEqualToWithSubselect<T> isEqualTo(Buildable<SelectModel> selectModelBuilder) {
        return IsEqualToWithSubselect.of(selectModelBuilder);
    }

    static <T> IsNotEqualTo<T> isNotEqualTo(T value) {
        return IsNotEqualTo.of(value);
    }

    static <T> IsNotEqualToWithSubselect<T> isNotEqualTo(Buildable<SelectModel> selectModelBuilder) {
        return IsNotEqualToWithSubselect.of(selectModelBuilder);
    }

    static <T> IsGreaterThan<T> isGreaterThan(T value) {
        return IsGreaterThan.of(value);
    }
    
    static <T> IsGreaterThanWithSubselect<T> isGreaterThan(Buildable<SelectModel> selectModelBuilder) {
        return IsGreaterThanWithSubselect.of(selectModelBuilder);
    }
    
    static <T> IsGreaterThanOrEqualTo<T> isGreaterThanOrEqualTo(T value) {
        return IsGreaterThanOrEqualTo.of(value);
    }
    
    static <T> IsGreaterThanOrEqualToWithSubselect<T> isGreaterThanOrEqualTo(
            Buildable<SelectModel> selectModelBuilder) {
        return IsGreaterThanOrEqualToWithSubselect.of(selectModelBuilder);
    }
    
    static <T> IsLessThan<T> isLessThan(T value) {
        return IsLessThan.of(value);
    }
    
    static <T> IsLessThanWithSubselect<T> isLessThan(Buildable<SelectModel> selectModelBuilder) {
        return IsLessThanWithSubselect.of(selectModelBuilder);
    }
    
    static <T> IsLessThanOrEqualTo<T> isLessThanOrEqualTo(T value) {
        return IsLessThanOrEqualTo.of(value);
    }
    
    static <T> IsLessThanOrEqualToWithSubselect<T> isLessThanOrEqualTo(Buildable<SelectModel> selectModelBuilder) {
        return IsLessThanOrEqualToWithSubselect.of(selectModelBuilder);
    }
    
    @SafeVarargs
    static <T> IsIn<T> isIn(T...values) {
        return isIn(Arrays.asList(values));
    }

    static <T> IsIn<T> isIn(List<T> values) {
        return IsIn.of(values);
    }
    
    static <T> IsInWithSubselect<T> isIn(Buildable<SelectModel> selectModelBuilder) {
        return IsInWithSubselect.of(selectModelBuilder);
    }

    @SafeVarargs
    static <T> IsNotIn<T> isNotIn(T...values) {
        return isNotIn(Arrays.asList(values));
    }
    
    static <T> IsNotIn<T> isNotIn(List<T> values) {
        return IsNotIn.of(values);
    }
    
    static <T> IsNotInWithSubselect<T> isNotIn(Buildable<SelectModel> selectModelBuilder) {
        return IsNotInWithSubselect.of(selectModelBuilder);
    }

    static <T> IsBetween.Builder<T> isBetween(T value1) {
        return IsBetween.isBetween(value1);
    }
    
    static <T> IsNotBetween.Builder<T> isNotBetween(T value1) {
        return IsNotBetween.isNotBetween(value1);
    }
    
    // conditions for strings only
    static IsLike isLike(String value) {
        return IsLike.of(value);
    }
    
    static IsLikeCaseInsensitive isLikeCaseInsensitive(String value) {
        return IsLikeCaseInsensitive.of(value);
    }
    
    static IsNotLike isNotLike(String value) {
        return IsNotLike.of(value);
    }
    
    static IsNotLikeCaseInsensitive isNotLikeCaseInsensitive(String value) {
        return IsNotLikeCaseInsensitive.of(value);
    }

    static IsInCaseInsensitive isInCaseInsensitive(String...values) {
        return IsInCaseInsensitive.of(Arrays.asList(values));
    }

    static IsNotInCaseInsensitive isNotInCaseInsensitive(String...values) {
        return IsNotInCaseInsensitive.of(Arrays.asList(values));
    }
    
    // order by support
    static SortSpecification sortColumn(String name) {
        return SimpleSortSpecification.of(name);
    }
}
