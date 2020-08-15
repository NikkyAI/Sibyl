package sibyl.api

import kotlinx.serialization.*
import org.joda.time.DateTime

@Serializer(forClass = DateTime::class)
object DateTimeSerializer: KSerializer<DateTime> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveDescriptor("datetime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): DateTime {
        return DateTime.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: DateTime) {
        encoder.encodeString(value.toString())
    }

}
