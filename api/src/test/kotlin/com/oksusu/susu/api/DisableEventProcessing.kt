package com.oksusu.susu.api

import org.springframework.test.context.TestPropertySource

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@TestPropertySource(properties = ["server.event-processing.enable=false"])
@MustBeDocumented
annotation class DisableEventProcessing
