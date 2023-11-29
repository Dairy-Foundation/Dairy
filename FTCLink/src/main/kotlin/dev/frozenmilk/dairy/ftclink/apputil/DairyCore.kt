package dev.frozenmilk.dairy.ftclink.apputil

/**
 * enables all features of DairyCore
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class DairyCore {
	/**
	 * enables only calcification from DairyCore
	 */
	@Retention(AnnotationRetention.RUNTIME)
	@Target(AnnotationTarget.CLASS)
	annotation class Calcify
}
