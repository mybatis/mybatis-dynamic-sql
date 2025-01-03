/*
 *    Copyright 2016-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
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
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.delete.DeleteDSL;
import org.mybatis.dynamic.sql.delete.DeleteModel;
import org.mybatis.dynamic.sql.insert.BatchInsertDSL;
import org.mybatis.dynamic.sql.insert.GeneralInsertDSL;
import org.mybatis.dynamic.sql.insert.InsertDSL;
import org.mybatis.dynamic.sql.insert.InsertSelectDSL;
import org.mybatis.dynamic.sql.insert.MultiRowInsertDSL;
import org.mybatis.dynamic.sql.select.ColumnSortSpecification;
import org.mybatis.dynamic.sql.select.CountDSL;
import org.mybatis.dynamic.sql.select.HavingDSL;
import org.mybatis.dynamic.sql.select.MultiSelectDSL;
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
import org.mybatis.dynamic.sql.select.caseexpression.SearchedCaseDSL;
import org.mybatis.dynamic.sql.select.caseexpression.SimpleCaseDSL;
import org.mybatis.dynamic.sql.select.function.Add;
import org.mybatis.dynamic.sql.select.function.Cast;
import org.mybatis.dynamic.sql.select.function.Concat;
import org.mybatis.dynamic.sql.select.function.Concatenate;
import org.mybatis.dynamic.sql.select.function.Divide;
import org.mybatis.dynamic.sql.select.function.Lower;
import org.mybatis.dynamic.sql.select.function.Multiply;
import org.mybatis.dynamic.sql.select.function.OperatorFunction;
import org.mybatis.dynamic.sql.select.function.Substring;
import org.mybatis.dynamic.sql.select.function.Subtract;
import org.mybatis.dynamic.sql.select.function.Upper;
import org.mybatis.dynamic.sql.update.UpdateDSL;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.where.WhereDSL;
import org.mybatis.dynamic.sql.where.condition.IsBetween;
import org.mybatis.dynamic.sql.where.condition.IsEqualTo;
import org.mybatis.dynamic.sql.where.condition.IsEqualToColumn;
import org.mybatis.dynamic.sql.where.condition.IsEqualToWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsGreaterThan;
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanColumn;
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanOrEqualTo;
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanOrEqualToColumn;
import org.mybatis.dynamic.sql.where.condition.IsGreaterThanOrEqualToWithSubselect;
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
import org.mybatis.dynamic.sql.where.condition.IsLessThanOrEqualToWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsLessThanWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsLike;
import org.mybatis.dynamic.sql.where.condition.IsLikeCaseInsensitive;
import org.mybatis.dynamic.sql.where.condition.IsNotBetween;
import org.mybatis.dynamic.sql.where.condition.IsNotEqualTo;
import org.mybatis.dynamic.sql.where.condition.IsNotEqualToColumn;
import org.mybatis.dynamic.sql.where.condition.IsNotEqualToWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsNotIn;
import org.mybatis.dynamic.sql.where.condition.IsNotInCaseInsensitive;
import org.mybatis.dynamic.sql.where.condition.IsNotInCaseInsensitiveWhenPresent;
import org.mybatis.dynamic.sql.where.condition.IsNotInWhenPresent;
import org.mybatis.dynamic.sql.where.condition.IsNotInWithSubselect;
import org.mybatis.dynamic.sql.where.condition.IsNotLike;
import org.mybatis.dynamic.sql.where.condition.IsNotLikeCaseInsensitive;
import org.mybatis.dynamic.sql.where.condition.IsNotNull;
import org.mybatis.dynamic.sql.where.condition.IsNull;

public interface SqlBuilder {

    // statements

    /**
     * Renders as select count(distinct column) from table...
     *
     * @param column
     *            the column to count
     *
     * @return the next step in the DSL
     */
    static CountDSL.FromGatherer<SelectModel> countDistinctColumn(BasicColumn column) {
        return CountDSL.countDistinct(column);
    }

    /**
     * Renders as select count(column) from table...
     *
     * @param column
     *            the column to count
     *
     * @return the next step in the DSL
     */
    static CountDSL.FromGatherer<SelectModel> countColumn(BasicColumn column) {
        return CountDSL.count(column);
    }

    /**
     * Renders as select count(*) from table...
     *
     * @param table
     *            the table to count
     *
     * @return the next step in the DSL
     */
    static CountDSL<SelectModel> countFrom(SqlTable table) {
        return CountDSL.countFrom(table);
    }

    static DeleteDSL<DeleteModel> deleteFrom(SqlTable table) {
        return DeleteDSL.deleteFrom(table);
    }

    static DeleteDSL<DeleteModel> deleteFrom(SqlTable table, String tableAlias) {
        return DeleteDSL.deleteFrom(table, tableAlias);
    }

    static <T> InsertDSL.IntoGatherer<T> insert(T row) {
        return InsertDSL.insert(row);
    }

    /**
     * Insert a Batch of records. The model object is structured to support bulk inserts with JDBC batch support.
     *
     * @param records
     *            records to insert
     * @param <T>
     *            the type of record to insert
     *
     * @return the next step in the DSL
     */
    @SafeVarargs
    static <T> BatchInsertDSL.IntoGatherer<T> insertBatch(T... records) {
        return BatchInsertDSL.insert(records);
    }

    /**
     * Insert a Batch of records. The model object is structured to support bulk inserts with JDBC batch support.
     *
     * @param records
     *            records to insert
     * @param <T>
     *            the type of record to insert
     *
     * @return the next step in the DSL
     */
    static <T> BatchInsertDSL.IntoGatherer<T> insertBatch(Collection<T> records) {
        return BatchInsertDSL.insert(records);
    }

    /**
     * Insert multiple records in a single statement. The model object is structured as a single insert statement with
     * multiple values clauses. This statement is suitable for use with a small number of records. It is not suitable
     * for large bulk inserts as it is possible to exceed the limit of parameter markers in a prepared statement.
     *
     * <p>For large bulk inserts, see {@link SqlBuilder#insertBatch(Object[])}
     * @param records
     *            records to insert
     * @param <T>
     *            the type of record to insert
     *
     * @return the next step in the DSL
     */
    @SafeVarargs
    static <T> MultiRowInsertDSL.IntoGatherer<T> insertMultiple(T... records) {
        return MultiRowInsertDSL.insert(records);
    }

    /**
     * Insert multiple records in a single statement. The model object is structured as a single insert statement with
     * multiple values clauses. This statement is suitable for use with a small number of records. It is not suitable
     * for large bulk inserts as it is possible to exceed the limit of parameter markers in a prepared statement.
     *
     * <p>For large bulk inserts, see {@link SqlBuilder#insertBatch(Collection)}
     * @param records
     *            records to insert
     * @param <T>
     *            the type of record to insert
     *
     * @return the next step in the DSL
     */
    static <T> MultiRowInsertDSL.IntoGatherer<T> insertMultiple(Collection<T> records) {
        return MultiRowInsertDSL.insert(records);
    }

    static InsertIntoNextStep insertInto(SqlTable table) {
        return new InsertIntoNextStep(table);
    }

    static FromGatherer<SelectModel> select(BasicColumn... selectList) {
        return SelectDSL.select(selectList);
    }

    static FromGatherer<SelectModel> select(Collection<? extends BasicColumn> selectList) {
        return SelectDSL.select(selectList);
    }

    static FromGatherer<SelectModel> selectDistinct(BasicColumn... selectList) {
        return SelectDSL.selectDistinct(selectList);
    }

    static FromGatherer<SelectModel> selectDistinct(Collection<? extends BasicColumn> selectList) {
        return SelectDSL.selectDistinct(selectList);
    }

    static MultiSelectDSL multiSelect(Buildable<SelectModel> selectModelBuilder) {
        return new MultiSelectDSL(selectModelBuilder);
    }

    static UpdateDSL<UpdateModel> update(SqlTable table) {
        return UpdateDSL.update(table);
    }

    static UpdateDSL<UpdateModel> update(SqlTable table, String tableAlias) {
        return UpdateDSL.update(table, tableAlias);
    }

    static WhereDSL.StandaloneWhereFinisher where() {
        return new WhereDSL().where();
    }

    static <T> WhereDSL.StandaloneWhereFinisher where(BindableColumn<T> column, VisitableCondition<T> condition,
                                                      AndOrCriteriaGroup... subCriteria) {
        return new WhereDSL().where(column, condition, subCriteria);
    }

    static WhereDSL.StandaloneWhereFinisher where(SqlCriterion initialCriterion, AndOrCriteriaGroup... subCriteria) {
        return new WhereDSL().where(initialCriterion, subCriteria);
    }

    static WhereDSL.StandaloneWhereFinisher where(ExistsPredicate existsPredicate, AndOrCriteriaGroup... subCriteria) {
        return new WhereDSL().where(existsPredicate, subCriteria);
    }

    static <T> HavingDSL.StandaloneHavingFinisher having(BindableColumn<T> column, VisitableCondition<T> condition,
                                              AndOrCriteriaGroup... subCriteria) {
        return new HavingDSL().having(column, condition, subCriteria);
    }

    static HavingDSL.StandaloneHavingFinisher having(SqlCriterion initialCriterion, AndOrCriteriaGroup... subCriteria) {
        return new HavingDSL().having(initialCriterion, subCriteria);
    }

    // where condition connectors
    static <T> CriteriaGroup group(BindableColumn<T> column, VisitableCondition<T> condition,
                                   AndOrCriteriaGroup... subCriteria) {
        return group(column, condition, Arrays.asList(subCriteria));
    }

    static <T> CriteriaGroup group(BindableColumn<T> column, VisitableCondition<T> condition,
                                   List<AndOrCriteriaGroup> subCriteria) {
        return new CriteriaGroup.Builder()
                .withInitialCriterion(new ColumnAndConditionCriterion.Builder<T>().withColumn(column)
                        .withCondition(condition).build())
                .withSubCriteria(subCriteria)
                .build();
    }

    static CriteriaGroup group(ExistsPredicate existsPredicate, AndOrCriteriaGroup... subCriteria) {
        return group(existsPredicate, Arrays.asList(subCriteria));
    }

    static CriteriaGroup group(ExistsPredicate existsPredicate, List<AndOrCriteriaGroup> subCriteria) {
        return new CriteriaGroup.Builder()
                .withInitialCriterion(new ExistsCriterion.Builder()
                        .withExistsPredicate(existsPredicate).build())
                .withSubCriteria(subCriteria)
                .build();
    }

    static CriteriaGroup group(SqlCriterion initialCriterion, AndOrCriteriaGroup... subCriteria) {
        return group(initialCriterion, Arrays.asList(subCriteria));
    }

    static CriteriaGroup group(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
        return new CriteriaGroup.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build();
    }

    static CriteriaGroup group(List<AndOrCriteriaGroup> subCriteria) {
        return new CriteriaGroup.Builder()
                .withSubCriteria(subCriteria)
                .build();
    }

    static <T> NotCriterion not(BindableColumn<T> column, VisitableCondition<T> condition,
                                AndOrCriteriaGroup... subCriteria) {
        return not(column, condition, Arrays.asList(subCriteria));
    }

    static <T> NotCriterion not(BindableColumn<T> column, VisitableCondition<T> condition,
                                List<AndOrCriteriaGroup> subCriteria) {
        return new NotCriterion.Builder()
                .withInitialCriterion(new ColumnAndConditionCriterion.Builder<T>().withColumn(column)
                        .withCondition(condition).build())
                .withSubCriteria(subCriteria)
                .build();
    }

    static NotCriterion not(ExistsPredicate existsPredicate, AndOrCriteriaGroup... subCriteria) {
        return not(existsPredicate, Arrays.asList(subCriteria));
    }

    static NotCriterion not(ExistsPredicate existsPredicate, List<AndOrCriteriaGroup> subCriteria) {
        return new NotCriterion.Builder()
                .withInitialCriterion(new ExistsCriterion.Builder()
                        .withExistsPredicate(existsPredicate).build())
                .withSubCriteria(subCriteria)
                .build();
    }

    static NotCriterion not(SqlCriterion initialCriterion, AndOrCriteriaGroup... subCriteria) {
        return not(initialCriterion, Arrays.asList(subCriteria));
    }

    static NotCriterion not(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
        return new NotCriterion.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build();
    }

    static NotCriterion not(List<AndOrCriteriaGroup> subCriteria) {
        return new NotCriterion.Builder()
                .withSubCriteria(subCriteria)
                .build();
    }

    static <T> AndOrCriteriaGroup or(BindableColumn<T> column, VisitableCondition<T> condition,
                                     AndOrCriteriaGroup... subCriteria) {
        return new AndOrCriteriaGroup.Builder()
                .withInitialCriterion(ColumnAndConditionCriterion.withColumn(column)
                        .withCondition(condition)
                        .build())
                .withConnector("or") //$NON-NLS-1$
                .withSubCriteria(Arrays.asList(subCriteria))
                .build();
    }

    static AndOrCriteriaGroup or(ExistsPredicate existsPredicate, AndOrCriteriaGroup... subCriteria) {
        return new AndOrCriteriaGroup.Builder()
                .withInitialCriterion(new ExistsCriterion.Builder()
                        .withExistsPredicate(existsPredicate).build())
                .withConnector("or") //$NON-NLS-1$
                .withSubCriteria(Arrays.asList(subCriteria))
                .build();
    }

    static AndOrCriteriaGroup or(SqlCriterion initialCriterion, AndOrCriteriaGroup... subCriteria) {
        return new AndOrCriteriaGroup.Builder()
                .withConnector("or") //$NON-NLS-1$
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(Arrays.asList(subCriteria))
                .build();
    }

    static AndOrCriteriaGroup or(List<AndOrCriteriaGroup> subCriteria) {
        return new AndOrCriteriaGroup.Builder()
                .withConnector("or") //$NON-NLS-1$
                .withSubCriteria(subCriteria)
                .build();
    }

    static <T> AndOrCriteriaGroup and(BindableColumn<T> column, VisitableCondition<T> condition,
                                      AndOrCriteriaGroup... subCriteria) {
        return new AndOrCriteriaGroup.Builder()
                .withInitialCriterion(ColumnAndConditionCriterion.withColumn(column)
                        .withCondition(condition)
                        .build())
                .withConnector("and") //$NON-NLS-1$
                .withSubCriteria(Arrays.asList(subCriteria))
                .build();
    }

    static AndOrCriteriaGroup and(ExistsPredicate existsPredicate, AndOrCriteriaGroup... subCriteria) {
        return new AndOrCriteriaGroup.Builder()
                .withInitialCriterion(new ExistsCriterion.Builder()
                        .withExistsPredicate(existsPredicate).build())
                .withConnector("and") //$NON-NLS-1$
                .withSubCriteria(Arrays.asList(subCriteria))
                .build();
    }

    static AndOrCriteriaGroup and(SqlCriterion initialCriterion, AndOrCriteriaGroup... subCriteria) {
        return new AndOrCriteriaGroup.Builder()
                .withConnector("and") //$NON-NLS-1$
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(Arrays.asList(subCriteria))
                .build();
    }

    static AndOrCriteriaGroup and(List<AndOrCriteriaGroup> subCriteria) {
        return new AndOrCriteriaGroup.Builder()
                .withConnector("and") //$NON-NLS-1$
                .withSubCriteria(subCriteria)
                .build();
    }

    // join support
    static <T> ColumnAndConditionCriterion<T> on(BindableColumn<T> joinColumn, VisitableCondition<T> joinCondition) {
        return ColumnAndConditionCriterion.withColumn(joinColumn)
                .withCondition(joinCondition)
                .build();
    }

    /**
     * Starting in version 2.0.0, this function is a synonym for {@link SqlBuilder#isEqualTo(BasicColumn)}.
     *
     * @param column the column
     * @param <T> the column type
     * @return an IsEqualToColumn condition
     * @deprecated since 2.0.0. Please replace with isEqualTo(column)
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    static <T> IsEqualToColumn<T> equalTo(BindableColumn<T> column) {
        return isEqualTo(column);
    }

    /**
     * Starting in version 2.0.0, this function is a synonym for {@link SqlBuilder#isEqualTo(Object)}.
     *
     * @param value the value
     * @param <T> the column type
     * @return an IsEqualTo condition
     * @deprecated since 2.0.0. Please replace with isEqualTo(value)
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    static <T> IsEqualTo<T> equalTo(T value) {
        return isEqualTo(value);
    }

    // case expressions
    @SuppressWarnings("java:S100")
    static <T> SimpleCaseDSL<T> case_(BindableColumn<T> column) {
        return SimpleCaseDSL.simpleCase(column);
    }

    @SuppressWarnings("java:S100")
    static SearchedCaseDSL case_() {
        return SearchedCaseDSL.searchedCase();
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

    static <T> Max<T> max(BindableColumn<T> column) {
        return Max.of(column);
    }

    static <T> Min<T> min(BindableColumn<T> column) {
        return Min.of(column);
    }

    static <T> Avg<T> avg(BindableColumn<T> column) {
        return Avg.of(column);
    }

    static <T> Sum<T> sum(BindableColumn<T> column) {
        return Sum.of(column);
    }

    static Sum<Object> sum(BasicColumn column) {
        return Sum.of(column);
    }

    static <T> Sum<T> sum(BindableColumn<T> column, VisitableCondition<T> condition) {
        return Sum.of(column, condition);
    }

    // constants
    static <T> Constant<T> constant(String constant) {
        return Constant.of(constant);
    }

    static StringConstant stringConstant(String constant) {
        return StringConstant.of(constant);
    }

    static <T> BoundValue<T> value(T value) {
        return BoundValue.of(value);
    }

    // functions
    static <T> Add<T> add(BindableColumn<T> firstColumn, BasicColumn secondColumn,
            BasicColumn... subsequentColumns) {
        return Add.of(firstColumn, secondColumn, subsequentColumns);
    }

    static <T> Divide<T> divide(BindableColumn<T> firstColumn, BasicColumn secondColumn,
            BasicColumn... subsequentColumns) {
        return Divide.of(firstColumn, secondColumn, subsequentColumns);
    }

    static <T> Multiply<T> multiply(BindableColumn<T> firstColumn, BasicColumn secondColumn,
            BasicColumn... subsequentColumns) {
        return Multiply.of(firstColumn, secondColumn, subsequentColumns);
    }

    static <T> Subtract<T> subtract(BindableColumn<T> firstColumn, BasicColumn secondColumn,
            BasicColumn... subsequentColumns) {
        return Subtract.of(firstColumn, secondColumn, subsequentColumns);
    }

    static CastFinisher cast(String value) {
        return cast(stringConstant(value));
    }

    static CastFinisher cast(Double value) {
        return cast(constant(value.toString()));
    }

    static CastFinisher cast(BasicColumn column) {
        return new CastFinisher(column);
    }

    /**
     * Concatenate function that renders as "(x || y || z)". This will not work on some
     * databases like MySql. In that case, use {@link SqlBuilder#concat(BindableColumn, BasicColumn...)}
     *
     * @param firstColumn first column
     * @param secondColumn second column
     * @param subsequentColumns subsequent columns
     * @param <T> type of column
     * @return a Concatenate instance
     */
    static <T> Concatenate<T> concatenate(BindableColumn<T> firstColumn, BasicColumn secondColumn,
            BasicColumn... subsequentColumns) {
        return Concatenate.concatenate(firstColumn, secondColumn, subsequentColumns);
    }

    /**
     * Concatenate function that renders as "concat(x, y, z)". This version works on more databases
     * than {@link SqlBuilder#concatenate(BindableColumn, BasicColumn, BasicColumn...)}
     *
     * @param firstColumn first column
     * @param subsequentColumns subsequent columns
     * @param <T> type of column
     * @return a Concat instance
     */
    static <T> Concat<T> concat(BindableColumn<T> firstColumn, BasicColumn... subsequentColumns) {
        return Concat.concat(firstColumn, subsequentColumns);
    }

    static <T> OperatorFunction<T> applyOperator(String operator, BindableColumn<T> firstColumn,
            BasicColumn secondColumn, BasicColumn... subsequentColumns) {
        return OperatorFunction.of(operator, firstColumn, secondColumn, subsequentColumns);
    }

    static <T> Lower<T> lower(BindableColumn<T> column) {
        return Lower.of(column);
    }

    static <T> Substring<T> substring(BindableColumn<T> column, int offset, int length) {
        return Substring.of(column, offset, length);
    }

    static <T> Upper<T> upper(BindableColumn<T> column) {
        return Upper.of(column);
    }

    // conditions for all data types
    static ExistsPredicate exists(Buildable<SelectModel> selectModelBuilder) {
        return ExistsPredicate.exists(selectModelBuilder);
    }

    static ExistsPredicate notExists(Buildable<SelectModel> selectModelBuilder) {
        return ExistsPredicate.notExists(selectModelBuilder);
    }

    static <T> IsNull<T> isNull() {
        return new IsNull<>();
    }

    static <T> IsNotNull<T> isNotNull() {
        return new IsNotNull<>();
    }

    static <T> IsEqualTo<T> isEqualTo(T value) {
        return IsEqualTo.of(value);
    }

    static <T> IsEqualTo<T> isEqualTo(Supplier<T> valueSupplier) {
        return isEqualTo(valueSupplier.get());
    }

    static <T> IsEqualToWithSubselect<T> isEqualTo(Buildable<SelectModel> selectModelBuilder) {
        return IsEqualToWithSubselect.of(selectModelBuilder);
    }

    static <T> IsEqualToColumn<T> isEqualTo(BasicColumn column) {
        return IsEqualToColumn.of(column);
    }

    static <T> IsEqualTo<T> isEqualToWhenPresent(@Nullable T value) {
        return value == null ? IsEqualTo.empty() : IsEqualTo.of(value);
    }

    static <T> IsEqualTo<T> isEqualToWhenPresent(Supplier<@Nullable T> valueSupplier) {
        return isEqualToWhenPresent(valueSupplier.get());
    }

    static <T> IsNotEqualTo<T> isNotEqualTo(T value) {
        return IsNotEqualTo.of(value);
    }

    static <T> IsNotEqualTo<T> isNotEqualTo(Supplier<T> valueSupplier) {
        return isNotEqualTo(valueSupplier.get());
    }

    static <T> IsNotEqualToWithSubselect<T> isNotEqualTo(Buildable<SelectModel> selectModelBuilder) {
        return IsNotEqualToWithSubselect.of(selectModelBuilder);
    }

    static <T> IsNotEqualToColumn<T> isNotEqualTo(BasicColumn column) {
        return IsNotEqualToColumn.of(column);
    }

    static <T> IsNotEqualTo<T> isNotEqualToWhenPresent(@Nullable T value) {
        return value == null ? IsNotEqualTo.empty() : IsNotEqualTo.of(value);
    }

    static <T> IsNotEqualTo<T> isNotEqualToWhenPresent(Supplier<@Nullable T> valueSupplier) {
        return isNotEqualToWhenPresent(valueSupplier.get());
    }

    static <T> IsGreaterThan<T> isGreaterThan(T value) {
        return IsGreaterThan.of(value);
    }

    static <T> IsGreaterThan<T> isGreaterThan(Supplier<T> valueSupplier) {
        return isGreaterThan(valueSupplier.get());
    }

    static <T> IsGreaterThanWithSubselect<T> isGreaterThan(Buildable<SelectModel> selectModelBuilder) {
        return IsGreaterThanWithSubselect.of(selectModelBuilder);
    }

    static <T> IsGreaterThanColumn<T> isGreaterThan(BasicColumn column) {
        return IsGreaterThanColumn.of(column);
    }

    static <T> IsGreaterThan<T> isGreaterThanWhenPresent(@Nullable T value) {
        return value == null ? IsGreaterThan.empty() : IsGreaterThan.of(value);
    }

    static <T> IsGreaterThan<T> isGreaterThanWhenPresent(Supplier<@Nullable T> valueSupplier) {
        return isGreaterThanWhenPresent(valueSupplier.get());
    }

    static <T> IsGreaterThanOrEqualTo<T> isGreaterThanOrEqualTo(T value) {
        return IsGreaterThanOrEqualTo.of(value);
    }

    static <T> IsGreaterThanOrEqualTo<T> isGreaterThanOrEqualTo(Supplier<T> valueSupplier) {
        return isGreaterThanOrEqualTo(valueSupplier.get());
    }

    static <T> IsGreaterThanOrEqualToWithSubselect<T> isGreaterThanOrEqualTo(
            Buildable<SelectModel> selectModelBuilder) {
        return IsGreaterThanOrEqualToWithSubselect.of(selectModelBuilder);
    }

    static <T> IsGreaterThanOrEqualToColumn<T> isGreaterThanOrEqualTo(BasicColumn column) {
        return IsGreaterThanOrEqualToColumn.of(column);
    }

    static <T> IsGreaterThanOrEqualTo<T> isGreaterThanOrEqualToWhenPresent(@Nullable T value) {
        return value == null ? IsGreaterThanOrEqualTo.empty() : IsGreaterThanOrEqualTo.of(value);
    }

    static <T> IsGreaterThanOrEqualTo<T> isGreaterThanOrEqualToWhenPresent(Supplier<@Nullable T> valueSupplier) {
        return isGreaterThanOrEqualToWhenPresent(valueSupplier.get());
    }

    static <T> IsLessThan<T> isLessThan(T value) {
        return IsLessThan.of(value);
    }

    static <T> IsLessThan<T> isLessThan(Supplier<T> valueSupplier) {
        return isLessThan(valueSupplier.get());
    }

    static <T> IsLessThanWithSubselect<T> isLessThan(Buildable<SelectModel> selectModelBuilder) {
        return IsLessThanWithSubselect.of(selectModelBuilder);
    }

    static <T> IsLessThanColumn<T> isLessThan(BasicColumn column) {
        return IsLessThanColumn.of(column);
    }

    static <T> IsLessThan<T> isLessThanWhenPresent(@Nullable T value) {
        return value == null ? IsLessThan.empty() : IsLessThan.of(value);
    }

    static <T> IsLessThan<T> isLessThanWhenPresent(Supplier<@Nullable T> valueSupplier) {
        return isLessThanWhenPresent(valueSupplier.get());
    }

    static <T> IsLessThanOrEqualTo<T> isLessThanOrEqualTo(T value) {
        return IsLessThanOrEqualTo.of(value);
    }

    static <T> IsLessThanOrEqualTo<T> isLessThanOrEqualTo(Supplier<T> valueSupplier) {
        return isLessThanOrEqualTo(valueSupplier.get());
    }

    static <T> IsLessThanOrEqualToWithSubselect<T> isLessThanOrEqualTo(Buildable<SelectModel> selectModelBuilder) {
        return IsLessThanOrEqualToWithSubselect.of(selectModelBuilder);
    }

    static <T> IsLessThanOrEqualToColumn<T> isLessThanOrEqualTo(BasicColumn column) {
        return IsLessThanOrEqualToColumn.of(column);
    }

    static <T> IsLessThanOrEqualTo<T> isLessThanOrEqualToWhenPresent(@Nullable T value) {
        return value == null ? IsLessThanOrEqualTo.empty() : IsLessThanOrEqualTo.of(value);
    }

    static <T> IsLessThanOrEqualTo<T> isLessThanOrEqualToWhenPresent(Supplier<@Nullable T> valueSupplier) {
        return isLessThanOrEqualToWhenPresent(valueSupplier.get());
    }

    @SafeVarargs
    static <T> IsIn<T> isIn(@Nullable T... values) {
        return IsIn.of(values);
    }

    static <T> IsIn<T> isIn(Collection<@Nullable T> values) {
        return IsIn.of(values);
    }

    static <T> IsInWithSubselect<T> isIn(Buildable<SelectModel> selectModelBuilder) {
        return IsInWithSubselect.of(selectModelBuilder);
    }

    @SafeVarargs
    static <T> IsInWhenPresent<T> isInWhenPresent(@Nullable T... values) {
        return IsInWhenPresent.of(values);
    }

    static <T> IsInWhenPresent<T> isInWhenPresent(@Nullable Collection<@Nullable T> values) {
        return values == null ? IsInWhenPresent.empty() : IsInWhenPresent.of(values);
    }

    @SafeVarargs
    static <T> IsNotIn<T> isNotIn(@Nullable T... values) {
        return IsNotIn.of(values);
    }

    static <T> IsNotIn<T> isNotIn(Collection<@Nullable T> values) {
        return IsNotIn.of(values);
    }

    static <T> IsNotInWithSubselect<T> isNotIn(Buildable<SelectModel> selectModelBuilder) {
        return IsNotInWithSubselect.of(selectModelBuilder);
    }

    @SafeVarargs
    static <T> IsNotInWhenPresent<T> isNotInWhenPresent(@Nullable T... values) {
        return IsNotInWhenPresent.of(values);
    }

    static <T> IsNotInWhenPresent<T> isNotInWhenPresent(@Nullable Collection<@Nullable T> values) {
        return values == null ? IsNotInWhenPresent.empty() : IsNotInWhenPresent.of(values);
    }

    static <T> IsBetween.Builder<T> isBetween(@Nullable T value1) {
        return IsBetween.isBetween(value1);
    }

    static <T> IsBetween.Builder<T> isBetween(Supplier<@Nullable T> valueSupplier1) {
        return isBetween(valueSupplier1.get());
    }

    static <T> IsBetween.WhenPresentBuilder<T> isBetweenWhenPresent(@Nullable T value1) {
        return IsBetween.isBetweenWhenPresent(value1);
    }

    static <T> IsBetween.WhenPresentBuilder<T> isBetweenWhenPresent(Supplier<@Nullable T> valueSupplier1) {
        return isBetweenWhenPresent(valueSupplier1.get());
    }

    static <T> IsNotBetween.Builder<T> isNotBetween(@Nullable T value1) {
        return IsNotBetween.isNotBetween(value1);
    }

    static <T> IsNotBetween.Builder<T> isNotBetween(Supplier<@Nullable T> valueSupplier1) {
        return isNotBetween(valueSupplier1.get());
    }

    static <T> IsNotBetween.WhenPresentBuilder<T> isNotBetweenWhenPresent(@Nullable T value1) {
        return IsNotBetween.isNotBetweenWhenPresent(value1);
    }

    static <T> IsNotBetween.WhenPresentBuilder<T> isNotBetweenWhenPresent(Supplier<@Nullable T> valueSupplier1) {
        return isNotBetweenWhenPresent(valueSupplier1.get());
    }

    // for string columns, but generic for columns with type handlers
    static <T> IsLike<T> isLike(T value) {
        return IsLike.of(value);
    }

    static <T> IsLike<T> isLike(Supplier<T> valueSupplier) {
        return isLike(valueSupplier.get());
    }

    static <T> IsLike<T> isLikeWhenPresent(@Nullable T value) {
        return value == null ? IsLike.empty() : IsLike.of(value);
    }

    static <T> IsLike<T> isLikeWhenPresent(Supplier<@Nullable T> valueSupplier) {
        return isLikeWhenPresent(valueSupplier.get());
    }

    static <T> IsNotLike<T> isNotLike(T value) {
        return IsNotLike.of(value);
    }

    static <T> IsNotLike<T> isNotLike(Supplier<T> valueSupplier) {
        return isNotLike(valueSupplier.get());
    }

    static <T> IsNotLike<T> isNotLikeWhenPresent(@Nullable T value) {
        return value == null ? IsNotLike.empty() : IsNotLike.of(value);
    }

    static <T> IsNotLike<T> isNotLikeWhenPresent(Supplier<@Nullable T> valueSupplier) {
        return isNotLikeWhenPresent(valueSupplier.get());
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
        return IsLikeCaseInsensitive.of(value);
    }

    static IsLikeCaseInsensitive isLikeCaseInsensitive(Supplier<String> valueSupplier) {
        return isLikeCaseInsensitive(valueSupplier.get());
    }

    static IsLikeCaseInsensitive isLikeCaseInsensitiveWhenPresent(@Nullable String value) {
        return value == null ? IsLikeCaseInsensitive.empty() : IsLikeCaseInsensitive.of(value);
    }

    static IsLikeCaseInsensitive isLikeCaseInsensitiveWhenPresent(Supplier<@Nullable String> valueSupplier) {
        return isLikeCaseInsensitiveWhenPresent(valueSupplier.get());
    }

    static IsNotLikeCaseInsensitive isNotLikeCaseInsensitive(String value) {
        return IsNotLikeCaseInsensitive.of(value);
    }

    static IsNotLikeCaseInsensitive isNotLikeCaseInsensitive(Supplier<String> valueSupplier) {
        return isNotLikeCaseInsensitive(valueSupplier.get());
    }

    static IsNotLikeCaseInsensitive isNotLikeCaseInsensitiveWhenPresent(@Nullable String value) {
        return value == null ? IsNotLikeCaseInsensitive.empty() : IsNotLikeCaseInsensitive.of(value);
    }

    static IsNotLikeCaseInsensitive isNotLikeCaseInsensitiveWhenPresent(Supplier<@Nullable String> valueSupplier) {
        return isNotLikeCaseInsensitiveWhenPresent(valueSupplier.get());
    }

    static IsInCaseInsensitive isInCaseInsensitive(String... values) {
        return IsInCaseInsensitive.of(values);
    }

    static IsInCaseInsensitive isInCaseInsensitive(Collection<String> values) {
        return IsInCaseInsensitive.of(values);
    }

    static IsInCaseInsensitiveWhenPresent isInCaseInsensitiveWhenPresent(@Nullable String... values) {
        return IsInCaseInsensitiveWhenPresent.of(values);
    }

    static IsInCaseInsensitiveWhenPresent isInCaseInsensitiveWhenPresent(
            @Nullable Collection<@Nullable String> values) {
        return values == null ? IsInCaseInsensitiveWhenPresent.empty() : IsInCaseInsensitiveWhenPresent.of(values);
    }

    static IsNotInCaseInsensitive isNotInCaseInsensitive(String... values) {
        return IsNotInCaseInsensitive.of(values);
    }

    static IsNotInCaseInsensitive isNotInCaseInsensitive(Collection<String> values) {
        return IsNotInCaseInsensitive.of(values);
    }

    static IsNotInCaseInsensitiveWhenPresent isNotInCaseInsensitiveWhenPresent(@Nullable String... values) {
        return IsNotInCaseInsensitiveWhenPresent.of(values);
    }

    static IsNotInCaseInsensitiveWhenPresent isNotInCaseInsensitiveWhenPresent(
            @Nullable Collection<@Nullable String> values) {
        return values == null ? IsNotInCaseInsensitiveWhenPresent.empty() :
                IsNotInCaseInsensitiveWhenPresent.of(values);
    }

    // order by support

    /**
     * Creates a sort specification based on a String. This is useful when a column has been
     * aliased in the select list. For example:
     *
     * <pre>
     *     select(foo.as("bar"))
     *     .from(baz)
     *     .orderBy(sortColumn("bar"))
     * </pre>
     *
     * @param name the string to use as a sort specification
     * @return a sort specification
     */
    static SortSpecification sortColumn(String name) {
        return SimpleSortSpecification.of(name);
    }

    /**
     * Creates a sort specification based on a column and a table alias. This can be useful in a join
     * where the desired sort order is based on a column not in the select list. This will likely
     * fail in union queries depending on database support.
     *
     * @param tableAlias the table alias
     * @param column the column
     * @return a sort specification
     */
    static SortSpecification sortColumn(String tableAlias, SqlColumn<?> column) {
        return new ColumnSortSpecification(tableAlias, column);
    }

    class InsertIntoNextStep {

        private final SqlTable table;

        private InsertIntoNextStep(SqlTable table) {
            this.table = Objects.requireNonNull(table);
        }

        public InsertSelectDSL withSelectStatement(Buildable<SelectModel> selectModelBuilder) {
            return InsertSelectDSL.insertInto(table)
                    .withSelectStatement(selectModelBuilder);
        }

        public InsertSelectDSL.SelectGatherer withColumnList(SqlColumn<?>... columns) {
            return InsertSelectDSL.insertInto(table)
                    .withColumnList(columns);
        }

        public InsertSelectDSL.SelectGatherer withColumnList(List<SqlColumn<?>> columns) {
            return InsertSelectDSL.insertInto(table)
                    .withColumnList(columns);
        }

        public <T> GeneralInsertDSL.SetClauseFinisher<T> set(SqlColumn<T> column) {
            return GeneralInsertDSL.insertInto(table)
                    .set(column);
        }
    }

    class CastFinisher {
        private final BasicColumn column;

        public CastFinisher(BasicColumn column) {
            this.column = column;
        }

        public Cast as(String targetType) {
            return new Cast.Builder()
                    .withColumn(column)
                    .withTargetType(targetType)
                    .build();
        }
    }
}
