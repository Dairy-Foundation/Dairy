//apply(from = "kotlin-android")

plugins {
	id("com.android.library")
	id("kotlin-android")
}

android {
	namespace = "dev.frozenmilk.dairy.ftclink"
	compileSdk = 29

	defaultConfig {
		minSdk = 24
		//noinspection ExpiredTargetSdkVersion
		targetSdk = 28

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		consumerProguardFiles("consumer-rules.pro")
	}

	compileSdk = 29

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
}

dependencies {
	implementation("androidx.appcompat:appcompat:1.2.0")
//	testImplementation("junit:junit:4.13.2")
	api(project(":Core"))

	compileOnly("org.firstinspires.ftc:RobotCore:9.0.1")
	compileOnly("org.firstinspires.ftc:Hardware:9.0.1")
	compileOnly("org.firstinspires.ftc:FtcCommon:9.0.1")
}