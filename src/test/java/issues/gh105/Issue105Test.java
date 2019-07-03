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
package issues.gh105;

import static issues.gh105.PersonDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

public class Issue105Test {

    @Test
    public void testFuzzySearchBothPresent() {
        String fName = "Fred";
        String lName = "Flintstone";
        
        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isLike(fName).when(Objects::nonNull).then(s -> "%" + s + "%"))
                .and(lastName, isLike(lName).when(Objects::nonNull).then(s -> "%" + s + "%"))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where first_name like #{parameters.p1}"
                + " and last_name like #{parameters.p2}";
                
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters().get("p1")).isEqualTo("%Fred%");
        assertThat(selectStatement.getParameters().get("p2")).isEqualTo("%Flintstone%");
    }

    @Test
    public void testFuzzySearchFirstNameNull() {
        String fName = null;
        String lName = "Flintstone";
        
        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isLike(fName).when(Objects::nonNull).then(SearchUtils::addWildcards))
                .and(lastName, isLike(lName).when(Objects::nonNull).then(SearchUtils::addWildcards))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where last_name like #{parameters.p1}";
                
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters().get("p1")).isEqualTo("%Flintstone%");
    }

    @Test
    public void testFuzzySearchLastNameNull() {
        String fName = "Fred";
        String lName = null;
        
        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isLike(fName).when(Objects::nonNull).then(SearchUtils::addWildcards))
                .and(lastName, isLike(lName).when(Objects::nonNull).then(SearchUtils::addWildcards))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person"
                + " where first_name like #{parameters.p1}";
                
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters().get("p1")).isEqualTo("%Fred%");
    }

    @Test
    public void testFuzzySearchBothNull() {
        String fName = null;
        String lName = null;
        
        SelectStatementProvider selectStatement = select(id, firstName, lastName)
                .from(person)
                .where(firstName, isLike(fName).when(Objects::nonNull).then(SearchUtils::addWildcards))
                .and(lastName, isLike(lName).when(Objects::nonNull).then(SearchUtils::addWildcards))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        String expected = "select person_id, first_name, last_name"
                + " from Person";
                
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters().size()).isEqualTo(0);
    }
}
