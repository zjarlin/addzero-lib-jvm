package site.addzero.kcloud.jimmer.model.entity.base

import org.babyfish.jimmer.sql.MappedSuperclass
import java.time.Instant

@MappedSuperclass
interface CreatedTime {
    /**
     * 创建时间
     */
    val createTime: Instant
}
