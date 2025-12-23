plugins {
    id("java")
    id("com.gradleup.shadow") version("8.3.2")
    id("io.freefair.lombok") version("8.11")
    id("maven-publish")
}

group = "com.mongenscave"
version = "1.0.2"

repositories {
    maven {
        name = "MonGens-Cave"
        url = uri("https://repo.mongenscave.com/")
        credentials {
            username = project.findProperty("mongensUsername") as String
            password = project.findProperty("mongensPassword") as String
        }
    }


    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://jitpack.io")
    maven("https://repo.nexomc.com/releases")
    maven("https://repo.oraxen.com/releases")
    maven("https://maven.devs.beer/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.36")
    compileOnly("com.nexomc:nexo:1.15.0")
    compileOnly("io.th0rgal:oraxen:1.200.0")
    compileOnly("dev.lone:api-itemsadder:4.0.10")

    implementation("dev.dejvokep:boosted-yaml:1.3.6")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.javadoc {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

val apiJar = tasks.register<Jar>("apiJar") {
    archiveBaseName.set("mc-MenuAPI")
    archiveClassifier.set("")
    archiveVersion.set(project.version.toString())

    from(sourceSets.main.get().output) {
        include("com/mongenscave/mcmenuapi/**")
    }
}

publishing {
    publications {
        create<MavenPublication>("apiJar") {
            artifact(apiJar.get()) {
                classifier = null
            }

            groupId = "com.mongenscave"
            artifactId = "mc-MenuAPI"
            version = project.version.toString()
        }
    }

    repositories {
        maven {
            name = "MonGens-Cave"
            url = uri("https://repo.mongenscave.com/releases")
            credentials {
                username = project.findProperty("mongensUsername") as String
                password = project.findProperty("mongensPassword") as String
            }
        }
    }
}

tasks.register("deployApi") {
    dependsOn("apiJar", "publishApiJarPublicationToMonGens-CaveRepository")
}