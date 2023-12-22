package datacarton.processors

import datacarton.CartonComponentRenderer
import java.util.function.Consumer

interface PublicationProcessor : Consumer<CartonComponentRenderer> {
	/**
	 * gets run before the processor gets given all the Renderers and updated
	 */
	fun initPublication()

	/**
	 * gets run once it holds all the renderers, to release the built result
	 */
	fun updatePublication()

	/**
	 * returns true if its not worth doing the update this cycle, for instance, for telemetry based processors
	 */
	fun ignoreUpdate(): Boolean

	/**
	 * gets run for each output Renderer
	 */
	override fun accept(p0: CartonComponentRenderer)
}
