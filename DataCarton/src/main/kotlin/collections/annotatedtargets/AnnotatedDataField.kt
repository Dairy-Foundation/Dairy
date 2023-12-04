package collections.annotatedtargets
//
//import datacarton.annotations.Data
//import java.lang.reflect.Field
//
////open class AnnotatedDataField(parentInstance: Any, field: Field, parentGroup: String) :
////        AnnotatedField<Data>(parentInstance, field, Data::class.java), ValidData {
////
////    override val group: String by lazy {
////        return@lazy if (annotation.group != "") annotation.group
////        else if (parentGroup != "") parentGroup
////        else parentInstance.javaClass.simpleName
////    }
////
////    override val label: String by lazy {
////        return@lazy if (annotation.label != "") annotation.label
////        else accessibleObject.name
////    }
////}
