package com.example.demo.config.shardConf;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {

    @Bean(name = "shard1DataSource")
    @ConfigurationProperties(prefix = "shard1.datasource") // <-- Updated here
    public DataSource shard1DataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "shard2DataSource")
    @ConfigurationProperties(prefix = "shard2.datasource") // <-- Updated here
    public DataSource shard2DataSource() {
        return DataSourceBuilder.create().build();
    }
}