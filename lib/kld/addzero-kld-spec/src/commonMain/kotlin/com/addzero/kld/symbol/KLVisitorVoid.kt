/*
 * Copyright 2020 Google LLC
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.addzero.kld.symbol

/**
 * A visitor that doesn't pass or return anything.
 */
open class KLVisitorVoid : com.addzero.kld.symbol.KLVisitor<Unit, Unit> {
    override fun visitNode(node: com.addzero.kld.symbol.KLNode, data: Unit) {
    }

    override fun visitAnnotated(annotated: com.addzero.kld.symbol.KLAnnotated, data: Unit) {
    }

    override fun visitAnnotation(annotation: com.addzero.kld.symbol.KLAnnotation, data: Unit) {
    }

    override fun visitModifierListOwner(modifierListOwner: com.addzero.kld.symbol.KLModifierListOwner, data: Unit) {
    }

    override fun visitDeclaration(declaration: com.addzero.kld.symbol.KLDeclaration, data: Unit) {
    }

    override fun visitDeclarationContainer(declarationContainer: com.addzero.kld.symbol.KLDeclarationContainer, data: Unit) {
    }

    override fun visitDynamicReference(reference: com.addzero.kld.symbol.KLDynamicReference, data: Unit) {
    }

    override fun visitFile(file: com.addzero.kld.symbol.KLFile, data: Unit) {
    }

    override fun visitFunctionDeclaration(function: com.addzero.kld.symbol.KLFunctionDeclaration, data: Unit) {
    }

    override fun visitCallableReference(reference: com.addzero.kld.symbol.KLCallableReference, data: Unit) {
    }

    override fun visitParenthesizedReference(reference: com.addzero.kld.symbol.KLParenthesizedReference, data: Unit) {
    }

    override fun visitPropertyDeclaration(property: com.addzero.kld.symbol.KLPropertyDeclaration, data: Unit) {
    }

    override fun visitPropertyAccessor(accessor: com.addzero.kld.symbol.KLPropertyAccessor, data: Unit) {
    }

    override fun visitPropertyGetter(getter: com.addzero.kld.symbol.KLPropertyGetter, data: Unit) {
    }

    override fun visitPropertySetter(setter: com.addzero.kld.symbol.KLPropertySetter, data: Unit) {
    }

    override fun visitClassifierReference(reference: com.addzero.kld.symbol.KLClassifierReference, data: Unit) {
    }

    override fun visitReferenceElement(element: com.addzero.kld.symbol.KLReferenceElement, data: Unit) {
    }

    override fun visitTypeAlias(typeAlias: com.addzero.kld.symbol.KLTypeAlias, data: Unit) {
    }

    override fun visitTypeArgument(typeArgument: com.addzero.kld.symbol.KLTypeArgument, data: Unit) {
    }

    override fun visitClassDeclaration(classDeclaration: com.addzero.kld.symbol.KLClassDeclaration, data: Unit) {
    }

    override fun visitTypeParameter(typeParameter: com.addzero.kld.symbol.KLTypeParameter, data: Unit) {
    }

    override fun visitTypeReference(typeReference: com.addzero.kld.symbol.KLTypeReference, data: Unit) {
    }

    override fun visitValueParameter(valueParameter: com.addzero.kld.symbol.KLValueParameter, data: Unit) {
    }

    override fun visitValueArgument(valueArgument: com.addzero.kld.symbol.KLValueArgument, data: Unit) {
    }

    override fun visitDefNonNullReference(reference: com.addzero.kld.symbol.KLDefNonNullReference, data: Unit) {
    }
}
