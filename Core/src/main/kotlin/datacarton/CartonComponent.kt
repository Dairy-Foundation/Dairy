package datacarton

interface CartonComponent {
	fun getData(): Collection<DataLine?>
	fun add(dataLine: DataLine?)

	/**
	 * MUST HAVE A NO PARAM CONSTRUCTOR TO BE USED
	 */
	interface TraceComponentBuilder {
		/**
		 * add for types that don't care about the message on initialisation
		 */
		fun add() {
			add(null)
		}

		fun add(dataLine: DataLine?)

		/**
		 * @return null if nothing was added
		 */
		fun build(settings: Any?): CartonComponent?
	}
}
