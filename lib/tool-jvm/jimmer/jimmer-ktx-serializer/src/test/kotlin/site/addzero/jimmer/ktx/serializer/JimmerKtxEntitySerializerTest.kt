package site.addzero.jimmer.ktx.serializer

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.babyfish.jimmer.ImmutableObjects
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import site.addzero.jimmer.ktx.serializer.fixture.Department
import site.addzero.jimmer.ktx.serializer.fixture.User
import java.time.LocalDate

class JimmerKtxEntitySerializerTest {

  @Test
  fun `顶层序列化会跳过未加载字段并递归关联`() {
    val user = User {
      id = 1L
      name = "Alice"
      dept {
        id = 10L
        name = "Platform"
      }
    }

    val json = Json { ignoreUnknownKeys = true }
    val actual = json.parseToJsonElement(json.encodeJimmerToString(user))
    val expected = buildJsonObject {
      put("id", 1L)
      put("name", "Alice")
      putJsonObject("dept") {
        put("id", 10L)
        put("name", "Platform")
      }
    }

    assertEquals(expected, actual)
    assertFalse(ImmutableObjects.isLoaded(user, "age"))
    assertFalse(ImmutableObjects.isLoaded(user, "birthday"))
  }

  @Test
  fun `顶层反序列化会恢复为 Jimmer 实体`() {
    val json = Json { ignoreUnknownKeys = true }
    val actual = json.decodeJimmerFromString<User>(
      """
        {
          "id": 2,
          "name": "Bob",
          "age": 18,
          "birthday": "2024-02-03",
          "dept": {
            "id": 20,
            "name": "Research"
          }
        }
      """.trimIndent(),
    )

    assertEquals(2L, actual.id)
    assertEquals("Bob", actual.name)
    assertEquals(18, actual.age)
    assertEquals(LocalDate.parse("2024-02-03"), actual.birthday)
    assertTrue(ImmutableObjects.isLoaded(actual, "dept"))
    assertEquals(20L, actual.dept?.id)
    assertEquals("Research", actual.dept?.name)
  }

  @Test
  fun `Contextual 注册后可以直接放进 DTO`() {
    val user = User {
      id = 3L
      name = "Carol"
      age = 25
      dept {
        id = 30L
        name = "Design"
      }
    }
    val envelope = UserEnvelope(user = user, dept = user.dept)
    val json = Json {
      ignoreUnknownKeys = true
      serializersModule = jimmerKtxSerializersModule(
        User::class,
        Department::class,
      )
    }

    val content = json.encodeToString(envelope)
    val decoded = json.decodeFromString<UserEnvelope>(content)

    assertEquals(3L, decoded.user.id)
    assertEquals("Carol", decoded.user.name)
    assertEquals(25, decoded.user.age)
    assertEquals(30L, decoded.dept?.id)
    assertEquals("Design", decoded.dept?.name)
  }
}

@Serializable
private data class UserEnvelope(
  @Contextual val user: User,
  @Contextual val dept: Department?,
)
