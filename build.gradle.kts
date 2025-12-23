plugins {
    id("java")
    id("com.gradleup.shadow") version("8.3.2")
    id("io.github.revxrsal.zapper") version("1.0.2")
    id("io.freefair.lombok") version("8.11")
}

group = "com.mongenscave"
version = "1.0.0"

repositories {
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

    zap("com.github.Anon8281:UniversalScheduler:0.1.6")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

zapper {
    libsFolder = "libs"
    relocationPrefix = "com.mongenscave.mcbox"

    repositories { includeProjectRepositories() }

    relocate("com.github.Anon8281.universalScheduler", "universalScheduler")
}