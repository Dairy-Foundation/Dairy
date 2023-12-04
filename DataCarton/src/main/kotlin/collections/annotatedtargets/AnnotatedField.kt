package collections.annotatedtargets

import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * should only be used on final fields
 */
//abstract class AnnotatedField<A : Annotation>(
//        parentInstance: Any,
//        field: Field,
//        annotation: Class<out A>,
//) : AnnotatedTarget<Field, A>(parentInstance, field, annotation) {
//    init {
//        if (!Modifier.isFinal(field.modifiers)) throw RuntimeException("Field ${field.name} on ${parentInstance::class.java.simpleName} was annotated with @${annotation.simpleName}, but is not final, @${annotation.simpleName} can only be applied to fields that are final, consider moving the @${annotation.simpleName} annotation to a private getter method that returns the field OR wrap this in a RefCell() and annotate that RefCell with @Pack")
//    }
//
//    // the child
//    val childInstance: Any = accessibleObject.get(parentInstance);
//}
