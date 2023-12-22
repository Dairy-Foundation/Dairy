package collections.annotatedtargets

import datacarton.annotations.Data
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.function.Supplier

class AnnotatedData(
        parentInstance: Supplier<*>,
        accessibleObject: AccessibleObject,
        parentGroup: String,
        flatten: Boolean
) : AnnotatedTarget<AccessibleObject, Data>(parentInstance, accessibleObject, Data::class.java, Data()), ValidData {

    override val group: String by lazy {
        return@lazy if (flatten) parentGroup
        else if (annotation.group != "") annotation.group
        else if (parentGroup != "") parentGroup
        else parentInstance.get().javaClass.simpleName
    }

    override val label: String by lazy {
        return@lazy if (annotation.label != "") annotation.label
        else if (accessibleObject as? Field != null) accessibleObject.name
        else if (accessibleObject as? Method != null) accessibleObject.name
        else ""
    }
}