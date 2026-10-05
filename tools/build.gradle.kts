plugins {
    id("application")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lib"))
    implementation("org.processing:core:4.5.6")
}

listOf("VisualizeWFC2D", "VisualizeTensorField2d").forEach { toolName ->
    tasks.register<JavaExec>(toolName) {
        description = "Run the $toolName executable."
        group = "tools"
        classpath = sourceSets.main.get().runtimeClasspath
        mainClass = toolName
    }
}
