    package com.addzero.generated.mcp

    import com.addzero.common.consts.sql
    import com.addzero.web.infra.jackson.toJson
    import org.babyfish.jimmer.ImmutableObjects
    import org.springframework.ai.tool.annotation.Tool
    import org.springframework.ai.tool.annotation.ToolParam
    import org.springframework.stereotype.Service
    import com.addzero.model.entity.JdbcTableMetadataAttach
    import com.addzero.generated.isomorphic.JdbcTableMetadataAttachIso

    /**
     * JdbcTableMetadataAttach MCP服务
     *
     * 提供jdbctablemetadataattach相关的CRUD操作和AI工具
     * 自动生成的代码，请勿手动修改
     */
    @Service
    class JdbcTableMetadataAttachMcpService  {

        @Tool(description = "保存jdbctablemetadataattach数据到数据库")
        fun saveJdbcTableMetadataAttach(@ToolParam(description = "jdbctablemetadataattach数据对象") entity: JdbcTableMetadataAttachIso): String {
               val toJson = entity.toJson()
val fromString = ImmutableObjects.fromString(JdbcTableMetadataAttach::class.java, toJson)
val save = sql.save(fromString)
return "保存成功"

        }
    }