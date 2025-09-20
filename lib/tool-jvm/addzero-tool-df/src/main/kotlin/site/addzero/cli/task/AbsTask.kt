package site.addzero.cli.task

import site.addzero.cli.biz.task.Status

interface AbsTask {
    val des: String
    val status: Status
}
