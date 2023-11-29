//apply(plugin = "kotlin")
//apply(from = "../build.common.gradle")
//apply(from = "../build.dependencies.gradle")
plugins {
	id("org.jetbrains.kotlin.jvm")
	id("org.jetbrains.dokka") version "1.9.10"

	`java-library`
}

kotlin {
	jvmToolchain(17)
}

repositories {
	mavenCentral()
}

dependencies {
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

	testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.3")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
	useJUnitPlatform()
}