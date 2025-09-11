package com.addzero.kld.symbol

interface KLDefNonNullReference : com.addzero.kld.symbol.KLReferenceElement {
    /**
     * Enclosed reference element of the Definitely non null type.
     * For a reference of `T & Any`, this returns `T`.
     */
    val enclosedType: com.addzero.kld.symbol.KLClassifierReference

    override fun <D, R> accept(visitor: com.addzero.kld.symbol.KLVisitor<D, R>, data: D): R {
        return visitor.visitDefNonNullReference(this, data)
    }
}
