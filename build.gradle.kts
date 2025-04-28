import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.ByteArrayOutputStream

plugins {
    java
    id("com.gradleup.shadow") version "9.0.0-beta12"
}

group = "cz.coffee"
version = "4.0.5"
description = "SkJson"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.skriptlang.org/releases")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repository.apache.org/content/repositories/snapshots/")
    maven("https://repo.bytecode.space/repository/maven-public/")
}

dependencies {
    compileOnly("org.eclipse.jetty:jetty-client:12.1.0.alpha2")
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
    compileOnly("com.github.SkriptLang:Skript:2.7.3")
    compileOnly("com.google.code.gson:gson:2.10.1")
    compileOnly("org.glassfish.jersey.core:jersey-server:2.34")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    compileOnly(files("tools/SkBee-3.5.4.jar"))
}

tasks.processResources {
    filteringCharset = "UTF-8"

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from("src/main/resources") {
        include("plugin.yml", "config.yml", "lang/default.lang")
    }

    filesMatching(listOf("plugin.yml", "lang/default.lang")) {
        expand(mapOf(
            "version" to project.version
        ))
    }
}

tasks.withType<ShadowJar>().configureEach  {
    minimize()

    archiveClassifier.set("shaded")
    archiveFileName.set("skjson.jar")

    relocate("com.github.skbee", "cz.coffee.api.nbt")
    relocate("org.bstats", "cz.coffee.api.bstats")

    exclude("META-INF/*.MF", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF")

    exclude("LICENSE")
    exclude("META-INF/maven/de.tr7zw/functional-annotations/**")

    from(zipTree("tools/SkBee-3.5.4.jar")) {
        exclude("*.yml", "*.properties", "assets")
    }

    doLast {
        println("> Task :copy to path")
        copy {
            from(archiveFile.get().asFile)
            into("\\\\wsl.localhost\\Ubuntu\\home\\coffee\\mc-developing\\plugins")
        }

        val outputStream = ByteArrayOutputStream()

        @Suppress("DEPRECATION")
        exec {
            executable = "curl"
            args = listOf(
                "-X", "POST",
                "http://localhost:8291",
                "-H", "Content-Type: application/json",
                "-d", """{\"cmd\": [\"reload confirm\"]}"""
            )
            standardOutput = outputStream
            errorOutput = System.err
            isIgnoreExitValue = true
        }

        println("> Response: $outputStream")
    }
}