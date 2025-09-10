package com.google.devtools.ksp.com.addzero.kld.symbol

interface KSDefNonNullReference : com.google.devtools.ksp.com.addzero.kld.symbol.KSReferenceElement {
    /**
     * Enclosed reference element of the Definitely non null type.
     * For a reference of `T & Any`, this returns `T`.
     */
    val enclosedType: com.google.devtools.ksp.com.addzero.kld.symbol.KSClassifierReference

    override fun <D, R> accept(visitor: com.google.devtools.ksp.com.addzero.kld.symbol.KSVisitor<D, R>, data: D): R {
        return visitor.visitDefNonNullReference(this, data)
    }
}
