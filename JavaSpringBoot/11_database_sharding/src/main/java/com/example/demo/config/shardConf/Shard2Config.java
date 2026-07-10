package com.example.demo.config.shardConf;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.demo.repo.shard2",
        entityManagerFactoryRef = "shard2EntityManagerFactory",
        transactionManagerRef = "shard2TransactionManager"
)
public class Shard2Config {

    @Bean(name = "shard2EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean shard2EntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("shard2DataSource") DataSource dataSource) {

        return builder
                .dataSource(dataSource)
                .packages("com.example.demo.models")
                .persistenceUnit("shard2")
                .build();
    }

    @Bean(name = "shard2TransactionManager")
    public PlatformTransactionManager shard2TransactionManager(
            @Qualifier("shard2EntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}