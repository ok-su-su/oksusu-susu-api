package com.goofy.boilerplate.config.validation

import org.hibernate.validator.internal.engine.DefaultClockProvider
import org.springframework.core.KotlinReflectionParameterNameDiscoverer
import org.springframework.core.LocalVariableTableParameterNameDiscoverer
import org.springframework.core.ParameterNameDiscoverer
import org.springframework.core.PrioritizedParameterNameDiscoverer
import org.springframework.core.StandardReflectionParameterNameDiscoverer
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import javax.validation.ClockProvider
import javax.validation.ParameterNameProvider
import kotlin.reflect.jvm.kotlinFunction

class CoroutinesLocalValidatorFactoryBean : LocalValidatorFactoryBean() {
    override fun getClockProvider(): ClockProvider {
        return DefaultClockProvider.INSTANCE
    }

    override fun postProcessConfiguration(configuration: javax.validation.Configuration<*>) {
        super.postProcessConfiguration(configuration)

        val discoverer = PrioritizedParameterNameDiscoverer().apply {
            this.addDiscoverer(SuspendAwareKotlinParameterNameDiscoverer())
            this.addDiscoverer(StandardReflectionParameterNameDiscoverer())
            this.addDiscoverer(LocalVariableTableParameterNameDiscoverer())
        }

        val defaultProvider = configuration.defaultParameterNameProvider
        configuration.parameterNameProvider(object : ParameterNameProvider {
            override fun getParameterNames(constructor: Constructor<*>): List<String> {
                val paramNames: Array<String>? = discoverer.getParameterNames(constructor)
                return paramNames?.toList() ?: defaultProvider.getParameterNames(constructor)
            }

            override fun getParameterNames(method: Method): List<String> {
                val paramNames: Array<String>? = discoverer.getParameterNames(method)
                return paramNames?.toList() ?: defaultProvider.getParameterNames(method)
            }
        })
    }
}

class SuspendAwareKotlinParameterNameDiscoverer : ParameterNameDiscoverer {
    private val defaultProvider = KotlinReflectionParameterNameDiscoverer()

    override fun getParameterNames(constructor: Constructor<*>): Array<String>? {
        return defaultProvider.getParameterNames(constructor)
    }

    override fun getParameterNames(method: Method): Array<String>? {
        val defaultNames = defaultProvider.getParameterNames(method) ?: return null
        val function = method.kotlinFunction

        return when {
            function != null && function.isSuspend -> defaultNames + ""
            else -> defaultNames
        }
    }
}
