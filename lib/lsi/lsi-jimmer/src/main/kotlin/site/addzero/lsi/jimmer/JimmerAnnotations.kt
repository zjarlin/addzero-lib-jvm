package site.addzero.lsi.jimmer

const val ENTITY = "org.babyfish.jimmer.sql.Entity"
const val MAPPED_SUPERCLASS = "org.babyfish.jimmer.sql.MappedSuperclass"
const val EMBEDDABLE = "org.babyfish.jimmer.sql.Embeddable"
const val IMMUTABLE = "org.babyfish.jimmer.Immutable"

const val ID = "org.babyfish.jimmer.sql.Id"
const val VERSION = "org.babyfish.jimmer.sql.Version"
const val LOGICAL_DELETED = "org.babyfish.jimmer.sql.LogicalDeleted"
const val KEY = "org.babyfish.jimmer.sql.Key"

const val MANY_TO_ONE = "org.babyfish.jimmer.sql.ManyToOne"
const val ONE_TO_ONE = "org.babyfish.jimmer.sql.OneToOne"
const val ONE_TO_MANY = "org.babyfish.jimmer.sql.OneToMany"
const val MANY_TO_MANY = "org.babyfish.jimmer.sql.ManyToMany"

const val ID_VIEW = "org.babyfish.jimmer.sql.IdView"
const val MANY_TO_MANY_VIEW = "org.babyfish.jimmer.sql.ManyToManyView"

const val FORMULA = "org.babyfish.jimmer.Formula"
const val TRANSIENT = "org.babyfish.jimmer.sql.Transient"

val ALL_JIMMER_ENTITY_ANNOTATIONS = setOf(ENTITY, MAPPED_SUPERCLASS, EMBEDDABLE, IMMUTABLE)
val ALL_JIMMER_ASSOCIATION_ANNOTATIONS = setOf(MANY_TO_ONE, ONE_TO_ONE, ONE_TO_MANY, MANY_TO_MANY)
