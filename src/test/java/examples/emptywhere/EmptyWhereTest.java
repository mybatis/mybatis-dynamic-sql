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
package examples.emptywhere;

import static examples.emptywhere.OrderDynamicSqlSupport.*;
import static examples.emptywhere.PersonDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.delete.DeleteDSL;
import org.mybatis.dynamic.sql.delete.DeleteModel;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateDSL;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.where.WhereDSL;
import org.mybatis.dynamic.sql.where.render.WhereClauseProvider;

public class EmptyWhereTest {

    @Test
    public void testDeleteThreeConditions() {
        String fName = "Fred";
        String lName = "Flintstone";
        
        DeleteDSL<DeleteModel>.DeleteWhereBuilder builder = deleteFrom(person)
                .where(id, isEqualTo(3));
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        DeleteStatementProvider deleteStatement = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "delete from person"
                + " where id = #{parameters.p1}"
                + " and first_name = #{parameters.p2}"
                + " and last_name = #{parameters.p3}";
        
        assertThat(deleteStatement.getDeleteStatement()).isEqualTo(expected);
    }

    @Test
    public void testDeleteTwoConditions() {
        String fName = "Fred";
        String lName = "Flintstone";
        
        DeleteDSL<DeleteModel>.DeleteWhereBuilder builder = deleteFrom(person)
                .where();
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.or(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        DeleteStatementProvider deleteStatement = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "delete from person"
                + " where first_name = #{parameters.p1}"
                + " or last_name = #{parameters.p2}";
        
        assertThat(deleteStatement.getDeleteStatement()).isEqualTo(expected);
    }

    @Test
    public void testDeleteFirstNameNull() {
        String fName = null;
        String lName = "Flintstone";
        
        DeleteDSL<DeleteModel>.DeleteWhereBuilder builder = deleteFrom(person)
                .where();
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        DeleteStatementProvider deleteStatement = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "delete from person"
                + " where last_name = #{parameters.p1}";
        
        assertThat(deleteStatement.getDeleteStatement()).isEqualTo(expected);
    }

    @Test
    public void testDeleteLastNameNull() {
        String fName = "Fred";
        String lName = null;
        
        DeleteDSL<DeleteModel>.DeleteWhereBuilder builder = deleteFrom(person)
                .where();
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        DeleteStatementProvider deleteStatement = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "delete from person"
                + " where first_name = #{parameters.p1}";
        
        assertThat(deleteStatement.getDeleteStatement()).isEqualTo(expected);
    }

    @Test
    public void testDeleteAllNull() {
        String fName = null;
        String lName = null;
        
        DeleteDSL<DeleteModel>.DeleteWhereBuilder builder = deleteFrom(person)
                .where();
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        DeleteStatementProvider deleteStatement = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "delete from person";
        
        assertThat(deleteStatement.getDeleteStatement()).isEqualTo(expected);
    }

    @Test
    public void testSelectThreeConditions() {
        String fName = "Fred";
        String lName = "Flintstone";
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder = select(id, firstName, lastName)
                .from(person)
                .where(id, isEqualTo(3));
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        SelectStatementProvider selectStatement = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "select id, first_name, last_name"
                + " from person"
                + " where id = #{parameters.p1}"
                + " and first_name = #{parameters.p2}"
                + " and last_name = #{parameters.p3}";
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    public void testSelectTwoConditions() {
        String fName = "Fred";
        String lName = "Flintstone";
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder = select(person.allColumns())
                .from(person)
                .where();
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        SelectStatementProvider selectStatement = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "select *"
                + " from person"
                + " where first_name = #{parameters.p1}"
                + " and last_name = #{parameters.p2}";
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    public void testSelectFirstNameNull() {
        String fName = null;
        String lName = "Flintstone";
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder = select(id, firstName, lastName)
                .from(person)
                .where();
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        SelectStatementProvider selectStatement = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "select id, first_name, last_name"
                + " from person"
                + " where last_name = #{parameters.p1}";
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    public void testSelectLastNameNull() {
        String fName = "Fred";
        String lName = null;
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder = select(id, firstName, lastName)
                .from(person)
                .where();
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        SelectStatementProvider selectStatement = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "select id, first_name, last_name"
                + " from person"
                + " where first_name = #{parameters.p1}";
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    public void testSelectAllNull() {
        String fName = null;
        String lName = null;
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder = select(id, firstName, lastName)
                .from(person)
                .where();
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        SelectStatementProvider selectStatement = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "select id, first_name, last_name"
                + " from person";
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    public void testJoinThreeConditions() {
        String fName = "Fred";
        String lName = "Flintstone";
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder = select(id, firstName, lastName, orderDate)
                .from(person).join(order).on(person.id, equalTo(order.personId))
                .where(id, isEqualTo(3));
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        SelectStatementProvider selectStatement = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "select person.id, person.first_name, person.last_name, order.order_date"
                + " from person"
                + " join order on person.id = order.person_id"
                + " where person.id = #{parameters.p1}"
                + " and person.first_name = #{parameters.p2}"
                + " and person.last_name = #{parameters.p3}";
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    public void testJoinTwoConditions() {
        String fName = "Fred";
        String lName = "Flintstone";
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder = select(id, firstName, lastName, orderDate)
                .from(person).join(order).on(person.id, equalTo(order.personId))
                .where();
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        SelectStatementProvider selectStatement = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "select person.id, person.first_name, person.last_name, order.order_date"
                + " from person"
                + " join order on person.id = order.person_id"
                + " where person.first_name = #{parameters.p1}"
                + " and person.last_name = #{parameters.p2}";
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    public void testJoinFirstNameNull() {
        String fName = null;
        String lName = "Flintstone";
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder = select(id, firstName, lastName, orderDate)
                .from(person).join(order).on(person.id, equalTo(order.personId))
                .where();
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        SelectStatementProvider selectStatement = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "select person.id, person.first_name, person.last_name, order.order_date"
                + " from person"
                + " join order on person.id = order.person_id"
                + " where person.last_name = #{parameters.p1}";
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    public void testJoinLastNameNull() {
        String fName = "Fred";
        String lName = null;
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder = select(id, firstName, lastName, orderDate)
                .from(person).join(order).on(person.id, equalTo(order.personId))
                .where();
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        SelectStatementProvider selectStatement = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "select person.id, person.first_name, person.last_name, order.order_date"
                + " from person"
                + " join order on person.id = order.person_id"
                + " where person.first_name = #{parameters.p1}";
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    public void testJoinAllNull() {
        String fName = null;
        String lName = null;
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder = select(id, firstName, lastName, orderDate)
                .from(person).join(order).on(person.id, equalTo(order.personId))
                .where();
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        SelectStatementProvider selectStatement = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "select person.id, person.first_name, person.last_name, order.order_date"
                + " from person"
                + " join order on person.id = order.person_id";
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    public void testUpdateThreeConditions() {
        String fName = "Fred";
        String lName = "Flintstone";
        
        UpdateDSL<UpdateModel>.UpdateWhereBuilder builder = update(person)
                .set(id).equalTo(3)
                .where(id, isEqualTo(3));
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        UpdateStatementProvider updateStatement = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "update person"
                + " set id = #{parameters.p1}"
                + " where id = #{parameters.p2}"
                + " and first_name = #{parameters.p3}"
                + " and last_name = #{parameters.p4}";
        
        assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected);
    }

    @Test
    public void testUpdateTwoConditions() {
        String fName = "Fred";
        String lName = "Flintstone";
        
        UpdateDSL<UpdateModel>.UpdateWhereBuilder builder = update(person)
                .set(id).equalTo(3)
                .where();
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        UpdateStatementProvider updateStatement = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "update person"
                + " set id = #{parameters.p1}"
                + " where first_name = #{parameters.p2}"
                + " and last_name = #{parameters.p3}";
        
        assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected);
    }

    @Test
    public void testUpdateFirstNameNull() {
        String fName = null;
        String lName = "Flintstone";
        
        DeleteDSL<DeleteModel>.DeleteWhereBuilder builder = deleteFrom(person)
                .where();
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        DeleteStatementProvider deleteStatement = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "delete from person"
                + " where last_name = #{parameters.p1}";
        
        assertThat(deleteStatement.getDeleteStatement()).isEqualTo(expected);
    }

    @Test
    public void testUpdateLastNameNull() {
        String fName = "Fred";
        String lName = null;
        
        UpdateDSL<UpdateModel>.UpdateWhereBuilder builder = update(person)
                .set(id).equalTo(3)
                .where();
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        UpdateStatementProvider updateStatement = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "update person"
                + " set id = #{parameters.p1}"
                + " where first_name = #{parameters.p2}";
        
        assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected);
    }

    @Test
    public void testUpdateAllNull() {
        String fName = null;
        String lName = null;
        
        UpdateDSL<UpdateModel>.UpdateWhereBuilder builder = update(person)
                .set(id).equalTo(3)
                .where();
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        UpdateStatementProvider updateStatement = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "update person"
                + " set id = #{parameters.p1}";
        
        assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected);
    }

    @Test
    public void testWhereThreeConditions() {
        String fName = "Fred";
        String lName = "Flintstone";
        
        WhereDSL builder = where(id, isEqualTo(3));
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        WhereClauseProvider whereClause = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "where id = #{parameters.p1}"
                + " and first_name = #{parameters.p2}"
                + " and last_name = #{parameters.p3}";
        
        assertThat(whereClause.getWhereClause()).isEqualTo(expected);
    }

    @Test
    public void testWhereTwoConditions() {
        String fName = "Fred";
        String lName = "Flintstone";
        
        WhereDSL builder = where();
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        WhereClauseProvider whereClause = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "where first_name = #{parameters.p1}"
                + " and last_name = #{parameters.p2}";
        
        assertThat(whereClause.getWhereClause()).isEqualTo(expected);
    }

    @Test
    public void testWhereFirstNameNull() {
        String fName = null;
        String lName = "Flintstone";
        
        WhereDSL builder = where();
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        WhereClauseProvider whereClause = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "where last_name = #{parameters.p1}";
        
        assertThat(whereClause.getWhereClause()).isEqualTo(expected);
    }

    @Test
    public void testWhereLastNameNull() {
        String fName = "Fred";
        String lName = null;
        
        WhereDSL builder = where();
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        WhereClauseProvider whereClause = builder.build().render(RenderingStrategies.MYBATIS3);
        
        String expected = "where first_name = #{parameters.p1}";
        
        assertThat(whereClause.getWhereClause()).isEqualTo(expected);
    }

    @Test
    public void testWhereAllNull() {
        String fName = null;
        String lName = null;
        
        WhereDSL builder = where();
        
        builder.and(firstName, isEqualTo(fName).when(Objects::nonNull));
        builder.and(lastName, isEqualTo(lName).when(Objects::nonNull));
        
        WhereClauseProvider whereClause = builder.build().render(RenderingStrategies.MYBATIS3);
        
        assertThat(whereClause.getWhereClause()).isEmpty();
    }
}
