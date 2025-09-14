    package site.addzero.generated.mcp

    import site.addzero.common.consts.sql
    import site.addzero.web.infra.jackson.toJson
    import org.babyfish.jimmer.ImmutableObjects
    import org.springframework.ai.tool.annotation.Tool
    import org.springframework.ai.tool.annotation.ToolParam
    import org.springframework.stereotype.Service
    import site.addzero.model.entity.SysColumnConfig
    import site.addzero.generated.isomorphic.SysColumnConfigIso

    /**
     * SysColumnConfig MCP服务
     *
     * 提供columnconfig相关的CRUD操作和AI工具
     * 自动生成的代码，请勿手动修改
     */
    @Service
    class SysColumnConfigMcpService  {

        @Tool(description = "保存columnconfig数据到数据库")
        fun saveSysColumnConfig(@ToolParam(description = "columnconfig数据对象") entity: SysColumnConfigIso): String {
               val toJson = entity.toJson()
val fromString = ImmutableObjects.fromString(SysColumnConfig::class.java, toJson)
val save = sql.save(fromString)
return "保存成功"

        }
    }