package site.addzero.jimmer.ddl.smoke.apt;

import org.babyfish.jimmer.sql.Column;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.GeneratedValue;
import org.babyfish.jimmer.sql.GenerationType;
import org.babyfish.jimmer.sql.Id;
import org.babyfish.jimmer.sql.Key;
import org.babyfish.jimmer.sql.Serialized;
import org.babyfish.jimmer.sql.Table;

import java.util.List;

@Entity
@Table(name = "apt_author")
public interface AptAuthor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();

    @Key
    String name();

    @Serialized
    @Column(sqlType = "json")
    List<String> aliases();

    boolean active();
}
