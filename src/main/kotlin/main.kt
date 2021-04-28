import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import javax.xml.stream.XMLStreamException

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import osm.OsmProcessor
import osm.OsmResultContainer


fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "Wrong argument count" }
    val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)

    rootLogger.info("File decompressing start")
    try {
        BZip2CompressorInputStream(FileInputStream(args[0])).use { inputStream ->
            rootLogger.info("File decompressing finish")

            val resultContainer: OsmResultContainer = OsmProcessor.process(inputStream)

            println("Changes statistic:")
            printStatistics(resultContainer.users)
            println("Tags statistic:")
            printStatistics(resultContainer.tags)
        }
    } catch (e: FileNotFoundException) {
        rootLogger.error("File not found", e)
    } catch (e: XMLStreamException) {
        rootLogger.error("XML reading error", e)
    } catch (e: IOException) {
        rootLogger.error("File read error", e)
    }
}

fun printStatistics (statistic: Map<String, Int>) {
    statistic.asSequence().sortedByDescending { it.value }.forEach {
        println("${it.key} : ${it.value}")
    }
}