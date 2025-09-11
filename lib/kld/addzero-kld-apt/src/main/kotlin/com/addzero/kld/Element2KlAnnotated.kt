package com.addzero.kld

import com.addzero.kld.symbol.KLAnnotated
import com.addzero.kld.symbol.KLAnnotation
import com.addzero.kld.symbol.KLNode
import com.addzero.kld.symbol.KLVisitor
import com.addzero.kld.symbol.Location
import com.addzero.kld.symbol.Origin
import javax.lang.model.element.Element

fun Element.toKld(): KLAnnotated {
    val annotated = object : KLAnnotated {
        override val annotations: Sequence<KLAnnotation>
            get() = TODO("Not yet implemented")
        override val origin: Origin
            get() = TODO("Not yet implemented")
        override val location: Location
            get() = TODO("Not yet implemented")
        override val parent: KLNode?
            get() = TODO("Not yet implemented")

        override fun <D, R> accept(visitor: KLVisitor<D, R>, data: D): R {
            TODO("Not yet implemented")
        }

    }
    return annotated
}
