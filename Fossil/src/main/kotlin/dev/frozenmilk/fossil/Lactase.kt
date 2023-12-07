package dev.frozenmilk.dairy.archaeological

import java.nio.CharBuffer

/**
 * deserialises
 */
class Lactase {
	fun processLine(buffer: CharBuffer): CharBuffer {
		when (buffer.get()) {
			'#' -> {
				buffer.advanceToAfterNext('\n')
			}
			'[' -> {
				processTable(buffer.captureToNext('\n'))
				buffer.advanceToAfterNext('\n')
			}
			else -> {
				processKV(buffer)
				buffer.advanceToAfterNext('\n')
			}
		}
		return buffer
	}

	fun processKV(buffer: CharBuffer): CharBuffer {
		val keyAndType = buffer.captureToNext('=')
		val key = processKey(keyAndType)
		val type = processTypeHint(keyAndType)
		return buffer
	}

	fun processTypeHint(buffer: CharBuffer): Class<Any> {

		TODO("FINISH")
	}

	fun processKey(buffer: CharBuffer): String {
		val builder = StringBuilder()
		var literal = false
		while (buffer.hasRemaining()) {
			when (buffer.get()) {
				'=', ' ', ':' -> {
					if (literal) continue
					if (builder.isBlank()) throw IllegalStateException("Empty key")
					buffer.rewind()
					return builder.toString().trim()
				}
				'\'' -> {
					if (literal && builder.isNotBlank()) {
						buffer.rewind()
						return builder.toString()
					}
					if (!literal && builder.isBlank()) literal = true
					else throw IllegalStateException("Found illegal \' in key")
				}
				else -> {
					builder.append(buffer.get())
					buffer.advance()
				}
			}
			buffer.advance()
		}
		throw IllegalStateException("Failed to process key")
	}
	fun processTable(buffer: CharBuffer): String {
		TODO("Unimplemented")
	}
}

class KV <T> (val key: String, val typeHint: Class<out T>, val contents: String)


//object Enummerator : Lactase<Set<Enum<*>>> {
//	override fun process(buffer: CharBuffer): Set<Enum<*>> {
//		buffer.findLast {  }
//	}
//}


fun deserialise(buffer: CharBuffer) {
	val skeleton = Skeleton()
	buffer.clear()
	when (buffer.get()) {
		'#' -> buffer.advanceToAfterNext('\n')
		else -> {
			buffer.captureToNext('\n')
			buffer.advanceToAfterNext('\n')
		}
	}
	// destructure document into keyed maps
	// start with enums
}

/**
 * advances the position to the next instance of the char
 *
 * @return self
 */
fun CharBuffer.advanceToNext(char: Char): CharBuffer {
	while(hasRemaining() && get() != char) {
		advance()
	}
	return this
}

/**
 * returns a subsequence from the current position to the next found character, inclusive,
 *
 * @return subsequence
 */
fun CharBuffer.captureToNext(char: Char): CharBuffer {
	mark()
	while(hasRemaining() && get() != char) {
		advance()
	}
	val mark = position()
	reset()
	return subSequence(0, mark)
}

/**
 * does a relative move of the position
 *
 * @return self
 */
fun CharBuffer.advance(positions: Int = 1): CharBuffer {
	position(position() + positions)
	return this
}

fun CharBuffer.advanceToAfterNext(char: Char): CharBuffer {
	return advanceToNext(char).advance()
}