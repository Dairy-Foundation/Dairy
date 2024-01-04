package dev.frozenmilk.dairy.milkman.messages

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import dev.frozenmilk.dairy.milkman.MilkManWebSocket
import dev.frozenmilk.dairy.milkman.messages.incoming.GetOpModeMetaData
import dev.frozenmilk.dairy.milkman.messages.incoming.InitOpMode
import dev.frozenmilk.dairy.milkman.messages.incoming.StartOpMode
import dev.frozenmilk.dairy.milkman.messages.incoming.StopOpMode
import dev.frozenmilk.dairy.milkman.messages.outgoing.ReturnOpModeMetaData
import dev.frozenmilk.dairy.milkman.messages.outgoing.RobotState
import java.lang.reflect.Type


val gson = GsonBuilder()
		.registerTypeAdapter(Message::class.java, MessageDeserializer)
		.create()

interface Message {
	val type: MessageType
	fun toJson(): String = gson.toJson(this)
	fun process(milkManWebSocket: MilkManWebSocket)
}

enum class MessageType(val message: Class<out Message>) {
	GET_OP_MODE_META_DATA(GetOpModeMetaData::class.java),
	INIT_OP_MODE(InitOpMode::class.java),
	START_OP_MODE(StartOpMode::class.java),
	STOP_OP_MODE(StopOpMode::class.java),

	RETURN_OP_MODE_META_DATA(ReturnOpModeMetaData::class.java),
	ROBOT_STATE(RobotState::class.java),
}

object MessageDeserializer : JsonDeserializer<Message> {
	override fun deserialize(jsonElement: JsonElement, type: Type, jsonDeserializationContext: JsonDeserializationContext): Message? {
		val messageObj: JsonObject = jsonElement.asJsonObject
		val messageType : MessageType? = try {
			jsonDeserializationContext.deserialize(messageObj["type"], MessageType::class.java)
		}
		catch (e: JsonParseException) { null }
		if (messageType?.message == null) {
			return null
		}
		val msgType = TypeToken.get(messageType.message).type
		return jsonDeserializationContext.deserialize(jsonElement, msgType)
	}
}