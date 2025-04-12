import java.security.MessageDigest
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id 'java'
    id 'com.github.johnrengelman.shadow' version '8.1.1'
    id 'xyz.jpenilla.run-paper' version '2.3.1'
    id 'org.hidetake.ssh' version '2.11.2'
}

group = 'cz.coffee'
version = '4.5'

def environment = project.hasProperty('env') ? project.property('env') : 'DEV'
println "Using environment: $environment"

jar {
    manifest {
        attributes(
                'Implementation-Title': 'SkJson',
                'Implementation-Version': version
        )
    }
}

static def generateSHA1() {
    def time = System.currentTimeMillis().toString()
    def md = MessageDigest.getInstance("SHA-1")
    def digest = md.digest(time.bytes)
    return new BigInteger(1, digest).toString(16).padLeft(40, '0')
}

static def generateShortSHA1(sha1) {
    return sha1.substring(0, 8)
}

repositories {
    mavenCentral()
    maven { url "https://jitpack.io" }
    maven { url "https://repo.skriptlang.org/releases" }
    maven { url "https://oss.sonatype.org/content/groups/public/" }
    maven { url "https://repo.papermc.io/repository/maven-public/" }
    maven { url "https://repo.purpurmc.org/snapshots" }
    maven { url "https://repo.codemc.io/repository/maven-public/" }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation 'org.eclipse.jetty:jetty-client:12.1.0.alpha0'
    compileOnly 'io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT'
    compileOnly 'com.github.SkriptLang:Skript:2.9.5'
    implementation 'com.google.code.gson:gson:2.10.1'
    compileOnly 'org.glassfish.jersey.core:jersey-server:2.34'
    implementation 'org.bstats:bstats-bukkit:3.1.0'
    implementation 'de.tr7zw:item-nbt-api:2.14.1'
    implementation 'org.yaml:snakeyaml:2.3'

    compileOnly 'org.projectlombok:lombok:1.18.34'
    annotationProcessor 'org.projectlombok:lombok:1.18.34'
    testCompileOnly 'org.projectlombok:lombok:1.18.34'
    testAnnotationProcessor 'org.projectlombok:lombok:1.18.34'
}

sourceSets {
    main {
        resources {
            srcDir 'src/main/resources'
            include 'plugin.yml', 'config.yml', 'lang/default.lang', 'tests/**'
        }
    }
}

tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
}

def fullRev = generateSHA1()
def shortRev = generateShortSHA1(fullRev)

tasks.processResources {
    filesMatching(["plugin.yml", "lang/default.lang"]) {
        expand("version": project.version, "rev": shortRev)
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType(ShadowJar).configureEach {
    archiveClassifier.set('shaded')
    archiveFileName.set('skjson.jar')

    relocate 'org.bstats', 'cz.coffee.shadowed.bstats'
    relocate 'de.tr7zw.changeme.nbtapi', 'cz.coffee.shadowed.nbtapi'

    exclude 'META-INF/*.MF', 'META-INF/*.SF', 'META-INF/*.DSA', 'META-INF/*.RSA'

    if (environment == "DEV") {
        doLast {
            println "> Task :copy to path"
            copy {
                from archiveFile.get().asFile
                into '/home/coffee/mc/plugins'
            }
        }
    }
}

remotes {
    coffee {
        role 'masterNode'
        host = 'coffeerequired.info'
        user = 'coffee'
        password = 'coffee'
    }
}

tasks.register("deployToServer") {
    doLast {
        println("> Uploading skjson.jar to remote server via SSH")

        ssh.run {
            session(remotes.coffee) {
                put from: file("${buildDir}/libs/skjson.jar"), into: "/home/coffee/mc/plugins/skjson.jar"
            }
        }
        println("> Plugin uploaded & server restarted!")
    }
}

tasks.register('withRemote') {
    dependsOn 'clean'
    dependsOn 'shadowJar'
    finalizedBy 'runRemote'
}

tasks.register('wDeploy') {
    dependsOn 'clean'
    dependsOn 'shadowJar'
    finalizedBy 'deployToServer'
}

static def callAPI(String cmd) {
    def command = "curl -s http://localhost:8080/command?cmd=" + cmd
    println "Executing: $command"
    def process = command.execute()
    def output = new StringBuffer()
    process.consumeProcessOutput(output, new StringBuffer())
    process.waitFor()

    if (process.exitValue() == 0) {
        println "Command executed successfully:\n$output"
    } else {
        println "Command failed with exit code ${process.exitValue()}:\n$output"
    }
}

tasks.register("runRemote") {
    doLast {
        callAPI("reload+confirm")
        // sleep(10000)
        // callAPI("gendocs")
    }
}