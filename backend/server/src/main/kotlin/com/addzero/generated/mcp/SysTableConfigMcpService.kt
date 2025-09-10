    package com.addzero.generated.mcp

    import com.addzero.common.consts.sql
    import com.addzero.web.infra.jackson.toJson
    import org.babyfish.jimmer.ImmutableObjects
    import org.springframework.ai.tool.annotation.Tool
    import org.springframework.ai.tool.annotation.ToolParam
    import org.springframework.stereotype.Service
    import com.addzero.model.entity.SysTableConfig
    import com.addzero.generated.isomorphic.SysTableConfigIso

    /**
     * SysTableConfig MCP服务
     *
     * 提供tableconfig相关的CRUD操作和AI工具
     * 自动生成的代码，请勿手动修改
     */
    @Service
    class SysTableConfigMcpService  {

        @Tool(description = "保存tableconfig数据到数据库")
        fun saveSysTableConfig(@ToolParam(description = "tableconfig数据对象") entity: SysTableConfigIso): String {
               val toJson = entity.toJson()
val fromString = ImmutableObjects.fromString(SysTableConfig::class.java, toJson)
val save = sql.save(fromString)
return "保存成功"

        }
    }