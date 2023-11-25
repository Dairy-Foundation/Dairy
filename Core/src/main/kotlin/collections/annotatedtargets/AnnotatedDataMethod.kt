package collections.annotatedtargets

//import datacarton.annotations.Data
//import java.lang.reflect.Method
//
//open class AnnotatedDataMethod(parentInstance: Any, method: Method, parentGroup: String) :
//        AnnotatedMethod<Data>(parentInstance, method, Data::class.java), ValidData {
//
//    init {
//        if (method.parameterCount != 0) throw RuntimeException("Attempted to apply @${Data::class.java.simpleName} to a method with parameters, this annotation should only be applied to fairly simple getter methods of non-final fields")
//    }
//
//    override val group: String by lazy {
//        return@lazy if (annotation.group != "") annotation.group
//        else if (parentGroup != "") parentGroup
//        else parentInstance.javaClass.simpleName
//    }
//
//    override val label: String by lazy {
//        return@lazy if (annotation.label != "") annotation.label
//        else accessibleObject.name
//    }
//
//}