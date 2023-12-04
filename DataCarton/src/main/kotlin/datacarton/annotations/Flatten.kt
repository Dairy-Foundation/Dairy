package datacarton.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(
	AnnotationTarget.FIELD,
	AnnotationTarget.FUNCTION,
)
annotation class Flatten
