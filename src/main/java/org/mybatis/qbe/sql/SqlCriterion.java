package org.mybatis.qbe.sql;

import java.util.Arrays;
import java.util.stream.Stream;

import org.mybatis.qbe.Condition;
import org.mybatis.qbe.Criterion;

public class SqlCriterion<T> extends Criterion<T, SqlField<T>, SqlCriterion<?>> {
    
    private SqlCriterion() {
        super();
    }
    
    public SqlCriterion<T> ignoringAlias() {
        return SqlCriterion.of(connector, field.ignoringAlias(), condition,
                subCriteria.stream().map(SqlCriterion::ignoringAlias));
    }

    public static <T> SqlCriterion<T> of(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...subCriteria) {
        return SqlCriterion.of(null,  field, condition, subCriteria);
    }
    
    public static <T> SqlCriterion<T> of(String connector, SqlField<T> field, Condition<T> condition, SqlCriterion<?>...subCriteria) {
        return SqlCriterion.of(connector, field, condition, Arrays.stream(subCriteria));
    }

    public static <T> SqlCriterion<T> of(String connector, SqlField<T> field, Condition<T> condition, Stream<SqlCriterion<?>> subCriteria) {
        SqlCriterion<T> criterion = new SqlCriterion<>();
        criterion.field = field;
        criterion.condition = condition;
        criterion.connector = connector;
        subCriteria.forEach(criterion.subCriteria::add);
        return criterion;
    }
}
