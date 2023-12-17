# Dairy
[![](https://www.jitpack.io/v/Dairy-Foundation/Dairy.svg)](https://www.jitpack.io/#Dairy-Foundation/Dairy)

This repository contains a multi-module project that contains both core functionality of the Dairy project, and sub-libraries built on the project

This repository also contains a copy of the FTC Robot Controller project, to enable fast development, and a single repository combination of source code and implementation samples

Each section below will go over the appropriate installation instructions

# Core
A framework library for writing libraries for the FTC Robot Controller

## Provides:
- hooks into the workings of OpModes
- a small number of utilities for working with OpModes
- powerful dependency resolution features that allow for libraries to depend on each other, and correctly attach and mount to OpModes
- support for both OpModes and LinearOpModes (OpModes are preferred)

## Installation:
all Dairy libraries will also require you to have core installed at a minimum

1. add `maven { url 'https://www.jitpack.io' }` to the bottom of your `repositories` block in your `build.dependencies.gradle`
2. add `implementation 'com.github.Dairy-Foundation.Dairy:Core:<version tag>'` to the bottom of your `dependencies` block in your `build.dependencies.gradle`
3. run a gradle sync

## Usage:
a guide to using Dairy libraries in your OpModes can be found at [Kotlin](https://github.com/Dairy-Foundation/Dairy/tree/master/TeamCode/src/main/kotlin/org/firstinspires/ftc/teamcode/examples) or [Java](https://github.com/Dairy-Foundation/Dairy/tree/master/TeamCode/src/main/java/org/firstinspires/ftc/teamcode/examples)

a guide to writing a library that works with DairyCore can be found at [Kotlin](https://github.com/Dairy-Foundation/Dairy/blob/master/TeamCode/src/main/kotlin/org/firstinspires/ftc/teamcode/examples/featuredev/KotlinWritingAFeature.kt) or [Java](https://github.com/Dairy-Foundation/Dairy/blob/master/TeamCode/src/main/java/org/firstinspires/ftc/teamcode/examples/featuredev/JavaWritingAFeature.java)

# Calcified
A library that provides custom drivers for FTC hardware, and improves developer ergonomics around interacting with the hardwareMap

## Provides:
- port-based access to hardware objects
- ignores the config file
- separates encoders from motors
- powerful auto-updating motor controller frameworks
- improved gamepad objects
- nicer angle classes

## Installation:
with Core installed:

1. add `implementation 'com.github.Dairy-Foundation.Dairy:Calcified:<version tag>'` to the bottom of your `dependencies` block in your `build.dependencies.gradle`
2. run a gradle sync

## Usage:
a guide to using Calcified in your OpModes can be found at [Kotlin](https://github.com/Dairy-Foundation/Dairy/blob/master/TeamCode/src/main/kotlin/org/firstinspires/ftc/teamcode/examples/calcified/KotlinOverview.kt) or [Java](https://github.com/Dairy-Foundation/Dairy/blob/master/TeamCode/src/main/java/org/firstinspires/ftc/teamcode/examples/calcified/JavaOverview.java)

# Feedback
Dairy and Calcified are currently in open beta, and we would love to get feedback, especially if you run into any issues with the drivers,
GitHub Issues should be set up shortly to provide a structured way of doing this, but for the moment, we can be found on the Unofficial FTC Discord Server,
Or @Froze-n-Milk can be emailed via [oscar.chevalier@pymblelc.nsw.edu.au](mailto:oscar.chevalier@pymblelc.nsw.edu.au)
