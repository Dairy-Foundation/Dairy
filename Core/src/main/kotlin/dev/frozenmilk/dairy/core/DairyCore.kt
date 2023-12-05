package dev.frozenmilk.dairy.core

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

	/**
	 * enables only DataCarton from DairyCore
	 */
	@Retention(AnnotationRetention.RUNTIME)
	@Target(AnnotationTarget.CLASS)
	annotation class DataCarton
}
