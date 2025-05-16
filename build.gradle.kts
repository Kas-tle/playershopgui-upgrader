plugins {
    application
    alias(libs.plugins.shadow)
    alias(libs.plugins.fabric.loom)
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://maven.fabricmc.net/") }
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    implementation(libs.guava)
    implementation(libs.sqlite.jdbc)
    implementation(libs.gson)
    modImplementation(libs.fabric.loader)
    modImplementation(libs.data.converter)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks {
    val shadowJar by existing(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
        archiveClassifier.set("")
        archiveFileName.set("PlayerShopGUIUpgrader.jar")
        isZip64 = true
    }

    build {
        dependsOn(shadowJar)
    }
}

application {
    mainClass = "dev.kastle.PlayerShopGUIUpgrader"
}
