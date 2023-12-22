package datacarton

class RenderOrder(val renders: List<Render<*>>) {
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

    companion object {
        val DEFAULT_MAPPING by lazy { RenderOrder(listOf(Render.DEFAULT_DATA_BLOCK, Render.DEFAULT_REVERSE_MESSAGE_BOARD)) }
        val BLOCK_FIRST by lazy { DEFAULT_MAPPING }
        val BLOCK_FIRST_MESSAGE_FORWARD by lazy { RenderOrder(listOf(Render.DEFAULT_DATA_BLOCK, Render.DEFAULT_MESSAGE_BOARD)) }
        val MESSAGE_FIRST_MESSAGE_FORWARD by lazy { RenderOrder(listOf(Render.DEFAULT_MESSAGE_BOARD, Render.DEFAULT_DATA_BLOCK)) }
        val MESSAGE_FIRST by lazy { RenderOrder(listOf(Render.DEFAULT_REVERSE_MESSAGE_BOARD, Render.DEFAULT_DATA_BLOCK)) }
    }
}

class MessageBoardRender(reversed: Boolean = true, len: Int? = null, minLen: Int = 5) : Render<MessageBoard.Settings>(MessageBoard::class.java, MessageBoard.Builder::class.java, MessageBoard.Settings(reversed, len, minLen))

class DataBlockRender() : Render<DataBlock.Settings>(DataBlock::class.java, DataBlock.Builder::class.java, DataBlock.Settings())
