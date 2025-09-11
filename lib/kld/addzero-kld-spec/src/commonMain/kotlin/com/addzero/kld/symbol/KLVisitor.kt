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
 * A visitor for program elements
 */
interface KLVisitor<D, R> {
    fun visitNode(node: com.addzero.kld.symbol.KLNode, data: D): R

    fun visitAnnotated(annotated: com.addzero.kld.symbol.KLAnnotated, data: D): R

    fun visitAnnotation(annotation: com.addzero.kld.symbol.KLAnnotation, data: D): R

    fun visitModifierListOwner(modifierListOwner: com.addzero.kld.symbol.KLModifierListOwner, data: D): R

    fun visitDeclaration(declaration: com.addzero.kld.symbol.KLDeclaration, data: D): R

    fun visitDeclarationContainer(declarationContainer: com.addzero.kld.symbol.KLDeclarationContainer, data: D): R

    fun visitDynamicReference(reference: com.addzero.kld.symbol.KLDynamicReference, data: D): R

    fun visitFile(file: com.addzero.kld.symbol.KLFile, data: D): R

    fun visitFunctionDeclaration(function: com.addzero.kld.symbol.KLFunctionDeclaration, data: D): R

    fun visitCallableReference(reference: com.addzero.kld.symbol.KLCallableReference, data: D): R

    fun visitParenthesizedReference(reference: com.addzero.kld.symbol.KLParenthesizedReference, data: D): R

    fun visitPropertyDeclaration(property: com.addzero.kld.symbol.KLPropertyDeclaration, data: D): R

    fun visitPropertyAccessor(accessor: com.addzero.kld.symbol.KLPropertyAccessor, data: D): R

    fun visitPropertyGetter(getter: com.addzero.kld.symbol.KLPropertyGetter, data: D): R

    fun visitPropertySetter(setter: com.addzero.kld.symbol.KLPropertySetter, data: D): R

    fun visitReferenceElement(element: com.addzero.kld.symbol.KLReferenceElement, data: D): R

    fun visitTypeAlias(typeAlias: com.addzero.kld.symbol.KLTypeAlias, data: D): R

    fun visitTypeArgument(typeArgument: com.addzero.kld.symbol.KLTypeArgument, data: D): R

    fun visitClassDeclaration(classDeclaration: com.addzero.kld.symbol.KLClassDeclaration, data: D): R

    fun visitTypeParameter(typeParameter: com.addzero.kld.symbol.KLTypeParameter, data: D): R

    fun visitTypeReference(typeReference: com.addzero.kld.symbol.KLTypeReference, data: D): R

    fun visitValueParameter(valueParameter: com.addzero.kld.symbol.KLValueParameter, data: D): R

    fun visitValueArgument(valueArgument: com.addzero.kld.symbol.KLValueArgument, data: D): R

    fun visitClassifierReference(reference: com.addzero.kld.symbol.KLClassifierReference, data: D): R

    fun visitDefNonNullReference(reference: com.addzero.kld.symbol.KLDefNonNullReference, data: D): R
}
