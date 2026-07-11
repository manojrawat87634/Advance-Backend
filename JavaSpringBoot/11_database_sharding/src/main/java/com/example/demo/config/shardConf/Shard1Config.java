package com.example.demo.config.shardConf;

import com.example.demo.repo.auth.userRepo.UserRepoShard1;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(
        basePackageClasses = UserRepoShard1.class, // <-- Direct target
        entityManagerFactoryRef = "shard1EntityManagerFactory",
        transactionManagerRef = "shard1TransactionManager"
)
public class Shard1Config {

   @Primary
@Bean(name = "shard1EntityManagerFactory")
public LocalContainerEntityManagerFactoryBean shard1EntityManagerFactory(
        @Qualifier("shard1DataSource") javax.sql.DataSource dataSource) {
    
    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(dataSource);
    em.setPackagesToScan("com.example.demo.models.auth"); // Your UserModel package
    em.setPersistenceUnitName("shard1");

    // Use Hibernate as the JPA provider natively
    org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter vendorAdapter = 
            new org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter();
    em.setJpaVendorAdapter(vendorAdapter);
    
    return em;
}

    @Primary
    @Bean(name = "shard1TransactionManager")
    public PlatformTransactionManager shard1TransactionManager(
            @Qualifier("shard1EntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}