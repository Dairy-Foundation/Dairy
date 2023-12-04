package datacarton

import datacarton.CartonComponent.TraceComponentBuilder

class RenderOrder(renders: List<Render<*>>) {
    val orderMapping: LinkedHashMap<Class<out CartonComponent?>, Pair<Int, Render<*>>> by lazy {
        val map = LinkedHashMap<Class<out CartonComponent?>, Pair<Int, Render<*>>>()
        renders.forEachIndexed { index, render ->
            map[render.component] = Pair(index, render)
        }
        map
    }

    fun getEntry(cartonComponent: Class<out CartonComponent?>): Pair<Int, Render<*>>? {
        return orderMapping[cartonComponent]
    }

    open class Render<S>(
            val component: Class<out CartonComponent?>,
            val componentBuilder: Class<out TraceComponentBuilder?>,
            val settings: S,
    )

    companion object {
        val DATA_BLOCK by lazy { Render(DataBlock::class.java, DataBlock.Builder::class.java, DataBlock.Settings()) }
        val REVERSE_MESSAGE_BOARD by lazy {
            Render(
                    MessageBoard::class.java,
                    MessageBoard.Builder::class.java,
                    MessageBoard.Settings(minLen = 3)
            )
        }
        val MESSAGE_BOARD by lazy {
            Render(
                    MessageBoard::class.java,
                    MessageBoard.Builder::class.java,
                    MessageBoard.Settings(minLen = 3, reversed = false)
            )
        }
        val DEFAULT_MAPPING by lazy { RenderOrder(listOf(DATA_BLOCK, REVERSE_MESSAGE_BOARD)) }
        val BLOCK_FIRST by lazy { DEFAULT_MAPPING }
        val BLOCK_FIRST_MESSAGE_FORWARD by lazy { RenderOrder(listOf(DATA_BLOCK, MESSAGE_BOARD)) }
        val MESSAGE_FIRST_MESSAGE_FORWARD by lazy { RenderOrder(listOf(MESSAGE_BOARD, DATA_BLOCK)) }
        val MESSAGE_FIRST by lazy { RenderOrder(listOf(REVERSE_MESSAGE_BOARD, DATA_BLOCK)) }
    }
}

class MessageBoardRender() : RenderOrder.Render<MessageBoard.Settings>(MessageBoard::class.java, MessageBoard.Builder::class.java, MessageBoard.Settings()) {
//    fun setLen(len: Int?): MessageBoardRender {
//        settings.setLen(len)
//        return this
//    }
//
//    fun setMinLen(minLen: Int?): MessageBoardRender {
//        settings.setMinLen(minLen)
//        return this
//    }
//
//    fun setReversed(reversed: Boolean): MessageBoardRender {
//        settings.setReversed(reversed)
//        return this
//    }
}

class DataBlockRender() : RenderOrder.Render<DataBlock.Settings>(DataBlock::class.java, DataBlock.Builder::class.java, DataBlock.Settings())
