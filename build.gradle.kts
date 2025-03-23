plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.4.3"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.netflix.dgs.codegen") version "7.0.3"
	id("org.graalvm.buildtools.native") version "0.10.5"
}

group = "org.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

val springModulithVersion: String by project.extra("1.3.3")

repositories {
	mavenCentral()
}

dependencies {
	// 기본 의존성 업데이트
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation(platform("org.springframework.modulith:spring-modulith-bom:$springModulithVersion"))
	implementation("org.springframework.modulith:spring-modulith-starter-core")

	// Spring Web 관련 의존성
	implementation("org.springframework.boot:spring-boot-starter-data-rest")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	// OpenAPI 문서화: 최신 호환 버전
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.7.0")

	// 기타 의존성 유지
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// 보안 및 암호화 업데이트
	implementation("org.jasypt:jasypt:1.9.3")
	implementation("com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.5") // 최신 안정 버전
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

	// 메트릭/모니터링
	implementation("io.micrometer:micrometer-core")
	implementation("io.micrometer:micrometer-registry-prometheus")
	implementation("io.github.microutils:kotlin-logging:1.12.5")

	// Thymeleaf 의존성
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6") // Spring Security와 통합

	// 런타임 및 DB
	runtimeOnly("com.h2database:h2")
	runtimeOnly("org.postgresql:postgresql")

	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.projectlombok:lombok")

	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")

	compileOnly("org.projectlombok:lombok")

	// 테스트 의존성
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

kotlin {
	jvmToolchain(21)
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks {
	generateJava {
		schemaPaths.add("${projectDir}/src/main/resources/graphql-client")
		packageName = "org.example.learnspring.codegen"
		generateClient = true
	}

	test {
		useJUnitPlatform()
	}
}