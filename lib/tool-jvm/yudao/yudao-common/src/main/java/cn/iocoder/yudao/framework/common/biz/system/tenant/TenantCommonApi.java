package cn.iocoder.yudao.framework.common.biz.system.tenant;

import cn.iocoder.yudao.framework.common.biz.system.tenant.dto.TenantCommonRespDTO;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "公共服务 - 多租户")
public interface TenantCommonApi {

    String PREFIX = "/system" + "/tenant";

    @GetMapping(PREFIX + "/id-list")
    @Operation(summary = "获得所有租户编号")
    CommonResult<List<Long>> getTenantIdList();

    @GetMapping(PREFIX + "/valid")
    @Operation(summary = "校验租户是否合法")
    @Parameter(name = "id", description = "租户编号", required = true, example = "1024")
    CommonResult<Boolean> validTenant(@RequestParam("id") Long id);

    @GetMapping(PREFIX + "/list")
    @Operation(summary = "获得所有租户列表")
    CommonResult<List<TenantCommonRespDTO>> getTenantList();

    @GetMapping(PREFIX + "/simple-list")
    @Operation(summary = "获取租户精简信息列表")
    CommonResult<List<TenantCommonRespDTO>> getTenantSimpleList();
}
