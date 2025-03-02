package org.example.learnspring.controller

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/example")
class ExampleController {
    @Operation(summary = "Example Endpoind", description = "This")
    fun getExample(): ResponseEntity<String> {
        return ResponseEntity.ok("Hello, World!")
    }
}