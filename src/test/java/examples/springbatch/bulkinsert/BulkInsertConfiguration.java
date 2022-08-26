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

import static examples.springbatch.mapper.PersonDynamicSqlSupport.firstName;
import static examples.springbatch.mapper.PersonDynamicSqlSupport.forPagingTest;
import static examples.springbatch.mapper.PersonDynamicSqlSupport.lastName;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.dynamic.sql.insert.InsertDSL;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

import examples.springbatch.common.PersonRecord;
import examples.springbatch.mapper.PersonDynamicSqlSupport;
import examples.springbatch.mapper.PersonMapper;

@EnableBatchProcessing
@Configuration
@ComponentScan("examples.springbatch.bulkinsert")
@ComponentScan("examples.springbatch.common")
@MapperScan("examples.springbatch.mapper")
public class BulkInsertConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .addScript("classpath:/org/springframework/batch/core/schema-drop-hsqldb.sql")
                .addScript("classpath:/org/springframework/batch/core/schema-hsqldb.sql")
                .addScript("classpath:/examples/springbatch/schema.sql")
                .build();
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        return sessionFactory.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public MyBatisBatchItemWriter<PersonRecord> writer(SqlSessionFactory sqlSessionFactory) {
        MyBatisBatchItemWriter<PersonRecord> writer = new MyBatisBatchItemWriter<>();
        writer.setSqlSessionFactory(sqlSessionFactory);

        writer.setItemToParameterConverter(record -> InsertDSL.insert(record)
                    .into(PersonDynamicSqlSupport.person)
                    .map(firstName).toProperty("firstName")
                    .map(lastName).toProperty("lastName")
                    .map(forPagingTest).toStringConstant("false")
                    .build()
                    .render(RenderingStrategies.MYBATIS3));

        writer.setStatementId(PersonMapper.class.getName() + ".insert");
        return writer;
    }

    @Bean
    public Step step1(ItemProcessor<PersonRecord, PersonRecord> processor, ItemWriter<PersonRecord> writer) {
        return stepBuilderFactory.get("step1")
                .<PersonRecord, PersonRecord>chunk(10)
                .reader(new TestRecordGenerator())
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job insertRecords(Step step1) {
        return jobBuilderFactory.get("insertRecords")
                .incrementer(new RunIdIncrementer())
                .flow(step1)
                .end()
                .build();
    }
}
