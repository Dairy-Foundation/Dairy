package dev.frozenmilk.dairy.ftclink.apputil

/**
 * Objects which implement this can run actions against a wide range of hooks into all OpModes
 *
 * Instances can be registered against the EventRegistrar using .registerListener(this), and should do so on instantiation
 *
 * Instances can be deregistered against the EventRegistrar using .deregisterListener(this), should they wish to do so of their own violation
 */
interface Feature {
	/**
	 * the feature flag that enables this for an OpMode
	 */
	val dependencyManager: FeatureFlagDependencyManager

	/**
	 * provided by OpModeManagerNotifier.Notifications
	 */
	fun preUserInitHook(opMode: OpModeWrapper)

	/**
	 * provided by OpModeWrapper
	 */
	fun postUserInitHook(opMode: OpModeWrapper)

	/**
	 * provided by OpModeWrapper
	 */
	fun preUserInitLoopHook(opMode: OpModeWrapper)

	/**
	 * provided by OpModeWrapper
	 */
	fun postUserInitLoopHook(opMode: OpModeWrapper)

	/**
	 * provided by OpModeManagerNotifier.Notifications
	 */
	fun preUserStartHook(opMode: OpModeWrapper)

	/**
	 * provided by OpModeWrapper
	 */
	fun postUserStartHook(opMode: OpModeWrapper)

	/**
	 * provided by OpModeWrapper
	 */
	fun preUserLoopHook(opMode: OpModeWrapper)

	/**
	 * provided by OpModeWrapper
	 */
	fun postUserLoopHook(opMode: OpModeWrapper)

	/**
	 * provided by OpModeWrapper
	 */
	fun preUserStopHook(opMode: OpModeWrapper)

	/**
	 * provided by OpModeManagerNotifier.Notifications
	 */
	fun postUserStopHook(opMode: OpModeWrapper)
}