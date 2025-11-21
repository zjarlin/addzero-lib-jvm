package site.addzero.mybatis.mputil

import cn.hutool.core.annotation.AnnotationUtil
import cn.hutool.core.bean.BeanUtil.copyProperties
import cn.hutool.core.bean.copier.CopyOptions
import cn.hutool.core.util.NumberUtil
import cn.hutool.core.util.ReflectUtil
import com.baomidou.mybatisplus.extension.service.IService
import site.addzero.mybatis.auto_wrapper.AutoWhereUtil
import site.addzero.mybatis.auto_wrapper.Where
import java.lang.reflect.Field
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors

/**
 * 级联操作
 * p是主表c是子表
 *
 * @author zjarlin
 * @since 2023/2/26 09:18
 */
//@Component
class MpUtil1<P> @JvmOverloads constructor(private val ps: IService<P>, val idName: String = "id") {

    /**
     * 前端和数据库的差集和交集
     *
     * @param collection 入参
     * @return [Pair]<[List]<[P]>, [List]<[P]>>
     * @author zjarlin
     * @since 2023/12/18
     */
    fun diffAndInter(collection: MutableCollection<P>): Pair<MutableList<P>, MutableList<P>> {
        val byAnno = this.isByAnno
        return diffAndInter(collection, byAnno)
    }

    fun voidcompareSaveOrUpdate(p: P): P? {
        if (p == null) return null

        val list = mutableListOf<P>(p)
        val listListBooleanTriple = voidcompareSaveOrUpdate(list)
        val left = listListBooleanTriple.first
        val middle = listListBooleanTriple.second
        if (middle != null && middle.isNotEmpty()) {
            val p1 = middle[0]
            return p1
        }
        if (left != null && left.isNotEmpty()) {
            val p1 = left[0]
            return p1
        }
        return p
    }

    fun voidcompareSaveOrUpdate(collection: MutableCollection<P>): Triple<MutableList<P>?, MutableList<out P?>?, Boolean> {
        if (collection.isEmpty()) {
            val listListBooleanTriple = Triple(null, null, false)
            return listListBooleanTriple
        }

        val listListPair = diffAndInter(collection)

        val left = listListPair.first

        var b = false
        if (left.isNotEmpty()) {
            left.forEach {
                val javaClass = it?.javaClass
                val field = ReflectUtil.getField(javaClass, idName)
                field?.isAccessible = true
                ReflectUtil.setFieldValue(it, field, null)
            }
            b = ps.saveBatch(left)
        }

        val intersection = listListPair.second


        if (intersection.isEmpty()) {
            val listListBooleanTriple = Triple(left, mutableListOf<P?>(), b)
            return listListBooleanTriple
        }

        var b1 = false
        val intersectionSize = intersection.size
        val voSize = collection.size

        if (intersectionSize <= voSize) {
            b1 = ps.updateBatchById(intersection)
        } else {
            //首先判断是不是命中全部了,命中全部说明唯一注解加的有问题,导致全表查了
            val count = ps.count()
            val equals: Boolean = NumberUtil.equals(count, intersectionSize)
            if (equals) {
                throw RuntimeException("请检查唯一性校验注解,查询出交集应当修改的行数大于输入的行数,可能会误修改数据,因此中断更新!")
            }
            //融合数据,todo 联表融合
            //检查当前表上是否有依赖关系,有的话依赖关系也要融合
//            result = handleMerge(collection, intersection);
        }

        val listListBooleanTriple = Triple(left, intersection, b && b1)
        return listListBooleanTriple
    }

    private val isByAnno: Boolean
        get() {
            val entityClass = ps.getEntityClass()
            val fields: Array<Field> = ReflectUtil.getFields(entityClass)
            val byAnno = Arrays.stream<Field>(fields).anyMatch {
                    AnnotationUtil.hasAnnotation(it, Where::class.java)
                }
            return byAnno
        }

    /**
     * 前端和数据库的差集和交集
     *
     * @param collection 入参
     * @return [Pair]<[List]<[P]>, [List]<[P]>>
     * @author zjarlin
     * @since 2023/12/18
     */
    fun diffPairAndInter(collection: MutableCollection<P>): Pair<MutableList<P>, MutableList<Pair<P, P>>> {
        val byAnno = this.isByAnno

        return diffPairAndInter(collection, byAnno)
    }

    fun diffPairAndInter(
        collection: MutableCollection<P>, byAnno: Boolean
    ): Pair<MutableList<P>, MutableList<Pair<P, P>>> {
        val ret: Pair<MutableList<P>, MutableList<Pair<P, P>>> = Pair(ArrayList(), ArrayList())
        if (collection.isEmpty()) {
            return ret
        }
        val aClass = ps.getEntityClass()

        collection.forEach(Consumer { e: P ->
//            根据实体中所有除了id以外的字段查一遍
            val pLambdaQueryWrapper =
                if (byAnno) AutoWhereUtil.lambdaQueryByAnnotation(aClass, e) else AutoWhereUtil.lambdaQueryByField(
                    aClass, e, true
                )
            val list = ps.list(pLambdaQueryWrapper)
            //查不到说明是差集(数据库不存在的)
            if (list == null || list.isEmpty()) {
                ret.first.add(e)
            } else {
//拿交集,但是以传过来的数据为主
                val copyOptions = CopyOptions.create()
                copyOptions.setPropertiesFilter({ field, value ->
                    val notId: Boolean = !idName.equals(field.name)
                    val notEmpty: Boolean = value != null && value.toString().isNotEmpty()
                    notId && notEmpty
                })
                val collect = list.stream().map { sjk -> Pair(e, sjk) }.collect(
                    Collectors.toList()
                )
                ret.second.addAll(collect)
            }
        })


        return ret
    }

    /**
     * 前端和数据库的差集和交集
     *
     * @param collection 入参
     * @return [Pair]<[List]<[P]>, [List]<[P]>>
     * @author zjarlin
     * @since 2023/12/18
     */
    fun diffAndInter(collection: MutableCollection<P>, byAnno: Boolean): Pair<MutableList<P>, MutableList<P>> {
        val ret = Pair(mutableListOf<P>(), mutableListOf<P>())
        if (collection.isEmpty()) {
            return ret
        }
        val aClass = ps.getEntityClass()

        collection.forEach(Consumer { e: P ->
//            根据实体中所有除了id以外的字段查一遍
            val pLambdaQueryWrapper =
                if (byAnno) AutoWhereUtil.lambdaQueryByAnnotation(aClass, e) else AutoWhereUtil.lambdaQueryByField(
                    aClass, e, true
                )
            val list = ps.list(pLambdaQueryWrapper)
            if (list == null || list.isEmpty()) {
                ret.first.add(e)
            } else {
                val copyOptions: CopyOptions = CopyOptions.create()
                copyOptions.setPropertiesFilter({ field, value ->
                    val notId: Boolean = idName != field.name
                    val notEmpty: Boolean = value != null && value.toString().isNotEmpty()
                    notId && notEmpty
                })
                val collect = list.stream().peek {
                    copyProperties(e, it, copyOptions)
                }.collect(
                    Collectors.toList()
                )
                ret.second.addAll(collect)
            }
        })
        return ret
    }

    /**
     * 只要数据库不存在的
     *
     * @param collection
     */
    fun checkExists(collection: MutableCollection<P>): MutableCollection<P> {
        val byAnno = this.isByAnno
        return checkExists(collection, byAnno)
    }


    //    private JSONObject diffJsonObj(P 前端传的1, P sjk1) {
    //        Class<P> aClass = (Class<P>) sjk1.getClass();
    //        Field[] declaredFields = aClass.getDeclaredFields();
    //        diffJsonObj
    //
    //        return null;
    //    }
    /**
     * 只要数据库不存在的
     */
    fun checkExists(collection: MutableCollection<P>, byAnno: Boolean): MutableCollection<P> {
        if (collection.isEmpty()) {
            return ArrayList<P>()
        }
        val aClass = ps.getEntityClass()
        val collect = collection.stream().filter {
//            根据实体中所有除了id以外的字段查一遍
            val pLambdaQueryWrapper =
                if (byAnno) AutoWhereUtil.lambdaQueryByAnnotation(aClass, it) else AutoWhereUtil.lambdaQueryByField(
                    aClass, it, true
                )
            val count = ps.count(pLambdaQueryWrapper)
            count <= 0
        }.collect(Collectors.toSet())
        return collect
    }

    companion object {
        fun <P> of(ps: IService<P>): MpUtil1<P> {
            return MpUtil1<P>(ps)
        }
    }
}
