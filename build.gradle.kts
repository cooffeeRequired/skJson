import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.regex.Pattern

plugins {
    java
    id("com.gradleup.shadow") version "9.0.0-beta13"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "cz.coffee"
version = "5.1.2"

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
    options.compilerArgs.add("-Xlint:deprecation")
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

tasks.shadowJar {

    archiveClassifier.set("shaded")
    archiveFileName.set("skjson.jar")

    relocate("org.bstats", "cz.coffeerequired.shadowed.bstats")
    relocate("de.tr7zw.changeme.nbtapi", "cz.coffeerequired.shadowed.nbtapi")
    relocate("com.google", "cz.coffeerequired.shadowed.google")

    exclude("META-INF/*.MF", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

    if (environment == "DEV") {
        doLast {
            println("> Task :copy to path")
            copy {
                from(archiveFile.get().asFile)
                into("C:\\Users\\Coffee\\Desktop\\mc-developing\\plugins")
            }
        }
    }
}

tasks.register("withRemote") {
    dependsOn("clean")
    dependsOn("shadowJar")
    dependsOn("errorLint")
    doLast {
        println("> Task :execute change")

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

tasks.register("errorLint") {
    group = "verification"
    description = "Builds the project and lists compile errors as file:line"

    doLast {
        val process = ProcessBuilder("./gradlew.bat", "compileJava", "--console=plain")
            .redirectErrorStream(true)
            .start()

        val pattern = Pattern.compile("""([^\s]+\.java):(\d+)""")
        process.inputStream.bufferedReader().lines().forEach { line ->
            val matcher = pattern.matcher(line)
            while (matcher.find()) {
                val file = matcher.group(1)
                val lineNum = matcher.group(2)
                val path = project.projectDir.toPath().resolve(file).normalize()
                println("$path:$lineNum")
            }
        }

        process.waitFor()
    }
}

tasks.register("withTesting") {
    dependsOn("clean")
    dependsOn("shadowJar")
    doLast {
        println("> Task :running tests")

        @Suppress("DEPRECATION")
        exec {
            workingDir = projectDir
            commandLine("C:\\Users\\Coffee\\AppData\\Local\\Microsoft\\WindowsApps\\python3.exe", "test_runner.py", "--configuration=1", "--jdk=auto", "--system=auto", "--no-interactive")
        }
    }
}