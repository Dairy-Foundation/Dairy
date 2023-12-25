import collections.annotatedtargets.GroupedData
import datacarton.CartonComponentRenderer
import datacarton.DataCarton
import datacarton.Render
import datacarton.annotations.Data
import datacarton.annotations.Import
import datacarton.annotations.Export
import datacarton.annotations.Pack
import datacarton.processors.PublicationProcessor
import dev.frozenmilk.util.cell.RefCell
import org.junit.Test

class DataCartonTest : GroupedData {
    @Pack(bundle = false, group = "OWO")
    @Import(dataFields = ["c"])
    val parent1 = Parent();
    val parent2 = Parent(Child(false));

    @Test
    fun simplePack() {
        DataCarton.initWithDefaultPackageProcessors(object : PublicationProcessor {
            private val outputBuilder = StringBuilder()
            override fun initPublication() {
                outputBuilder.clear()
            }

            override fun updatePublication() {
                print(outputBuilder.toString())
            }

            override fun ignoreUpdate(): Boolean {
                return false
            }

            override fun accept(p0: CartonComponentRenderer) {
                outputBuilder.append(p0).append("\n")
            }

        })
        DataCarton
                .configureFor("MessageOut")
                .with(Render.DEFAULT_REVERSE_MESSAGE_BOARD)
        DataCarton.packageData(this)

        // test code
        parent2.childCell.get().call()
        parent2.childCell.get().call()
        parent2.childCell.get().call()
        parent2.childCell.get().call()
        parent2.childCell.get().call()
        parent2.childCell.get().call()

//        @Suppress("ControlFlowWithEmptyBody")
        DataCarton.awaitUpdate()
//        while (!DataCarton.update()) { }
    }
}

class Parent(child: Child = Child()) {
    @Data
    @Export
    val childCell = RefCell(child)

//    @Data
    val c = 10
}

class Child(@Data @Export private val flag: Boolean = true) {
    private var callCount = 0;

    fun call() {
        DataCarton.publishMessage("MessageOut", "Hello World", callCount++)
    }

    fun empty() {
    }
}
