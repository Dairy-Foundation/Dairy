package dev.frozenmilk.dairy.calcified

/**
 * enables only calcification from DairyCore
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Calcify(
		/**
		 * Controls if the caches are automatically handled by [Calcified] or not
		 *
		 * Set to false if you want to handle the clearing of the module caches by hand
		 *
		 * Clearing the caches should probably be done using [dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule.refreshBulkCache]
		 */
		val automatedCacheHandling: Boolean = true,
		/**
		 * Controls when [Calcified] drops its modules and this hardware objects
		 *
		 * Set to true if you want to prompt [Calcified] to drop its hardware objects by hand.
		 * This should be done using [Calcified.clearModules]
		 */
		val crossPollinate: Boolean = true
)