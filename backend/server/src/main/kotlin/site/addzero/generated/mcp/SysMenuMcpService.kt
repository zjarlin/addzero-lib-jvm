    package site.addzero.generated.mcp

    import site.addzero.common.consts.sql
    import site.addzero.web.infra.jackson.toJson
    import org.babyfish.jimmer.ImmutableObjects
    import org.springframework.ai.tool.annotation.Tool
    import org.springframework.ai.tool.annotation.ToolParam
    import org.springframework.stereotype.Service
    import site.addzero.model.entity.SysMenu
    import site.addzero.generated.isomorphic.SysMenuIso

    /**
     * SysMenu MCP服务
     *
     * 提供menu相关的CRUD操作和AI工具
     * 自动生成的代码，请勿手动修改
     */
    @Service
    class SysMenuMcpService  {

        @Tool(description = "保存menu数据到数据库")
        fun saveSysMenu(@ToolParam(description = "menu数据对象") entity: SysMenuIso): String {
               val toJson = entity.toJson()
val fromString = ImmutableObjects.fromString(SysMenu::class.java, toJson)
val save = sql.save(fromString)
return "保存成功"

        }
    }