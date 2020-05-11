package examples.custom_render;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

public class PgContainer extends PostgreSQLContainer<PgContainer> {

    public PgContainer(String initScriptPath) {
        super();
        withInitScript(initScriptPath);
    }

    public DataSource getUnpooledDataSource() {
        return new UnpooledDataSource(getDriverClassName(), getJdbcUrl(), getUsername(), getPassword());
    }
}
