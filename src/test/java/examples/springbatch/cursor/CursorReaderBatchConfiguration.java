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
package examples.springbatch.cursor;

import static examples.springbatch.mapper.PersonDynamicSqlSupport.lastName;
import static examples.springbatch.mapper.PersonDynamicSqlSupport.person;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.springbatch.SpringBatchUtility;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

import examples.springbatch.common.Person;
import examples.springbatch.mapper.PersonMapper;

@EnableBatchProcessing
@Configuration
@ComponentScan("examples.springbatch.common")
@MapperScan("examples.springbatch.mapper")
public class CursorReaderBatchConfiguration {
    
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
                .addScript("classpath:/examples/springbatch/data.sql")
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
    public MyBatisCursorItemReader<Person> reader(SqlSessionFactory sqlSessionFactory) {
        SelectStatementProvider selectStatement =  SpringBatchUtility.selectForCursor(person.allColumns())
                .from(person)
                .where(lastName, isEqualTo("flintstone"))
                .build()
                .render();
        
        MyBatisCursorItemReader<Person> reader = new MyBatisCursorItemReader<>();
        reader.setQueryId(PersonMapper.class.getName() + ".selectMany");
        reader.setSqlSessionFactory(sqlSessionFactory);
        reader.setParameterValues(SpringBatchUtility.toParameterValues(selectStatement));
        return reader;
    }
    
    @Bean
    public MyBatisBatchItemWriter<Person> writer(SqlSessionFactory sqlSessionFactory,
            Converter<Person, UpdateStatementProvider> convertor) {
        MyBatisBatchItemWriter<Person> writer = new MyBatisBatchItemWriter<>();
        writer.setSqlSessionFactory(sqlSessionFactory);
        writer.setItemToParameterConverter(convertor);
        writer.setStatementId(PersonMapper.class.getName() + ".update");
        return writer;
    }
    
    @Bean
    public Step step1(ItemReader<Person> reader, ItemProcessor<Person, Person> processor, ItemWriter<Person> writer) {
        return stepBuilderFactory.get("step1")
                .<Person, Person>chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job upperCaseLastName(Step step1) {
        return jobBuilderFactory.get("upperCaseLastName")
                .incrementer(new RunIdIncrementer())
                .flow(step1)
                .end()
                .build();
    }
}
