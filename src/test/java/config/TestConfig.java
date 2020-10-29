package config;

import dao.SocialNetworkDao;
import org.apache.log4j.Logger;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


@Configuration
@ComponentScan(basePackages = {"entities", "dao"})
public class TestConfig {

    private static Logger logger = Logger.getRootLogger();

    @Bean
    public DataSource dataSource() {
        try {
            EmbeddedDatabaseBuilder dbBuilder = new EmbeddedDatabaseBuilder();
            return dbBuilder.setType(EmbeddedDatabaseType.H2)
                    .addScripts("db/schema.sql", "db/test-data.sql").build();
        } catch (Exception e) {
            logger.error("Embedded DataSource bean cannot be created!", e);
            return null;
        }
    }

    @Bean
    public DatabaseConnection databaseConnection(DataSource dataSource) throws SQLException, DatabaseUnitException {
        Connection connection = dataSource.getConnection();
        DatabaseConnection databaseConnection = new DatabaseConnection(connection);
        DatabaseConfig databaseConfig = databaseConnection.getConfig();
        databaseConfig.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
        return databaseConnection;
    }

    @Bean
    public SocialNetworkDao testDao() {
        SocialNetworkDao testDao = new SocialNetworkDao();
        testDao.setDataSource(dataSource());
        return testDao;
    }

}
