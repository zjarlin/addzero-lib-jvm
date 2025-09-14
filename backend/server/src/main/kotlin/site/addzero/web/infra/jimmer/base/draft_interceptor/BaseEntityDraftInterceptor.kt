package site.addzero.web.infra.jimmer.base.draft_interceptor

import cn.hutool.core.util.ObjUtil
import site.addzero.common.kt_util.isNull
import site.addzero.common.util.LoginUtil
import site.addzero.model.common.BaseEntity
import site.addzero.model.common.BaseEntityDraft
import org.babyfish.jimmer.kt.isLoaded
import org.babyfish.jimmer.kt.unload
import org.babyfish.jimmer.sql.DraftInterceptor
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class BaseEntityDraftInterceptor : DraftInterceptor<BaseEntity, BaseEntityDraft> {


    override fun beforeSave(draft: BaseEntityDraft, original: BaseEntity?) {
        val loginUserId = LoginUtil.getLoginUserId()
        val now = LocalDateTime.now()

        val loaded = isLoaded(draft, BaseEntityDraft::id)
        val empty = ObjUtil.isEmpty(draft.id)
        val bool = loaded && empty
        if (bool) {
            unload(draft, BaseEntityDraft::id)
        }

        if (!isLoaded(draft, BaseEntityDraft::updateTime) || draft.updateTime.isNull()) {
            draft.updateTime = now
        }

        if (!isLoaded(draft, BaseEntityDraft::updateBy) || draft.updateBy.isNull()) {
            draft.updateBy {
                id = loginUserId
            }
        }

        if (original === null) {
            draft.createTime = now
            if (!isLoaded(draft, BaseEntityDraft::createBy) || draft.createBy.isNull()) {
                draft.createBy {
                    id = loginUserId
                }
            }
        }
    }
}
