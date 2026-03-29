package site.addzero.kcloud.codegen

import site.addzero.kcloud.codegen.ir.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SchemaParserTest {

    private val parser = SchemaParser()

    // ── 一个最小的合法 OpenAPI 3.0 规范 ──

    private val minimalSpec = """
        openapi: "3.0.3"
        info:
          title: Test API
          version: "1.0"
        paths:
          /api/items:
            get:
              tags:
                - Items
              operationId: getItems
              summary: List all items
              parameters:
                - name: page
                  in: query
                  required: false
                  schema:
                    type: integer
              responses:
                "200":
                  description: OK
                  content:
                    application/json:
                      schema:
                        type: array
                        items:
                          ${'$'}ref: '#/components/schemas/Item'
            post:
              tags:
                - Items
              operationId: createItem
              summary: Create an item
              requestBody:
                required: true
                content:
                  application/json:
                    schema:
                      ${'$'}ref: '#/components/schemas/CreateItemRequest'
              responses:
                "201":
                  description: Created
                  content:
                    application/json:
                      schema:
                        ${'$'}ref: '#/components/schemas/Item'
          /api/items/{itemId}:
            get:
              tags:
                - Items
              operationId: getItemById
              parameters:
                - name: itemId
                  in: path
                  required: true
                  schema:
                    type: string
              responses:
                "200":
                  description: OK
                  content:
                    application/json:
                      schema:
                        ${'$'}ref: '#/components/schemas/Item'
            delete:
              tags:
                - Items
              operationId: deleteItem
              parameters:
                - name: itemId
                  in: path
                  required: true
                  schema:
                    type: string
              responses:
                "204":
                  description: Deleted
        components:
          schemas:
            Item:
              type: object
              required:
                - id
                - name
              properties:
                id:
                  type: string
                name:
                  type: string
                count:
                  type: integer
                  description: Item count
                active:
                  type: boolean
                  default: true
            CreateItemRequest:
              type: object
              required:
                - name
              properties:
                name:
                  type: string
                count:
                  type: integer
    """.trimIndent()


    // ── 基本解析测试 ──

    @Test
    fun `parse valid spec returns Success`() {
        val result = parser.parseContent(minimalSpec)
        assertIs<ParseResult.Success>(result)
    }

    @Test
    fun `parse invalid content returns Failure`() {
        val result = parser.parseContent("this is not valid openapi")
        assertIs<ParseResult.Failure>(result)
        assertTrue(result.errors.isNotEmpty())
    }

    @Test
    fun `parse empty string returns Failure`() {
        val result = parser.parseContent("")
        assertIs<ParseResult.Failure>(result)
    }

    // ── 接口分组测试 ──

    @Test
    fun `operations grouped by tag into interfaces`() {
        val result = parser.parseContent(minimalSpec)
        assertIs<ParseResult.Success>(result)
        val ir = result.ir

        assertEquals(1, ir.interfaces.size)
        assertEquals("ItemsApi", ir.interfaces[0].name)
        assertEquals(4, ir.interfaces[0].operations.size)
    }

    @Test
    fun `operations without tags go to DefaultApi`() {
        val spec = """
            openapi: "3.0.3"
            info:
              title: Test
              version: "1.0"
            paths:
              /ping:
                get:
                  operationId: ping
                  responses:
                    "200":
                      description: OK
        """.trimIndent()

        val result = parser.parseContent(spec)
        assertIs<ParseResult.Success>(result)
        assertEquals("DefaultApi", result.ir.interfaces[0].name)
    }

    // ── Operation 转换测试 ──

    @Test
    fun `operation has correct httpMethod and path`() {
        val result = parser.parseContent(minimalSpec) as ParseResult.Success
        val ops = result.ir.interfaces[0].operations

        val getItems = ops.first { it.functionName == "getItems" }
        assertEquals(HttpMethod.GET, getItems.httpMethod)
        assertEquals("/api/items", getItems.path)

        val createItem = ops.first { it.functionName == "createItem" }
        assertEquals(HttpMethod.POST, createItem.httpMethod)

        val deleteItem = ops.first { it.functionName == "deleteItem" }
        assertEquals(HttpMethod.DELETE, deleteItem.httpMethod)
    }

    @Test
    fun `operation functionName derived from operationId`() {
        val result = parser.parseContent(minimalSpec) as ParseResult.Success
        val names = result.ir.interfaces[0].operations.map { it.functionName }
        assertTrue("getItems" in names)
        assertTrue("createItem" in names)
        assertTrue("getItemById" in names)
        assertTrue("deleteItem" in names)
    }

    @Test
    fun `operation summary is captured`() {
        val result = parser.parseContent(minimalSpec) as ParseResult.Success
        val getItems = result.ir.interfaces[0].operations.first { it.functionName == "getItems" }
        assertEquals("List all items", getItems.summary)
    }

    // ── 参数测试 ──

    @Test
    fun `query parameter parsed correctly`() {
        val result = parser.parseContent(minimalSpec) as ParseResult.Success
        val getItems = result.ir.interfaces[0].operations.first { it.functionName == "getItems" }

        assertEquals(1, getItems.parameters.size)
        val param = getItems.parameters[0]
        assertEquals("page", param.name)
        assertEquals(ParameterLocation.QUERY, param.location)
        assertEquals(TypeRef.Primitive("Int"), param.type)
        assertEquals(false, param.required)
    }

    @Test
    fun `path parameter parsed correctly`() {
        val result = parser.parseContent(minimalSpec) as ParseResult.Success
        val getById = result.ir.interfaces[0].operations.first { it.functionName == "getItemById" }

        assertEquals(1, getById.parameters.size)
        val param = getById.parameters[0]
        assertEquals("itemId", param.name)
        assertEquals(ParameterLocation.PATH, param.location)
        assertEquals(true, param.required)
    }

    // ── RequestBody / ResponseType 测试 ──

    @Test
    fun `requestBody extracted from POST operation`() {
        val result = parser.parseContent(minimalSpec) as ParseResult.Success
        val createItem = result.ir.interfaces[0].operations.first { it.functionName == "createItem" }

        assertEquals(TypeRef.Reference("CreateItemRequest"), createItem.requestBody)
    }

    @Test
    fun `responseType extracted from 200 response`() {
        val result = parser.parseContent(minimalSpec) as ParseResult.Success
        val getItems = result.ir.interfaces[0].operations.first { it.functionName == "getItems" }

        assertEquals(TypeRef.ListType(TypeRef.Reference("Item")), getItems.responseType)
    }

    @Test
    fun `responseType is Unit when no response body`() {
        val result = parser.parseContent(minimalSpec) as ParseResult.Success
        val deleteItem = result.ir.interfaces[0].operations.first { it.functionName == "deleteItem" }

        assertEquals(TypeRef.Unit, deleteItem.responseType)
    }


    // ── Model 转换测试 ──

    @Test
    fun `models parsed from components schemas`() {
        val result = parser.parseContent(minimalSpec) as ParseResult.Success
        assertEquals(2, result.ir.models.size)

        val modelNames = result.ir.models.map { it.name }.toSet()
        assertTrue("Item" in modelNames)
        assertTrue("CreateItemRequest" in modelNames)
    }

    @Test
    fun `model properties with required flag`() {
        val result = parser.parseContent(minimalSpec) as ParseResult.Success
        val item = result.ir.models.first { it.name == "Item" }

        assertEquals(4, item.properties.size)

        val idProp = item.properties.first { it.name == "id" }
        assertTrue(idProp.required)
        assertEquals(TypeRef.Primitive("String"), idProp.type)

        val countProp = item.properties.first { it.name == "count" }
        assertEquals(false, countProp.required)
        assertEquals(TypeRef.Primitive("Int"), countProp.type)
        assertEquals("Item count", countProp.description)
    }

    @Test
    fun `model property default value captured`() {
        val result = parser.parseContent(minimalSpec) as ParseResult.Success
        val item = result.ir.models.first { it.name == "Item" }

        val activeProp = item.properties.first { it.name == "active" }
        assertEquals("true", activeProp.defaultValue)
    }

    // ── allOf 展平测试 ──

    @Test
    fun `allOf schemas are flattened`() {
        val spec = """
            openapi: "3.0.3"
            info:
              title: Test
              version: "1.0"
            paths: {}
            components:
              schemas:
                Base:
                  type: object
                  required:
                    - id
                  properties:
                    id:
                      type: string
                Extended:
                  allOf:
                    - ${'$'}ref: '#/components/schemas/Base'
                    - type: object
                      required:
                        - extra
                      properties:
                        extra:
                          type: integer
        """.trimIndent()

        val result = parser.parseContent(spec) as ParseResult.Success
        val extended = result.ir.models.first { it.name == "Extended" }

        assertEquals(2, extended.properties.size)
        val propNames = extended.properties.map { it.name }.toSet()
        assertTrue("id" in propNames)
        assertTrue("extra" in propNames)
    }

    // ── oneOf/anyOf 回退测试 ──

    @Test
    fun `oneOf picks first schema properties`() {
        val spec = """
            openapi: "3.0.3"
            info:
              title: Test
              version: "1.0"
            paths: {}
            components:
              schemas:
                Mixed:
                  oneOf:
                    - type: object
                      properties:
                        alpha:
                          type: string
                    - type: object
                      properties:
                        beta:
                          type: integer
        """.trimIndent()

        val result = parser.parseContent(spec) as ParseResult.Success
        val mixed = result.ir.models.first { it.name == "Mixed" }

        assertEquals(1, mixed.properties.size)
        assertEquals("alpha", mixed.properties[0].name)
    }

    // ── 命名辅助方法测试 ──

    @Test
    fun `toCamelCase converts correctly`() {
        assertEquals("getUserInfo", SchemaParser.toCamelCase("get_user_info"))
        assertEquals("getUserInfo", SchemaParser.toCamelCase("get-user-info"))
        assertEquals("getUserInfo", SchemaParser.toCamelCase("GetUserInfo"))
        assertEquals("hello", SchemaParser.toCamelCase("hello"))
    }

    @Test
    fun `toPascalCase converts correctly`() {
        assertEquals("UserInfo", SchemaParser.toPascalCase("user_info"))
        assertEquals("UserInfo", SchemaParser.toPascalCase("user-info"))
        assertEquals("Hello", SchemaParser.toPascalCase("hello"))
    }

    // ── 操作数量守恒测试 ──

    @Test
    fun `total operations count matches paths`() {
        val result = parser.parseContent(minimalSpec) as ParseResult.Success
        val totalOps = result.ir.interfaces.sumOf { it.operations.size }
        // minimalSpec has: GET /api/items, POST /api/items, GET /api/items/{itemId}, DELETE /api/items/{itemId}
        assertEquals(4, totalOps)
    }

    @Test
    fun `model count matches components schemas`() {
        val result = parser.parseContent(minimalSpec) as ParseResult.Success
        // minimalSpec has: Item, CreateItemRequest
        assertEquals(2, result.ir.models.size)
    }

    // ── 无 operationId 时从 method+path 生成函数名 ──

    @Test
    fun `function name generated from method and path when no operationId`() {
        val spec = """
            openapi: "3.0.3"
            info:
              title: Test
              version: "1.0"
            paths:
              /api/users/{userId}:
                get:
                  responses:
                    "200":
                      description: OK
        """.trimIndent()

        val result = parser.parseContent(spec) as ParseResult.Success
        val op = result.ir.interfaces[0].operations[0]
        // 应该从 GET + /api/users/{userId} 生成
        assertTrue(op.functionName.isNotBlank())
        assertTrue(op.functionName.contains("api", ignoreCase = true))
    }

    // ── 空 paths / schemas ──

    @Test
    fun `empty paths produces no interfaces`() {
        val spec = """
            openapi: "3.0.3"
            info:
              title: Test
              version: "1.0"
            paths: {}
        """.trimIndent()

        val result = parser.parseContent(spec) as ParseResult.Success
        assertTrue(result.ir.interfaces.isEmpty())
        assertTrue(result.ir.models.isEmpty())
    }
}
