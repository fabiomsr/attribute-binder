package org.fabiomsr.yeko.compiler

import com.google.auto.common.AnnotationMirrors
import com.google.auto.common.MoreElements
import org.fabiomsr.yeko.annotation.CustomView
import org.fabiomsr.yeko.annotation.SaveStated
import org.fabiomsr.yeko.compiler.generator.YekoViewGenerator
import org.fabiomsr.yeko.compiler.model.Resources
import org.simpleframework.xml.core.Persister
import java.io.File
import javax.annotation.processing.Filer
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.ElementFilter
import javax.lang.model.util.Elements

/**
 * Created by Fabiomsr on 26/7/16.
 */
class YekoProcessorImpl(val logger: Logger, val filer: Filer, val elementsUtils: Elements, val options: Map<String, String>) {

    val resSrcDirs: String by options.withDefault { "[]" }

    fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.RELEASE_7
    }

    fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(SaveStated::class.java.canonicalName,
                CustomView::class.java.canonicalName)
    }

    fun getSupportedOptions(): Set<String> {
        return setOf("resSrcDirs")
    }

    fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        val processingOver = roundEnv?.processingOver()!!

        if (!processingOver) {
            val elements = roundEnv?.getElementsAnnotatedWith(CustomView::class.java)!!
            elements.filter { it is TypeElement }
                    .map { it as TypeElement }
                    .map { getCustomViewMetadata(it) }
                    .map { YekoViewGenerator(it, logger) }
                    .forEach { it.generateView(elementsUtils, filer) }
        }

        return false
    }

    private fun getCustomViewMetadata(typeElement: TypeElement): CustomViewMetaModel {
        val attrsFile = getAttributesFile(typeElement.simpleName.toString())
        val attributes = getViewAttributes(attrsFile)

        val saveStatedFields = findSaveStatedFields(typeElement)
        val customViewValues = getCustomViewValues(typeElement)

        return CustomViewMetaModel(typeElement, attributes, saveStatedFields, customViewValues)
    }

    private fun getCustomViewValues(typeElement: TypeElement): Map<String, AnnotationValue> {
        val customViewMirror = MoreElements.getAnnotationMirror(typeElement, CustomView::class.java).get()
        val customViewElementsAndValues = AnnotationMirrors.getAnnotationValuesWithDefaults(customViewMirror)
        return customViewElementsAndValues.mapKeys {
            it.key.simpleName.toString()
        }
    }

    private fun findSaveStatedFields(customViewElement: TypeElement): List<VariableElement> {
        val fields = ElementFilter.fieldsIn(customViewElement.enclosedElements)
        return fields.filter { it.getAnnotation(SaveStated::class.java) != null }
    }


    internal fun getViewAttributes(attrsFile: File?): Resources {
        if (attrsFile != null) {
            val serializer = Persister()
            return serializer.read(Resources::class.java, attrsFile, false)
        }

        return Resources()
    }

    private fun getAttributesFile(attributesFileName: String): File? {
        val resDirs = resSrcDirs.substring(1, resSrcDirs.length - 1)
                .split(',')

        for (resDir in resDirs) {
            val attrFile = File("$resDir${File.separator}values${File.separator}$attributesFileName.xml")
            if (attrFile.exists()) {
                return attrFile
            }
        }

        return null
    }
}