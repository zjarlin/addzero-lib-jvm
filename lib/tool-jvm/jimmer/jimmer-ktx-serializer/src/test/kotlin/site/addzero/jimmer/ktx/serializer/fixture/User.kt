package site.addzero.jimmer.ktx.serializer.fixture

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.ManyToOne
import java.time.LocalDate

@Entity
interface User {
  @Id
  val id: Long
  val name: String
  val age: Int?
  val birthday: LocalDate?

  @ManyToOne
  val dept: Department?
}
