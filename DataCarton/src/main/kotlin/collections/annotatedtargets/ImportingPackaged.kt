package collections.annotatedtargets

import datacarton.annotations.Import
import java.lang.reflect.AccessibleObject
import java.util.function.Supplier

class ImportingPackaged(parentInstance: Supplier<*>, accessibleObject: AccessibleObject, private val parentGroup: String?) :
        Packaged(parentInstance, accessibleObject, parentGroup) {
    val import: Import = accessibleObject.getAnnotation(Import::class.java) ?: throw Exception("internal error, target was not annotated with @Import")
    val includeDefaults = import.includeDefaults

    val dataFields by lazy {
        import.dataFields.mapNotNull {
            var searchClass = childInstance.get()?.javaClass
            while (searchClass != null && searchClass != Any::class.java && searchClass != Object::class.java) {
                val res = childInstance.get()?.javaClass?.getDeclaredField(it)
                if (res != null) return@mapNotNull res
                searchClass = searchClass?.superclass
            }
            return@mapNotNull null
        }
    }

    val dataMethods by lazy {
        import.dataMethods.mapNotNull {
            var searchClass = childInstance.get()?.javaClass
            while (searchClass != null && searchClass != Any::class.java && searchClass != Object::class.java) {
                val res = childInstance.get()?.javaClass?.getDeclaredMethod(it)
                if (res != null) return@mapNotNull res
                searchClass = searchClass?.superclass
            }
            return@mapNotNull null
        }
    }

    val packFields by lazy {
        import.packFields.mapNotNull {
            var searchClass = childInstance.get()?.javaClass
            while (searchClass != null && searchClass != Any::class.java && searchClass != Object::class.java) {
                val res = childInstance.get()?.javaClass?.getDeclaredField(it)
                if (res != null) return@mapNotNull res
                searchClass = searchClass?.superclass
            }
            return@mapNotNull null
        }
    }

    val packMethods by lazy {
        import.packMethods.mapNotNull {
            var searchClass = childInstance.get()?.javaClass
            while (searchClass != null && searchClass != Any::class.java && searchClass != Object::class.java) {
                val res = childInstance.get()?.javaClass?.getDeclaredMethod(it)
                if (res != null) return@mapNotNull res
                searchClass = searchClass?.superclass
            }
            return@mapNotNull null
        }
    }
}
