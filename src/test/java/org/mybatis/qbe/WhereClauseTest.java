package org.mybatis.qbe;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.sql.JDBCType;

import org.junit.Test;
import org.mybatis.qbe.condition.IsLessThanCondition;
import org.mybatis.qbe.condition.IsNullCondition;
import org.mybatis.qbe.field.Field;

public class WhereClauseTest {

    @Test
    public void testWhereClauseBuilder() {
        Field<Integer> field = Field.of("id", JDBCType.INTEGER);
        IsLessThanCondition<Integer> isLessThanCondition = IsLessThanCondition.of(3);
        IsNullCondition<Integer> isNullCondition = new IsNullCondition<>();
        
        WhereClause whereClause = WhereClause.of(field, isLessThanCondition)
                .or(field, isNullCondition)
                .build();
        
        // kind of a silly test - just makes sure there are two criteria
        StringBuilder sb = new StringBuilder();
        whereClause.visitCriteria(c -> {
            sb.append(c.fieldName());
            sb.append(',');
        });
        
        assertThat(sb.toString(), is("id,id,"));
    }
}
