package site.addzero.model.common

import site.addzero.model.entity.SysUser
import org.babyfish.jimmer.sql.*

@MappedSuperclass
interface BaseEntity : BaseDateTime {
    @Id
    @GeneratedValue(generatorType = SnowflakeIdGenerator::class)
    val id: Long

    @ManyToOne
    @OnDissociate(DissociateAction.DELETE)
    @JoinColumn(foreignKeyType = ForeignKeyType.FAKE, name = "update_by")
    val updateBy: SysUser?

    @ManyToOne
    @JoinColumn(foreignKeyType = ForeignKeyType.FAKE, name = "create_by")
    @OnDissociate(DissociateAction.DELETE)
    val createBy: SysUser?
}
