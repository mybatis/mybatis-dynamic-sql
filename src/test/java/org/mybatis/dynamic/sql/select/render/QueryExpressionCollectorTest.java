/**
 *    Copyright 2016-2017 the original author or authors.
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
package org.mybatis.dynamic.sql.select.render;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mybatis.dynamic.sql.where.render.WhereClauseProvider;

@RunWith(JUnitPlatform.class)
public class QueryExpressionCollectorTest {

    @Test
    public void testQueryExpressionCollectorMerge() {
        List<QueryExpression> queryExpressions = new ArrayList<>();
        
        Map<String, Object> parms1 = new HashMap<>();
        parms1.put("p1", 1);

        WhereClauseProvider wcp1 = WhereClauseProvider.withWhereClause("where fred = ?")
                .withParameters(parms1)
                .build();
        
        QueryExpression qe1 = QueryExpression.withColumnList("foo")
                .withConnector(Optional.empty())
                .withTableName("bar")
                .withWhereClause(Optional.of(wcp1))
                .build();
        queryExpressions.add(qe1);

        Map<String, Object> parms2 = new HashMap<>();
        parms2.put("p2", 2);

        WhereClauseProvider wcp2 = WhereClauseProvider.withWhereClause("where betty = ?")
                .withParameters(parms2)
                .build();
        
        QueryExpression qe2 = QueryExpression.withColumnList("bar")
                .withConnector(Optional.of("union"))
                .withTableName("foo")
                .withWhereClause(Optional.of(wcp2))
                .build();
        queryExpressions.add(qe2);

        // parallelStream should trigger the merge
        QueryExpressionCollector collector = queryExpressions.parallelStream()
                .collect(QueryExpressionCollector.collect());

        assertThat(collector.queryExpression()).isEqualTo("select foo from bar where fred = ? union select bar from foo where betty = ?");
        assertThat(collector.parameters().size()).isEqualTo(2);
        assertThat(collector.parameters().get("p1")).isEqualTo(1);
        assertThat(collector.parameters().get("p2")).isEqualTo(2);
    }
}
