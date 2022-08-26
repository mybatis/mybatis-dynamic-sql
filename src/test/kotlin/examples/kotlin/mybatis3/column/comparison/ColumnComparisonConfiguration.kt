/*
 *    Copyright 2016-2021 the original author or authors.
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
package examples.kotlin.mybatis3.column.comparison

import org.apache.ibatis.session.SqlSessionFactory
import org.mybatis.spring.SqlSessionFactoryBean
import org.mybatis.spring.annotation.MapperScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import javax.sql.DataSource

@Configuration
@MapperScan("examples.kotlin.mybatis3.column.comparison")
open class ColumnComparisonConfiguration {
    @Bean
    open fun dataSource(): DataSource =
        EmbeddedDatabaseBuilder().run {
            setType(EmbeddedDatabaseType.HSQL)
            generateUniqueName(true)
            addScript("classpath:/examples/column/comparison/CreateDB.sql")
            build()
        }

    @Bean
    open fun sqlSessionFactory(dataSource: DataSource): SqlSessionFactory =
        SqlSessionFactoryBean().run {
            setDataSource(dataSource)
            `object`!!
        }
}
