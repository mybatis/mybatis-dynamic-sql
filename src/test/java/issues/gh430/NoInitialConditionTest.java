package issues.gh430;

import static org.mybatis.dynamic.sql.SqlBuilder.*;
import static org.mybatis.dynamic.sql.subselect.FooDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class NoInitialConditionTest {

    @Test
    void testNoInitialConditionEmptyList() {
        List<AndOrCriteriaGroup> criteria = new ArrayList<>();

        SelectStatementProvider selectStatement = buildSelectStatement(criteria);

        String expected = "select column1, column2 from foo where column1 < :p1";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testNoInitialConditionSingleSub() {
        List<AndOrCriteriaGroup> criteria = new ArrayList<>();
        criteria.add(or(column2, isEqualTo(3)));

        SelectStatementProvider selectStatement = buildSelectStatement(criteria);

        String expected = "select column1, column2 from foo where column1 < :p1 " +
                "and column2 = :p2";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testNoInitialConditionMultipleSubs() {
        List<AndOrCriteriaGroup> criteria = new ArrayList<>();
        criteria.add(or(column2, isEqualTo(3)));
        criteria.add(or(column2, isEqualTo(4)));
        criteria.add(or(column2, isEqualTo(5)));

        SelectStatementProvider selectStatement = buildSelectStatement(criteria);

        String expected = "select column1, column2 from foo where column1 < :p1 " +
                "and (column2 = :p2 or column2 = :p3 or column2 = :p4)";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    private SelectStatementProvider buildSelectStatement(List<AndOrCriteriaGroup> criteria) {
        return select(column1, column2)
                .from(foo)
                .where(column1, isLessThan(new Date()))
                .and(criteria) // this is a new method to support lists of criteria directly
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);
    }
}
