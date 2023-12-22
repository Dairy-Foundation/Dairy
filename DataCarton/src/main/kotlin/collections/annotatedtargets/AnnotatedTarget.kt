package collections.annotatedtargets

import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.Objects
import java.util.function.Supplier

abstract class AnnotatedTarget<O : AccessibleObject, A : Annotation>(
        // holds the accessible object
        val parentInstance: Supplier<*>,
        val accessibleObject: O,
        annotation: Class<out A>,
        defaultAnnotation: A
) : GroupedData {

    val childInstance by lazy {
        if (accessibleObject is Field) {
            val childInstance = accessibleObject.get(parentInstance.get())
            if (childInstance is Supplier<*>) {
                return@lazy childInstance
            }
            return@lazy Supplier {
                accessibleObject.get(parentInstance.get())
            }
        } else if (accessibleObject is Method) {
            val childInstance = accessibleObject.invoke(parentInstance.get())
            if (childInstance is Supplier<*>) {
                return@lazy childInstance
            }
            return@lazy Supplier {
                accessibleObject.invoke(parentInstance.get())
            }
        }
        Supplier { null }
    }

    init {
        accessibleObject.isAccessible = true

//        if (accessibleObject as? Method != null /*todo get this working: && accessibleObject.parameterCount != 0 */) throw RuntimeException("Method ${accessibleObject.name} on ${parentInstance.get()::class.java.simpleName} was annotated with @${annotation.simpleName}, this annotation should only be applied to fairly simple getter methods of non-final fields")
        if (accessibleObject is Field && !Modifier.isFinal(accessibleObject.modifiers)) throw RuntimeException("Field ${accessibleObject.name} on ${parentInstance.get()::class.java.simpleName} was annotated with @${annotation.simpleName}, but is not final, @${annotation.simpleName} can only be applied to fields that are final, consider moving the @${annotation.simpleName} annotation to a private getter method that returns the field OR wrap this in a RefCell() and annotate that RefCell with @Pack")
    }

    // exists on the accessible object
    val annotation: A = accessibleObject.getAnnotation(annotation) ?: defaultAnnotation
}
