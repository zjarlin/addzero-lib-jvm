package site.addzero.network.call.tianyancha.utils

import site.addzero.network.call.tianyancha.domain.baseinfo.BaseInfo
import java.util.Date

fun site.addzero.network.call.tianyancha.entity.Data.toBaseInfo2(): BaseInfo {
    val data = this
    return BaseInfo(
        id = data.id.toString(),
        name = this.name,
        percentileScore = this.percentileScore.toString(),
        staffNumRange = this.staffNumRange,
        fromTime = if (this.fromTime > 0) Date(this.fromTime).toString() else "",
        type = EnumParser.parseType(this.type),
//        isMicroEnt = EnumParser.parseIsMicroEnt(this.isMicroEnt),
        regNumber = this.regNumber,
        regCapital = this.regCapital,
        regInstitute = this.regInstitute,
        regLocation = this.regLocation,
        industry = this.industry,
        approvedTime = if (this.approvedTime > 0) Date(this.approvedTime).toString() else "",
        socialStaffNum = this.socialStaffNum.toString(),
        tags = this.tags,
        taxNumber = this.taxNumber,
        businessScope = this.businessScope,
        property3 = this.property3,
        alias = this.alias,
        orgNumber = this.orgNumber,
        regStatus = this.regStatus,
        estiblishTime = this.estiblishTimeTitleName,
        legalPersonName = this.legalPersonName,
//        toTime = if (this.toTime > 0) Date(this.toTime).toString() else "",
        actualCapital = this.actualCapital,
        companyOrgType = this.companyOrgType,
        base = this.base,
        creditCode = this.creditCode,
        email = this.email,
        websiteList = this.websiteList,
        phoneNumber = this.phoneNumber
    )
}
