package examples.simple;

import static examples.simple.SimpleTableFields.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.assertThat;
import static org.mybatis.qbe.sql.SqlConditions.*;
import static org.mybatis.qbe.sql.where.WhereSupportBuilder.*;

import org.junit.Test;
import org.mybatis.qbe.sql.where.WhereSupport;

public class SampleWhereClausesTest {

    @Test
    public void simpleClause1() {
        WhereSupport whereSupport = where(id, isEqualTo(3))
                .build();
        
        assertThat(whereSupport.getWhereClause(),
                is("where a.id = #{parameters.p1,jdbcType=INTEGER}"));
    }
    
    @Test
    public void simpleClause2() {
        WhereSupport whereSupport = where(id, isNull())
                .build();
        
        assertThat(whereSupport.getWhereClause(),
                is("where a.id is null"));
    }
    
    @Test
    public void betweenClause() {
        WhereSupport whereSupport = where(id, isBetween(1).and(4))
                .build();
        
        assertThat(whereSupport.getWhereClause(),
                is("where a.id between #{parameters.p1,jdbcType=INTEGER} and #{parameters.p2,jdbcType=INTEGER}"));
    }

    @Test
    public void complexClause() {
        WhereSupport whereSupport = where(id, isGreaterThan(2))
                .or(occupation, isNull(), and(id, isLessThan(6)))
                .buildIgnoringAlias();
        
        assertThat(whereSupport.getWhereClause(),
                is("where id > #{parameters.p1,jdbcType=INTEGER} or (occupation is null and id < #{parameters.p2,jdbcType=INTEGER})"));
    }
}
