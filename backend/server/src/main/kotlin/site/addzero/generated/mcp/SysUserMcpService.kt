    package site.addzero.generated.mcp

    import site.addzero.common.consts.sql
    import site.addzero.web.infra.jackson.toJson
    import org.babyfish.jimmer.ImmutableObjects
    import org.springframework.ai.tool.annotation.Tool
    import org.springframework.ai.tool.annotation.ToolParam
    import org.springframework.stereotype.Service
    import site.addzero.model.entity.SysUser
    import site.addzero.generated.isomorphic.SysUserIso

    /**
     * SysUser MCP服务
     *
     * 提供用户相关的CRUD操作和AI工具
     * 自动生成的代码，请勿手动修改
     */
    @Service
    class SysUserMcpService  {

        @Tool(description = "保存用户数据到数据库")
        fun saveSysUser(@ToolParam(description = "用户数据对象") entity: SysUserIso): String {
               val toJson = entity.toJson()
val fromString = ImmutableObjects.fromString(SysUser::class.java, toJson)
val save = sql.save(fromString)
return "保存成功"

        }
    }