package app.racla.raclaspringproxyservice.Config;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

        @Bean
        public RestTemplate restTemplate() {
                RestTemplate restTemplate = new RestTemplate();

                StringHttpMessageConverter stringConverter =
                                new StringHttpMessageConverter(StandardCharsets.UTF_8);
                stringConverter.setSupportedMediaTypes(List.of(MediaType.TEXT_HTML,
                                MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON,
                                new MediaType("text", "*"), new MediaType("application", "*+json"),
                                MediaType.ALL));

                MappingJackson2HttpMessageConverter jacksonConverter =
                                new MappingJackson2HttpMessageConverter();
                List<MediaType> supportedMediaTypes =
                                new ArrayList<>(jacksonConverter.getSupportedMediaTypes());
                supportedMediaTypes.add(MediaType.TEXT_HTML);
                supportedMediaTypes.add(MediaType.TEXT_PLAIN);
                supportedMediaTypes.add(new MediaType("text", "*"));
                supportedMediaTypes.add(MediaType.ALL);
                jacksonConverter.setSupportedMediaTypes(supportedMediaTypes);

                List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
                messageConverters.add(stringConverter);
                messageConverters.add(jacksonConverter);

                restTemplate.setMessageConverters(messageConverters);

                return restTemplate;
        }
}
