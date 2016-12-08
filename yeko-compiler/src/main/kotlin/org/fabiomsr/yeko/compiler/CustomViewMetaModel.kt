package org.fabiomsr.yeko.compiler

import org.fabiomsr.yeko.compiler.model.Resources
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Elements

/**
 * Created by Fabiomsr on 25/7/16.
 */
class CustomViewMetaModel(val element: TypeElement,
                          val resources: Resources,
                          val saveStatedFields: List<VariableElement>,
                          map: Map<String, Any?>) {

    val parent: AnnotationValue by map
    val rClass: AnnotationValue by map

    val name : String
        get() = "Yeko${element.simpleName}"

    val parentClassName : String
        get() = parent.value.toString()

    val rClassName : String
        get() = rClass.value.toString()

    fun getPackageName(elementsUtils: Elements) : String{
        return elementsUtils.getPackageOf(element).qualifiedName.toString()
    }

    override fun toString(): String {
        return "Parent:${parent.value?.toString()}  R:${rClass.value?.toString()}"
    }
}
