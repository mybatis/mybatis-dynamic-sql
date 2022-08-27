/*
 *    Copyright 2016-2022 the original author or authors.
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
package issues.gh105;

import static issues.gh105.PersonDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.Predicates;

class Issue105Test {

    @Test
    void testFuzzyLikeBothPresent() {
        String fName = "Fred";
        String lName = "Flintstone";

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isLike(fName).filter(Objects::nonNull).map(s -> "%" + s + "%"))
                .and(lastName, isLike(lName).filter(Objects::nonNull).map(s -> "%" + s + "%"))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where first_name like #{parameters.p1}"
                + " and last_name like #{parameters.p2}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", "%Fred%");
        assertThat(selectStatement.getParameters()).containsEntry("p2", "%Flintstone%");
    }

    @Test
    void testFuzzyLikeFirstNameNull() {
        String fName = null;
        String lName = "Flintstone";

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isLike(fName).filter(Objects::nonNull).map(SearchUtils::addWildcards))
                .and(lastName, isLike(lName).filter(Objects::nonNull).map(SearchUtils::addWildcards))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where last_name like #{parameters.p1}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", "%Flintstone%");
    }

    @Test
    void testFuzzyLikeLastNameNull() {
        String fName = "Fred";
        String lName = null;

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isLike(fName).filter(Objects::nonNull).map(SearchUtils::addWildcards))
                .and(lastName, isLike(lName).filter(Objects::nonNull).map(SearchUtils::addWildcards))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where first_name like #{parameters.p1}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", "%Fred%");
    }

    @Test
    void testFuzzyLikeBothNull() {
        String fName = null;
        String lName = null;

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isLike(fName).filter(Objects::nonNull).map(SearchUtils::addWildcards))
                .and(lastName, isLike(lName).filter(Objects::nonNull).map(SearchUtils::addWildcards))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).isEmpty();
    }

    @Test
    void testBetweenTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isBetween(1).and(10).map(i1 -> i1 + 1,  i2 -> i2 + 2))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where age between #{parameters.p1} and #{parameters.p2}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 2);
        assertThat(selectStatement.getParameters()).containsEntry("p2", 12);
    }

    @Test
    void testBetweenWhenPresentTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isBetweenWhenPresent(1).and(10).map(i1 -> i1 + 1,  i2 -> i2 + 2))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where age between #{parameters.p1} and #{parameters.p2}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 2);
        assertThat(selectStatement.getParameters()).containsEntry("p2", 12);
    }

    @Test
    void testEqualTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isEqualTo(1).map(i -> i + 1))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where age = #{parameters.p1}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 2);
    }

    @Test
    void testEqualWhenPresentTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isEqualToWhenPresent(1).map(i -> i + 1))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where age = #{parameters.p1}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 2);
    }

    @Test
    void testGreaterThanTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isGreaterThan(1).map(i -> i + 1))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where age > #{parameters.p1}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 2);
    }

    @Test
    void testGreaterThanOrEqualTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isGreaterThanOrEqualTo(1).map(i -> i + 1))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where age >= #{parameters.p1}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 2);
    }

    @Test
    void testGreaterThanOrEqualWhenPresentTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isGreaterThanOrEqualToWhenPresent(1).map(i -> i + 1))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where age >= #{parameters.p1}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 2);
    }

    @Test
    void testGreaterThanWhenPresentTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isGreaterThanWhenPresent(1).map(i -> i + 1))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where age > #{parameters.p1}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 2);
    }

    @Test
    void testLessThanTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isLessThan(1).map(i -> i + 1))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where age < #{parameters.p1}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 2);
    }

    @Test
    void testLessThanOrEqualTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isLessThanOrEqualTo(1).map(i -> i + 1))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where age <= #{parameters.p1}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 2);
    }

    @Test
    void testLessThanOrEqualWhenPresentTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isLessThanOrEqualToWhenPresent(1).map(i -> i + 1))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where age <= #{parameters.p1}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 2);
    }

    @Test
    void testLessThanWhenPresentTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isLessThanWhenPresent(1).map(i -> i + 1))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where age < #{parameters.p1}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 2);
    }

    @Test
    void testLikeTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isLike("fred").map(SearchUtils::addWildcards))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where first_name like #{parameters.p1}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", "%fred%");
    }

    @Test
    void testLikeCaseInsensitiveTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isLikeCaseInsensitive("fred").map(SearchUtils::addWildcards))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where upper(first_name) like #{parameters.p1}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", "%FRED%");
    }

    @Test
    void testLikeCaseInsensitiveWhenPresentTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isLikeCaseInsensitiveWhenPresent("fred").map(SearchUtils::addWildcards))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where upper(first_name) like #{parameters.p1}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", "%FRED%");
    }

    @Test
    void testLikeWhenPresentTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isLikeWhenPresent("fred").map(SearchUtils::addWildcards))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where first_name like #{parameters.p1}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", "%fred%");
    }

    @Test
    void testNotBetweenTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isNotBetween(1).and(10).map(i1 -> i1 + 1,  i2 -> i2 + 2))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where age not between #{parameters.p1} and #{parameters.p2}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 2);
        assertThat(selectStatement.getParameters()).containsEntry("p2", 12);
    }

    @Test
    void testNotBetweenWhenPresentTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isNotBetweenWhenPresent(1).and(10).map(i1 -> i1 + 1,  i2 -> i2 + 2))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where age not between #{parameters.p1} and #{parameters.p2}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 2);
        assertThat(selectStatement.getParameters()).containsEntry("p2", 12);
    }

    @Test
    void testNotEqualTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isNotEqualTo(1).map(i -> i + 1))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where age <> #{parameters.p1}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 2);
    }

    @Test
    void testNotEqualWhenPresentTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isNotEqualToWhenPresent(1).map(i -> i + 1))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where age <> #{parameters.p1}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 2);
    }

    @Test
    void testNotLikeTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isNotLike("fred").map(SearchUtils::addWildcards))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where first_name not like #{parameters.p1}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", "%fred%");
    }

    @Test
    void testNotLikeCaseInsensitiveTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isNotLikeCaseInsensitive("fred").map(SearchUtils::addWildcards))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where upper(first_name) not like #{parameters.p1}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", "%FRED%");
    }

    @Test
    void testNotLikeCaseInsensitiveWhenPresentTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isNotLikeCaseInsensitiveWhenPresent("fred").map(SearchUtils::addWildcards))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where upper(first_name) not like #{parameters.p1}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", "%FRED%");
    }

    @Test
    void testNotLikeWhenPresentTransform() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isNotLikeWhenPresent("fred").map(SearchUtils::addWildcards))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where first_name not like #{parameters.p1}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", "%fred%");
    }

    @Test
    void testBetweenTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isBetween(1).and((Integer) null).filter(Predicates.bothPresent()).map(i1 -> i1 + 1,  i2 -> i2 + 2))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testBetweenWhenPresentTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isBetweenWhenPresent(1).and((Integer) null).map(i1 -> i1 + 1,  i2 -> i2 + 2))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testEqualTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isEqualTo((Integer) null).filter(Objects::nonNull).map(i -> i + 1))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testEqualWhenPresentTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isEqualToWhenPresent((Integer) null).map(i -> i + 1))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testGreaterThanTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isGreaterThan((Integer) null).filter(Objects::nonNull).map(i -> i + 1))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testGreaterThanOrEqualTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isGreaterThanOrEqualTo((Integer) null).filter(Objects::nonNull).map(i -> i + 1))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testGreaterThanOrEqualWhenPresentTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isGreaterThanOrEqualToWhenPresent((Integer) null).map(i -> i + 1))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testGreaterThanWhenPresentTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isGreaterThanWhenPresent((Integer) null).map(i -> i + 1))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testLessThanTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isLessThan((Integer) null).filter(Objects::nonNull).map(i -> i + 1))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testLessThanOrEqualTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isLessThanOrEqualTo((Integer) null).filter(Objects::nonNull).map(i -> i + 1))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testLessThanOrEqualWhenPresentTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isLessThanOrEqualToWhenPresent((Integer) null).map(i -> i + 1))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testLessThanWhenPresentTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isLessThanWhenPresent((Integer) null).map(i -> i + 1))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testLikeTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isLike((String) null).filter(Objects::nonNull).map(SearchUtils::addWildcards))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testLikeCaseInsensitiveTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isLikeCaseInsensitive((String) null).filter(Objects::nonNull).map(SearchUtils::addWildcards))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testLikeCaseInsensitiveWhenPresentTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isLikeCaseInsensitiveWhenPresent((String) null).map(SearchUtils::addWildcards))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testLikeWhenPresentTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isLikeWhenPresent((String) null).map(SearchUtils::addWildcards))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testNotBetweenTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isNotBetween((Integer) null).and(10).filter(Predicates.bothPresent()).map(i1 -> i1 + 1,  i2 -> i2 + 2))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testNotBetweenWhenPresentTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isNotBetweenWhenPresent(1).and((Integer) null).map(i1 -> i1 + 1,  i2 -> i2 + 2))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testNotEqualTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isNotEqualTo((Integer) null).filter(Objects::nonNull).map(i -> i + 1))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testNotEqualWhenPresentTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(age, isNotEqualToWhenPresent((Integer) null).map(i -> i + 1))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testNotLikeTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isNotLike((String) null).filter(Objects::nonNull).map(SearchUtils::addWildcards))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testNotLikeCaseInsensitiveTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isNotLikeCaseInsensitive((String) null).filter(Objects::nonNull).map(SearchUtils::addWildcards))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testNotLikeCaseInsensitiveWhenPresentTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isNotLikeCaseInsensitiveWhenPresent((String) null).map(SearchUtils::addWildcards))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testNotLikeWhenPresentTransformWithNull() {

        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isNotLikeWhenPresent((String) null).map(SearchUtils::addWildcards))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
}
