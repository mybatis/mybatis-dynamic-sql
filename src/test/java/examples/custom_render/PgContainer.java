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
package examples.custom_render;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PgContainer extends PostgreSQLContainer<PgContainer> {

    public PgContainer(String initScriptPath) {
        super(DockerImageName.parse(IMAGE).withTag(DEFAULT_TAG));
        withInitScript(initScriptPath);
    }

    public DataSource getUnpooledDataSource() {
        return new UnpooledDataSource(getDriverClassName(), getJdbcUrl(), getUsername(), getPassword());
    }
}
