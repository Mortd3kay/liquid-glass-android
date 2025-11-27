plugins {
    alias(libs.plugins.kmp)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.vanniktech.publishing)
}

kotlin {
    // will be added more in next PRs
    android {
        namespace = "com.mrtdk.glass"
        compileSdk = 36
        minSdk = 21
    }
    sourceSets {
        commonMain {
            dependencies {
                // will be
            }
        }
        androidMain {
            dependencies {
                implementation(libs.androidx.material3)
                implementation(project.dependencies.platform(libs.androidx.compose.bom))
                implementation(libs.androidx.ui)
                implementation(libs.androidx.ui.graphics)
                implementation(libs.androidx.ui.tooling.preview)
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("com.mrtdk", "glass", "0.1.0")

    pom {
        name.set("Glass")
        description.set("A library for creating glass morphism effects in Jetpack Compose with support for Android API 24+")
        url.set("https://github.com/Mortd3kay/liquid-glass-compose")

        licenses {
            license {
                name.set("Apache License 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }

        developers {
            developer {
                id.set("mrtdk")
                name.set("MRTDK")
                email.set("undergroundcome@gmail.com")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/Mortd3kay/liquid-glass-compose.git")
            developerConnection.set("scm:git:ssh://github.com/Mortd3kay/liquid-glass-compose.git")
            url.set("https://github.com/Mortd3kay/liquid-glass-compose")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("ossrhUsername") as String?
                password = project.findProperty("ossrhPassword") as String?
            }
        }
    }
}