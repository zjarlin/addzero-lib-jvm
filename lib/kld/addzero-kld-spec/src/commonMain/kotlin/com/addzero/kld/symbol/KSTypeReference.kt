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
 * A [KSTypeReference] combines a [com.google.devtools.ksp.com.addzero.kld.symbol.KSReferenceElement] with annotations and modifiers.
 */
interface KSTypeReference : com.google.devtools.ksp.com.addzero.kld.symbol.KSAnnotated,
    com.google.devtools.ksp.com.addzero.kld.symbol.KSModifierListOwner {

    /**
     * Underlying element of this type reference, without annotations and modifiers.
     */
    val element: com.google.devtools.ksp.com.addzero.kld.symbol.KSReferenceElement?

    /**
     * Resolves to the original declaration site.
     * @return A type resolved from this type reference.
     * Calling [resolve] is expensive and should be avoided if possible.
     */
    fun resolve(): com.google.devtools.ksp.com.addzero.kld.symbol.KSType
}
