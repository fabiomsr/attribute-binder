package org.fabiomsr.yeko.compiler.generator

import com.squareup.javapoet.*
import org.fabiomsr.yeko.compiler.CustomViewMetaModel
import org.fabiomsr.yeko.compiler.Logger
import org.fabiomsr.yeko.compiler.model.AttributeValue
import org.fabiomsr.yeko.compiler.model.StyleDefaultValue
import org.fabiomsr.yeko.compiler.model.ViewAttribute
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier
import javax.lang.model.util.Elements

/**
 * Created by Fabiomsr on 2/8/16.
 */
class YekoViewGenerator(private val metaModel: CustomViewMetaModel, private val logger: Logger) {

    private val contextClassName = ClassName.get("android.content", "Context")
    private val attributeSetClassName = ClassName.get("android.util", "AttributeSet")
    private val typeArrayClassName = ClassName.get("android.content.res", "TypedArray")

    private val contextParam = ParameterSpec.builder(contextClassName, CONTEXT_VAR_NAME).build()
    private val attributeSetParam = ParameterSpec.builder(attributeSetClassName, ATTRS_VAR_NAME).build()
    private val typeArrayParam = ParameterSpec.builder(typeArrayClassName, TYPED_ARRAY_VAR_NAME).build()


    fun generateView(elementsUtils: Elements, filer: Filer) {
        JavaFile.builder(metaModel.getPackageName(elementsUtils), generateClass())
                .build()
                .writeTo(filer)
    }

    private fun generateClass(): TypeSpec {
        val setterClass = SetterGenerator(metaModel.resources.declareStyleable.attributes, logger)

        return TypeSpec.classBuilder(metaModel.name)
                .addModifiers(Modifier.ABSTRACT)
                .superclass(ClassName.bestGuess(metaModel.parentClassName))
                .addFields(generateStaticFields())
                .addFields(generateFields())
                .addField(generateSetterField())
                .addMethods(generateConstructors())
                .addMethods(generateInitViewMethods())
                .addMethods(generateUpdateMethods())
                .addMethod(generateSetterMethod())
                .addMethod(generateApplyChangesMethod())
                .addType(setterClass.generate())
                .build()
    }

    private fun generateSetterField(): FieldSpec? {
        return FieldSpec.builder(ClassName.get("", SETTER_CLASS_NAME),
                SETTER_VAR_NAME, Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $SETTER_CLASS_NAME()")
                .build();
    }

    private fun generateSetterMethod(): MethodSpec {
        return MethodSpec.methodBuilder("$SETTER_METHOD")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get("", SETTER_CLASS_NAME))
                .addStatement("return $SETTER_VAR_NAME")
                .build();
    }

    private fun generateApplyChangesMethod(): MethodSpec {
        return MethodSpec.methodBuilder("$APPLY_CHANGES_METHOD")
                .addModifiers(Modifier.PROTECTED)
                .addStatement("invalidate()")
                .build();
    }

    private fun generateUpdateMethods(): List<MethodSpec> {
        val attrs = metaModel.resources.declareStyleable.attributes;

        return attrs.filter { it.expose }
                .map { generateUpdateMethod(it) }
                .toList()
    }

    private fun generateUpdateMethod(attr: ViewAttribute): MethodSpec {
        val param = ParameterSpec.builder(attributeTypes[attr.format], attr.name).build()
        return MethodSpec.methodBuilder("$UPDATE_METHOD${attr.name.capitalize()}")
                .addModifiers(Modifier.PROTECTED)
                .addParameter(param)
                .addStatement("this.${attr.name} = ${attr.name}")
                .build();
    }

    private fun generateStaticFields(): List<FieldSpec> {
        val attrs = metaModel.resources.declareStyleable.attributes;

        return attrs.filter { isAttributeEnumOrFlag(it.format) }
                .map { generateStaticField(it) }
                .flatMap { it }
                .toList();
    }

    private fun generateStaticField(attr: ViewAttribute): List<FieldSpec> {
        val values = if (attr.enumValues.isNotEmpty()) attr.enumValues else attr.flagValues

        return values.map { buildStaticField(attr.name, it) }
                .toList()
    }

    private fun buildStaticField(namePrefix: String, styleDefaultValue: AttributeValue): FieldSpec {
        val name = "${namePrefix}_${styleDefaultValue.name}".toUpperCase();
        return FieldSpec.builder(Int::class.java,
                name, Modifier.PROTECTED, Modifier.STATIC, Modifier.FINAL)
                .initializer(styleDefaultValue.value)
                .build();
    }

    private fun generateFields(): List<FieldSpec> {
        val attrs = metaModel.resources.declareStyleable.attributes;

        return attrs.map { generateField(it) }
                .toList();
    }

    private fun generateField(viewAttribute: ViewAttribute): FieldSpec {
        return FieldSpec.builder(attributeTypes[viewAttribute.format],
                viewAttribute.name)
                .build();
    }

    private fun generateConstructors(): List<MethodSpec> {
        val defStyleAttrParam = ParameterSpec.builder(Int::class.java, DEF_STYLE_ATTR_VAR_NAME).build()
        val defStyleResParam = ParameterSpec.builder(Int::class.java, DEF_STYLE_RES_VAR_NAME).build()

        val params = listOf<ParameterSpec>(contextParam, attributeSetParam,
                defStyleAttrParam, defStyleResParam)

        return params.indices
                .map {
                    generateConstructor(params.subList(0, it + 1))
                }.toList()
    }

    private fun generateConstructor(params: List<ParameterSpec>): MethodSpec {
        val initMethodCall = if (params.size == 1)
            "$INIT_METHOD($CONTEXT_VAR_NAME, null)"
        else "$INIT_METHOD($CONTEXT_VAR_NAME, $ATTRS_VAR_NAME)"

        return MethodSpec.constructorBuilder().apply {
            addModifiers(Modifier.PUBLIC)
            params.forEach { addParameter(it) }
            addStatement("super(${params.joinToString(",") { it.name }})")
            addStatement(initMethodCall)
        }.build()
    }

    private fun generateInitViewMethods(): List<MethodSpec> {
        val rClassPackage = metaModel.rClassName.substringBeforeLast(".")
        val rClassName = ClassName.get(rClassPackage, "R")

        val styleableName = metaModel.resources.declareStyleable.name;
        val defaultStyleName = metaModel.resources.style.name;

        val attributes = metaModel.resources.declareStyleable.attributes
        val defaultAttrValues = metaModel.resources.style.defaultValues

        val initCode = CodeBlock.builder().apply {
            addStatement("\$T $TYPED_ARRAY_VAR_NAME = $CONTEXT_VAR_NAME.getTheme().obtainStyledAttributes($ATTRS_VAR_NAME, \$T.styleable.$styleableName," +
                    " 0, \$T.style.$defaultStyleName)", typeArrayClassName, rClassName, rClassName)
            beginControlFlow("try")

            attributes.forEach {
                addStatement(generateGetAttributeStatement(styleableName, it, defaultAttrValues), rClassName)
            }

            addStatement("$INIT_VIEW_METHOD($CONTEXT_VAR_NAME, $TYPED_ARRAY_VAR_NAME)")
            endControlFlow()
            beginControlFlow("finally")
            addStatement("typedArray.recycle()")
            endControlFlow()
        }.build();

        val privateInitView = MethodSpec.methodBuilder(INIT_METHOD)
                .addModifiers(Modifier.PRIVATE)
                .addParameters(listOf(contextParam, attributeSetParam))
                .addCode(initCode)
                .build();

        val protectedInitView = MethodSpec.methodBuilder(INIT_VIEW_METHOD)
                .addModifiers(Modifier.PROTECTED)
                .addParameters(listOf(contextParam, typeArrayParam))
                .build();

        return listOf(privateInitView, protectedInitView)
    }

    private fun generateGetAttributeStatement(styleableName: String, attr: ViewAttribute, defaultAttrValues: List<StyleDefaultValue>): String {
        val statementFormat = "${attr.name} = ${typedArrayMethods[attr.format]}"

        if (isAttributeEnumOrFlag(attr.format)) {
            val attrValues = if (attr.enumValues.isNotEmpty())
                attr.enumValues
            else attr.flagValues

            val fieldName = "${attr.name}_${attrValues[0].name}".toUpperCase();
            return statementFormat.format("${styleableName}_${attr.name}", fieldName)
        }

        return statementFormat.format("${styleableName}_${attr.name}")
    }

    private fun isAttributeEnumOrFlag(format: String) = format.isEmpty() || format == "flag"
            || format == "enum"
}

const val SETTER_CLASS_NAME = "Setter"

const val INIT_METHOD = "init"
const val INIT_VIEW_METHOD = "initView"
const val UPDATE_METHOD = "update"
const val APPLY_CHANGES_METHOD = "applyChanges"
const val SETTER_METHOD = "setter"

const val CONTEXT_VAR_NAME = "context"
const val ATTRS_VAR_NAME = "attrs"
const val DEF_STYLE_ATTR_VAR_NAME = "defStyleAttr"
const val DEF_STYLE_RES_VAR_NAME = "defStyleRes"
const val TYPED_ARRAY_VAR_NAME = "typedArray"
const val SETTER_VAR_NAME = "setter"

val attributeTypes = mapOf(
        "boolean" to Boolean::class.java,
        "reference" to Int::class.java,
        "color" to Int::class.java,
        "integer" to Int::class.java,
        "enum" to Int::class.java,
        "flag" to Int::class.java,
        "float" to Float::class.java,
        "dimension" to Float::class.java,
        "fraction" to Float::class.java,
        "string" to String::class.java,
        "" to Int::class.java // Implicitly defined (Enums of flags)
);

val typedArrayMethods = mapOf(
        "boolean" to "$TYPED_ARRAY_VAR_NAME.getBoolean(\$T.styleable.%s, true)",
        "reference" to "$TYPED_ARRAY_VAR_NAME.getResourceId(\$T.styleable.%s, -1)",
        "color" to "$TYPED_ARRAY_VAR_NAME.getColor(\$T.styleable.%s, 0)",
        "integer" to "$TYPED_ARRAY_VAR_NAME.getInt(\$T.styleable.%s, 0)",
        "enum" to "$TYPED_ARRAY_VAR_NAME.getInt(\$T.styleable.%s, %s)",
        "flag" to "$TYPED_ARRAY_VAR_NAME.getInt(\$T.styleable.%s, %s)",
        "float" to "$TYPED_ARRAY_VAR_NAME.getFloat(\$T.styleable.%s, 0)",
        "dimension" to "$TYPED_ARRAY_VAR_NAME.getDimension(\$T.styleable.%s, 0)",
        "fraction" to "$TYPED_ARRAY_VAR_NAME.getFraction(\$T.styleable.%s, 1, 1, 0)",
        "string" to "$TYPED_ARRAY_VAR_NAME.getString(\$T.styleable.%s)",
        "" to "$TYPED_ARRAY_VAR_NAME.getInt(\$T.styleable.%s, %s)" // Implicitly defined (Enums of flags)
);



