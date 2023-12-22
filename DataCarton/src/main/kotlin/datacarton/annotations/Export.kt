package datacarton.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class Export(
		val bundle: Boolean = true,
//		val targets: Array<ExportTarget> = [ExportTarget.DS, ExportTarget.LOG],
//		val externalTargets: Array<String> = [],
)
