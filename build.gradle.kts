import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.regex.Pattern

plugins {
    java
    id("com.gradleup.shadow") version "9.4.3"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "cz.coffee"
version = "6.0.0"

// --- dependency versions (Skript 2.15.x / Paper 26.2.x, July 2026) ---
val paperApiVersion = "26.2.build.47-alpha"
val skriptVersion = "2.15.4"
val gsonVersion = "2.14.0"
val bstatsVersion = "3.2.1"
val nbtApiVersion = "2.15.7"
val lombokVersion = "1.18.46"

val environment: String = if (project.hasProperty("env")) project.property("env") as String else "DEV"
val devDeployPath: String = (findProperty("devDeployPath") as String?) ?: "\\custom\\mc-developing"

println("Using environment: $environment")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
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
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
    compileOnly("com.github.SkriptLang:Skript:$skriptVersion")

    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("org.bstats:bstats-bukkit:$bstatsVersion")
    implementation("de.tr7zw:item-nbt-api:$nbtApiVersion")

    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")

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
            include("plugin.yml", "config.yml", "lang/default.lang", "libraries/**")
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:deprecation")
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks {
    runServer {
        minecraftVersion("26.2")
        downloadPlugins {
            url("https://github.com/SkriptLang/Skript/releases/download/$skriptVersion/Skript-$skriptVersion.jar")
        }
    }
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
    filesMatching(listOf("plugin.yml", "lang/default.lang", "libraries/configuration.properties")) {
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
            println("> Task :copy to path ($devDeployPath)")
            copy {
                from(archiveFile.get().asFile)
                into(devDeployPath)
            }
        }
    }
}

tasks.register("withRemote") {
    dependsOn("clean", "shadowJar")
    doLast {
        println("> Task :execute change (remote reload disabled)")
        @Suppress("UNUSED_VARIABLE")
        val outputStream = ByteArrayOutputStream()
        // curl reload can be re-enabled when a local test server endpoint is available
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
    dependsOn("clean", "shadowJar")
    doLast {
        println("> Task :running tests")

        val process = ProcessBuilder(
            "C:\\Users\\Coffee\\AppData\\Local\\Microsoft\\WindowsApps\\python3.exe",
            "test_runner.py",
            "--configuration=1",
            "--jdk=auto",
            "--system=auto",
            "--no-interactive"
        )
            .directory(projectDir)
            .inheritIO()
            .start()
        process.waitFor()
    }
}
