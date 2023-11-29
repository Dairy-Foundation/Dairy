package dev.frozenmilk.dairy.ftclink.apputil

/**
 * <p>Objects which implement this can run actions against a wide range of hooks into all OpModes</p>
 * <p>Instances can be registered against the EventRegistrar using .registerListener(this), and should do so on instantiation</p>
 * <p>Instances can be deregistered against the EventRegistrar using .deregisterListener(this), should they wish to do so of their own violation</p>
 */
interface Listener {
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