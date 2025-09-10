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
 * A value argument to function / constructor calls.
 *
 * Currently, only appears in annotation arguments.
 */
interface KSValueArgument : com.google.devtools.ksp.com.addzero.kld.symbol.KSAnnotated {
    /**
     * The name for the named argument, or null otherwise.
     *
     * For example, in `ignore(name=123456)`, the name value is "name"
     */
    val name: com.google.devtools.ksp.com.addzero.kld.symbol.KSName?

    /**
     * True if it is a spread argument (i.e., has a "*" in front of the argument).
     */
    val isSpread: Boolean

    /**
     * The value of the argument.
     *
     * Can be of one of the possible types:
     *
     * * [Boolean];
     * * [Byte];
     * * [Char];
     * * [Short];
     * * [Int];
     * * [Long];
     * * [Float];
     * * [Double];
     * * [String];
     * * [com.google.devtools.ksp.com.addzero.kld.symbol.KSType] for annotation arguments of type [kotlin.reflect.KClass];
     * * [com.google.devtools.ksp.com.addzero.kld.symbol.KSClassDeclaration] for annotation arguments of type [Enum] (in this case[com.google.devtools.ksp.com.addzero.kld.symbol.KSClassDeclaration.classKind]
     *   equals to [com.google.devtools.ksp.com.addzero.kld.symbol.ClassKind.ENUM_CLASS]);
     * * [com.google.devtools.ksp.com.addzero.kld.symbol.KSAnnotation] for embedded annotation arguments;
     * * [Array] of a possible type listed above.
     */
    val value: Any?
}
