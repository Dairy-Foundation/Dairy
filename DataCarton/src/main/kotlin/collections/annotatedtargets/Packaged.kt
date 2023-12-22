package collections.annotatedtargets

import datacarton.annotations.Pack
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.function.Supplier

open class Packaged(parentInstance: Supplier<*>, accessibleObject: AccessibleObject, parentGroup: String?) : AnnotatedTarget<AccessibleObject, Pack>(parentInstance, accessibleObject, Pack::class.java, Pack()), GroupedData {
    override val group: String by lazy {
        return@lazy parentGroup
                ?: if (annotation.group != "") annotation.group
                else if (accessibleObject as? Field != null) accessibleObject.name
                else if (accessibleObject as? Method != null) accessibleObject.name
                else parentInstance.get().javaClass.simpleName
    }
    val bundle: Boolean = annotation.bundle
}
