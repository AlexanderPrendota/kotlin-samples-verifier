package com.samples.verifier.internal

import com.samples.verifier.*
import com.samples.verifier.internal.utils.RequestHelper
import com.samples.verifier.internal.utils.cloneRepository
import com.samples.verifier.internal.utils.processFile
import com.samples.verifier.model.ExecutionResult
import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

internal class SamplesVerifierInstance(compilerUrl: String, compilerType: CompilerType) : SamplesVerifier {
    private val logger: Logger by lazy { LoggerFactory.getLogger(javaClass) }
    private val requestHelper = RequestHelper(compilerUrl, compilerType)

    override fun collect(url: String, attributes: List<String>, type: FileType): Map<ExecutionResult, Code> {
        check(url, attributes, type)
        return requestHelper.results
    }

    override fun check(url: String, attributes: List<String>, type: FileType) {
        val dir = File("rep")
        try {
            cloneRepository(dir, url)
            processFiles(dir, attributes, type)
        } catch (e: GitException) {
            //TODO
            logger.error("${e.message}\n")
        } catch (e: CallException) {
            //TODO
        } catch (e: IOException) {
            //TODO
            logger.error("${e.message}\n")
        }
        finally {
            if (dir.isDirectory) {
                FileUtils.deleteDirectory(dir)
            } else {
                dir.delete()
            }
        }
    }

    private fun processFiles(directory: File, attributes: List<String>, type: FileType) {
        Files.walk(directory.toPath()).use {
            it.forEach { path: Path ->
                val file = path.toFile()
                when (type) {
                    FileType.MARKDOWN -> {
                        if (file.extension == "md") {
                            processFile(file, type, attributes, requestHelper)
                        }
                    }
                    FileType.HTML -> {
                        if (file.extension == "html") {
                            processFile(file, type, attributes, requestHelper)
                        }
                    }
                }
            }
        }
    }
}