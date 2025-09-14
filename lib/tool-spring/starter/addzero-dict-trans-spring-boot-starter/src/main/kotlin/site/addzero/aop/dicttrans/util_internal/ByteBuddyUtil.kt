package site.addzero.aop.dicttrans.util_internal

import cn.hutool.core.bean.BeanUtil
import cn.hutool.core.collection.CollUtil
import cn.hutool.core.util.ArrayUtil
import cn.hutool.core.util.ReflectUtil
import site.addzero.aop.dicttrans.dictaop.entity.NeedAddInfo
import net.bytebuddy.ByteBuddy
import net.bytebuddy.dynamic.DynamicType
import java.lang.reflect.Field
import java.lang.reflect.Type
import java.util.*
import java.util.function.Function

/**
 * 字节码工具类
 *
 * @author zjarlin
 * @since 2023/01/12
 */
internal class ByteBuddyUtil {


    companion object {
        fun 判断能不能跳出去(o: Any, getNeedAddInfoFun: Function<Any, MutableList<NeedAddInfo>>): Boolean {
            val fields = ReflectUtil.getFields(o.javaClass)
            val b = Arrays.stream<Field>(fields).anyMatch { field: Field ->
                val objectField: Boolean = RefUtil.isObjectField(o, field)
                val collectionField: Boolean = RefUtil.isCollectionField(field)
                val apply = getNeedAddInfoFun.apply(o)
                val b1 = apply.size == 0
                objectField || collectionField || b1
            }
            return !b
        }

        /**
         * 只要有一个集合或者字段就不能跳出去
         *
         * @param o 入参
         * @return boolean
         * @author zjarlin
         * @since 2023/12/28
         */
        fun canNotSkipTrans(o: Any): Boolean {
            val fields = ReflectUtil.getFields(o.javaClass)
            val b = Arrays.stream<Field>(fields).anyMatch { f: Field ->
                val objectField = RefUtil.isObjectField(o, f)
                val objectField1: Boolean = RefUtil.isCollectionField(f)
                objectField1 || objectField
            }
            return b
        }

        fun genChildObjectRecursion(
            o: Any?, getNeedAddInfoFun: Function<Any,
                    MutableList<NeedAddInfo>>
        ): Any? {
            o ?: return o
            //非T类型比如String Integer
            val t = !RefUtil.isT(o)
            if (t) {
                return o
            }

            val needAddFields = getNeedAddInfoFun.apply(o)
            //        needAddFields = needAddFields.distinct();
//        needAddFields = Streams.unique(needAddFields, NeedAddInfo::getFieldName);
            //有一个实体或者集合就不能跳出去
            if (CollUtil.isEmpty(needAddFields) && !canNotSkipTrans(o)) {
                return o
            }
            val aClass = o.javaClass

            var subclass = try {
                ByteBuddy().subclass(aClass)
            } catch (e: Exception) {
                e.printStackTrace()
                error(e.message+"字节码生成失败,检查class 是否open,是否有无参构造")

            }

            for (e in needAddFields) {
                val fieldName = e.fieldName
                val type = e.type
                subclass = subclass.defineProperty(fieldName, type)
            }


            val loaded = subclass.make().load(aClass.classLoader).getLoaded()
            val o1 = loaded.newInstance()


            val fields = ReflectUtil.getFields(aClass)
            val collect = fields.filter { RefUtil.isNonNullField(o1, it) }
            val size = collect.size
            Arrays.stream<Field>(fields).forEach { e: Field ->
                val fieldValue = ReflectUtil.getFieldValue(o, e)
                if (RefUtil.isObjectField(o, e)) {
                    val afterObject = genChildObjectRecursion(fieldValue, getNeedAddInfoFun)
                    ReflectUtil.setFieldValue(o, e, afterObject)
                } else if (RefUtil.isCollectionField(e)) {
                    val fieldValue1 = fieldValue as MutableCollection<*>
                    if (CollUtil.isNotEmpty(fieldValue1)) {
                        val collect31 = fieldValue1
                            .map { genChildObjectRecursion(it, getNeedAddInfoFun) }

                        ReflectUtil.setFieldValue(o, e, collect31)
                    }
                }
            }
            BeanUtil.copyProperties(o, o1)

            return o1
        }


        fun genChildObjectRecursion(
            claz: Class<*>,
            getNeedAddInfoFun: (Any) -> MutableList<NeedAddInfo>
        ): Class<Any>? {
            val o = claz.newInstance()
            val o3 = genChildObjectRecursion(o, getNeedAddInfoFun)
            return o3?.javaClass
        }

        fun <T> genChildClassRecursion(
            tClass: Class<T>, getNeedAddInfoFun: Function<Class<*>, MutableList<NeedAddInfo>>
        ): Class<out T> {
            val needAddFields = getNeedAddInfoFun.apply(tClass)
            if (CollUtil.isEmpty(needAddFields)) {
                return tClass
            }

            var subclass = ByteBuddy().subclass(tClass)
            for (e in needAddFields) {
                val fieldName = e.fieldName
                val rootObject = e.rootObject
                var type= e.type
                val recur =e.recur
                val isT = e.isT
                if (recur == true && isT == true) {
                    val aClass: Class<*> = genChildClassRecursion(type, getNeedAddInfoFun)
                    type = aClass
                }
                subclass = subclass.defineProperty(fieldName, type)
            }

            return subclass.make().load(tClass.getClassLoader()).getLoaded()
        }

        fun <T> genChildClass(needAddFields: MutableList<String>, superClass: Class<T>): Class<out T> {
            return genChildClass<T>(needAddFields, superClass, String::class.java)
        }

        fun <T> genChildClass(needAddFields: MutableList<String>, superClass: Class<T>, type: Type): Class<out T> {
            if (CollUtil.isEmpty(needAddFields)) {
                return superClass
            }
            var subclass = ByteBuddy().subclass(superClass)
            val tOptional = null
            for (needAddField in needAddFields!!) {
                subclass = subclass.defineProperty(needAddField, type)
            }
            return subclass.withToString().make().load(superClass.getClassLoader()).getLoaded()
        }

        fun <T> genChildClassWithPair(
            needAddFields: MutableSet<Pair<String, out Class<*>>>, superClass: Class<T>
        ): Class<out T> {
            if (ArrayUtil.isEmpty(needAddFields)) {
                return superClass
            }
            var subclass: DynamicType.Builder<T> = ByteBuddy().subclass(superClass)
            for (item in needAddFields) {
                val key: String = item.first
                val value: Type = item.second
                subclass = subclass.defineProperty(key, value)
            }
            val loaded: Class<out T> = subclass.withToString() //                .withHashCodeEquals()
                .make().load(superClass.getClassLoader()).getLoaded()
            return loaded
        }

    }
}
