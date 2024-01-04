import com.nishtahir.CargoBuildTask
import com.nishtahir.CargoExtension

plugins {
	id("com.android.library")
	id("kotlin-android")
	id("org.jetbrains.dokka") version "1.9.10"
	id("maven-publish")

	id("org.mozilla.rust-android-gradle.rust-android") version "0.9.3"
}

android {
	namespace = "dev.frozenmilk.dairy.milkman"
	compileSdk = 29

	defaultConfig {
		minSdk = 24
		//noinspection ExpiredTargetSdkVersion
		targetSdk = 28

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		consumerProguardFiles("consumer-rules.pro")
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
	}
	ndkVersion = "26.1.10909125"
}

extensions.configure(CargoExtension::class) {
	module = "./rust"
	libname = "milkman"
	targets = listOf(
			"arm",
			"arm64",
			"darwin-aarch64" // added for debugging on macOS
	)
	pythonCommand = "python3"
}

tasks.preBuild.configure {
	dependsOn.add(tasks.withType(CargoBuildTask::class.java))
}

dependencies {
	//noinspection GradleDependency
	implementation("androidx.appcompat:appcompat:1.2.0")
	testImplementation("org.testng:testng:6.9.6")

	compileOnly(project(":Core"))
	testImplementation(project(":Core"))

	compileOnly("org.firstinspires.ftc:RobotCore:9.0.1")
	compileOnly("org.firstinspires.ftc:Hardware:9.0.1")
	compileOnly("org.firstinspires.ftc:FtcCommon:9.0.1")

	implementation("org.nanohttpd:nanohttpd-websocket:2.3.1") {
		exclude(module= "nanohttpd")
	}
}

project.afterEvaluate {
	tasks.withType(CargoBuildTask::class)
			.forEach { buildTask ->
				tasks.withType(com.android.build.gradle.tasks.MergeSourceSetFolders::class)
						.configureEach {
							this.inputs.dir(
									layout.buildDirectory.dir("rustJniLibs" + File.separatorChar + buildTask.toolchain!!.folder)
							)
							this.dependsOn(buildTask)
						}
			}
}

publishing {
	publications {
		register<MavenPublication>("release") {
			groupId = "dev.frozenmilk.dairy"
			artifactId = "MilkMan"
			version = "v0.0.0"

			afterEvaluate {
				from(components["release"])
			}
		}
	}
	repositories {
		maven {
			name = "MilkMan"
			url = uri("${project.buildDir}/release")
		}
	}
}
