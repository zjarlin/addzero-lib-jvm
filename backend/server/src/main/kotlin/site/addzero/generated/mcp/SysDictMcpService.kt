    package site.addzero.generated.mcp

    import site.addzero.common.consts.sql
    import site.addzero.web.infra.jackson.toJson
    import org.babyfish.jimmer.ImmutableObjects
    import org.springframework.ai.tool.annotation.Tool
    import org.springframework.ai.tool.annotation.ToolParam
    import org.springframework.stereotype.Service
    import site.addzero.model.entity.SysDict
    import site.addzero.generated.isomorphic.SysDictIso

    /**
     * SysDict MCP服务
     *
     * 提供字典相关的CRUD操作和AI工具
     * 自动生成的代码，请勿手动修改
     */
    @Service
    class SysDictMcpService  {

        @Tool(description = "保存字典数据到数据库")
        fun saveSysDict(@ToolParam(description = "字典数据对象") entity: SysDictIso): String {
               val toJson = entity.toJson()
val fromString = ImmutableObjects.fromString(SysDict::class.java, toJson)
val save = sql.save(fromString)
return "保存成功"

        }
    }