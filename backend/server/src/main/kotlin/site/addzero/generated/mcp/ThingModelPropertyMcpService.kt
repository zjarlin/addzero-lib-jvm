    package site.addzero.generated.mcp

    import site.addzero.common.consts.sql
    import site.addzero.web.infra.jackson.toJson
    import org.babyfish.jimmer.ImmutableObjects
    import org.springframework.ai.tool.annotation.Tool
    import org.springframework.ai.tool.annotation.ToolParam
    import org.springframework.stereotype.Service
    import site.addzero.model.entity.biz_device.ThingModelProperty

    /**
     * ThingModelProperty MCP服务
     *
     * 提供thingmodelproperty相关的CRUD操作和AI工具
     * 自动生成的代码，请勿手动修改
     */
    @Service
    class ThingModelPropertyMcpService  {

        @Tool(description = "保存thingmodelproperty数据到数据库")
        fun saveThingModelProperty(@ToolParam(description = "thingmodelproperty数据对象") entity: ThingModelPropertyIso): String {
               val toJson = entity.toJson()
val fromString = ImmutableObjects.fromString(ThingModelProperty::class.java, toJson)
val save = sql.save(fromString)
return "保存成功"

        }
    }
