/*
 *    Copyright 2016-2020 the original author or authors.
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
                .where(firstName, isLike(fName).when(Objects::nonNull).then(s -> "%" + s + "%"))
                .and(lastName, isLike(lName).when(Objects::nonNull).then(s -> "%" + s + "%"))
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
                .where(firstName, isLike(fName).when(Objects::nonNull).then(SearchUtils::addWildcards))
                .and(lastName, isLike(lName).when(Objects::nonNull).then(SearchUtils::addWildcards))
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
                .where(firstName, isLike(fName).when(Objects::nonNull).then(SearchUtils::addWildcards))
                .and(lastName, isLike(lName).when(Objects::nonNull).then(SearchUtils::addWildcards))
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
                .where(firstName, isLike(fName).when(Objects::nonNull).then(SearchUtils::addWildcards))
                .and(lastName, isLike(lName).when(Objects::nonNull).then(SearchUtils::addWildcards))
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
                .where(age, isBetween(1).and(10).then(i1 -> i1 + 1,  i2 -> i2 + 2))
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
                .where(age, isBetweenWhenPresent(1).and(10).then(i1 -> i1 + 1,  i2 -> i2 + 2))
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
                .where(age, isEqualTo(1).then(i -> i + 1))
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
                .where(age, isEqualToWhenPresent(1).then(i -> i + 1))
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
                .where(age, isGreaterThan(1).then(i -> i + 1))
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
                .where(age, isGreaterThanOrEqualTo(1).then(i -> i + 1))
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
                .where(age, isGreaterThanOrEqualToWhenPresent(1).then(i -> i + 1))
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
                .where(age, isGreaterThanWhenPresent(1).then(i -> i + 1))
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
                .where(age, isLessThan(1).then(i -> i + 1))
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
                .where(age, isLessThanOrEqualTo(1).then(i -> i + 1))
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
                .where(age, isLessThanOrEqualToWhenPresent(1).then(i -> i + 1))
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
                .where(age, isLessThanWhenPresent(1).then(i -> i + 1))
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
                .where(firstName, isLike("fred").then(SearchUtils::addWildcards))
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
                .where(firstName, isLikeCaseInsensitive("fred").then(SearchUtils::addWildcards))
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
                .where(firstName, isLikeCaseInsensitiveWhenPresent("fred").then(SearchUtils::addWildcards))
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
                .where(firstName, isLikeWhenPresent("fred").then(SearchUtils::addWildcards))
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
                .where(age, isNotBetween(1).and(10).then(i1 -> i1 + 1,  i2 -> i2 + 2))
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
                .where(age, isNotBetweenWhenPresent(1).and(10).then(i1 -> i1 + 1,  i2 -> i2 + 2))
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
                .where(age, isNotEqualTo(1).then(i -> i + 1))
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
                .where(age, isNotEqualToWhenPresent(1).then(i -> i + 1))
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
                .where(firstName, isNotLike("fred").then(SearchUtils::addWildcards))
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
                .where(firstName, isNotLikeCaseInsensitive("fred").then(SearchUtils::addWildcards))
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
                .where(firstName, isNotLikeCaseInsensitiveWhenPresent("fred").then(SearchUtils::addWildcards))
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
                .where(firstName, isNotLikeWhenPresent("fred").then(SearchUtils::addWildcards))
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
                .where(age, isBetween(1).and((Integer) null).when(Predicates.bothPresent()).then(i1 -> i1 + 1,  i2 -> i2 + 2))
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
                .where(age, isBetweenWhenPresent(1).and((Integer) null).then(i1 -> i1 + 1,  i2 -> i2 + 2))
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
                .where(age, isEqualTo((Integer) null).when(Objects::nonNull).then(i -> i + 1))
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
                .where(age, isEqualToWhenPresent((Integer) null).then(i -> i + 1))
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
                .where(age, isGreaterThan((Integer) null).when(Objects::nonNull).then(i -> i + 1))
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
                .where(age, isGreaterThanOrEqualTo((Integer) null).when(Objects::nonNull).then(i -> i + 1))
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
                .where(age, isGreaterThanOrEqualToWhenPresent((Integer) null).then(i -> i + 1))
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
                .where(age, isGreaterThanWhenPresent((Integer) null).then(i -> i + 1))
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
                .where(age, isLessThan((Integer) null).when(Objects::nonNull).then(i -> i + 1))
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
                .where(age, isLessThanOrEqualTo((Integer) null).when(Objects::nonNull).then(i -> i + 1))
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
                .where(age, isLessThanOrEqualToWhenPresent((Integer) null).then(i -> i + 1))
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
                .where(age, isLessThanWhenPresent((Integer) null).then(i -> i + 1))
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
                .where(firstName, isLike((String) null).when(Objects::nonNull).then(SearchUtils::addWildcards))
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
                .where(firstName, isLikeCaseInsensitive((String) null).when(Objects::nonNull).then(SearchUtils::addWildcards))
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
                .where(firstName, isLikeCaseInsensitiveWhenPresent((String) null).then(SearchUtils::addWildcards))
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
                .where(firstName, isLikeWhenPresent((String) null).then(SearchUtils::addWildcards))
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
                .where(age, isNotBetween((Integer) null).and(10).when(Predicates.bothPresent()).then(i1 -> i1 + 1,  i2 -> i2 + 2))
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
                .where(age, isNotBetweenWhenPresent(1).and((Integer) null).then(i1 -> i1 + 1,  i2 -> i2 + 2))
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
                .where(age, isNotEqualTo((Integer) null).when(Objects::nonNull).then(i -> i + 1))
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
                .where(age, isNotEqualToWhenPresent((Integer) null).then(i -> i + 1))
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
                .where(firstName, isNotLike((String) null).when(Objects::nonNull).then(SearchUtils::addWildcards))
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
                .where(firstName, isNotLikeCaseInsensitive((String) null).when(Objects::nonNull).then(SearchUtils::addWildcards))
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
                .where(firstName, isNotLikeCaseInsensitiveWhenPresent((String) null).then(SearchUtils::addWildcards))
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
                .where(firstName, isNotLikeWhenPresent((String) null).then(SearchUtils::addWildcards))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";
                
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
}
