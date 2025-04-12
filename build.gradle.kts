import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.security.MessageDigest

plugins {
    java
    id("com.gradleup.shadow") version "9.0.0-beta12"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "cz.coffee"
version = "5.0"

val environment: String by project.extra { if (project.hasProperty("env")) project.property("env") as String else "DEV" }
println("Using environment: $environment")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.skriptlang.org/releases")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.purpurmc.org/snapshots")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven { url = uri("https://jitpack.io") }
}

dependencies {

    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.github.SkriptLang:Skript:2.11.0-pre1")
    implementation("com.google.code.gson:gson:2.13.0")
    implementation("com.google.guava:guava:32.1.3-jre")


    implementation("org.bstats:bstats-bukkit:3.1.0")
    implementation("de.tr7zw:item-nbt-api:2.14.1")


    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    testCompileOnly("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")

    components {
        withModule("com.github.SkriptLang:Skript") {
            allVariants {
                withDependencies {}
            }
        }
    }
}

sourceSets {
    named("main") {
        resources {
            srcDir("src/main/resources")
            include("plugin.yml", "config.yml", "lang/default.lang", "tests/**")
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    // options.compilerArgs.add("-Xlint:deprecation")
}

fun generateSHA1(): String {
    val time = System.currentTimeMillis().toString()
    val md = MessageDigest.getInstance("SHA-1")
    val digest = md.digest(time.toByteArray())
    return BigInteger(1, digest).toString(16).padStart(40, '0')
}

fun generateShortSHA1(sha1: String): String = sha1.substring(0, 8)

val fullRev = generateSHA1()
val shortRev = generateShortSHA1(fullRev)

tasks.processResources {
    filesMatching(listOf("plugin.yml", "lang/default.lang")) {
        expand(mapOf("version" to project.version, "rev" to shortRev))
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<ShadowJar>().configureEach {
    archiveClassifier.set("shaded")
    archiveFileName.set("skjson.jar")

    relocate("org.bstats", "cz.coffee.shadowed.bstats")
    relocate("de.tr7zw.changeme.nbtapi", "cz.coffee.shadowed.nbtapi")
    relocate("com.google.gson", "cz.coffee.shadowed.gson")

    exclude("META-INF/*.MF", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

    if (environment == "DEV") {
        doLast {
            println("> Task :copy to path")
            copy {
                from(archiveFile.get().asFile)
                into("\\\\wsl.localhost\\Ubuntu\\home\\coffee\\mc-developing\\plugins")
            }
        }
    }
}

tasks.register("withRemote") {
    dependsOn("clean")
    finalizedBy("shadowJar")
}

tasks.register("withTesting") {
    dependsOn("clean")
    dependsOn("shadowJar")
    doLast {
        println("> Task :running tests")
        exec {
            workingDir = projectDir
            commandLine("python", "test_runner.py", "--configuration=1", "--jdk=auto", "--system=auto", "--no-interactive")
        }
    }
}