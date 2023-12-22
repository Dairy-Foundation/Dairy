package datacarton.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class Import (
		val includeDefaults: Boolean = true,
		val dataFields: Array<String> = [],
		val dataMethods: Array<String> = [],
		val packFields: Array<String> = [],
		val packMethods: Array<String> = [],
)
