@file:Suppress("MayBeConstant")

fun DependencyHandler.impl(vararg dep: Any) = dep.forEach { implementation(it) }
fun DependencyHandler.testImpl(vararg dep: Any) = dep.forEach { testImplementation(it) }

plugins {
    java
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    application
}

configurations.all {
    resolutionStrategy {
        dependencySubstitution {
            // The maven property ${osgi.platform} is not handled by Gradle
            // so we replace the dependency, using the osgi platform from the project settings
            val os = System.getProperty("os.name").toLowerCase()
            if (os.contains("windows")) {
                substitute(module("org.eclipse.platform:org.eclipse.swt.\${osgi.platform}")).with(
                        module("org.eclipse.platform:org.eclipse.swt.win32.win32.x86_64:3.115.0"))
            }
            else if (os.contains("linux")) {
                substitute(module("org.eclipse.platform:org.eclipse.swt.\${osgi.platform}")).with(
                        module("org.eclipse.platform:org.eclipse.swt.gtk.linux.x86_64:3.115.0"))
            }
            else if (os.contains("mac")) {
                substitute(module("org.eclipse.platform:org.eclipse.swt.\${osgi.platform}")).with(
                        module("org.eclipse.platform:org.eclipse.swt.cocoa.macosx.x86_64:3.115.0"))
            }
        }
    }
}

repositories {
    jcenter()
    mavenCentral()
    maven { setUrl("https://plugins.gradle.org/m2") }
}

defaultTasks(ApplicationPlugin.TASK_RUN_NAME)

object This {
    val artifactId = "sixty"
}

dependencies {
    impl(kotlin("stdlib"), "com.beust:jcommander:1.72", "ch.qos.logback:logback-classic:1.2.3",
            "org.eclipse.platform:org.eclipse.jface:3.21.0")
    testImpl(kotlin("test"), "org.testng:testng:7.0.0", "org.assertj:assertj-core:3.10.0")
}

application {
    mainClassName = "com.beust.app.MainKt"
}

tasks {
    withType<Test> {
        useTestNG()
    }
}
