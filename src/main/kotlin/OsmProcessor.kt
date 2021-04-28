import java.io.InputStream
import java.lang.AssertionError
import javax.xml.namespace.QName
import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.XMLStreamException

import org.slf4j.LoggerFactory

class OsmProcessor private constructor() {
    init {
        throw IllegalAccessError()
    }

    companion object {
        private val USER_ATTR_NAME = QName("user")
        private val ID_ATTR_NAME = QName("id")
        private val KEY_ATTR_NAME = QName("k")
        private const val NODE = "node"
        private const val TAG = "tag"

        private val processorLogger = LoggerFactory.getLogger(this::class.java)

        fun process(inputStream: InputStream): OsmResultContainer {
            processorLogger.info("OSM processing start")
            val factory = XMLInputFactory.newInstance()
            var eventReader: XMLEventReader? = null
            val result = OsmResultContainer()

            try {
                eventReader = factory.createXMLEventReader(inputStream)
                while (eventReader.hasNext()) {
                    val event = eventReader.nextEvent()
                    if (XMLStreamConstants.START_ELEMENT == event.eventType) {
                        val startElement = event.asStartElement()

                        if (NODE == startElement.name.localPart) {
                            val userAttribute = startElement.getAttributeByName(USER_ATTR_NAME)
                            result.userEditsCount(userAttribute.value)
                            val idAttribute = startElement.getAttributeByName(ID_ATTR_NAME)

                            processorLogger.debug("Processing tags for node with id {} start", idAttribute)
                            processTags(result, eventReader)
                            processorLogger.debug("Tags processing finish")
                        }
                    }
                }
            } finally {
                eventReader?.close() ?: throw AssertionError("eventReader is null")
            }

            processorLogger.info("OSM processing finish")
            return result
        }

        private fun processTags(result: OsmResultContainer, eventReader: XMLEventReader) {
            while (eventReader.hasNext()) {
                val event = eventReader.nextEvent()
                if (XMLStreamConstants.END_ELEMENT == event.eventType && NODE == event.asEndElement().name.localPart) {
                    return
                }

                if (XMLStreamConstants.START_ELEMENT == event.eventType) {
                    val startElement = event.asStartElement()
                    if (TAG == startElement.name.localPart) {
                        val key = startElement.getAttributeByName(KEY_ATTR_NAME)
                        result.incrementTagCount(key.value)
                    }
                }
            }

            throw XMLStreamException("Unexpected end of stream")
        }
    }
}
