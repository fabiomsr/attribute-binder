package org.fabiomsr.yeko.compiler
import org.junit.Test
import org.mockito.Mockito.mock
import java.io.File
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.lang.model.util.Elements

/**
 * Created by Fabiomsr on 31/7/16.
 */

class AttributesDeserializationTest {

    @Test
    fun deserializationTest() {

        val attributesXmlUrl = javaClass.getResource("/exampleAttributes.xml")
        val attributesXml = File(attributesXmlUrl.toURI())

        val yekoProcessor = YekoProcessorImpl(mock(Messager::class.java),
                mock(Filer::class.java),
                mock(Elements::class.java),
                mapOf())


        val viewAttributes = yekoProcessor.getViewAttributes(attributesXml)

        println(viewAttributes)
       // val resultBook = deserializeBook(bookXml)
       // val book = Book("A Song of Ice and Fire", 864)

       // assertTrue(book.equals(resultBook))
    }
}