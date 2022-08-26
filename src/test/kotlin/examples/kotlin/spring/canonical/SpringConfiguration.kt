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
package examples.kotlin.spring.canonical

import javax.sql.DataSource

import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager

@Configuration
open class SpringConfiguration {
    @Bean
    open fun datasource(): DataSource =
        EmbeddedDatabaseBuilder().run {
            setType(EmbeddedDatabaseType.HSQL)
            generateUniqueName(true)
            addScript("classpath:/examples/kotlin/spring/CreateGeneratedAlwaysDB.sql")
            addScript("classpath:/examples/kotlin/spring/CreateSimpleDB.sql")
            build()
        }

    @Bean
    open fun template(dataSource: DataSource) = NamedParameterJdbcTemplate(dataSource)

    @Bean
    open fun transactionManager(dataSource: DataSource) = DataSourceTransactionManager(dataSource)
}
