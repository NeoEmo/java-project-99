plugins {
	java
	id("org.springframework.boot") version "4.1.2"
	id("io.spring.dependency-management") version "1.1.7"
	id("checkstyle")
	id("io.sentry.jvm.gradle") version "6.27.0"
	id("org.sonarqube") version "7.6.0.18749"
	jacoco
}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-web")


	developmentOnly("org.springframework.boot:spring-boot-devtools")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")

	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.2.1")

	implementation("io.sentry:sentry-spring-boot-4:8.65.0")
	implementation("io.sentry:sentry-async-profiler:8.65.0")

	implementation("io.sentry:sentry-async-profiler")

	runtimeOnly("com.h2database:h2")
	compileOnly("com.h2database:h2")
	runtimeOnly("org.postgresql:postgresql")

	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testCompileOnly("org.projectlombok:lombok")
	testAnnotationProcessor("org.projectlombok:lombok")

	// Source: https://mvnrepository.com/artifact/org.mapstruct/mapstruct
	implementation("org.mapstruct:mapstruct:1.6.3")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
	testAnnotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

	// Source: https://mvnrepository.com/artifact/org.openapitools/jackson-databind-nullable
	implementation("org.openapitools:jackson-databind-nullable:0.2.13")

	// Source: https://mvnrepository.com/artifact/net.javacrumbs.json-unit/json-unit-assertj
	testImplementation("net.javacrumbs.json-unit:json-unit-assertj:6.4.0")

	// Source: https://mvnrepository.com/artifact/org.instancio/instancio-junit
	testImplementation("org.instancio:instancio-junit:6.2.1")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		html.required.set(true)
	}
}

checkstyle {
	toolVersion = "10.12.4"
}

sonar {
	properties {
		property("sonar.projectKey", "NeoEmo_java-project-99")
		property("sonar.organization", "neoemo")
		property("sonar.host.url", "https://sonarcloud.io")
		property("sonar.coverage.jacoco.xmlReportPaths", "${layout.buildDirectory.get()}/reports/jacoco/test/jacocoTestReport.xml")
	}
}
