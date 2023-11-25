package collections

import java.util.*
import kotlin.math.abs
import kotlin.math.max

@Suppress("unused", "UNCHECKED_CAST")
class LimitedQueue<T>(capacity: Int) : AbstractQueue<T?>() {
	private val array: Array<T?>
	private var currentLen: Int
	private var startIndex = 0

	init {
		currentLen = startIndex
		array = arrayOfNulls<Any>(capacity) as Array<T?>
	}

	override fun iterator(): MutableIterator<T?> {
		return LimitedQueueIterator(this)
	}

	fun reverseIterator(): MutableIterator<T?> {
		return ReverseLimitedQueueIterator(this)
	}

	private val endIndex: Int
		get() = (startIndex + currentLen) % array.size

	override val size: Int
		get() = currentLen;

	override fun offer(t: T?): Boolean {
		if (t == null) return false
		array[endIndex] = t
		currentLen++
		if (currentLen > array.size) {
			currentLen = array.size
			startIndex++
			startIndex %= array.size
		}
		return true
	}

	override fun poll(): T? {
		val result = array[startIndex]
		array[startIndex] = null
		currentLen--
		currentLen = max(0.0, currentLen.toDouble()).toInt()
		startIndex++
		startIndex %= array.size
		return result
	}

	override fun peek(): T? {
		return array[startIndex]
	}

	class LimitedQueueIterator<T>(private val queue: LimitedQueue<T>) : MutableIterator<T> {
		private val startIndex: Int = queue.startIndex
		private var index = 0

		override fun hasNext(): Boolean {
			return index < queue.size
		}

		override fun next(): T {
			return queue.array[(startIndex + index++) % queue.size]!!
		}

		override fun remove() {
			val i = (startIndex + --index) % queue.size
			queue.remove(queue.array[i])
			queue.currentLen--
		}
	}

	class ReverseLimitedQueueIterator<T>(private val queue: LimitedQueue<T>) : MutableIterator<T> {
		private val startIndex: Int
		private var index: Int

		init {
			var startIndex = queue.endIndex - 1
			if (startIndex < 0) startIndex += queue.size
			this.startIndex = startIndex
			index = 0
		}

		override fun hasNext(): Boolean {
			return abs(index.toDouble()) < queue.size
		}

		override fun next(): T {
			var i = (startIndex + index--) % queue.size
			if (i < 0) i += queue.size
			return queue.array[i]!!
		}

		override fun remove() {
			var i = (startIndex + index + 1) % queue.size
			if (i < 0) i += queue.size
			queue.array[i] = null
			queue.currentLen--
		}
	}
}