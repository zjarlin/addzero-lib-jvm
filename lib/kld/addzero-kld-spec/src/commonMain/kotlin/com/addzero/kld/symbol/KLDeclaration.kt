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
 * A declaration, can be function declaration, class declaration and property declaration, or a type alias.
 */
interface KLDeclaration : com.addzero.kld.symbol.KLModifierListOwner,
    com.addzero.kld.symbol.KLAnnotated,
    com.addzero.kld.symbol.KLExpectActual {
    /**
     * Simple name of this declaration, usually the name identifier at the declaration site.
     */
    val simpleName: com.addzero.kld.symbol.KLName

    /**
     * Fully qualified name of this declaration, might not exist for some declarations like local declarations.
     */
    val qualifiedName: com.addzero.kld.symbol.KLName?

    /**
     * List of [type parameters][com.addzero.kld.symbol.KLTypeParameter] of the declaration.
     */
    val typeParameters: List<com.addzero.kld.symbol.KLTypeParameter>

    /**
     * The name of the package at which this declaration is declared.
     */
    val packageName: com.addzero.kld.symbol.KLName

    /**
     * Parent declaration of this declaration, i.e. the declaration that directly contains this declaration.
     * KLFile is not a declaration, so this property will be null for top level declarations.
     */
    val parentDeclaration: com.addzero.kld.symbol.KLDeclaration?

    /**
     * The containing source file of this declaration, can be null if symbol does not come from a source file, i.e. from a class file.
     */
    val containingFile: com.addzero.kld.symbol.KLFile?

    /**
     * The doc string enclosed by \/\*\* and \*\/
     */
    val docString: String?
}
