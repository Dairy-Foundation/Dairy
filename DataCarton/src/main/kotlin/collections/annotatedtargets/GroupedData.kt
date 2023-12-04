package collections.annotatedtargets

interface GroupedData {
    val group: String; get() = this.javaClass.simpleName;
}
