package com.raidiam.trustframework.bank.db;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class DbInitHandler implements RequestHandler<Map<String,String>, String> {

    private static final Logger log = LoggerFactory.getLogger(DbInitHandler.class);

    @Override
    public String handleRequest(Map<String,String> event, Context context)
    {
        log.info("Starting Flyway migrations");
        String dbUrl = System.getenv("DB_URL");
        String dbUsername = System.getenv("DB_USERNAME");
        String dbPassword = System.getenv("DB_PASSWORD");
        boolean existingEnvironment = Boolean.parseBoolean(System.getenv("LEGACY_DB"));
        Flyway flyway = Flyway.configure().dataSource(dbUrl, dbUsername, dbPassword).load();
        if (existingEnvironment) {
            log.info("Running baseline operation");
            flyway.baseline();
        }
        MigrateResult migrateResult = flyway.migrate();
        log.info("Flyway migration completed, migrations executed {}", migrateResult.migrationsExecuted);
        return "200 OK";
    }
}
