plugins {
	id("java-library")
	id("org.jetbrains.kotlin.jvm")
	id("maven-publish")
}

group = "dev.frozenmilk.util"

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

publishing {
	publications {
		register<MavenPublication>("release") {
			groupId = "dev.frozenmilk.dairy"
			artifactId = "Util"
			version = "v0.0.0"

			afterEvaluate {
				from(components["kotlin"])
			}
		}
	}
	repositories {
		maven {
			name = "Util"
			url = uri("${project.buildDir}/release")
		}
	}
}
