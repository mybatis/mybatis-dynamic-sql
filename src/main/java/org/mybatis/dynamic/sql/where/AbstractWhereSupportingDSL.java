package org.mybatis.dynamic.sql.where;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.VisitableCondition;

import java.util.function.Consumer;

/**
 * Base class for DSLs that support where clauses.
 *
 * @param <W> the implementation of the Where DSL.
 */
public abstract class AbstractWhereSupportingDSL<W extends AbstractWhereDSL<?>> {

    public <T> W where(BindableColumn<T> column, VisitableCondition<T> condition, SqlCriterion<?>...subCriteria) {
        return apply(w -> w.where(column, condition, subCriteria));
    }

    public W applyWhere(WhereApplier whereApplier) {
        return apply(w -> w.applyWhere(whereApplier));
    }

    private W apply(Consumer<W> block) {
        W dsl = where();
        block.accept(dsl);
        return dsl;
    }

    public abstract W where();
}
