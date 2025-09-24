package site.addzero.model.entity.biz_device

import site.addzero.entity2form.annotation.LabelProp
import site.addzero.model.common.BaseEntity
import org.babyfish.jimmer.sql.*

/**
 * 物模型属性实体类，属于物模型的一部分
 */
@Entity
@Table(name = "thing_model_property")
interface ThingModelProperty : BaseEntity {

    /**
     * 所属物模型
     */
    @ManyToOne
    val thingModel: ThingModel?

    /**
     * 属性标识
     */
    @Key
    val identifier: String

    /**
     * 属性名称
     */
    @Key
    @LabelProp
    val name: String

    /**
     * 数据类型，例如：int32, float, double, string, bool, enum 等
     */
    @Key
    val dataType: String

    /**
     * 数据范围，根据数据类型可能表示数值范围或枚举值
     */
    val dataSpecs: String?

    /**
     * 精度值设置，对数值类型有效
     */
    val dataPrecision: Int?

    /**
     * 读取方式：读、写、上报
     */
    @Key
    val accessMode: String
}
