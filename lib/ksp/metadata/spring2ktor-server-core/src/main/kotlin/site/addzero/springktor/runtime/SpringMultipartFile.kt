package site.addzero.springktor.runtime

import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

internal class ByteArraySpringMultipartFile(
    private val name: String,
    private val originalFilename: String?,
    private val contentType: String?,
    private val bytes: ByteArray,
) : MultipartFile {
    override fun getName(): String {
        return name
    }

    override fun getOriginalFilename(): String? {
        return originalFilename
    }

    override fun getContentType(): String? {
        return contentType
    }

    override fun isEmpty(): Boolean {
        return bytes.isEmpty()
    }

    override fun getSize(): Long {
        return bytes.size.toLong()
    }

    override fun getBytes(): ByteArray {
        return bytes.copyOf()
    }

    override fun getInputStream(): InputStream {
        return ByteArrayInputStream(bytes)
    }

    override fun transferTo(dest: File) {
        dest.writeBytes(bytes)
    }
}
