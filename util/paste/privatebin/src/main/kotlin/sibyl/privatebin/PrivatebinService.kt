package sibyl.privatebin

import io.github.novacrypto.base58.Base58
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.bouncycastle.crypto.modes.GCMBlockCipher
import org.bouncycastle.crypto.params.AEADParameters
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.encoders.Base64
import sibyl.module.paste.PasteService
import kotlin.random.Random

private val ByteArray.base64: String get() = String(Base64.encode(this))

class PrivatebinService(
    val httpClient: HttpClient,
    val privateBinInstance: String = "https://privatebin.net/",
): PasteService {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val jsonSerializer = Json {
            prettyPrint = false
            encodeDefaults = false
        }

        val kdf_iterations = 100000 // was 10000 before PrivateBin version 1.3
        val kdf_keysize = 256 // bits of resulting kdf_key
        val cipher_algo = "aes"
        val cipher_mode = "gcm" // was "ccm" before PrivateBin version 1.0
        val cipher_tag_size = 128
    }

    fun deriveKey(key: ByteArray, kdf_salt: ByteArray): ByteArray {
        val gen = PKCS5S2ParametersGenerator(SHA256Digest())
        gen.init(key, kdf_salt, kdf_iterations)
        return (gen.generateDerivedParameters(kdf_keysize) as KeyParameter).key
    }

    override suspend fun paste(text: String): String {
        val password: String = ""
        val cipher_iv = Random.nextBytes(16) // 128 bit
        val kdf_salt = Random.nextBytes(8) // 8 bytes
        val key = Random.nextBytes(kdf_keysize / 8) // # 256 cipher block bits

        // derive PBKDF2 from key
        val password_bytes = password.toByteArray(Charsets.UTF_8)
        val kdf_key = deriveKey(key + password_bytes, kdf_salt)

        val compression = "none"
        val discussion = 0
        val burn_after_reading = 0

        val adata = """[["${cipher_iv.base64}","${kdf_salt.base64}",$kdf_iterations,$kdf_keysize,$cipher_tag_size,"$cipher_algo","$cipher_mode","$compression"],"plaintext",$discussion,$burn_after_reading]""".trimIndent()
        val adata_bytes = adata.toByteArray(Charsets.UTF_8)

        val cipher_message = mutableMapOf("paste" to text)
        if(false) {
            cipher_message["attachment"] = TODO("attachment")
            cipher_message["attachment_name"] = TODO("attachment_name")
        }

        val cipher_message_bytes = jsonSerializer.encodeToString(
            MapSerializer(
                String.serializer(),
                String.serializer()
            ), cipher_message
        ).toByteArray(Charsets.UTF_8)

        // initial cipher
        val cipher = GCMBlockCipher(AESEngine())
        cipher.init(true, AEADParameters(KeyParameter(kdf_key), cipher_tag_size, cipher_iv, adata_bytes))

        val cipherText = ByteArray(cipher.getOutputSize(cipher_message_bytes.size))
        var ctLength = 0
//        ctLength += cipher.processBytes(adata_bytes, 0, adata_bytes.size, cipherText, ctLength)
        ctLength += cipher.processBytes(cipher_message_bytes, 0, cipher_message_bytes.size, cipherText, ctLength)
        logger.debug { "ctLength: $ctLength" }
        ctLength += cipher.doFinal(cipherText, ctLength)
        logger.debug { "ctLength: $ctLength" }

        val ct = cipherText.base64

        logger.debug { "kdf_key: ${kdf_key.base64}" }
        logger.debug { "cipher_iv: ${cipher_iv.base64}" }
        logger.debug { "kdf_salt: ${String(Base64.encode(kdf_salt))}" }
        logger.debug { "CipherText: ${cipherText.dropLast(16).toByteArray().base64}" }
        logger.debug { "CipherTag: ${cipherText.drop(16).toByteArray().base64}" }
        logger.debug { "ct: $ct" }

        val passphrase = Base58.base58Encode(key)
        logger.debug { "passphrase: $passphrase" }

        val expire = "1hour"
        val jsonBody = """{"v":2,"adata":$adata,"ct":"$ct","meta":{"expire":"$expire"}}"""

        logger.debug { "json: $jsonBody" }

        val urlResponse = httpClient.post<String>(privateBinInstance) {
            header("X-Requested-With", "JSONHttpRequest")
            body = TextContent(
                text = jsonBody, contentType = ContentType.Application.Json
            )
        }

        val response = jsonSerializer.decodeFromString(PrivatebinResponse.serializer(), urlResponse)

        logger.debug { "response: $urlResponse" }
        return privateBinInstance + "?" + response.id + "#" + passphrase
    }
}

@Serializable
data class PrivatebinResponse(
    val status: Int,
    val id: String,
    val url: String,
    val deletetoken: String
)