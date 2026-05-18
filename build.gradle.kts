plugins {
    kotlin("jvm") version "2.1.0" apply false
    kotlin("plugin.spring") version "2.1.0" apply false
    id("org.springframework.boot") version "3.4.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
}

subprojects {
    group = "dev.portfolio.warehouse"
    version = "0.1.0"
}

