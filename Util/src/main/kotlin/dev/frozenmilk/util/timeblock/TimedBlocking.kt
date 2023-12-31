package dev.frozenmilk.util.timeblock

class TimedBlocking(duration: Double, toRun: Runnable = Runnable {} ) {
	init {
		val durationNano = duration * 1E9
		val startTime = System.nanoTime()
		while (System.nanoTime() - startTime < durationNano) { toRun.run() }
	}
}