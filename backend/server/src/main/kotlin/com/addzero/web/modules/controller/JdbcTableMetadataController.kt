package com.addzero.web.modules.controller

import com.addzero.common.consts.sql
import com.addzero.model.entity.JdbcTableMetadata
import com.addzero.model.entity.fetchBy
import com.addzero.model.entity.tableName
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/jdbcTableMetadata")
class JdbcTableMetadataController {


    @GetMapping("/getTableMetadata")
    fun getTableMetadata(tablename: String): List<JdbcTableMetadata> {
        return sql.executeQuery(JdbcTableMetadata::class) {
            where(table.tableName eq tablename)
            select(table.fetchBy {
                allScalarFields()
                columns {
                    allScalarFields()
                    jdbcColumnMetadataAttach {
                        allScalarFields()
                    }
                }
            })
        }

    }

}
