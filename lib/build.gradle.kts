plugins {
    id("java-library")
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    // Efficient primitive collections
    implementation("it.unimi.dsi:fastutil:8.5.19")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "lib"
        }
    }
}
