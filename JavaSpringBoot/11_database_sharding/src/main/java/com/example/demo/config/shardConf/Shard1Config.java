package com.example.demo.config.shardConf;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.demo.repo.shard1",
        entityManagerFactoryRef = "shard1EntityManagerFactory",
        transactionManagerRef = "shard1TransactionManager"
)
public class Shard1Config {

    @Primary
    @Bean(name = "shard1EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean shard1EntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("shard1DataSource") DataSource dataSource) {

        return builder
                .dataSource(dataSource)
                .packages("com.example.demo.models")
                .persistenceUnit("shard1")
                .build();
    }

    @Primary
    @Bean(name = "shard1TransactionManager")
    public PlatformTransactionManager shard1TransactionManager(
            @Qualifier("shard1EntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}