package config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


@Configuration
@PropertySource("classpath:jdbc.properties")
@ComponentScan(basePackages = {"entities", "dao"})
public class AppConfig {

    private static Logger logger = Logger.getRootLogger();
    @Value("${jdbc.driverClassName}")
    private String driverClassName;
    @Value("${jdbc.url}")
    private String url;
    @Value("${jdbc.username}")
    private String username;
    @Value("${jdbc.password}")
    private String password;


    @Bean(destroyMethod = "close")
    public BasicDataSource dataSource() {
        try{
            BasicDataSource dataSource = new BasicDataSource();
            dataSource.setDriverClassName(driverClassName);
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            logger.info("Database connected.");
            return dataSource;
        } catch (Exception e) {
            logger.info("Database connection failed.");
            logger.error(e.getMessage());
            return null;
        }
    }
}
