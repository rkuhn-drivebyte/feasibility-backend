package de.numcodex.feasibility_gui_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CommonSpringConfig {

    @Primary
    @Bean
    ObjectMapper jsonUtil() {
        return new ObjectMapper();
    }
}
