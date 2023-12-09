//package org.firstinspires.ftc.teamcode
//
//import collections.annotatedtargets.GroupedData
//import com.qualcomm.robotcore.eventloop.opmode.OpMode
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp
//import datacarton.DataCarton
//import dev.frozenmilk.dairy.calcified.Calcified
//import dev.frozenmilk.dairy.core.DairyCore
//import dev.frozenmilk.dairy.core.FeatureRegistrar
//
//@DairyCore
//@TeleOp
//class DataCartonTest : OpMode(), GroupedData {
//	init {
//		FeatureRegistrar.checkFeatures(DataCarton, Calcified)
//	}
//	override fun init() {
//		DataCarton.initFromTelemetry(telemetry)
//		DataCarton.mapSettings {
//
//		}
//		DataCarton.packageData(this)
//	}
//
//	override fun loop() {
//	}
//}