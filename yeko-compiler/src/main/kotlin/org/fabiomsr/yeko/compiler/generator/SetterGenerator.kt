package org.fabiomsr.yeko.compiler.generator

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeSpec
import org.fabiomsr.yeko.compiler.Logger
import org.fabiomsr.yeko.compiler.model.ViewAttribute
import javax.lang.model.element.Modifier


/**
 * Created by Fabiomsr on 11/9/16.
 */
class SetterGenerator(private val viewAttributes: List<ViewAttribute>, private val logger: Logger) {

    fun generate(): TypeSpec {
        return TypeSpec.classBuilder(SETTER_CLASS_NAME)
                .addMethods(generateSetMethods())
                .addMethod(generateApplyMethod())
                .build()
    }

    private fun generateSetMethods(): List<MethodSpec> {
        return viewAttributes.filter { it.expose }
                .map { generateSetMethod(it) }
                .toList()
    }

    private fun generateSetMethod(attr: ViewAttribute): MethodSpec {
        val param = ParameterSpec.builder(attributeTypes[attr.format], attr.name).build()
        return MethodSpec.methodBuilder("${attr.name}")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(param)
                .addStatement("$UPDATE_METHOD${attr.name.capitalize()}(${attr.name})")
                .addStatement("return this")
                .returns(ClassName.get("", SETTER_CLASS_NAME))
                .build();
    }

    private fun generateApplyMethod(): MethodSpec {
        return MethodSpec.methodBuilder("apply")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$APPLY_CHANGES_METHOD()")
                .build();
    }
}