/*
 *    Copyright 2016-2024 the original author or authors.
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
package examples.kotlin.mybatis3

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource
import org.apache.ibatis.jdbc.ScriptRunner
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory
import java.io.InputStreamReader
import javax.sql.DataSource
import kotlin.reflect.KClass

class TestUtils {
    companion object {
        const val JDBC_URL = "jdbc:hsqldb:mem:aname"
        const val JDBC_DRIVER = "org.hsqldb.jdbcDriver"

        fun buildSqlSessionFactory(configurator: FactoryConfig.() -> Unit): SqlSessionFactory {
            val factoryConfig = FactoryConfig().apply(configurator)

            val dataSource = factoryConfig.dataSource?:defaultDataSource()

            factoryConfig.initializationScript?.let {
                val script = TestUtils::class.java.getResourceAsStream(it)
                dataSource.getConnection("sa", "").use { connection ->
                    val sr = ScriptRunner(connection)
                    sr.setLogWriter(null)
                    sr.runScript(InputStreamReader(script!!))
                }
            }

            val environment = Environment("test", JdbcTransactionFactory(), dataSource)
            with(Configuration(environment)) {
                factoryConfig.typeHandlers.map(KClass<*>::java).forEach(typeHandlerRegistry::register)
                factoryConfig.mappers.map(KClass<*>::java).forEach { addMapper(it) }
                return SqlSessionFactoryBuilder().build(this)
            }
        }

        private fun defaultDataSource() = UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "")
    }

    class FactoryConfig {
        internal var initializationScript: String? = null
        internal val mappers = mutableListOf<KClass<*>>()
        internal val typeHandlers = mutableListOf<KClass<*>>()
        internal var dataSource: DataSource? = null

        fun withInitializationScript(initializationScript: String) {
            this.initializationScript = initializationScript
        }

        fun withMapper(mapper: KClass<*>) {
            this.mappers.add(mapper)
        }

        fun withTypeHandler(typeHandler: KClass<*>) {
            this.typeHandlers.add(typeHandler)
        }

        fun withDataSource(dataSource: DataSource) {
            this.dataSource = dataSource
        }
    }
}
