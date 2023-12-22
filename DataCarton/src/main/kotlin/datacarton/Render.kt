package datacarton

open class Render<S>(
		val component: Class<out CartonComponent?>,
		val componentBuilder: Class<out CartonComponent.TraceComponentBuilder?>,
		val settings: S,
){
    companion object {
        val DEFAULT_DATA_BLOCK by lazy { Render(DataBlock::class.java, DataBlock.Builder::class.java, DataBlock.Settings()) }
        val DEFAULT_REVERSE_MESSAGE_BOARD by lazy {
			Render(
					MessageBoard::class.java,
					MessageBoard.Builder::class.java,
					MessageBoard.Settings()
			)
        }
        val DEFAULT_MESSAGE_BOARD by lazy {
			Render(
					MessageBoard::class.java,
					MessageBoard.Builder::class.java,
					MessageBoard.Settings(reversed = false)
			)
        }
    }
}