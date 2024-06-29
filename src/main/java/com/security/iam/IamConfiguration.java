package com.security.iam;

import com.zaxxer.hikari.HikariDataSource;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.casbin.adapter.JDBCAdapter;
import org.casbin.annotation.CasbinDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.sql.DataSource;

@OpenAPIDefinition
@EnableJpaAuditing
@Configuration
public final class IamConfiguration {
    @Bean
    @CasbinDataSource
    public DataSource casbinDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:postgresql://localhost:5436/iam-db");
        dataSource.setUsername("user");
        dataSource.setPassword("password");
        return dataSource;
    }

    @Bean
    public JDBCAdapter casbinJDBCAdapter() throws Exception {
        return new JDBCAdapter(casbinDataSource());
    }
}
