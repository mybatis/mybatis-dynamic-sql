package simple.example;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.assertThat;
import static org.mybatis.qbe.condition.Conditions.*;
import static org.mybatis.qbe.mybatis3.WhereClauseAndParameters.where;
import static simple.example.SimpleTableFields.*;

import org.junit.Test;
import org.mybatis.qbe.mybatis3.WhereClauseAndParameters;

public class SampleWhereClausesTest {

    @Test
    public void simpleClause1() {
        WhereClauseAndParameters whereClauseAndParameters = where(id, isEqualTo(3))
                .build();
        
        assertThat(whereClauseAndParameters.getWhereClause(),
                is("where a.id = #{parameters.p1,jdbcType=INTEGER}"));
    }
    
    @Test
    public void simpleClause2() {
        WhereClauseAndParameters whereClauseAndParameters = where(id, isNull())
                .build();
        
        assertThat(whereClauseAndParameters.getWhereClause(),
                is("where a.id is null"));
    }
    
    @Test
    public void betweenClause() {
        WhereClauseAndParameters whereClauseAndParameters = where(id, isBetween(1).and(4))
                .build();
        
        assertThat(whereClauseAndParameters.getWhereClause(),
                is("where a.id between #{parameters.p1,jdbcType=INTEGER} and #{parameters.p2,jdbcType=INTEGER}"));
    }

    @Test
    public void complexClause() {
        WhereClauseAndParameters whereClauseAndParameters = where(id, isGreaterThan(2))
                .or(occupation, isNull(), and(id, isLessThan(6)))
                .buildWithoutTableAlias();
        
        assertThat(whereClauseAndParameters.getWhereClause(),
                is("where id > #{parameters.p1,jdbcType=INTEGER} or (occupation is null and id < #{parameters.p2,jdbcType=INTEGER})"));
    }
}
