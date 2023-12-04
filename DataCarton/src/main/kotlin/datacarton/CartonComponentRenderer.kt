package datacarton

import datacarton.CartonComponent.TraceComponentBuilder
import datacarton.annotations.PublicationProcessor
import java.lang.reflect.InvocationTargetException

class CartonComponentRenderer private constructor(
		private val title: String,
		private val cartonComponents: Array<CartonComponent?>,
		private val renderOrder: RenderOrder,
) {
	override fun toString(): String {
		val builder = StringBuilder(title).append("\n")
		for (trace in cartonComponents) {
			if (trace == null) continue
			builder.append(trace)
		}
		return builder.toString()
	}

	fun add(traceClass: Class<out CartonComponent?>, dataLine: DataLine?) {
		val entry = renderOrder.getEntry(traceClass)
				?: throw RuntimeException("Target trace class was not in the render order map")
		cartonComponents[entry.component1()]!!.add(dataLine)
	}

	class Builder(private val renderOrder: RenderOrder) {
		private val traceComponentBuilders: Array<TraceComponentBuilder?> = arrayOfNulls(renderOrder.orderMapping.size)

		init {
			for ((_, value) in renderOrder.orderMapping) {
				try {
					val builder = value.second.componentBuilder
					traceComponentBuilders[value.component1()] = builder.getConstructor().newInstance()
				} catch (e: IllegalAccessException) {
					throw RuntimeException(e)
				} catch (e: InstantiationException) {
					throw RuntimeException(e)
				} catch (e: InvocationTargetException) {
					throw RuntimeException(e)
				} catch (e: NoSuchMethodException) {
					throw RuntimeException(e)
				}
			}
		}

		fun add(cartonComponentClass: Class<out CartonComponent>, dataLine: DataLine?): Builder {
			val entry = renderOrder.getEntry(cartonComponentClass)
					?: throw RuntimeException("Target trace class was not in the render order map")
			traceComponentBuilders[entry.component1()]!!.add(dataLine)
			return this
		}

		fun add(cartonComponentClass: Class<out CartonComponent>): Builder {
			val entry = renderOrder.getEntry(cartonComponentClass)
					?: throw RuntimeException("Target trace class was not in the render order map")
			traceComponentBuilders[entry.component1()]!!.add()
			return this
		}

		fun build(title: String): CartonComponentRenderer {
			val cartonComponents = arrayOfNulls<CartonComponent>(traceComponentBuilders.size)
			var i = 0
			val it: Iterator<Map.Entry<Class<out CartonComponent?>, Pair<Int, RenderOrder.Render<*>>>> =
					renderOrder.orderMapping.entries.iterator()
			while (it.hasNext()) {
				val (_, value) = it.next()
				val builder = traceComponentBuilders[value.component1()]
				cartonComponents[i] = builder!!.build(value.component2().settings)
				i++
			}
			return CartonComponentRenderer(title, cartonComponents, renderOrder)
		}
	}
}
