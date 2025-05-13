plugins {
    application
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation(libs.guava)
    implementation(libs.data.converter)
    implementation(libs.sqlite.jdbc)
    implementation(libs.gson)
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
    }

    build {
        dependsOn(shadowJar)
    }
}

application {
    mainClass = "dev.kastle.PlayerShopGUIUpgrader"
}
