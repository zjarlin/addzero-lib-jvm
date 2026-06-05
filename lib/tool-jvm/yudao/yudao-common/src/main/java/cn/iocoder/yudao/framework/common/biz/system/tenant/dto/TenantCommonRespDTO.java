package cn.iocoder.yudao.framework.common.biz.system.tenant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "公共服务 - 租户 Response DTO")
@Data
public class TenantCommonRespDTO {
    @Schema(description = "租户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "租户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
    private String name;

    private BigDecimal longitude;
    private BigDecimal latitude;
    private String address;
    private String screenName;
}
