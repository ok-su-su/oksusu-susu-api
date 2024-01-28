package com.oksusu.susu.config.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.oksusu.susu.auth.application.AuthFacade
import com.oksusu.susu.auth.resolver.ReactiveAuthResolver
import com.oksusu.susu.user.resolver.ReactiveDeviceContextResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.ReactiveAdapterRegistry
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver
import org.springframework.data.web.ReactiveSortHandlerMethodArgumentResolver
import org.springframework.format.FormatterRegistry
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.util.MimeType
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.ViewResolverRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer
import org.thymeleaf.spring6.ISpringWebFluxTemplateEngine
import org.thymeleaf.spring6.SpringWebFluxTemplateEngine
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver
import org.thymeleaf.spring6.view.reactive.ThymeleafReactiveViewResolver
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ITemplateResolver
import java.nio.charset.Charset

@Configuration
class WebFluxConfig(
    private val objectMapper: ObjectMapper,
    private val authFacade: AuthFacade,
) : WebFluxConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOriginPatterns(CorsConfiguration.ALL)
            .allowedMethods(CorsConfiguration.ALL)
            .allowedHeaders(CorsConfiguration.ALL)
            .allowCredentials(true)
            .maxAge(3600)
    }

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        val mimeTypes = arrayOf(
            MimeType("application", "json"),
            MimeType("application", "*+json"),
            MimeType("application", "json", Charset.forName("UTF-8"))
        )

        configurer.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper, *mimeTypes))
        configurer.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper))
    }

    override fun configureArgumentResolvers(configurer: ArgumentResolverConfigurer) {
        val registry = ReactiveAdapterRegistry()
        val serverCodecConfigurer = ServerCodecConfigurer.create()
        configureHttpMessageCodecs(serverCodecConfigurer)

        configurer.addCustomResolver(
            ReactiveDeviceContextResolver(registry),
            ReactiveAuthResolver(registry, authFacade),
            ReactiveSortHandlerMethodArgumentResolver(),
            ReactivePageableHandlerMethodArgumentResolver()
        )
    }

    override fun addFormatters(registry: FormatterRegistry) {
        val registrar = DateTimeFormatterRegistrar()
        registrar.setUseIsoFormat(true)
        registrar.registerFormatters(registry)
        super.addFormatters(registry)
    }

    /** thymeleaf 설정 */
    override fun configureViewResolvers(registry: ViewResolverRegistry) {
        registry.viewResolver(thymeleafReactiveViewResolver())
    }

    @Bean
    fun thymeleafReactiveViewResolver(): ThymeleafReactiveViewResolver {
        val viewResolver = ThymeleafReactiveViewResolver()
        viewResolver.templateEngine = thymeleafTemplateEngine()
        return viewResolver
    }

    @Bean
    fun thymeleafTemplateEngine(): ISpringWebFluxTemplateEngine {
        val templateEngine = SpringWebFluxTemplateEngine()
        templateEngine.setTemplateResolver(thymeleafTemplateResolver())
        return templateEngine
    }

    @Bean
    fun thymeleafTemplateResolver(): ITemplateResolver {
        val resolver = SpringResourceTemplateResolver()
        resolver.prefix = "classpath:templates/"
        resolver.suffix = ".html"
        resolver.templateMode = TemplateMode.HTML
        resolver.isCacheable = false
        resolver.checkExistence = false
        resolver.characterEncoding = "UTF-8"
        resolver.order = 1
        return resolver
    }
}
