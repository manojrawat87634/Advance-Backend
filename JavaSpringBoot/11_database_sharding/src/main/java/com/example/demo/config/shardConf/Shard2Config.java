package com.example.demo.config.shardConf;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.demo.repo.auth.userRepo.shard2.UserRepoShard2;

@Configuration
@EnableJpaRepositories(
        basePackageClasses = UserRepoShard2.class, // <-- Direct target
        entityManagerFactoryRef = "shard2EntityManagerFactory",
        transactionManagerRef = "shard2TransactionManager"
)
public class Shard2Config {

    @Bean(name = "shard2EntityManagerFactory")
public LocalContainerEntityManagerFactoryBean shard2EntityManagerFactory(
        @Qualifier("shard2DataSource") javax.sql.DataSource dataSource) {
    
    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(dataSource);
    em.setPackagesToScan("com.example.demo.models.auth"); // Your UserModel package
    em.setPersistenceUnitName("shard2");

    org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter vendorAdapter = 
            new org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter();
    em.setJpaVendorAdapter(vendorAdapter);
    
    return em;
}

    @Bean(name = "shard2TransactionManager")
    public PlatformTransactionManager shard2TransactionManager(
            @Qualifier("shard2EntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}