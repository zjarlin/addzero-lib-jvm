package site.addzero.kcloud.jimmer.interceptor

import org.babyfish.jimmer.kt.isLoaded
import org.babyfish.jimmer.sql.DraftInterceptor
import org.koin.core.annotation.Single
import site.addzero.kcloud.jimmer.model.entity.base.UpdateTime
import site.addzero.kcloud.jimmer.model.entity.base.UpdateTimeDraft
import java.time.Instant

@Single
class UpdateTimeInterceptor : DraftInterceptor<UpdateTime, UpdateTimeDraft> {

    override fun beforeSave(draft: UpdateTimeDraft, original: UpdateTime?) {

        if (isLoaded(draft, UpdateTime::updateTime)) {
            throw IllegalStateException("不允许手动设置 updateTime 字段")
        }
        draft.updateTime = Instant.now()
    }
}
