//package org.babyfish.jimmer.lsi.immutable
//
//import org.babyfish.jimmer.Formula
//import org.babyfish.jimmer.Immutable
//import org.babyfish.jimmer.ksp.immutable.generator.DRAFT
//import org.babyfish.jimmer.ksp.immutable.generator.FETCHER_DSL
//import org.babyfish.jimmer.ksp.immutable.generator.PROPS
//import org.babyfish.jimmer.lsi.MetaException
//import org.babyfish.jimmer.sql.Embeddable
//import org.babyfish.jimmer.sql.Entity
//import org.babyfish.jimmer.sql.Id
//import org.babyfish.jimmer.sql.MappedSuperclass
//import site.addzero.util.lsi.clazz.LsiClass
//import site.addzero.util.lsi.clazz.getArg
//import site.addzero.util.lsi.clazz.hasAnnotation
//import site.addzero.util.lsi.clazz.packageName
//import site.addzero.util.lsi.field.LsiField
//import site.addzero.util.lsi.field.getArg
//import kotlin.collections.iterator
//
//
//class ImmutableType(
//    val classDeclaration: LsiClass
//) {
////    private val immutableAnnoTypeName: String =
////        listOf(
////            classDeclaration.annotation(Entity::class),
////            classDeclaration.annotation(MappedSuperclass::class),
////            classDeclaration.annotation(Embeddable::class),
////            classDeclaration.annotation(Immutable::class)
////        ).filterNotNull().map {
////            it.annotationType.fastResolve().declaration.fullName
////        }.also {
////            if (it.size > 1) {
////                throw MetaException(
////                    classDeclaration,
////                    "Conflict annotations: $it"
////                )
////            }
////        }.first()
//
//    val isEntity = classDeclaration.hasAnnotation(Entity::class.qualifiedName ?: "")
//
//    val isMappedSuperclass = classDeclaration.hasAnnotation(MappedSuperclass::class.qualifiedName ?: "")
//
//    val isEmbeddable = classDeclaration.hasAnnotation(Embeddable::class.qualifiedName ?: "")
//
//    val isImmutable = classDeclaration.hasAnnotation(Immutable::class.qualifiedName ?: "")
//
//    val simpleName = classDeclaration.name
//
//
//    val propsClassName = classDeclaration.name + PROPS
//
//    val draftClassName = classDeclaration.name + DRAFT
//
//    val fetcherDslClassName = classDeclaration.name + FETCHER_DSL
//
//    fun draftClassName(vararg nestedNames: String) = classDeclaration.name + DRAFT
//
//    val sqlAnnotationType: KClass<out Annotation>? = run {
//        var annotationType: KClass<out Annotation>? = null
//        for (sqlAnnotationType in SQL_ANNOTATION_TYPES) {
//            classDeclaration.annotation(sqlAnnotationType)?.let {
//                if (annotationType != null) {
//                    throw MetaException(
//                        classDeclaration,
//                        "it cannot be decorated by both " + "@${annotationType!!.qualifiedName} and ${sqlAnnotationType.qualifiedName}"
//                    )
//                }
//                annotationType = sqlAnnotationType
//            }
//        }
//        annotationType
//    }
//
//    val name
//        get() = classDeclaration.name
//
//    val packageName
//        get() = classDeclaration.packageName
//
//    val qualifiedName
//        get() = classDeclaration.qualifiedName
//
//    val isAcrossMicroServices = true == (classDeclaration.getArg("MappedSuperclass", "acrossMicroServices") ?: false)
//
//    val microServiceName: String = (
//            classDeclaration.getArg("Entity", "microServiceName") ?: classDeclaration.getArg(
//                "MappedSuperclass", "microServiceName"
//            ) ?: "").also {
//        if (it.isNotEmpty() && isAcrossMicroServices) {
//            throw RuntimeException(
//                "the `acrossMicroServices` of its annotation \"@" + MappedSuperclass::class.java.name + "\" is true so that `microServiceName` cannot be specified"
//            )
//        }
//    }
//    val superTypes = null
//    val primarySuperType = null
//    val declaredProperties: Map<String, LsiField>
//    val redefinedProps: Map<String, LsiField>
//
//    init {
//        val superPropMap =
//            superTypes.flatMap { it.properties.values }.groupBy { it.name }.toList().associateBy({ it.first }) {
//                if (it.second.size > 1) {
//                    val prop1 = it.second[0]
//                    val prop2 = it.second[1]
//                    if (prop1.propDeclaration.type.fastResolve() != prop2.propDeclaration.type.fastResolve()) {
//                        throw MetaException(
//                            classDeclaration,
//                            "There are two super properties with the same name: \"" + prop1 + "\" and \"" + prop2 + "\", but their return type are different"
//                        )
//                    }
//                }
//                it.second.first()
//            }
//
//        for (propDeclaration in classDeclaration.fields) {
//            val superProp = superPropMap[propDeclaration.name]
//            if (superProp != null) {
//                throw RuntimeException(
//                    "it overrides '$superProp', this is not allowed"
//                )
//            }
//            val formula = propDeclaration.getArg(Formula::class)
//            if (isEmbeddable && formula !== null && (formula[Formula::sql] ?: "").isNotEmpty()) {
//                throw RuntimeException(
//                    "The sql based formula property cannot be declared in embeddable type"
//                )
//            }
//            if (propDeclaration.isAbstract) {
//                if (formula !== null) {
//                    val sql = formula[Formula::sql] ?: ""
//                    if (sql.isEmpty()) {
//                        throw MetaException(
//                            propDeclaration,
//                            "it is abstract and decorated by @" + Formula::class.java.name + ", abstract modifier means simple calculation property based on " + "SQL expression so that the `sql` of that annotation must be specified"
//                        )
//                    }
//                    val dependencies = formula.getListArgument(Formula::dependencies) ?: emptyList()
//                    if (dependencies.isNotEmpty()) {
//                        throw MetaException(
//                            propDeclaration,
//                            "it is abstract and decorated by @" + Formula::class.java.name + ", abstract modifier means simple calculation property based on " + "SQL expression so that the `dependencies` of that annotation cannot be specified"
//                        )
//                    }
//                }
//            } else {
//                for (anno in propDeclaration.annotations) {
//                    if (anno.fullName.startsWith("org.babyfish.jimmer.") && anno.fullName != FORMULA_CLASS_NAME) {
//                        throw MetaException(
//                            propDeclaration,
//                            "it is not abstract so that " + "it cannot be decorated by " + "any jimmer annotations except @" + FORMULA_CLASS_NAME
//                        )
//                    }
//                    if (formula !== null) {
//                        formula[Formula::sql]?.takeIf { it.isNotEmpty() }?.let {
//                            throw MetaException(
//                                propDeclaration,
//                                "it is non-abstract and decorated by @" + Formula::class.java.name + ", non-abstract modifier means simple calculation property based on " + "kotlin expression so that the `sql` of that annotation cannot be specified"
//                            )
//                        }
//                        val dependencies = formula.getListArgument(Formula::dependencies) ?: emptyList()
//                        if (dependencies.isEmpty()) {
//                            throw RuntimeException(
//                                propDeclaration,
//                                "it is non-abstract and decorated by @" + Formula::class.java.name + ", non-abstract modifier means simple calculation property based on " + "kotlin expression so that the `dependencies` of that annotation must be specified"
//                            )
//                        }
//                    }
//                }
//            }
//        }
//
//        for (function in classDeclaration.methods) {
//            if (function.isAbstract) {
//                throw RuntimeException("only non-abstract function is acceptable")
//            }
//            for (anno in function.annotations) {
//                if (anno.qualifiedName.startsWith("org.babyfish.jimmer.")) {
//                    throw RuntimeException(
//                        "Non-abstract function cannot be decorated by any jimmer annotations"
//                    )
//                }
//            }
//        }
//
//        var propIdSequence = primarySuperType?.properties?.size ?: 0
//        redefinedProps = superPropMap.filterKeys {
//            primarySuperType == null || !primarySuperType.properties.containsKey(it)
//        }.mapValues {
//            ImmutableProp(ctx, this, propIdSequence++, it.value.propDeclaration)
//        }
//
//        declaredProperties = classDeclaration.getDeclaredProperties().filter { it.annotation(Id::class) != null }
//            .associateBy({ it.name }) {
//                ImmutableProp(ctx, this, propIdSequence++, it)
//            } + classDeclaration.getDeclaredProperties().filter { it.annotation(Id::class) == null }
//            .associateBy({ it.name }) {
//                ImmutableProp(ctx, this, propIdSequence++, it)
//            }
//    }
//
//    val properties: Map<String, LsiField> = if (superTypes.isEmpty()) {
//        declaredProperties
//    } else {
//        val map = mutableMapOf<String, LsiField>()
//        for (superType in superTypes) {
//            for ((name, prop) in superType.properties) {
//                if (prop.isId) {
//                    map[name] = prop
//                }
//            }
//        }
//        for ((name, prop) in redefinedProps) {
//            if (prop.isId) {
//                map[name] = prop
//            }
//        }
//        for ((name, prop) in declaredProperties) {
//            if (prop.isId) {
//                map[name] = prop
//            }
//        }
//        for (superType in superTypes) {
//            for ((name, prop) in superType.properties) {
//                if (!prop.isId) {
//                    map[name] = prop
//                }
//            }
//        }
//        for ((name, prop) in redefinedProps) {
//            if (!prop.isId) {
//                map[name] = prop
//            }
//        }
//        for ((name, prop) in declaredProperties) {
//            if (!prop.isId) {
//                map[name] = prop
//            }
//        }
//        map
//    }
//
//    private val idPropNameMap: Map<String, String> by lazy {
//        mutableMapOf<String, String>().also { map ->
//            for (prop in properties.values) {
//                val baseProp = prop.idViewBaseProp
//                if (baseProp !== null) {
//                    map[baseProp.name] = prop.name
//                }
//            }
//            for (prop in properties.values) {
//                if (prop.isReverse) {
//                    continue
//                }
//                if (prop.annotation(OneToOne::class) === null && prop.annotation(ManyToOne::class) === null) {
//                    continue
//                }
//                if (map.containsKey(prop.name)) {
//                    continue
//                }
//                val expectedPropName = "${prop.name}Id"
//                properties[expectedPropName]?.let {
//                    throw MetaException(
//                        it.propDeclaration,
//                        "It looks like @IdView of association \"${it}\", please add the @IdView annotation"
//                    )
//                }
//                map[prop.name] = expectedPropName
//            }
//        }
//    }
//
//    fun getIdPropName(prop: String): String? = idPropNameMap[prop]
//
//    val propsOrderById: List<LsiField> by lazy {
//        properties.values.sortedBy { it.id }
//    }
//
//    val idProp: ImmutableProp? by lazy {
//        val idProps = declaredProperties.values.filter { it.isId }
//        if (idProps.size > 1) {
//            throw MetaException(
//                classDeclaration, "two many properties are decorated by \"@${Id::class.qualifiedName}\": " + idProps
//            )
//        }
//        val superIdProp = superTypes.firstOrNull { it.idProp !== null }?.idProp
//        if (superIdProp != null && idProps.isNotEmpty()) {
//            throw MetaException(
//                classDeclaration,
//                "it cannot declare id property " + "because id property has been declared by super type"
//            )
//        }
//        val prop = idProps.firstOrNull() ?: superIdProp
//        if (prop == null && isEntity) {
//            throw MetaException(
//                classDeclaration,
//                "it is decorated by \"@${Entity::class.qualifiedName}\" " + "but there is no id property"
//            )
//        }
//        prop
//    }
//
//    val validationMessages: Map<ClassName, String> = parseValidationMessages(classDeclaration)
//
//    override fun toString(): String = classDeclaration.qualifiedName.toString()
//
//
//    companion object {
//
//        @JvmStatic
//        private val SQL_ANNOTATION_TYPES = setOf(Entity::class, MappedSuperclass::class, Embeddable::class)
//
//        @JvmStatic
//        private val FORMULA_CLASS_NAME = Formula::class.qualifiedName
//    }
//}
