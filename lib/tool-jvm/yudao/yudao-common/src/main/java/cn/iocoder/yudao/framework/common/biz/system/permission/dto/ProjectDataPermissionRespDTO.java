package cn.iocoder.yudao.framework.common.biz.system.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Schema(description = "公共服务 - 项目的数据权限 Response DTO")
@Data
public class ProjectDataPermissionRespDTO {

    @Schema(description = "是否可查看全部数据", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean all;

    @Schema(description = "可查看的项目编号数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 3]")
    private Set<Long> projectIds;

    public ProjectDataPermissionRespDTO() {
        this.all = false;
        this.projectIds = new HashSet<>();
    }

}
