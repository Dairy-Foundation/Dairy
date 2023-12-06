package dev.frozenmilk.dairy.core.cell.reinitialisable

import dev.frozenmilk.dairy.core.cell.RefCell
import java.lang.reflect.Field

/**
 * @see [reinitFrom]
 */
open class ReinitCell<T>(t: T? = null) : RefCell<T?>(t) {
	/**
	 * allows for an expensive comparison that changes variables annotated with [ReinitialisableField] to match the respective fields in [t] if the current contents of the cell are not null, or replaces the contents if the contents of the cell are
	 *
	 * @param t the comparative target
	 */
	fun reinitFrom(t: T): T {
		val internal = get()
		if (internal != null) reinitialiseFrom(internal, t)
		accept(internal ?: t)
		return get()!!;
	}
}

private fun <T> reinitialiseFrom(previous: T, new: T) {
	val fields = mutableListOf<Field>();
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

