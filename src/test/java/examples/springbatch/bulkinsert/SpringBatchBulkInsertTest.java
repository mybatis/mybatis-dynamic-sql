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
package examples.springbatch.bulkinsert;

import static examples.springbatch.mapper.PersonDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.CountDSL;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import examples.springbatch.mapper.PersonMapper;

@SpringBatchTest
@SpringJUnitConfig(classes = BulkInsertConfiguration.class)
class SpringBatchBulkInsertTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Test
    void testThatRowsAreInserted() throws Exception {
        // starting condition
        assertThat(rowCount()).isZero();

        JobExecution execution = jobLauncherTestUtils.launchJob();
        assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(numberOfRowsProcessed(execution)).isEqualTo(TestRecordGenerator.recordCount());

        // ending condition
        assertThat(rowCount()).isEqualTo(TestRecordGenerator.recordCount());
    }

    private int numberOfRowsProcessed(JobExecution jobExecution) {
        return jobExecution.getStepExecutions().stream()
                .map(StepExecution::getExecutionContext)
                .mapToInt(this::getRowCount)
                .sum();
    }

    private int getRowCount(ExecutionContext executionContext) {
        return executionContext.getInt("row_count", 0);
    }

    private long rowCount() throws Exception {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);

            SelectStatementProvider selectStatement = CountDSL.countFrom(person)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            return personMapper.count(selectStatement);
        }
    }
}
