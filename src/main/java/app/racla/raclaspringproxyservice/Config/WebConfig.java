package app.racla.raclaspringproxyservice.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Bean
  public ObjectMapper objectMapper() {
    return Jackson2ObjectMapperBuilder.json()
        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .modules(new JavaTimeModule()).build();
  }
}
