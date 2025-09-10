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
 * A visitor that doesn't pass or return anything.
 */
open class KSVisitorVoid : com.google.devtools.ksp.com.addzero.kld.symbol.KSVisitor<Unit, Unit> {
    override fun visitNode(node: com.google.devtools.ksp.com.addzero.kld.symbol.KSNode, data: Unit) {
    }

    override fun visitAnnotated(annotated: com.google.devtools.ksp.com.addzero.kld.symbol.KSAnnotated, data: Unit) {
    }

    override fun visitAnnotation(annotation: com.google.devtools.ksp.com.addzero.kld.symbol.KSAnnotation, data: Unit) {
    }

    override fun visitModifierListOwner(modifierListOwner: com.google.devtools.ksp.com.addzero.kld.symbol.KSModifierListOwner, data: Unit) {
    }

    override fun visitDeclaration(declaration: com.google.devtools.ksp.com.addzero.kld.symbol.KSDeclaration, data: Unit) {
    }

    override fun visitDeclarationContainer(declarationContainer: com.google.devtools.ksp.com.addzero.kld.symbol.KSDeclarationContainer, data: Unit) {
    }

    override fun visitDynamicReference(reference: com.google.devtools.ksp.com.addzero.kld.symbol.KSDynamicReference, data: Unit) {
    }

    override fun visitFile(file: com.google.devtools.ksp.com.addzero.kld.symbol.KSFile, data: Unit) {
    }

    override fun visitFunctionDeclaration(function: com.google.devtools.ksp.com.addzero.kld.symbol.KSFunctionDeclaration, data: Unit) {
    }

    override fun visitCallableReference(reference: com.google.devtools.ksp.com.addzero.kld.symbol.KSCallableReference, data: Unit) {
    }

    override fun visitParenthesizedReference(reference: com.google.devtools.ksp.com.addzero.kld.symbol.KSParenthesizedReference, data: Unit) {
    }

    override fun visitPropertyDeclaration(property: com.google.devtools.ksp.com.addzero.kld.symbol.KSPropertyDeclaration, data: Unit) {
    }

    override fun visitPropertyAccessor(accessor: com.google.devtools.ksp.com.addzero.kld.symbol.KSPropertyAccessor, data: Unit) {
    }

    override fun visitPropertyGetter(getter: com.google.devtools.ksp.com.addzero.kld.symbol.KSPropertyGetter, data: Unit) {
    }

    override fun visitPropertySetter(setter: com.google.devtools.ksp.com.addzero.kld.symbol.KSPropertySetter, data: Unit) {
    }

    override fun visitClassifierReference(reference: com.google.devtools.ksp.com.addzero.kld.symbol.KSClassifierReference, data: Unit) {
    }

    override fun visitReferenceElement(element: com.google.devtools.ksp.com.addzero.kld.symbol.KSReferenceElement, data: Unit) {
    }

    override fun visitTypeAlias(typeAlias: com.google.devtools.ksp.com.addzero.kld.symbol.KSTypeAlias, data: Unit) {
    }

    override fun visitTypeArgument(typeArgument: com.google.devtools.ksp.com.addzero.kld.symbol.KSTypeArgument, data: Unit) {
    }

    override fun visitClassDeclaration(classDeclaration: com.google.devtools.ksp.com.addzero.kld.symbol.KSClassDeclaration, data: Unit) {
    }

    override fun visitTypeParameter(typeParameter: com.google.devtools.ksp.com.addzero.kld.symbol.KSTypeParameter, data: Unit) {
    }

    override fun visitTypeReference(typeReference: com.google.devtools.ksp.com.addzero.kld.symbol.KSTypeReference, data: Unit) {
    }

    override fun visitValueParameter(valueParameter: com.google.devtools.ksp.com.addzero.kld.symbol.KSValueParameter, data: Unit) {
    }

    override fun visitValueArgument(valueArgument: com.google.devtools.ksp.com.addzero.kld.symbol.KSValueArgument, data: Unit) {
    }

    override fun visitDefNonNullReference(reference: com.google.devtools.ksp.com.addzero.kld.symbol.KSDefNonNullReference, data: Unit) {
    }
}
