@import site.addzero.apt.dict.template.TranslationMethodContext
@import site.addzero.apt.dict.template.TranslationMethodType

@param TranslationMethodContext context

@if(context.methodType == TranslationMethodType.SYNCHRONOUS)
/**
 * Performs dictionary translation for all annotated fields
 * @param dictService The dictionary service to use for translation
 */
fun translate(dictService: site.addzero.apt.dict.service.DictService) {
    @for(var dictField : context.dictFields)
        @if(!dictField.dictCode.isEmpty())
    // System dictionary translation for ${dictField.sourceField}
    this.${dictField.targetField} = dictService.translateByDictCode("${dictField.dictCode}", this.${dictField.sourceField}?.toString())
        @elseif(!dictField.table.isEmpty())
    // Table dictionary translation for ${dictField.sourceField}
    this.${dictField.targetField} = dictService.translateByTable("${dictField.table}", "${dictField.codeColumn}", "${dictField.nameColumn}", this.${dictField.sourceField})
        @elseif(!dictField.spelExp.isEmpty())
    // SPEL expression translation for ${dictField.sourceField}
    this.${dictField.targetField} = dictService.translateBySpel("${dictField.spelExp}", this)
        @endif
    @endfor
}
@elseif(context.methodType == TranslationMethodType.CONTEXT_BASED)
/**
 * Performs dictionary translation using pre-loaded translation context
 * This method eliminates N+1 query problems by using batch-loaded data
 * @param context The translation context containing pre-loaded dictionary data
 */
fun translateWithContext(context: site.addzero.apt.dict.context.TranslationContext) {
    @for(var dictField : context.dictFields)
        @if(!dictField.dictCode.isEmpty())
    // System dictionary translation for ${dictField.sourceField}
    this.${dictField.targetField} = context.getSystemDictTranslation("${dictField.dictCode}", this.${dictField.sourceField}?.toString())
        @elseif(!dictField.table.isEmpty())
    // Table dictionary translation for ${dictField.sourceField}
    this.${dictField.targetField} = context.getTableDictTranslation("${dictField.table}", "${dictField.codeColumn}", "${dictField.nameColumn}", this.${dictField.sourceField})
        @endif
    @endfor
}
@elseif(context.methodType == TranslationMethodType.BATCH)
/**
 * Populates translation fields from a context map
 * @param context Map containing translation data keyed by field names
 */
fun populateFromContext(context: Map<String, Any?>) {
    @for(var dictField : context.dictFields)
    this.${dictField.targetField} = context["${dictField.targetField}"] as? String
    @endfor
}
@elseif(context.methodType == TranslationMethodType.ASYNCHRONOUS)
/**
 * Performs asynchronous dictionary translation for all annotated fields
 * @param dictService The dictionary service to use for translation
 * @return CompletableFuture that completes when translation is finished
 */
fun translateAsync(dictService: site.addzero.apt.dict.service.DictService): java.util.concurrent.CompletableFuture<Unit> {
    return dictService.translateAsync(this)
}
@endif