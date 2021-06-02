package com.samples.pusher.core

import com.samples.pusher.core.utils.getFilenameFromPath
import com.samples.pusher.core.utils.md5
import org.slf4j.LoggerFactory
import java.io.File

typealias EncodedPath = String

class SnippetManager(private val dirSamples: File) {
  private val logger = LoggerFactory.getLogger("Samples Pusher")

  data class PathEntity(var countSnippets: Int, var encodedPath: EncodedPath)


  private val mapPath = mutableMapOf<String, PathEntity>()

  // md5(path) maps to path
  private val decoderPath = mutableMapOf<EncodedPath, String>()

  var changed: Boolean = false
    private set

  /**
   * Create a snippet file in the folder dirSamples/FileName
   * Example: for snippet from "src/test.html"
   * It will create some .kt file in dirSamples/test
   *
   * @return name of created snippet file
   */
  fun addSnippet(code: String, path: String): String {
    val filename = getFilenameFromPath(path)


    val entity = mapPath.getOrPut(path) {
      removeAllSnippets(path)
      val encodedPath = md5(path)
      decoderPath[encodedPath] = path
      PathEntity(0, encodedPath)
    }
    entity.countSnippets++
    val newName = "${entity.encodedPath}.${entity.countSnippets}.kt"
    val targetDir = dirSamples.resolve(filename)
    if (!targetDir.exists())
      if (!targetDir.mkdirs())
        throw Exception("Can't make dirs to ${targetDir.path}")

    File(targetDir, newName).writeText(code)
    changed = true
    logger.info("Created a snippet file: $newName")
    return newName
  }

  fun removeAllSnippets(path: String) {
    val filename = getFilenameFromPath(path)
    val hash = md5(path)
    val directory = dirSamples.resolve(filename)
    if (!directory.exists())
      return
    val fileTree = directory.walkTopDown()
    for (file in fileTree) {

      if (file.name.startsWith(hash)) {
        if (!file.delete()) {
          logger.error("Can't remove the snippet file: ${file.name}")
        }
        changed = true
        logger.info("Removed the snippet file: ${file.name}")
      }
    }

    if (directory.listFiles().isEmpty())
      directory.delete()
  }

  /**
   * It only works within the framework of current SnippetManager instance
   */
  fun translateFilenameToAddedSnippetPath(filename: String): String {
    val encodedPath = getFilenameFromPath(filename).substringBefore('.')
    return decoderPath.getOrDefault(encodedPath, "")
  }
}

