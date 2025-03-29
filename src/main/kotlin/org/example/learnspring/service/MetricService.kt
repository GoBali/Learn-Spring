package org.example.learnspring.service

import io.micrometer.core.instrument.simple.SimpleMeterRegistry

class MetricService {
    private val meterRegistry = SimpleMeterRegistry()

    fun process() {
        meterRegistry.counter("my_custom_metric").increment()
        println("Metric incremented!")
    }
}