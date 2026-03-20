package site.addzero.kcp.allobjectjvmstatic.plugin

import org.jetbrains.kotlin.DeprecatedForRemovalCompilerApi
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isFakeOverride
import org.jetbrains.kotlin.name.FqName

@OptIn(DeprecatedForRemovalCompilerApi::class)
class AllObjectJvmStaticIrGenerationExtension : IrGenerationExtension {

    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext,
    ) {
        val jvmStaticAnnotationClass = pluginContext.referenceClass(AllObjectJvmStaticPluginKeys.jvmStaticAnnotation)
            ?: return
        val annotationConstructor = jvmStaticAnnotationClass.owner.declarations
            .filterIsInstance<IrConstructor>()
            .singleOrNull()
            ?: return

        moduleFragment.files.forEach { file ->
            file.declarations
                .filterIsInstance<IrClass>()
                .forEach { declaration ->
                    annotateObjectFunctionsRecursively(
                        declaration = declaration,
                        annotationType = jvmStaticAnnotationClass.defaultType,
                        constructor = annotationConstructor.symbol,
                    )
                }
        }
    }

    private fun annotateObjectFunctionsRecursively(
        declaration: IrClass,
        annotationType: org.jetbrains.kotlin.ir.types.IrType,
        constructor: org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol,
    ) {
        if (declaration.kind == ClassKind.OBJECT) {
            declaration.declarations
                .filterIsInstance<IrSimpleFunction>()
                .filter(::shouldAnnotate)
                .forEach { function ->
                    function.annotations += createJvmStaticAnnotation(
                        function = function,
                        annotationType = annotationType,
                        constructor = constructor,
                    )
                }
        }

        declaration.declarations
            .filterIsInstance<IrClass>()
            .forEach { child ->
                annotateObjectFunctionsRecursively(
                    declaration = child,
                    annotationType = annotationType,
                    constructor = constructor,
                )
            }
    }

    private fun shouldAnnotate(function: IrSimpleFunction): Boolean {
        if (function.isFakeOverride) {
            return false
        }
        if (function.hasAnnotation(AllObjectJvmStaticPluginKeys.jvmStaticAnnotation)) {
            return false
        }
        if (function.origin.name.contains("SYNTHETIC_ACCESSOR", ignoreCase = true)) {
            return false
        }
        if (function.dispatchReceiverParameter == null) {
            return false
        }
        if (function.correspondingPropertySymbol != null) {
            return false
        }
        return true
    }

    private fun createJvmStaticAnnotation(
        function: IrSimpleFunction,
        annotationType: org.jetbrains.kotlin.ir.types.IrType,
        constructor: org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol,
    ): IrConstructorCall {
        return IrConstructorCallImpl(
            startOffset = function.startOffset,
            endOffset = function.endOffset,
            type = annotationType,
            origin = null,
            symbol = constructor,
            source = SourceElement.NO_SOURCE,
            typeArgumentsCount = 0,
        )
    }
}
