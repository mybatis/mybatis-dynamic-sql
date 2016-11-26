package org.mybatis.qbe.sql.where;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.sql.JDBCType;

import org.junit.Test;
import org.mybatis.qbe.mybatis3.MyBatis3Field;
import org.mybatis.qbe.sql.where.WhereClause;
import org.mybatis.qbe.sql.where.condition.IsLessThanCondition;
import org.mybatis.qbe.sql.where.condition.IsNullCondition;

public class WhereClauseTest {

    @Test
    public void testWhereClauseBuilder() {
        MyBatis3Field<Integer> field = MyBatis3Field.of("id", JDBCType.INTEGER);
        IsLessThanCondition<Integer> isLessThanCondition = IsLessThanCondition.of(3);
        IsNullCondition<Integer> isNullCondition = new IsNullCondition<>();
        
        WhereClause whereClause = new WhereClause.Builder(field, isLessThanCondition)
                .or(field, isNullCondition)
                .build();
        
        // kind of a silly test - just makes sure there are two criteria
        StringBuilder sb = new StringBuilder();
        whereClause.visitCriteria(c -> {
            sb.append(c.field().render());
            sb.append(',');
        });
        
        assertThat(sb.toString(), is("id,id,"));
    }

    @Test
    public void testWhereClauseAliasIgnoringBuilder() {
        MyBatis3Field<Integer> field = MyBatis3Field.of("id", JDBCType.INTEGER);
        IsLessThanCondition<Integer> isLessThanCondition = IsLessThanCondition.of(3);
        IsNullCondition<Integer> isNullCondition = new IsNullCondition<>();
        
        WhereClause whereClause = new WhereClause.Builder(field, isLessThanCondition)
                .or(field, isNullCondition)
                .buildIgnoringAlias();
        
        // kind of a silly test - just makes sure there are two criteria
        StringBuilder sb = new StringBuilder();
        whereClause.visitCriteria(c -> {
            sb.append(c.field().render());
            sb.append(',');
        });
        
        assertThat(sb.toString(), is("id,id,"));
    }
}
