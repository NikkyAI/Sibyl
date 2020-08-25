package sibyl.module.paste

interface PasteService {
    /**
     * @param content text content of paste
     *
     * @return url of paste
     */
    suspend fun paste(content: String): String?
}
