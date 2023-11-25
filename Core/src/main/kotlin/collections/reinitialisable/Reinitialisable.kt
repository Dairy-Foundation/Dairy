package collections.reinitialisable

import java.lang.reflect.Field

class Reinitialisable<T> {
	private var internal: T? = null

	fun from(t: T): T {
		internal?.let { reinitialiseFrom(internal, t) }
		internal = internal ?: t;
		return internal!!;
	}
}

private fun <T> reinitialiseFrom(previous: T, new: T) {
	val fields = emptyList<Field>().toMutableList();
	var searchTargetClass: Class<*>? = previous!!::class.java;
	while (searchTargetClass != null && searchTargetClass != Any::class.java) {
		fields += searchTargetClass.declaredFields;
		searchTargetClass = searchTargetClass.superclass;
	}

	fields
		.filter { it.isAnnotationPresent(ReinitialisableField::class.java) }
		.forEach {
			it.isAccessible = true;
			it.set(previous, it.get(new));
		}
}

