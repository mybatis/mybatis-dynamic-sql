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
package examples.springbatch;

import static org.mybatis.dynamic.sql.SqlBuilder.*;

import static examples.springbatch.PersonDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.SelectDSL;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes=CursorReaderBatchConfiguration.class)
public class SpringBatchCursorTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private SqlSessionFactoryBean sqlSessionFactory;

    @Test
    public void testThatRowsAreTransformedToUpperCase() throws Exception {
        // starting condition
        assertThat(upperCaseRowCount()).isEqualTo(0);

        JobExecution execution = jobLauncherTestUtils.launchJob();
        assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(numberOfRowsProcessed(execution)).isEqualTo(2);

        // ending condition
        assertThat(upperCaseRowCount()).isEqualTo(2);
    }

    private int numberOfRowsProcessed(JobExecution jobExecution) {
        return jobExecution.getStepExecutions().stream()
                .map(StepExecution::getExecutionContext)
                .mapToInt(ec -> ec.getInt("row_count"))
                .sum();
    }
    
    private long upperCaseRowCount() throws Exception {
        try (SqlSession sqlSession = sqlSessionFactory.getObject().openSession()) {
            PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);

            SelectStatementProvider selectStatement = SelectDSL.select(count())
                    .from(person)
                    .where(lastName, isEqualTo("FLINTSTONE"))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            long count = personMapper.count(selectStatement);
            return count;
        }
    }
}
