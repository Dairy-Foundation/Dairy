package collections.annotatedtargets

import datacarton.annotations.Pack
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.function.Supplier

open class Packaged(parentInstance: Supplier<*>, accessibleObject: AccessibleObject) : AnnotatedTarget<AccessibleObject, Pack>(parentInstance, accessibleObject, Pack::class.java), GroupedData {

    override val group: String by lazy {
        return@lazy if (annotation.group != "") annotation.group
        else if (accessibleObject as? Field != null) accessibleObject.name
        else if (accessibleObject as? Method != null) accessibleObject.name
        else parentInstance.get().javaClass.simpleName
    }

//    open val annotatedDataFieldStream: Stream<AnnotatedDataField>
//        get() {
//            val fields = ArrayList<Field>()
//            var searchTargetClass: Class<*>? = childInstance.javaClass
//            while (searchTargetClass != null && searchTargetClass != Any::class.java) {
//                fields += searchTargetClass.declaredFields.toList()
//                searchTargetClass = searchTargetClass.superclass
//            }
//            return fields.stream()
//                    .filter { f: Field ->
//                        f.isAnnotationPresent(
//                                Data::class.java
//                        )
//                    }
//                    .peek { f: Field -> f.isAccessible = true }
//                    .map { f: Field -> AnnotatedDataField(childInstance, f, group) }
//        }
//
//    open val annotatedPublisherStream: Stream<AnnotatedPublisher>
//        get() {
//            val methods = ArrayList<Method>()
//            var searchTargetClass: Class<*>? = childInstance.javaClass
//            while (searchTargetClass != null && searchTargetClass != Any::class.java) {
//                methods += searchTargetClass.declaredMethods.toList();
//                searchTargetClass = searchTargetClass.superclass
//            }
//            return methods.stream()
//                    .filter { m: Method ->
//                        m.isAnnotationPresent(
//                                Publishes::class.java
//                        )
//                    }
//                    .peek { m: Method -> m.isAccessible = true }
//                    .map { m: Method -> AnnotatedPublisher(childInstance, m, group) }
//        }
}
