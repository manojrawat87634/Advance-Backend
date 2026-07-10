package com.example.demo.config.shardConf;
import java.util.Map;
import java.util.function.Function;
import javax.sql.DataSource;

import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
public class JpaBuilderConfig {

    @Bean
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder(
            JpaProperties jpaProperties) {

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();

        Function<DataSource, Map<String, ?>> propertiesFactory =
                dataSource -> jpaProperties.getProperties();

        return new EntityManagerFactoryBuilder(
                vendorAdapter,
                propertiesFactory,
                null
        );
    }
}