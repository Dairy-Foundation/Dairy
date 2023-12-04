import collections.annotatedtargets.GroupedData
import collections.refcell.RefCell
import datacarton.DataBlockRender
import datacarton.DataCarton
import datacarton.MessageBoardRender
import datacarton.RenderOrder
import datacarton.annotations.Data
import datacarton.annotations.DataPackageProcessor
import datacarton.annotations.DriverStationPublicationProcessor
import datacarton.annotations.Flatpack
import datacarton.annotations.Flatten
import datacarton.annotations.Pack
import org.junit.Test

class DataCartonTest : GroupedData {
    // todo replace/extend @Flatpack annotations and @Flatten annotations with @Import and @Export with more powerful features
    @Pack
    val parent1 = Parent();
    val parent2 = Parent(Child(false));
    private val output = Output();

    @Test
    fun simplePack() {
        val settings = DataCarton.SettingsMapper.SettingsBuilder()
        settings.with(DataBlockRender()) {}
        settings.with(MessageBoardRender()) {
            minLen = 10
            reversed = true
        }
        val dataCarton = DataCarton.Builder()
                .renderWithByDefault(RenderOrder.DEFAULT_MAPPING)
                .addPublicationProcessors(DriverStationPublicationProcessor(output, Output::class.java.getDeclaredField("output")))
                .addPackageProcessors(DataPackageProcessor)
                .buildIntoInstance()

//        val dataCarton = DataCarton.new(output, Output::class.java.getDeclaredField("output"), RenderOrder.DEFAULT_MAPPING);
        dataCarton.mapSettings {
            setFor("MessageOut") {
//                with(DataBlockRender()) {}
                with(MessageBoardRender()) {
                    minLen = 3
                    reversed = true
                }
            }
        }
//        dataCarton.setFor("MessageOut") {
//            RenderOrder(
//                    RenderOrder.Render.DATA_BLOCK(),
//                    RenderOrder.Render.MESSAGE_BOARD().settings.setMinLen(10)
//            )
//        }

        dataCarton.packageData(this);

        parent1.childCell.get().call()

        dataCarton.update();
        println(output);

        parent1.childCell.accept(Child(false))
        parent1.childCell.get().call()
        parent1.childCell.get().call()

        dataCarton.update();
        println(output);
    }
}

class Parent(child: Child = Child()) {
    @Pack
    @Flatpack
    val childCell = RefCell(child);

    @Data
    val c = 10
}

class Child(@Data @Flatten private val flag: Boolean = true) {
    private var callCount = 0;

    fun call() {
        DataCarton.publishMessage("MessageOut", "Hello World " + callCount++)
    }

    fun empty() {
    }
}

class Output(private val output: String = "") {
    override fun toString(): String {
        return output;
    }
}