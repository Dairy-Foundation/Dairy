//package dev.frozenmilk.util.cell
//
//import java.lang.reflect.Field
//
///**
// * A special type of [LateInitCell] that performs a safe 're-init' operation
// *
// * @see [reinitFrom]
// */
//open class ReinitCell<T> : LateInitCell<T>() {
//	/**
//	 * allows for an expensive comparison that changes variables annotated with [ReinitialisableField] to match the respective fields in [t] if the current contents of the cell are not null, or replaces the contents if the contents of the cell are
//	 *
//	 * @param t the comparative target
//	 */
//	fun reinitFrom(t: T): T {
//		val internal = safeGet()
//		if (internal != null) reinitialiseFrom(internal, t)
//		accept(internal ?: t)
//		return get()!!;
//	}
//}
//
//private fun <T> reinitialiseFrom(previous: T, new: T) {
//	val fields = mutableListOf<Field>();
//	var searchTargetClass: Class<*>? = previous!!::class.java;
//	while (searchTargetClass != null && searchTargetClass != Any::class.java) {
//		fields += searchTargetClass.declaredFields;
//		searchTargetClass = searchTargetClass.superclass;
//	}
//
//	fields
//		.filter { it.isAnnotationPresent(ReinitialisableField::class.java) }
//		.forEach {
//			it.isAccessible = true;
//			it.set(previous, it.get(new));
//		}
//}
//
//@Retention(AnnotationRetention.RUNTIME)
//@Target(AnnotationTarget.FIELD)
//annotation class ReinitialisableField
//
