package site.addzero.jimmer.ktx.serializer.fixture

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id

@Entity
interface Department {
  @Id
  val id: Long
  val name: String
}
