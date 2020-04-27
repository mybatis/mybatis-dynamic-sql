package examples.custom_render;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

public class PgContainer extends PostgreSQLContainer<PgContainer> {

    public PgContainer(String initScriptPath) {
        super();
        withInitScript(initScriptPath);
    }

    public Connection connect() throws SQLException {
        DriverManager.registerDriver(getJdbcDriverInstance());
        return DriverManager.getConnection(getJdbcUrl(), getUsername(), getPassword());
    }

    public DataSource getUnpooledDataSource() {
        return new UnpooledDataSource(getDriverClassName(), getJdbcUrl(), getUsername(), getPassword());
    }
}
