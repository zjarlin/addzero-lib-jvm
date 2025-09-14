    package site.addzero.generated.mcp

    import site.addzero.common.consts.sql
    import site.addzero.web.infra.jackson.toJson
    import org.babyfish.jimmer.ImmutableObjects
    import org.springframework.ai.tool.annotation.Tool
    import org.springframework.ai.tool.annotation.ToolParam
    import org.springframework.stereotype.Service
    import site.addzero.model.entity.SysAiPrompt
    import site.addzero.generated.isomorphic.SysAiPromptIso

    /**
     * SysAiPrompt MCP服务
     *
     * 提供AI提示词相关的CRUD操作和AI工具
     * 自动生成的代码，请勿手动修改
     */
    @Service
    class SysAiPromptMcpService  {

        @Tool(description = "保存AI提示词数据到数据库")
        fun saveSysAiPrompt(@ToolParam(description = "AI提示词数据对象") entity: SysAiPromptIso): String {
               val toJson = entity.toJson()
val fromString = ImmutableObjects.fromString(SysAiPrompt::class.java, toJson)
val save = sql.save(fromString)
return "保存成功"

        }
    }