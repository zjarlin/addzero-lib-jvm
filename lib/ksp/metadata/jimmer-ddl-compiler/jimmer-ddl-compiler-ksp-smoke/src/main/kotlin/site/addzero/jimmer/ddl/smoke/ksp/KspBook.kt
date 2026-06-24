package site.addzero.jimmer.ddl.smoke.ksp

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.Serialized
import org.babyfish.jimmer.sql.Table

@Entity
@Table(name = "ksp_book")
interface KspBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    @Key
    @Column(length = 120)
    val title: String

    @Serialized
    @Column(sqlType = "json")
    val tags: List<String>

    val published: Boolean
}
