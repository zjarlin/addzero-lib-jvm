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
package com.google.devtools.ksp.com.addzero.kld.symbol

/**
 * A visitor for program elements
 */
interface KSVisitor<D, R> {
    fun visitNode(node: com.google.devtools.ksp.com.addzero.kld.symbol.KSNode, data: D): R

    fun visitAnnotated(annotated: com.google.devtools.ksp.com.addzero.kld.symbol.KSAnnotated, data: D): R

    fun visitAnnotation(annotation: com.google.devtools.ksp.com.addzero.kld.symbol.KSAnnotation, data: D): R

    fun visitModifierListOwner(modifierListOwner: com.google.devtools.ksp.com.addzero.kld.symbol.KSModifierListOwner, data: D): R

    fun visitDeclaration(declaration: com.google.devtools.ksp.com.addzero.kld.symbol.KSDeclaration, data: D): R

    fun visitDeclarationContainer(declarationContainer: com.google.devtools.ksp.com.addzero.kld.symbol.KSDeclarationContainer, data: D): R

    fun visitDynamicReference(reference: com.google.devtools.ksp.com.addzero.kld.symbol.KSDynamicReference, data: D): R

    fun visitFile(file: com.google.devtools.ksp.com.addzero.kld.symbol.KSFile, data: D): R

    fun visitFunctionDeclaration(function: com.google.devtools.ksp.com.addzero.kld.symbol.KSFunctionDeclaration, data: D): R

    fun visitCallableReference(reference: com.google.devtools.ksp.com.addzero.kld.symbol.KSCallableReference, data: D): R

    fun visitParenthesizedReference(reference: com.google.devtools.ksp.com.addzero.kld.symbol.KSParenthesizedReference, data: D): R

    fun visitPropertyDeclaration(property: com.google.devtools.ksp.com.addzero.kld.symbol.KSPropertyDeclaration, data: D): R

    fun visitPropertyAccessor(accessor: com.google.devtools.ksp.com.addzero.kld.symbol.KSPropertyAccessor, data: D): R

    fun visitPropertyGetter(getter: com.google.devtools.ksp.com.addzero.kld.symbol.KSPropertyGetter, data: D): R

    fun visitPropertySetter(setter: com.google.devtools.ksp.com.addzero.kld.symbol.KSPropertySetter, data: D): R

    fun visitReferenceElement(element: com.google.devtools.ksp.com.addzero.kld.symbol.KSReferenceElement, data: D): R

    fun visitTypeAlias(typeAlias: com.google.devtools.ksp.com.addzero.kld.symbol.KSTypeAlias, data: D): R

    fun visitTypeArgument(typeArgument: com.google.devtools.ksp.com.addzero.kld.symbol.KSTypeArgument, data: D): R

    fun visitClassDeclaration(classDeclaration: com.google.devtools.ksp.com.addzero.kld.symbol.KSClassDeclaration, data: D): R

    fun visitTypeParameter(typeParameter: com.google.devtools.ksp.com.addzero.kld.symbol.KSTypeParameter, data: D): R

    fun visitTypeReference(typeReference: com.google.devtools.ksp.com.addzero.kld.symbol.KSTypeReference, data: D): R

    fun visitValueParameter(valueParameter: com.google.devtools.ksp.com.addzero.kld.symbol.KSValueParameter, data: D): R

    fun visitValueArgument(valueArgument: com.google.devtools.ksp.com.addzero.kld.symbol.KSValueArgument, data: D): R

    fun visitClassifierReference(reference: com.google.devtools.ksp.com.addzero.kld.symbol.KSClassifierReference, data: D): R

    fun visitDefNonNullReference(reference: com.google.devtools.ksp.com.addzero.kld.symbol.KSDefNonNullReference, data: D): R
}
