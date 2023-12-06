plugins {
	id("java-library")
	id("org.jetbrains.kotlin.jvm")
	id("maven-publish")
}

group = "dev.frozenmilk.fossil"

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}