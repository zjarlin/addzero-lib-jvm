package site.addzero.kcloud.jimmer.interceptor

import org.babyfish.jimmer.kt.isLoaded
import org.babyfish.jimmer.sql.DraftInterceptor
import org.koin.core.annotation.Single
import site.addzero.kcloud.jimmer.model.entity.base.CreatedTime
import site.addzero.kcloud.jimmer.model.entity.base.CreatedTimeDraft
import java.time.Instant

@Single
class CreateTimeInterceptor : DraftInterceptor<CreatedTime, CreatedTimeDraft> {

    override fun beforeSave(draft: CreatedTimeDraft, original: CreatedTime?) {

        if (isLoaded(draft, CreatedTime::createTime)) {
            throw IllegalStateException("不允许手动设置 createTime 字段")
        }
        draft.createTime = Instant.now()
    }
}
