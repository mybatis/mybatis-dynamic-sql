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
package org.mybatis.dynamic.sql;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

import org.mybatis.dynamic.sql.delete.DeleteDSL;
import org.mybatis.dynamic.sql.delete.DeleteModel;
import org.mybatis.dynamic.sql.insert.BatchInsertDSL;
import org.mybatis.dynamic.sql.insert.InsertDSL;
import org.mybatis.dynamic.sql.insert.InsertSelectDSL;
import org.mybatis.dynamic.sql.insert.MultiRowInsertDSL;
import org.mybatis.dynamic.sql.select.CountDSL;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL.FromGatherer;
import org.mybatis.dynamic.sql.select.SelectDSL;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.SimpleSortSpecification;
import org.mybatis.dynamic.sql.select.aggregate.Avg;
import org.mybatis.dynamic.sql.select.aggregate.Count;
import org.mybatis.dynamic.sql.select.aggregate.CountAll;
import org.mybatis.dynamic.sql.select.aggregate.CountDistinct;
import org.mybatis.dynamic.sql.select.aggregate.Max;
import org.mybatis.dynamic.sql.select.aggregate.Min;
import org.mybatis.dynamic.sql.select.aggregate.Sum;
import org.mybatis.dynamic.sql.select.function.Add;
import org.mybatis.dynamic.sql.select.function.Divide;
import org.mybatis.dynamic.sql.select.function.Lower;
import org.mybatis.dynamic.sql.select.function.Multiply;
import org.mybatis.dynamic.sql.select.function.Substring;
import org.mybatis.dynamic.sql.select.function.Subtract;
import org.mybatis.dynamic.sql.select.function.Upper;
import org.mybatis.dynamic.sql.select.join.EqualTo;
import org.mybatis.dynamic.sql.select.join.JoinCondition;
import org.mybatis.dynamic.sql.select.join.JoinCriterion;
import org.mybatis.dynamic.sql.update.UpdateDSL;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.where.WhereDSL;
import org.mybatis.dynamic.sql.where.condition.IsBetween;
import org.mybatis.dynamic.sql.where.condition.IsBetweenWhenPresent;
import org.mybatis.dynamic.sql.where.condition.IsEqualTo;
import org.mybatis.dynamic.sql.where.condition.IsEqualToColumn;
import org.mybatis.dynamic.sql.where.condition.IsEqualToWhenPresent;
import org.mybatis.dynamic.sql.where.condition.IsEqualToWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsGreaterThan;
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanColumn;
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanOrEqualTo;
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanOrEqualToColumn;
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanOrEqualToWhenPresent;
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanOrEqualToWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanWhenPresent;
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsIn;
import org.mybatis.dynamic.sql.where.condition.IsInCaseInsensitive;
import org.mybatis.dynamic.sql.where.condition.IsInCaseInsensitiveWhenPresent;
import org.mybatis.dynamic.sql.where.condition.IsInWhenPresent;
import org.mybatis.dynamic.sql.where.condition.IsInWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsLessThan;
import org.mybatis.dynamic.sql.where.condition.IsLessThanColumn;
import org.mybatis.dynamic.sql.where.condition.IsLessThanOrEqualTo;
import org.mybatis.dynamic.sql.where.condition.IsLessThanOrEqualToColumn;
import org.mybatis.dynamic.sql.where.condition.IsLessThanOrEqualToWhenPresent;
import org.mybatis.dynamic.sql.where.condition.IsLessThanOrEqualToWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsLessThanWhenPresent;
import org.mybatis.dynamic.sql.where.condition.IsLessThanWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsLike;
import org.mybatis.dynamic.sql.where.condition.IsLikeCaseInsensitive;
import org.mybatis.dynamic.sql.where.condition.IsLikeCaseInsensitiveWhenPresent;
import org.mybatis.dynamic.sql.where.condition.IsLikeWhenPresent;
import org.mybatis.dynamic.sql.where.condition.IsNotBetween;
import org.mybatis.dynamic.sql.where.condition.IsNotBetweenWhenPresent;
import org.mybatis.dynamic.sql.where.condition.IsNotEqualTo;
import org.mybatis.dynamic.sql.where.condition.IsNotEqualToColumn;
import org.mybatis.dynamic.sql.where.condition.IsNotEqualToWhenPresent;
import org.mybatis.dynamic.sql.where.condition.IsNotEqualToWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsNotIn;
import org.mybatis.dynamic.sql.where.condition.IsNotInCaseInsensitive;
import org.mybatis.dynamic.sql.where.condition.IsNotInCaseInsensitiveWhenPresent;
import org.mybatis.dynamic.sql.where.condition.IsNotInWhenPresent;
import org.mybatis.dynamic.sql.where.condition.IsNotInWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsNotLike;
import org.mybatis.dynamic.sql.where.condition.IsNotLikeCaseInsensitive;
import org.mybatis.dynamic.sql.where.condition.IsNotLikeCaseInsensitiveWhenPresent;
import org.mybatis.dynamic.sql.where.condition.IsNotLikeWhenPresent;
import org.mybatis.dynamic.sql.where.condition.IsNotNull;
import org.mybatis.dynamic.sql.where.condition.IsNull;

public interface SqlBuilder {

    // statements
    static CountDSL<SelectModel> countFrom(SqlTable table) {
        return CountDSL.countFrom(table);
    }
    
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
    
    static <T> BatchInsertDSL.IntoGatherer<T> insert(Collection<T> records) {
        return BatchInsertDSL.insert(records);
    }
    
    @SafeVarargs
    static <T> MultiRowInsertDSL.IntoGatherer<T> insertMultiple(T...records) {
        return MultiRowInsertDSL.insert(records);
    }
    
    static <T> MultiRowInsertDSL.IntoGatherer<T> insertMultiple(Collection<T> records) {
        return MultiRowInsertDSL.insert(records);
    }
    
    static InsertSelectDSL.InsertColumnGatherer insertInto(SqlTable table) {
        return InsertSelectDSL.insertInto(table);
    }
    
    static FromGatherer<SelectModel> select(BasicColumn...selectList) {
        return SelectDSL.select(selectList);
    }
    
    static FromGatherer<SelectModel> select(Collection<BasicColumn> selectList) {
        return SelectDSL.select(selectList);
    }
    
    static FromGatherer<SelectModel> selectDistinct(BasicColumn...selectList) {
        return SelectDSL.selectDistinct(selectList);
    }
    
    static FromGatherer<SelectModel> selectDistinct(Collection<BasicColumn> selectList) {
        return SelectDSL.selectDistinct(selectList);
    }
    
    static UpdateDSL<UpdateModel> update(SqlTable table) {
        return UpdateDSL.update(table);
    }

    static WhereDSL where() {
        return WhereDSL.where();
    }
    
    static <T> WhereDSL where(BindableColumn<T> column, VisitableCondition<T> condition) {
        return WhereDSL.where(column, condition);
    }
    
    static <T> WhereDSL where(BindableColumn<T> column, VisitableCondition<T> condition,
            SqlCriterion<?>... subCriteria) {
        return WhereDSL.where(column, condition, subCriteria);
    }
    
    // where condition connectors
    static <T> SqlCriterion<T> or(BindableColumn<T> column, VisitableCondition<T> condition) {
        return SqlCriterion.withColumn(column)
                .withConnector("or") //$NON-NLS-1$
                .withCondition(condition)
                .build();
    }

    static <T> SqlCriterion<T> or(BindableColumn<T> column, VisitableCondition<T> condition,
            SqlCriterion<?>...subCriteria) {
        return SqlCriterion.withColumn(column)
                .withConnector("or") //$NON-NLS-1$
                .withCondition(condition)
                .withSubCriteria(Arrays.asList(subCriteria))
                .build();
    }

    static <T> SqlCriterion<T> and(BindableColumn<T> column, VisitableCondition<T> condition) {
        return SqlCriterion.withColumn(column)
                .withConnector("and") //$NON-NLS-1$
                .withCondition(condition)
                .build();
    }

    static <T> SqlCriterion<T> and(BindableColumn<T> column, VisitableCondition<T> condition,
            SqlCriterion<?>...subCriteria) {
        return SqlCriterion.withColumn(column)
                .withConnector("and") //$NON-NLS-1$
                .withCondition(condition)
                .withSubCriteria(Arrays.asList(subCriteria))
                .build();
    }

    // join support
    static JoinCriterion and(BasicColumn joinColumn, JoinCondition joinCondition) {
        return new JoinCriterion.Builder()
                .withConnector("and") //$NON-NLS-1$
                .withJoinColumn(joinColumn)
                .withJoinCondition(joinCondition)
                .build();
    }
    
    static JoinCriterion on(BasicColumn joinColumn, JoinCondition joinCondition) {
        return new JoinCriterion.Builder()
                .withConnector("on") //$NON-NLS-1$
                .withJoinColumn(joinColumn)
                .withJoinCondition(joinCondition)
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
    
    static CountDistinct countDistinct(BasicColumn column) {
        return CountDistinct.of(column);
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

    // constants
    static Constant constant(String constant) {
        return Constant.of(constant);
    }
    
    static StringConstant stringConstant(String constant) {
        return StringConstant.of(constant);
    }
    
    // functions
    @SafeVarargs
    static <T extends Number> Add<T> add(BindableColumn<T> firstColumn, BasicColumn secondColumn,
            BasicColumn... subsequentColumns) {
        return Add.of(firstColumn, secondColumn, Arrays.asList(subsequentColumns));
    }
    
    @SafeVarargs
    static <T extends Number> Divide<T> divide(BindableColumn<T> firstColumn, BasicColumn secondColumn,
            BasicColumn... subsequentColumns) {
        return Divide.of(firstColumn, secondColumn, Arrays.asList(subsequentColumns));
    }
    
    @SafeVarargs
    static <T extends Number> Multiply<T> multiply(BindableColumn<T> firstColumn, BasicColumn secondColumn,
            BasicColumn... subsequentColumns) {
        return Multiply.of(firstColumn, secondColumn, Arrays.asList(subsequentColumns));
    }
    
    @SafeVarargs
    static <T extends Number> Subtract<T> subtract(BindableColumn<T> firstColumn, BasicColumn secondColumn,
            BasicColumn... subsequentColumns) {
        return Subtract.of(firstColumn, secondColumn, Arrays.asList(subsequentColumns));
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
        return isEqualTo(() -> value);
    }

    static <T> IsEqualTo<T> isEqualTo(Supplier<T> valueSupplier) {
        return IsEqualTo.of(valueSupplier);
    }

    static <T> IsEqualToWithSubselect<T> isEqualTo(Buildable<SelectModel> selectModelBuilder) {
        return IsEqualToWithSubselect.of(selectModelBuilder);
    }
    
    static <T> IsEqualToColumn<T> isEqualTo(BasicColumn column) {
        return IsEqualToColumn.of(column);
    }

    static <T> IsEqualToWhenPresent<T> isEqualToWhenPresent(T value) {
        return isEqualToWhenPresent(() -> value);
    }

    static <T> IsEqualToWhenPresent<T> isEqualToWhenPresent(Supplier<T> valueSupplier) {
        return IsEqualToWhenPresent.of(valueSupplier);
    }
    
    static <T> IsNotEqualTo<T> isNotEqualTo(T value) {
        return isNotEqualTo(() -> value);
    }

    static <T> IsNotEqualTo<T> isNotEqualTo(Supplier<T> valueSupplier) {
        return IsNotEqualTo.of(valueSupplier);
    }

    static <T> IsNotEqualToWithSubselect<T> isNotEqualTo(Buildable<SelectModel> selectModelBuilder) {
        return IsNotEqualToWithSubselect.of(selectModelBuilder);
    }

    static <T> IsNotEqualToColumn<T> isNotEqualTo(BasicColumn column) {
        return IsNotEqualToColumn.of(column);
    }

    static <T> IsNotEqualToWhenPresent<T> isNotEqualToWhenPresent(T value) {
        return isNotEqualToWhenPresent(() -> value);
    }

    static <T> IsNotEqualToWhenPresent<T> isNotEqualToWhenPresent(Supplier<T> valueSupplier) {
        return IsNotEqualToWhenPresent.of(valueSupplier);
    }
    
    static <T> IsGreaterThan<T> isGreaterThan(T value) {
        return isGreaterThan(() -> value);
    }
    
    static <T> IsGreaterThan<T> isGreaterThan(Supplier<T> valueSupplier) {
        return IsGreaterThan.of(valueSupplier);
    }
    
    static <T> IsGreaterThanWithSubselect<T> isGreaterThan(Buildable<SelectModel> selectModelBuilder) {
        return IsGreaterThanWithSubselect.of(selectModelBuilder);
    }
    
    static <T> IsGreaterThanColumn<T> isGreaterThan(BasicColumn column) {
        return IsGreaterThanColumn.of(column);
    }

    static <T> IsGreaterThanWhenPresent<T> isGreaterThanWhenPresent(T value) {
        return isGreaterThanWhenPresent(() -> value);
    }
    
    static <T> IsGreaterThanWhenPresent<T> isGreaterThanWhenPresent(Supplier<T> valueSupplier) {
        return IsGreaterThanWhenPresent.of(valueSupplier);
    }
    
    static <T> IsGreaterThanOrEqualTo<T> isGreaterThanOrEqualTo(T value) {
        return isGreaterThanOrEqualTo(() -> value);
    }
    
    static <T> IsGreaterThanOrEqualTo<T> isGreaterThanOrEqualTo(Supplier<T> valueSupplier) {
        return IsGreaterThanOrEqualTo.of(valueSupplier);
    }
    
    static <T> IsGreaterThanOrEqualToWithSubselect<T> isGreaterThanOrEqualTo(
            Buildable<SelectModel> selectModelBuilder) {
        return IsGreaterThanOrEqualToWithSubselect.of(selectModelBuilder);
    }
    
    static <T> IsGreaterThanOrEqualToColumn<T> isGreaterThanOrEqualTo(BasicColumn column) {
        return IsGreaterThanOrEqualToColumn.of(column);
    }

    static <T> IsGreaterThanOrEqualToWhenPresent<T> isGreaterThanOrEqualToWhenPresent(T value) {
        return isGreaterThanOrEqualToWhenPresent(() -> value);
    }
    
    static <T> IsGreaterThanOrEqualToWhenPresent<T> isGreaterThanOrEqualToWhenPresent(Supplier<T> valueSupplier) {
        return IsGreaterThanOrEqualToWhenPresent.of(valueSupplier);
    }
    
    static <T> IsLessThan<T> isLessThan(T value) {
        return isLessThan(() -> value);
    }
    
    static <T> IsLessThan<T> isLessThan(Supplier<T> valueSupplier) {
        return IsLessThan.of(valueSupplier);
    }
    
    static <T> IsLessThanWithSubselect<T> isLessThan(Buildable<SelectModel> selectModelBuilder) {
        return IsLessThanWithSubselect.of(selectModelBuilder);
    }
    
    static <T> IsLessThanColumn<T> isLessThan(BasicColumn column) {
        return IsLessThanColumn.of(column);
    }

    static <T> IsLessThanWhenPresent<T> isLessThanWhenPresent(T value) {
        return isLessThanWhenPresent(() -> value);
    }
    
    static <T> IsLessThanWhenPresent<T> isLessThanWhenPresent(Supplier<T> valueSupplier) {
        return IsLessThanWhenPresent.of(valueSupplier);
    }
    
    static <T> IsLessThanOrEqualTo<T> isLessThanOrEqualTo(T value) {
        return isLessThanOrEqualTo(() -> value);
    }
    
    static <T> IsLessThanOrEqualTo<T> isLessThanOrEqualTo(Supplier<T> valueSupplier) {
        return IsLessThanOrEqualTo.of(valueSupplier);
    }
    
    static <T> IsLessThanOrEqualToWithSubselect<T> isLessThanOrEqualTo(Buildable<SelectModel> selectModelBuilder) {
        return IsLessThanOrEqualToWithSubselect.of(selectModelBuilder);
    }
    
    static <T> IsLessThanOrEqualToColumn<T> isLessThanOrEqualTo(BasicColumn column) {
        return IsLessThanOrEqualToColumn.of(column);
    }

    static <T> IsLessThanOrEqualToWhenPresent<T> isLessThanOrEqualToWhenPresent(T value) {
        return isLessThanOrEqualToWhenPresent(() -> value);
    }
    
    static <T> IsLessThanOrEqualToWhenPresent<T> isLessThanOrEqualToWhenPresent(Supplier<T> valueSupplier) {
        return IsLessThanOrEqualToWhenPresent.of(valueSupplier);
    }
    
    @SafeVarargs
    static <T> IsIn<T> isIn(T...values) {
        return isIn(Arrays.asList(values));
    }

    static <T> IsIn<T> isIn(Collection<T> values) {
        return IsIn.of(values);
    }
    
    static <T> IsInWithSubselect<T> isIn(Buildable<SelectModel> selectModelBuilder) {
        return IsInWithSubselect.of(selectModelBuilder);
    }

    @SafeVarargs
    static <T> IsInWhenPresent<T> isInWhenPresent(T...values) {
        return isInWhenPresent(Arrays.asList(values));
    }

    static <T> IsInWhenPresent<T> isInWhenPresent(Collection<T> values) {
        return IsInWhenPresent.of(values);
    }
    
    @SafeVarargs
    static <T> IsNotIn<T> isNotIn(T...values) {
        return isNotIn(Arrays.asList(values));
    }
    
    static <T> IsNotIn<T> isNotIn(Collection<T> values) {
        return IsNotIn.of(values);
    }
    
    static <T> IsNotInWithSubselect<T> isNotIn(Buildable<SelectModel> selectModelBuilder) {
        return IsNotInWithSubselect.of(selectModelBuilder);
    }

    @SafeVarargs
    static <T> IsNotInWhenPresent<T> isNotInWhenPresent(T...values) {
        return isNotInWhenPresent(Arrays.asList(values));
    }

    static <T> IsNotInWhenPresent<T> isNotInWhenPresent(Collection<T> values) {
        return IsNotInWhenPresent.of(values);
    }
    
    static <T> IsBetween.Builder<T> isBetween(T value1) {
        return isBetween(() -> value1);
    }
    
    static <T> IsBetween.Builder<T> isBetween(Supplier<T> valueSupplier1) {
        return IsBetween.isBetween(valueSupplier1);
    }
    
    static <T> IsBetweenWhenPresent.Builder<T> isBetweenWhenPresent(T value1) {
        return isBetweenWhenPresent(() -> value1);
    }
    
    static <T> IsBetweenWhenPresent.Builder<T> isBetweenWhenPresent(Supplier<T> valueSupplier1) {
        return IsBetweenWhenPresent.isBetweenWhenPresent(valueSupplier1);
    }
    
    static <T> IsNotBetween.Builder<T> isNotBetween(T value1) {
        return isNotBetween(() -> value1);
    }
    
    static <T> IsNotBetween.Builder<T> isNotBetween(Supplier<T> valueSupplier1) {
        return IsNotBetween.isNotBetween(valueSupplier1);
    }

    static <T> IsNotBetweenWhenPresent.Builder<T> isNotBetweenWhenPresent(T value1) {
        return isNotBetweenWhenPresent(() -> value1);
    }
    
    static <T> IsNotBetweenWhenPresent.Builder<T> isNotBetweenWhenPresent(Supplier<T> valueSupplier1) {
        return IsNotBetweenWhenPresent.isNotBetweenWhenPresent(valueSupplier1);
    }
    
    // for string columns, but generic for columns with type handlers
    static <T> IsLike<T> isLike(T value) {
        return isLike(() -> value);
    }
    
    static <T> IsLike<T> isLike(Supplier<T> valueSupplier) {
        return IsLike.of(valueSupplier);
    }
    
    static <T> IsLikeWhenPresent<T> isLikeWhenPresent(T value) {
        return isLikeWhenPresent(() -> value);
    }
    
    static <T> IsLikeWhenPresent<T> isLikeWhenPresent(Supplier<T> valueSupplier) {
        return IsLikeWhenPresent.of(valueSupplier);
    }
    
    static <T> IsNotLike<T> isNotLike(T value) {
        return isNotLike(() -> value);
    }
    
    static <T> IsNotLike<T> isNotLike(Supplier<T> valueSupplier) {
        return IsNotLike.of(valueSupplier);
    }
    
    static <T> IsNotLikeWhenPresent<T> isNotLikeWhenPresent(T value) {
        return isNotLikeWhenPresent(() -> value);
    }
    
    static <T> IsNotLikeWhenPresent<T> isNotLikeWhenPresent(Supplier<T> valueSupplier) {
        return IsNotLikeWhenPresent.of(valueSupplier);
    }

    // shortcuts for booleans
    static IsEqualTo<Boolean> isTrue() {
        return isEqualTo(Boolean.TRUE);
    }

    static IsEqualTo<Boolean> isFalse() {
        return isEqualTo(Boolean.FALSE);
    }

    // conditions for strings only
    static IsLikeCaseInsensitive isLikeCaseInsensitive(String value) {
        return isLikeCaseInsensitive(() -> value);
    }
    
    static IsLikeCaseInsensitive isLikeCaseInsensitive(Supplier<String> valueSupplier) {
        return IsLikeCaseInsensitive.of(valueSupplier);
    }
    
    static IsLikeCaseInsensitiveWhenPresent isLikeCaseInsensitiveWhenPresent(String value) {
        return isLikeCaseInsensitiveWhenPresent(() -> value);
    }
    
    static IsLikeCaseInsensitiveWhenPresent isLikeCaseInsensitiveWhenPresent(Supplier<String> valueSupplier) {
        return IsLikeCaseInsensitiveWhenPresent.of(valueSupplier);
    }
    
    static IsNotLikeCaseInsensitive isNotLikeCaseInsensitive(String value) {
        return isNotLikeCaseInsensitive(() -> value);
    }

    static IsNotLikeCaseInsensitive isNotLikeCaseInsensitive(Supplier<String> valueSupplier) {
        return IsNotLikeCaseInsensitive.of(valueSupplier);
    }

    static IsNotLikeCaseInsensitiveWhenPresent isNotLikeCaseInsensitiveWhenPresent(String value) {
        return isNotLikeCaseInsensitiveWhenPresent(() -> value);
    }
    
    static IsNotLikeCaseInsensitiveWhenPresent isNotLikeCaseInsensitiveWhenPresent(Supplier<String> valueSupplier) {
        return IsNotLikeCaseInsensitiveWhenPresent.of(valueSupplier);
    }
    
    static IsInCaseInsensitive isInCaseInsensitive(String...values) {
        return isInCaseInsensitive(Arrays.asList(values));
    }

    static IsInCaseInsensitive isInCaseInsensitive(Collection<String> values) {
        return IsInCaseInsensitive.of(values);
    }

    static IsInCaseInsensitiveWhenPresent isInCaseInsensitiveWhenPresent(String...values) {
        return isInCaseInsensitiveWhenPresent(Arrays.asList(values));
    }

    static IsInCaseInsensitiveWhenPresent isInCaseInsensitiveWhenPresent(Collection<String> values) {
        return IsInCaseInsensitiveWhenPresent.of(values);
    }

    static IsNotInCaseInsensitive isNotInCaseInsensitive(String...values) {
        return isNotInCaseInsensitive(Arrays.asList(values));
    }
    
    static IsNotInCaseInsensitive isNotInCaseInsensitive(Collection<String> values) {
        return IsNotInCaseInsensitive.of(values);
    }
    
    static IsNotInCaseInsensitiveWhenPresent isNotInCaseInsensitiveWhenPresent(String...values) {
        return isNotInCaseInsensitiveWhenPresent(Arrays.asList(values));
    }

    static IsNotInCaseInsensitiveWhenPresent isNotInCaseInsensitiveWhenPresent(Collection<String> values) {
        return IsNotInCaseInsensitiveWhenPresent.of(values);
    }

    // order by support
    static SortSpecification sortColumn(String name) {
        return SimpleSortSpecification.of(name);
    }
}
