package sibyl.api

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.joda.time.DateTime

@Serializer(forClass = DateTime::class)
object DateTimeSerializer: KSerializer<DateTime> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("datetime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): DateTime {
        return DateTime.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: DateTime) {
        encoder.encodeString(value.toString())
    }

}
