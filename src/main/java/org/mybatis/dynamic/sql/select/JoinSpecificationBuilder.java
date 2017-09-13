package org.mybatis.dynamic.sql.select;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.select.SelectModelBuilder.SelectSupportJoinBuilder;
import org.mybatis.dynamic.sql.select.join.JoinCondition;

public class JoinSpecificationBuilder {

    private SqlTable joinTable;
    
    public JoinSpecificationBuilder(SqlTable joinTable) {
        this.joinTable = joinTable;
    }
    
    public <T> SelectSupportJoinBuilder on(SqlColumn<T> joinColumn, JoinCondition<T> joinCondition) {
        joinBuilder.addJoinSpecification(JoinSpecification.of(joinTable, JoinColumnAndCondition.of(joinColumn, joinCondition)));
        return joinBuilder;
    }

    public <T> SelectSupportJoinBuilder on(SqlColumn<T> joinColumn, JoinCondition<T> joinCondition, JoinColumnAndCondition<?>...joinColumnsAndConditions) {
        joinBuilder.addJoinSpecification(JoinSpecification.of(joinTable, JoinColumnAndCondition.of(joinColumn, joinCondition), joinColumnsAndConditions));
        return joinBuilder;
    }
}
