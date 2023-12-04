package collections.annotatedtargets

import datacarton.annotations.Flatpack
import java.lang.reflect.AccessibleObject
import java.util.Objects
import java.util.function.Supplier

class Flatpackaged(parentInstance: Supplier<*>, accessibleObject: AccessibleObject, private val parentGroup: String) :
        Packaged(parentInstance, accessibleObject) {
    val flatpack: Flatpack = Objects.requireNonNull(accessibleObject.getAnnotation(Flatpack::class.java))
    override val group: String by lazy {
        parentGroup
    }
    //    override val annotatedDataFieldStream: Stream<AnnotatedDataField>
//        get() = super.annotatedDataFieldStream
//                .filter { dataField: AnnotatedDataField ->
//                    dataField.accessibleObject.isAnnotationPresent(
//                            Flatten::class.java
//                    ) && flatpack.includeDefaults || flatpack.targets
//                            .contains(dataField.accessibleObject.name)
//                }
//                .map { dataField: AnnotatedDataField ->
//                    FlattenedDataField(
//                            dataField.parentInstance,
//                            dataField.accessibleObject,
//                            parentGroup
//                    )
//                }
//    override val annotatedPublisherStream: Stream<AnnotatedPublisher>
//        get() = super.annotatedPublisherStream
//                .filter { onCall: AnnotatedPublisher ->
//                    onCall.accessibleObject.isAnnotationPresent(
//                            Flatten::class.java
//                    ) && flatpack.includeDefaults || flatpack.targets.contains(onCall.accessibleObject.name)
//                }
//                .map { onCall: AnnotatedPublisher ->
//                    FlattenedPublisher(
//                            onCall.parentInstance,
//                            onCall.accessibleObject,
//                            parentGroup
//                    )
//                }
}
