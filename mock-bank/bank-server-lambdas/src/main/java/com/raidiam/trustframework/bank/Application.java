package com.raidiam.trustframework.bank;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.runtime.Micronaut;
import org.postgresql.Driver;

import javax.inject.Singleton;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@TypeHint({
  Driver.class,
})
public class Application {

  @Singleton
  static class ObjectMapperBeanEventListener implements BeanCreatedEventListener<ObjectMapper> {

    @Override
    public ObjectMapper onCreated(BeanCreatedEvent<ObjectMapper> event) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
      SimpleModule timeModule = new SimpleModule();
      timeModule.addSerializer(OffsetDateTime.class, new JsonSerializer<OffsetDateTime>() {
        @Override
        public void serialize(OffsetDateTime offsetDateTime, JsonGenerator gen, SerializerProvider serializers) throws IOException {
          gen.writeString(formatter.format(offsetDateTime));
        }
      });
      final ObjectMapper mapper = event.getBean()
              .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
              .registerModule(timeModule);
      return mapper;
    }
  }

  public static void main(String[] args) {
    Micronaut.run(Application.class);
  }
}
