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
 * A reference to a callable entity, such as a function or a property.
 */
interface KLCallableReference : com.addzero.kld.symbol.KLReferenceElement {
    /**
     * A reference to the type of its receiver.
     */
    val receiverType: com.addzero.kld.symbol.KLTypeReference?

    /**
     * Parameters to this callable.
     */
    val functionParameters: List<com.addzero.kld.symbol.KLValueParameter>

    /**
     * A reference to its return type.
     */
    val returnType: com.addzero.kld.symbol.KLTypeReference

    override fun <D, R> accept(visitor: com.addzero.kld.symbol.KLVisitor<D, R>, data: D): R {
        return visitor.visitCallableReference(this, data)
    }
}
