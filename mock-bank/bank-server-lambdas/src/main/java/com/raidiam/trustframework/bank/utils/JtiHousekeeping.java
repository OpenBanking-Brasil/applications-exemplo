package com.raidiam.trustframework.bank.utils;

import com.raidiam.trustframework.bank.repository.JtiRepository;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.event.ApplicationStartupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.time.OffsetDateTime;

@Singleton
@Requires(beans = DataSource.class)
public class JtiHousekeeping implements ApplicationEventListener<ApplicationStartupEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(JtiHousekeeping.class);

    @Inject
    private JtiRepository jtiRepository;

    @Override
    public void onApplicationEvent(ApplicationStartupEvent event) {
        LOG.trace("Cleaning out old JTI entries");
        jtiRepository.deleteOld(OffsetDateTime.now().minusDays(7));
    }
}
