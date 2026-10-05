plugins {
    id("java")
    id("maven-publish")
    id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT"
    id("com.diffplug.spotless") version "8.10.1" apply false
}

allprojects {
    group = "org.markscitygen"
    version = "1.0.0"

    apply(plugin = "com.diffplug.spotless")
    plugins.withId("com.diffplug.spotless") {
        configure<com.diffplug.gradle.spotless.SpotlessExtension> {
            java {
                googleJavaFormat().aosp() // Android Open Source Project style
            }
        }
    }
}


dependencies {
    implementation(project(":lib"))

    minecraft("com.mojang:minecraft:26.2")

    implementation("net.fabricmc:fabric-loader:0.19.3")
    implementation("net.fabricmc.fabric-api:fabric-api:0.156.0+26.2")
}

// Fabric mod configuration
loom {
    splitEnvironmentSourceSets()

    mods {
        register("marks-citygen") {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets.getByName("client"))
        }
    }
}

tasks.processResources {
    val version = version
    inputs.property("version", version)

    filesMatching("fabric.mod.json") {
        expand("version" to version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.jar {
    val projectName = project.name
    inputs.property("projectName", projectName)

    from("LICENSE.txt") {
        rename { "LICENSE-$projectName.txt" }
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
    }
}
