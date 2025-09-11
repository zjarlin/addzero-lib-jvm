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
 * Instance of a constructor-call-like annotation.
 */
interface KLAnnotation : com.addzero.kld.symbol.KLNode {
    /**
     * Reference to the type of the annotation class declaration.
     */
    val annotationType: com.addzero.kld.symbol.KLTypeReference

    /**
     * The arguments applied to the constructor call to construct this annotation.
     * Must be compile time constants.
     * @see [com.addzero.kld.symbol.KLValueArgument] for operations on its values.
     */
    val arguments: List<com.addzero.kld.symbol.KLValueArgument>

    /**
     * The default values of the annotation members
     */
    val defaultArguments: List<com.addzero.kld.symbol.KLValueArgument>

    /**
     * Short name for this annotation, equivalent to the simple name of the declaration of the annotation class.
     */
    val shortName: com.addzero.kld.symbol.KLName

    /**
     * Use site target of the annotation. Could be null if no annotation use site target is specified.
     */
    val useSiteTarget: com.addzero.kld.symbol.AnnotationUseSiteTarget?
}
