package com.oksusu.susu.config

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@Configuration
class JacksonConfig {
    /**
     * this Bean can customize spring boot auto-configured ObjectMapper.
     * you can also customize via yml configuration. spring.jackson.x
     */
    @Bean
    fun customizeJson(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder: Jackson2ObjectMapperBuilder ->
            builder
                .failOnUnknownProperties(false)
            // 이거 설정해두면 널 값이 response에 표기되지 않아서 주석처리 했습니다
//                .serializationInclusion(JsonInclude.Include.NON_ABSENT)
        }
    }
}
