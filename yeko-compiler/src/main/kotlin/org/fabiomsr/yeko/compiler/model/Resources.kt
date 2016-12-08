package org.fabiomsr.yeko.compiler.model

import org.simpleframework.xml.*

/**
 * Created by Fabiomsr on 31/7/16.
 */
@Root(strict = false)
data class Resources(@field:Element(name = "declare-styleable", required = false)
                     var declareStyleable: DeclareStyleable,
                     @field:Element(required = false) var style: Style){

    constructor( ) : this(DeclareStyleable(), Style()) {}

}

data class DeclareStyleable(@field:Attribute var name : String,
                            @field:ElementList(inline = true, entry="attr")
                            var attributes: List<ViewAttribute>){

    constructor( ) : this("", mutableListOf<ViewAttribute>()) {}
}

data class Style(@field:Attribute var name:String,
                 @field:ElementList(inline = true, entry="item")
                 var defaultValues: List<StyleDefaultValue>){
    constructor( ) : this("", mutableListOf<StyleDefaultValue>()) {}
}

data class ViewAttribute(@field:Attribute var name : String,
                         @field:Attribute(required = false) var format : String,
                         @field:Attribute(required = false) var expose : Boolean,
                         @field:ElementList(required = false, inline = true, entry="enum")
                         var enumValues: List<AttributeValue>,
                         @field:ElementList(required = false, inline = true, entry="flag")
                         var flagValues: List<AttributeValue>){
    constructor( ) : this("", "", true, mutableListOf(), mutableListOf()) {}
}

data class StyleDefaultValue(@field:Attribute var name : String,
                             @field:Text var value: String){
    constructor( ) : this("", "") {}
}

data class AttributeValue(@field:Attribute var name : String,
                           @field:Attribute var value: String){
    constructor( ) : this("", "") {}
}