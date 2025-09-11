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
package com.addzero.kld.processing

import com.addzero.kld.symbol.KLAnnotated
import com.addzero.kld.symbol.KLAnnotation
import com.addzero.kld.symbol.KLClassDeclaration
import com.addzero.kld.symbol.KLDeclaration
import com.addzero.kld.symbol.KLDeclarationContainer
import com.addzero.kld.symbol.KLFile
import com.addzero.kld.symbol.KLFunctionDeclaration
import com.addzero.kld.symbol.KLName
import com.addzero.kld.symbol.KLPropertyAccessor
import com.addzero.kld.symbol.KLPropertyDeclaration
import com.addzero.kld.symbol.KLType
import com.addzero.kld.symbol.KLTypeArgument
import com.addzero.kld.symbol.KLTypeReference
import com.addzero.kld.symbol.Modifier
import com.addzero.kld.symbol.Variance

/**
 * [KLResolver] provides [SymbolProcessor] with access to compiler details such as Symbols.
 */
interface KLResolver {
    /**
     * Get all new files in the module / compilation unit.
     *
     * @return new files generated from last last round of processing in the module.
     */
    fun getNewFiles(): Sequence<KLFile>

    /**
     * Get all files in the module / compilation unit.
     *
     * @return all input files including generated files from previous rounds, note when incremental is enabled, only dirty files up for processing will be returned.
     */
    fun getAllFiles(): Sequence<KLFile>

    /**
     * Get all symbols with specified annotation in the current compilation unit.
     * Note that in multiple round processing, only symbols from deferred symbols of last round and symbols from newly generated files will be returned in this function.
     *
     * @param annotationName is the fully qualified name of the annotation; using '.' as separator.
     * @param inDepth whether to check symbols in depth, i.e. check symbols from local declarations. Operation can be expensive if true.
     * @return Elements annotated with the specified annotation.
     *
     * @see getDeclarationsFromPackage to get declarations outside the current compilation unit.
     */
    fun getSymbolsWithAnnotation(annotationName: String, inDepth: Boolean = false): Sequence<KLAnnotated>

    /**
     * Find a class in the compilation classpath for the given name.
     *
     * This returns the exact platform class when given a platform name. Note that java.lang.String isn't compatible
     * with kotlin.String in the type system. Therefore, processors need to use mapJavaNameToKotlin() and mapKotlinNameToJava()
     * explicitly to find the corresponding class names before calling getClassDeclarationByName if type checking
     * is needed for the classes loaded by this.
     *
     * This behavior is limited to getClassDeclarationByName; When processors get a class or type from a Java source
     * file, the conversion is done automatically. E.g., a java.lang.String in a Java source file is loaded as
     * kotlin.String in KSP.
     *
     * @param name fully qualified name of the class to be loaded; using '.' as separator.
     * @return a KLClassDeclaration, or null if not found.
     */
    fun getClassDeclarationByName(name: KLName): KLClassDeclaration?

    /**
     * Find functions in the compilation classpath for the given name.
     *
     * @param name fully qualified name of the function to be loaded; using '.' as separator.
     * @param includeTopLevel a boolean value indicate if top level functions should be searched. Default false. Note if top level functions are included, this operation can be expensive.
     * @return a Sequence of KLFunctionDeclaration
     */
    fun getFunctionDeclarationsByName(name: KLName, includeTopLevel: Boolean = false): Sequence<KLFunctionDeclaration>

    /**
     * Find a property in the compilation classpath for the given name.
     *
     * @param name fully qualified name of the property to be loaded; using '.' as separator.
     * @param includeTopLevel a boolean value indicate if top level properties should be searched. Default false. Note if top level properties are included, this operation can be expensive.
     * @return a KLPropertyDeclaration, or null if not found.
     */
    fun getPropertyDeclarationByName(name: KLName, includeTopLevel: Boolean = false): KLPropertyDeclaration?

    /**
     * Compose a type argument out of a type reference and a variance
     *
     * @param typeRef a type reference to be used in type argument
     * @param variance specifies a use-site variance
     * @return a type argument with use-site variance
     */
    fun getTypeArgument(typeRef: KLTypeReference, variance: Variance): KLTypeArgument

    /**
     * Get a [KLName] from a String.
     */
    fun getKLNameFromString(name: String): KLName

    /**
     * Create a [KLTypeReference] from a [KLType]
     */
    fun createKLTypeReferenceFromKLType(type: KLType): KLTypeReference

    /**
     * Provides built in types for convenience. For example, [KSBuiltins.anyType] is the KLType instance for class 'kotlin.Any'.
     */
    val builtIns: KLBuiltIns

    /**
     * map a declaration to jvm signature.
     * This function might fail due to resolution error, in case of error, null is returned.
     * Resolution error could be caused by bad code that could not be resolved by compiler, or KSP bugs.
     * If you believe your code is correct, please file a bug at https://github.com/google/ksp/issues/new
     */
    fun mapToJvmSignature(declaration: KLDeclaration): String?

    /**
     * @param overrider the candidate overriding declaration being checked.
     * @param overridee the candidate overridden declaration being checked.
     * @return boolean value indicating whether [overrider] overrides [overridee]
     * Calling [overrides] is expensive and should be avoided if possible.
     */
    fun overrides(overrider: KLDeclaration, overridee: KLDeclaration): Boolean

    /**
     * @param overrider the candidate overriding declaration being checked.
     * @param overridee the candidate overridden declaration being checked.
     * @param containingClass the containing class of candidate overriding and overridden declaration being checked.
     * @return boolean value indicating whether [overrider] overrides [overridee]
     * Calling [overrides] is expensive and should be avoided if possible.
     */
    fun overrides(overrider: KLDeclaration, overridee: KLDeclaration, containingClass: KLClassDeclaration): Boolean

    /**
     * Returns the jvm name of the given function.
     * This function might fail due to resolution error, in case of error, null is returned.
     * Resolution error could be caused by bad code that could not be resolved by compiler, or KSP bugs.
     * If you believe your code is correct, please file a bug at https://github.com/google/ksp/issues/new
     *
     * The jvm name of a function might depend on the Kotlin Compiler version hence it is not guaranteed to be
     * compatible between different compiler versions except for the rules outlined in the Java interoperability
     * documentation: https://kotlinlang.org/docs/reference/java-to-kotlin-interop.html.
     *
     * If the [declaration] is annotated with [JvmName], that name will be returned from this function.
     *
     * Note that this might be different from the name declared in the Kotlin source code in two cases:
     * a) If the function receives or returns an inline class, its name will be mangled according to
     * https://kotlinlang.org/docs/reference/inline-classes.html#mangling.
     * b) If the function is declared as internal, it will include a suffix with the module name.
     *
     * NOTE: As inline classes are an experimental feature, the result of this function might change based on the
     * kotlin version used in the project.
     */
    fun getJvmName(declaration: KLFunctionDeclaration): String?

    /**
     * Returns the jvm name of the given property accessor.
     * This function might fail due to resolution error, in case of error, null is returned.
     * Resolution error could be caused by bad code that could not be resolved by compiler, or KSP bugs.
     * If you believe your code is correct, please file a bug at https://github.com/google/ksp/issues/new
     *
     * The jvm name of an accessor might depend on the Kotlin Compiler version hence it is not guaranteed to be
     * compatible between different compiler versions except for the rules outlined in the Java interoperability
     * documentation: https://kotlinlang.org/docs/reference/java-to-kotlin-interop.html.
     *
     * If the [accessor] is annotated with [JvmName], that name will be returned from this function.
     *
     * By default, this name will match the name calculated according to
     * https://kotlinlang.org/docs/reference/java-to-kotlin-interop.html#properties.
     * Note that the result of this function might be different from that name in two cases:
     * a) If the property's type is an internal class, accessor's name will be mangled according to
     * https://kotlinlang.org/docs/reference/inline-classes.html#mangling.
     * b) If the function is declared as internal, it will include a suffix with the module name.
     *
     * NOTE: As inline classes are an experimental feature, the result of this function might change based on the
     * kotlin version used in the project.
     * see: https://kotlinlang.org/docs/reference/java-to-kotlin-interop.html#properties
     */
    fun getJvmName(accessor: KLPropertyAccessor): String?

    /**
     * Returns the [binary class name](https://asm.ow2.io/javadoc/org/objectweb/asm/Type.html#getClassName()) of the
     * owner class in JVM for the given [KLPropertyDeclaration].
     *
     * For properties declared in classes / interfaces; this value is the binary class name of the declaring class.
     *
     * For top level properties, this is the binary class name of the synthetic class that is generated for the Kotlin
     * file.
     * see: https://kotlinlang.org/docs/java-to-kotlin-interop.html#package-level-functions
     *
     * Note that, for properties declared in companion objects, the returned owner class will be the Companion class.
     * see: https://kotlinlang.org/docs/java-to-kotlin-interop.html#static-methods
     */
    fun getOwnerJvmClassName(declaration: KLPropertyDeclaration): String?

    /**
     * Returns the [binary class name](https://asm.ow2.io/javadoc/org/objectweb/asm/Type.html#getClassName()) of the
     * owner class in JVM for the given [KLFunctionDeclaration].
     *
     * For functions declared in classes / interfaces; this value is the binary class name of the declaring class.
     *
     * For top level functions, this is the binary class name of the synthetic class that is generated for the Kotlin
     * file.
     * see: https://kotlinlang.org/docs/java-to-kotlin-interop.html#package-level-functions
     *
     * Note that, for functions declared in companion objects, the returned owner class will be the Companion class.
     * see: https://kotlinlang.org/docs/java-to-kotlin-interop.html#static-methods
     */

    fun getOwnerJvmClassName(declaration: KLFunctionDeclaration): String?

    /**
     * Returns checked exceptions declared in a function's header.
     * @return A sequence of [KLType] declared in `throws` statement for a Java method or in @Throws annotation for a Kotlin function.
     * Checked exceptions from class files are not supported yet, an empty sequence will be returned instead.
     */

    fun getJvmCheckedException(function: KLFunctionDeclaration): Sequence<KLType>

    /**
     * Returns checked exceptions declared in a property accessor's header.
     * @return A sequence of [KLType] declared @Throws annotation for a Kotlin property accessor.
     * Checked exceptions from class files are not supported yet, an empty sequence will be returned instead.
     */

    fun getJvmCheckedException(accessor: KLPropertyAccessor): Sequence<KLType>

    /**
     * Returns declarations with the given package name.
     *
     * getDeclarationsFromPackage looks for declaration in the whole classpath, including dependencies.
     *
     * @param packageName the package name to look up.
     * @return A sequence of [KLDeclaration] with matching package name.
     * This will return declarations from both dependencies and source.
     */

    fun getDeclarationsFromPackage(packageName: String): Sequence<KLDeclaration>

    /**
     * Returns the corresponding Kotlin class with the given Java class.
     *
     * E.g.
     * java.lang.String -> kotlin.String
     * java.lang.Integer -> kotlin.Int
     * java.util.List -> kotlin.List
     * java.util.Map.Entry -> kotlin.Map.Entry
     * java.lang.Void -> null
     *
     * @param javaName a Java class name
     * @return corresponding Kotlin class name or null
     */

    fun mapJavaNameToKotlin(javaName: KLName): KLName?

    /**
     * Returns the corresponding Java class with the given Kotlin class.
     *
     * E.g.
     * kotlin.Throwable -> java.lang.Throwable
     * kotlin.Int -> java.lang.Integer
     * kotlin.Nothing -> java.lang.Void
     * kotlin.IntArray -> null
     *
     * @param kotlinName a Java class name
     * @return corresponding Java class name or null
     */

    fun mapKotlinNameToJava(kotlinName: KLName): KLName?

    /**
     * Same as KLDeclarationContainer.declarations, but sorted by declaration order in the source.
     *
     * Note that this is SLOW. AVOID IF POSSIBLE.
     */

    fun getDeclarationsInSourceOrder(container: KLDeclarationContainer): Sequence<KLDeclaration>

    /**
     * Returns a set of effective Java modifiers, if declaration is being / was generated to Java bytecode.
     */

    fun effectiveJavaModifiers(declaration: KLDeclaration): Set<Modifier>

    /**
     * Compute the corresponding Java wildcard, from the given reference.
     *
     * @param reference the reference to the type usage
     * @return an equivalent type reference from the Java wildcard's point of view
     */

    fun getJavaWildcard(reference: KLTypeReference): KLTypeReference

    /**
     * Tests a type if it was declared as legacy "raw" type in Java - a type with its type arguments fully omitted.
     *
     * @param type a type to check.
     * @return True if the type is a "raw" type.
     */

    fun isJavaRawType(type: KLType): Boolean

    /**
     * Returns annotations applied in package-info.java (if applicable) for given package name.
     *
     * @param packageName package name to check.
     * @return a sequence of KLAnnotations applied in corresponding package-info.java file.
     */

    fun getPackageAnnotations(packageName: String): Sequence<KLAnnotation>

    /**
     * Returns name of packages with given annotation.
     *
     * @param annotationName name of the annotation to be queried.
     * @return a sequence of package names with corresponding annotation name.
     */

    fun getPackagesWithAnnotation(annotationName: String): Sequence<String>

    /**
     * @return the name of the kotlin module this resolver is running on.
     */

    fun getModuleName(): KLName
}
