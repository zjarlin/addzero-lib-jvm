    package site.addzero.generated.mcp

    import site.addzero.common.consts.sql
    import site.addzero.web.infra.jackson.toJson
    import org.babyfish.jimmer.ImmutableObjects
    import org.springframework.ai.tool.annotation.Tool
    import org.springframework.ai.tool.annotation.ToolParam
    import org.springframework.stereotype.Service
    import site.addzero.model.entity.SysArea
    import site.addzero.generated.isomorphic.SysAreaIso

    /**
     * SysArea MCP服务
     *
     * 提供area相关的CRUD操作和AI工具
     * 自动生成的代码，请勿手动修改
     */
    @Service
    class SysAreaMcpService  {

        @Tool(description = "保存area数据到数据库")
        fun saveSysArea(@ToolParam(description = "area数据对象") entity: SysAreaIso): String {
               val toJson = entity.toJson()
val fromString = ImmutableObjects.fromString(SysArea::class.java, toJson)
val save = sql.save(fromString)
return "保存成功"

        }
    }