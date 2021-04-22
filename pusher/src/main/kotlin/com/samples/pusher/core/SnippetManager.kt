package com.samples.pusher.core
import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.security.MessageDigest

import java.io.File

fun md5(input:String): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
}

class SnippetManager(val dirSamples: File) {
    private val logger = LoggerFactory.getLogger("Samples Pusher")

    data class PathEntity(var countSnippets: Int, var hash: String)

    private val mapPath = mutableMapOf<String, PathEntity>()

    var changed: Boolean = false
        private set

    /*
    Create a snippet file in the folder dirSamples/FileName
    Example: for snippet from "src/test.html"
    It will create some .kt file in dirSamples/test
     */
    fun addSnippet(code: String, path: String) {
        val filename = path.substringAfterLast('/').substringBeforeLast('.')

        val entity = mapPath.getOrPut(path) { removeAllSnippets(path)
                                              PathEntity(0, md5(path)) }
        entity.countSnippets++
        val newName = "${entity.hash}.${entity.countSnippets}.kt"
        val targetDir = dirSamples.resolve(filename)
        if(!targetDir.exists())
            if(!targetDir.mkdirs())
                throw Exception("Can't make dirs to ${targetDir.path}")

        File(targetDir, newName).writeText(code)
        changed = true
        logger.info("Created a snippet file: ${newName}")
    }

    fun removeAllSnippets(path: String) {
        val filename = path.substringAfterLast('/').substringBeforeLast('.')
        val hash = md5(path)
        val directory = dirSamples.resolve(filename)
        var folder_empty = true
        val fileTree = directory.walkTopDown()
        for (file in fileTree) {
            if (file.name.startsWith(hash)) {
                if (!file.delete()) {
                    logger.error("Can't remove the snippet file: ${file.name}")
                }
                changed = true
                logger.info("Removed the snippet file: ${file.name}")
            } else {
                folder_empty = false
            }
        }
        if(folder_empty)
            directory.delete()
    }
}