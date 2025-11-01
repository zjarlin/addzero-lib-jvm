package site.addzero.network.call.tianyancha.utils

import com.alibaba.fastjson2.JSONReader
import com.alibaba.fastjson2.parseObject
import org.junit.jupiter.api.Test
import site.addzero.network.call.tianyancha.entity.CompanyInfoRes

class TianyanchaApiUtilTest {

    @Test
    fun `test getBaseInfo withTycApi valid response`() {
        val searchCompany = TycApi.searchCompany("中洛佳")
        println()
    }

    @Test
    fun odijasodij(): Unit {
        val lng = 3398690435
        val baseInfo = TycApi.getBaseInfo(lng)
        println(baseInfo)

    }

    @Test
    fun paseTest(): Unit {
        val responseBody = """
           {
  "state" : "ok",
  "message" : "",
  "special" : "",
  "vipMessage" : "",
  "isLogin" : 0,
  "data" : {
    "estiblishTimeTitleName" : "成立日期",
    "emailList" : [ "****@163.com", "****@qq.com", "****@qq.com", "****@163.com" ],
    "year4SocialStaffNum" : 2024,
    "orgTypeName" : "有限责任公司",
    "relatedIndustryChainCount" : 7,
    "type" : 1,
    "equityUrl" : "https://cdn.tianyancha.com/equity/3afa37109960a083979d93c38b0a1ba9.png",
    "showTrendChart" : true,
    "legalPersonType" : 1,
    "property3" : "Henan Zhongluojia Technology Co., Ltd",
    "companyShowBizTypeName" : "有限责任公司(自然人投资或控股)",
    "regCapitalLabel" : "小型",
    "companyTimeTitle" : "成立日期",
    "industryNationalStdLv3Code" : "656",
    "industry2017" : "软件和信息技术服务业",
    "id" : 3398690435,
    "orgNumber" : "MA47WC78-8",
    "isClaimed" : 0,
    "listedStatusTypeForSenior" : 0,
    "taxPhone" : "0379-65199909",
    "enterpriseScaleInfo" : {
      "explainText" : "基于天眼查大数据模型，结合不同行业企业的经营数据，如企业的资产总额、经营利润、人员规模等（其中上市/发债企业以合并报表口径），综合计算形成的L(大型)、M(中型)、S(小型)和XS(微型)四类企业规模分类体系（其中不包含缺乏判定指标或新成立的企业）",
      "explainTextHtml" : "基于天眼查大数据模型，结合不同行业企业的经营数据，如企业的资产总额、经营利润、人员规模等（其中上市/发债企业以合并报表口径），综合计算形成的<em> L (大型)、M (中型)、S (小型)和 XS (微型)</em>四类企业规模分类体系（其中不包含缺乏判定指标或新成立的企业）",
      "scale" : "小型",
      "iconType" : "S"
    },
    "entityType" : 1,
    "companyBizOrgType" : "1130",
    "taxNumber" : "91410307MA47WC788D",
    "shortname" : "中洛佳",
    "tags" : "高新技术企业",
    "taxQualification" : "增值税一般纳税人",
    "scienceTechnologyInfo" : {
      "score" : 64,
      "grade" : "普通",
      "gradeColor" : "#0084FF",
      "exceedPercent" : "84%"
    },
    "extraInfo" : null,
    "baseInfo" : "河南中洛佳科技有限公司，成立于2019年，位于河南省洛阳市，是一家以从事软件和信息技术服务业为主的企业。企业注册资本1000万人民币，实缴资本447.5万人民币。通过天眼查大数据分析，河南中洛佳科技有限公司参与招投标项目56次；知识产权方面有商标信息10条，专利信息2条，著作权信息39条；此外企业还拥有行政许可2个。",
    "operatingIncomeInfo" : null,
    "regCapital" : "1000万人民币",
    "staffNumRange" : "小于50人",
    "industry" : null,
    "companyAssets" : null,
    "industryInfo" : {
      "explainText" : "数据来自企业工商信息的国标行业。",
      "nameLevel1" : "信息传输、软件和信息技术服务业",
      "nameLevel2" : "软件和信息技术服务业",
      "nameLevel3" : "信息技术咨询服务",
      "nameLevel4" : null,
      "code" : "I6560"
    },
    "emailDetailList" : [ {
      "showSource" : "2024年报",
      "sourceDisplayWeight" : "4",
      "reportYear" : "2024",
      "sameEmailCount" : "3",
      "email" : "hnzljkj@163.com"
    }, {
      "showSource" : "2023年报",
      "sourceDisplayWeight" : "5",
      "reportYear" : "2023",
      "sameEmailCount" : "13",
      "email" : "419569781@qq.com"
    }, {
      "showSource" : "2021年报",
      "sourceDisplayWeight" : "7",
      "reportYear" : "2021",
      "sameEmailCount" : null,
      "email" : "41956781@qq.com"
    }, {
      "showSource" : "2019年报",
      "sourceDisplayWeight" : "9",
      "reportYear" : "2019",
      "sameEmailCount" : null,
      "email" : "zlj65199909@163.com"
    } ],
    "tagListV2" : [ {
      "clickUrl" : "",
      "hover" : "企业依法存在并继续正常营业。",
      "color" : "#089944",
      "tagId" : 30,
      "background" : "#E9FCF1",
      "name" : "存续",
      "clickHyperLinkType" : 0,
      "logo" : "",
      "title" : "存续"
    }, {
      "clickUrl" : "",
      "hover" : "小微企业概念来自工信部《中小企业划型标准规定》，具体标准根据企业从业人员、营业收入、资产总额等指标，结合行业特点制定。",
      "color" : "#0084FF",
      "tagId" : 24,
      "background" : "#E8F4FF",
      "name" : "小微企业",
      "clickHyperLinkType" : 0,
      "logo" : "",
      "title" : "小微企业"
    }, {
      "clickUrl" : "",
      "hover" : "",
      "color" : "#0084FF",
      "tagId" : 6,
      "background" : "#E8F4FF",
      "name" : "高新技术企业",
      "clickHyperLinkType" : 0,
      "logo" : "",
      "title" : "高新技术企业"
    }, {
      "clickUrl" : "",
      "hover" : "",
      "color" : "#0084FF",
      "tagId" : 7,
      "background" : "#E8F4FF",
      "name" : "科技型中小企业",
      "clickHyperLinkType" : 0,
      "logo" : "",
      "title" : "科技型中小企业"
    } ],
    "companyProfilePlainText4Copy" : "基本信息\n河南中洛佳科技有限公司，成立于2019年，位于河南省洛阳市，是一家以从事软件和信息技术服务业为主的企业。企业注册资本1000万人民币，实缴资本447.5万人民币。\n营运状况\n通过天眼查大数据分析，河南中洛佳科技有限公司参与招投标项目56次；知识产权方面有商标信息10条，专利信息2条，著作权信息39条；此外企业还拥有行政许可2个。",
    "tagListV3" : [ {
      "borderColor" : "#089944",
      "routingName" : null,
      "color" : "#089944",
      "hoverNoticeType" : 1,
      "hoverNoticeContent" : "企业依法存在并继续正常营业。",
      "profileTagClickHyperlinkType" : 0,
      "title" : "存续",
      "type" : 10,
      "popName" : "存续",
      "profileTagTypeId" : 5,
      "fontFamily" : "系统",
      "profileTagClickHyperlinkDetails" : "",
      "borderWidth" : 0.5,
      "logo" : "",
      "borderTransparency" : 0.1,
      "id" : 30,
      "guideColor" : "#089944",
      "profileTagTypeRanking" : 1,
      "order" : 1,
      "guideTransparency" : 0.4,
      "iOSVersion" : null,
      "routingAction" : false,
      "showCondition" : null,
      "actionType" : "pop",
      "background" : "#E9FCF1",
      "androidVersion" : null,
      "name" : "存续",
      "fontSize" : 12,
      "routingAddr" : null
    }, {
      "borderColor" : "#0084FF",
      "routingName" : null,
      "color" : "#0084FF",
      "hoverNoticeType" : 1,
      "hoverNoticeContent" : "小微企业概念来自工信部《中小企业划型标准规定》，具体标准根据企业从业人员、营业收入、资产总额等指标，结合行业特点制定。",
      "profileTagClickHyperlinkType" : 0,
      "title" : "小微企业",
      "type" : 10,
      "popName" : "小微企业",
      "profileTagTypeId" : 3,
      "fontFamily" : "系统",
      "profileTagClickHyperlinkDetails" : "",
      "borderWidth" : 0.5,
      "logo" : "",
      "borderTransparency" : 0.1,
      "id" : 24,
      "guideColor" : "#0084FF",
      "profileTagTypeRanking" : 8,
      "order" : 1,
      "guideTransparency" : 0.4,
      "iOSVersion" : null,
      "routingAction" : false,
      "showCondition" : null,
      "actionType" : "pop",
      "background" : "#E8F4FF",
      "androidVersion" : null,
      "name" : "小微企业",
      "fontSize" : 12,
      "routingAddr" : null
    }, {
      "borderColor" : "#0084FF",
      "routingName" : "更多荣誉",
      "color" : "#0084FF",
      "hoverNoticeType" : 3,
      "hoverNoticeContent" : "{\"title\":\"科技标签\",\"body\":[[{\"underline\":false,\"fontSize\":\"14px\",\"text\":\"发布日期：\",\"fontColor\":\"#8590A6\",\"fontWeight\":\"NORMAL\",\"internalLink\":false},{\"underline\":false,\"rights\":[\"NON-VIP\"],\"fontSize\":\"14px\",\"text\":\"2024-11-12\",\"fontColor\":\"#262F40\",\"fontWeight\":\"NORMAL\"}],[{\"underline\":false,\"fontSize\":\"14px\",\"text\":\"级别：\",\"fontColor\":\"#8590A6\",\"fontWeight\":\"NORMAL\",\"internalLink\":false},{\"underline\":false,\"rights\":[\"NON-VIP\"],\"fontSize\":\"14px\",\"text\":\"国家级\",\"fontColor\":\"#262F40\",\"fontWeight\":\"NORMAL\"}],[{\"underline\":false,\"fontSize\":\"14px\",\"text\":\"来源：\",\"fontColor\":\"#8590A6\",\"fontWeight\":\"NORMAL\",\"internalLink\":false},{\"routeUrlApp\":\"tyc://app.tianyancha.com/graph/web?showLogo=1&useH5Container=1&hideNav=0&hiddenStatusBar=0&hiddenBottomLogo=1[[[{\\\"url\\\":\\\"https://m.tianyancha.com/app/h5/fileViewer/pdfViewerPlus.html?file=https%3A%2F%2Fstatic1.tianyancha.com%2FcompanyHonorLabel%2Fother1%2F9c3883ec8364b01eefc30e8c32d8f72d.pdf&shareTitle=%E5%AF%B9%E6%B2%B3%E5%8D%97%E7%9C%81%E8%AE%A4%E5%AE%9A%E6%9C%BA%E6%9E%842024%E5%B9%B4%E8%AE%A4%E5%AE%9A%E6%8A%A5%E5%A4%87%E7%9A%84%E7%AC%AC%E4%B8%80%E6%89%B9%E9%AB%98%E6%96%B0%E6%8A%80%E6%9C%AF%E4%BC%81%E4%B8%9A%E8%BF%9B%E8%A1%8C%E5%A4%87%E6%A1%88%E7%9A%84%E5%85%AC%E5%91%8A&shareSubTitle=%E6%B2%B3%E5%8D%97%E4%B8%AD%E6%B4%9B%E4%BD%B3%E7%A7%91%E6%8A%80%E6%9C%89%E9%99%90%E5%85%AC%E5%8F%B8\\\",\\\"title\\\":\\\"对河南省认定机构2024年认定报备的第一批高新技术企业进行备案的公告\\\"}]]]\",\"underline\":false,\"routeUrlWeb\":\"https://static1.tianyancha.com/companyHonorLabel/other1/9c3883ec8364b01eefc30e8c32d8f72d.pdf\",\"rights\":[\"NON-VIP\"],\"fontSize\":\"14px\",\"text\":\"对河南省认定机构2024年认定报备的第一批高新技术企业进行备案的公告\",\"isLinkType\":true,\"linkIconType\":3,\"linkFontColor\":\"#0084FF\",\"fontColor\":\"#262F40\",\"fontWeight\":\"NORMAL\"}],[{\"underline\":false,\"fontSize\":\"14px\",\"text\":\"定义：\",\"fontColor\":\"#8590A6\",\"fontWeight\":\"NORMAL\",\"internalLink\":false},{\"underline\":false,\"rights\":[\"NON-VIP\"],\"fontSize\":\"14px\",\"text\":\"高新技术企业指在《国家重点支持的高新技术领域》内，持续进行研究开发与技术成果转化，形成企业核心自主知识产权，并以此为基础开展经营活动，在中国境内（不包括港、澳、台地区）注册的居民企业。\",\"fontColor\":\"#262F40\",\"fontWeight\":\"NORMAL\"}]]}",
      "profileTagClickHyperlinkType" : 0,
      "title" : "高新技术企业",
      "type" : 9,
      "popName" : "高新技术企业",
      "profileTagTypeId" : 3,
      "fontFamily" : "系统",
      "profileTagClickHyperlinkDetails" : "",
      "borderWidth" : 0.5,
      "logo" : "",
      "borderTransparency" : 0.1,
      "id" : 6,
      "guideColor" : "#0084FF",
      "profileTagTypeRanking" : 8,
      "order" : 2,
      "guideTransparency" : 0.4,
      "iOSVersion" : null,
      "routingAction" : true,
      "showCondition" : null,
      "actionType" : "pop",
      "background" : "#E8F4FF",
      "androidVersion" : null,
      "name" : "高新技术企业",
      "fontSize" : 12,
      "routingAddr" : "honor"
    }, {
      "borderColor" : "#0084FF",
      "routingName" : "更多荣誉",
      "color" : "#0084FF",
      "hoverNoticeType" : 3,
      "hoverNoticeContent" : "{\"title\":\"科技标签\",\"body\":[[{\"underline\":false,\"fontSize\":\"14px\",\"text\":\"发布日期：\",\"fontColor\":\"#8590A6\",\"fontWeight\":\"NORMAL\",\"internalLink\":false},{\"underline\":false,\"rights\":[\"NON-VIP\"],\"fontSize\":\"14px\",\"text\":\"2024-08-07\",\"fontColor\":\"#262F40\",\"fontWeight\":\"NORMAL\"}],[{\"underline\":false,\"fontSize\":\"14px\",\"text\":\"级别：\",\"fontColor\":\"#8590A6\",\"fontWeight\":\"NORMAL\",\"internalLink\":false},{\"underline\":false,\"rights\":[\"NON-VIP\"],\"fontSize\":\"14px\",\"text\":\"国家级\",\"fontColor\":\"#262F40\",\"fontWeight\":\"NORMAL\"}],[{\"underline\":false,\"fontSize\":\"14px\",\"text\":\"来源：\",\"fontColor\":\"#8590A6\",\"fontWeight\":\"NORMAL\",\"internalLink\":false},{\"routeUrlApp\":\"tyc://app.tianyancha.com/graph/web?showLogo=1&useH5Container=1&hideNav=0&hiddenStatusBar=0&hiddenBottomLogo=1[[[{\\\"url\\\":\\\"https://m.tianyancha.com/app/h5/fileViewer/pdfViewerPlus.html?file=https%3A%2F%2Fstatic1.tianyancha.com%2FcompanyHonorLabel%2Fother1%2F8de8cc27a82347434fcf81d8c6cfad1b.pdf&shareTitle=%E6%B2%B3%E5%8D%97%E7%9C%812024%E5%B9%B4%E7%AC%AC1%E6%89%B9%E5%85%A5%E5%BA%93%E7%A7%91%E6%8A%80%E5%9E%8B%E4%B8%AD%E5%B0%8F%E4%BC%81%E4%B8%9A%E5%90%8D%E5%8D%95%E5%85%AC%E5%91%8A&shareSubTitle=%E6%B2%B3%E5%8D%97%E4%B8%AD%E6%B4%9B%E4%BD%B3%E7%A7%91%E6%8A%80%E6%9C%89%E9%99%90%E5%85%AC%E5%8F%B8\\\",\\\"title\\\":\\\"河南省2024年第1批入库科技型中小企业名单公告\\\"}]]]\",\"underline\":false,\"routeUrlWeb\":\"https://static1.tianyancha.com/companyHonorLabel/other1/8de8cc27a82347434fcf81d8c6cfad1b.pdf\",\"rights\":[\"NON-VIP\"],\"fontSize\":\"14px\",\"text\":\"河南省2024年第1批入库科技型中小企业名单公告\",\"isLinkType\":true,\"linkIconType\":3,\"linkFontColor\":\"#0084FF\",\"fontColor\":\"#262F40\",\"fontWeight\":\"NORMAL\"}],[{\"underline\":false,\"fontSize\":\"14px\",\"text\":\"定义：\",\"fontColor\":\"#8590A6\",\"fontWeight\":\"NORMAL\",\"internalLink\":false},{\"underline\":false,\"rights\":[\"NON-VIP\"],\"fontSize\":\"14px\",\"text\":\"科技型中小企业指依托一定数量的科技人员从事科学技术研究开发活动，取得自主知识产权并将其转化为高新技术产品或服务，从而实现可持续发展的中小企业。\",\"fontColor\":\"#262F40\",\"fontWeight\":\"NORMAL\"}]]}",
      "profileTagClickHyperlinkType" : 0,
      "title" : "科技型中小企业",
      "type" : 9,
      "popName" : "科技型中小企业",
      "profileTagTypeId" : 3,
      "fontFamily" : "系统",
      "profileTagClickHyperlinkDetails" : "",
      "borderWidth" : 0.5,
      "logo" : "",
      "borderTransparency" : 0.1,
      "id" : 7,
      "guideColor" : "#0084FF",
      "profileTagTypeRanking" : 8,
      "order" : 3,
      "guideTransparency" : 0.4,
      "iOSVersion" : null,
      "routingAction" : true,
      "showCondition" : null,
      "actionType" : "pop",
      "background" : "#E8F4FF",
      "androidVersion" : null,
      "name" : "科技型中小企业",
      "fontSize" : 12,
      "routingAddr" : "honor"
    } ],
    "regNumber" : "410394000177215",
    "addressList" : [ {
      "showSource" : "注册地址",
      "sourceDisplayWeight" : 1,
      "reportYear" : null,
      "address" : "河南省洛阳市太康路与汇通街交叉口大数据产业园综合楼四楼405室",
      "latitude" : "34.63379498198122",
      "longitude" : "112.50651703077477"
    }, {
      "showSource" : "2020年报",
      "sourceDisplayWeight" : 8,
      "reportYear" : "2020",
      "address" : "河南省洛阳市洛龙区长厦门街与开元大道交叉口863创智广场3栋412室",
      "latitude" : "0.0",
      "longitude" : "0.0"
    } ],
    "fromTime" : 1576512000000,
    "socialStaffNum" : "21",
    "companyOrgType" : "有限责任公司(自然人投资或控股)",
    "year4BranchSocialStaffNum" : 2024,
    "enNameSource" : "（自动翻译）",
    "taxAddress" : "河南省洛阳市太康路与汇通街交叉口大数据产业园综合楼四楼405室",
    "email" : "****@163.com",
    "actualCapital" : "447.5万人民币",
    "companyTimeExplain" : "根据《事业单位登记管理暂行条例》及其实施细则，登记机关对准予登记的事业单位颁发《事业单位法人证书》，该证书有效期为五年，有效期截止日前30日需换领新的证书。超过有效期的证书，自动废止。",
    "phoneSourceList" : [ {
      "phoneNumber" : "0379-6519****",
      "oriPhoneNumber" : null,
      "showSource" : "2024年报",
      "hasMoreCompany" : 0,
      "companyCount" : 0,
      "companyCountStr" : "0",
      "companyTotalStr" : "1",
      "phoneType" : 9,
      "phoneTips" : "固定电话或非大陆号码",
      "phoneTag" : null,
      "phoneTagList" : null,
      "reportYear" : "2024",
      "suspectedAccountTag" : null,
      "suspectedAccountTagUrl" : null,
      "companyName" : "河南中洛佳科技有限公司",
      "companyType" : 1,
      "gid" : 3398690435,
      "cid" : 209237739,
      "phoneTagType" : 2,
      "province" : "河南",
      "city" : "洛阳"
    } ],
    "abInfo" : [ {
      "abValue" : "0",
      "abKey" : "company_person_info_2025_pc"
    } ],
    "estiblishTime" : 1576512000000,
    "branchSocialStaffNum" : "0",
    "regInstitute" : "洛阳市洛龙区市场监督管理局",
    "listedStatusType" : 0,
    "parentId" : null,
    "websiteList" : null,
    "safetype" : "unknown",
    "legalPersonId" : 3398690439,
    "complexName" : "河南中洛佳科技有限公司",
    "companyProfileRichText" : [ {
      "title" : "基本信息",
      "content" : "河南中洛佳科技有限公司，成立于2019年，位于河南省洛阳市，是一家以从事<a href='https://www.tianyancha.com/advance/search/e-pc_homeicon'>软件和信息技术服务业</a>为主的企业。企业<a href='https://www.tianyancha.com/company/3398690435#baseInfo'>注册资本</a>1000万人民币，<a href='https://www.tianyancha.com/company/3398690435#baseInfo'>实缴资本</a>447.5万人民币。"
    }, {
      "title" : "营运状况",
      "content" : "通过天眼查大数据分析，河南中洛佳科技有限公司参与<a href='https://www.tianyancha.com/company/3398690435/jingzhuang#bid'>招投标</a>项目56次；知识产权方面有<a href='https://www.tianyancha.com/company/3398690435/zhishi#tm'>商标信息</a>10条，<a href='https://www.tianyancha.com/company/3398690435/zhishi#patentV4'>专利信息</a>2条，<a href='https://www.tianyancha.com/company/3398690435/zhishi#copyrightWorks'>著作权信息</a>39条；此外企业还拥有<a href='https://www.tianyancha.com/company/3398690435/jingzhuang#mergeLicense'>行政许可</a>2个。"
    } ],
    "staffNumInfo" : {
      "explainText" : "结合工商照面信息的人员规模及年报信息的参保人数综合计算得出。",
      "num" : 21,
      "branchNum" : 0,
      "year" : 2024,
      "source" : "工商年报",
      "sourceForApp" : "工商年报参保人数",
      "existBranch" : true,
      "route" : "https://nianbao.tianyancha.com/3398690435/2024",
      "showTrendChart" : true,
      "showBranchTrendChart" : true
    },
    "updatetime" : 1748312631000,
    "companyCreditCode" : "91410307MA47WC788D",
    "regStatus" : "存续",
    "listedPlateList" : [ ],
    "phoneList" : [ "0379-6519****" ],
    "legalPersonPid" : "N01U22F02LFUN1LLL",
    "sensitiveEntityType" : 4,
    "industryChainList" : [ {
      "code" : "473",
      "name" : "新一代信息技术"
    }, {
      "code" : "194",
      "name" : "数字"
    } ],
    "companyProfilePlainText" : "河南中洛佳科技有限公司，成立于2019年，位于河南省洛阳市，是一家以从事软件和信息技术服务业为主的企业。企业注册资本1000万人民币，实缴资本447.5万人民币。通过天眼查大数据分析，河南中洛佳科技有限公司参与招投标项目56次；知识产权方面有商标信息10条，专利信息2条，著作权信息39条；此外企业还拥有行政许可2个。",
    "approvedTime" : 1637251200000,
    "logo" : "",
    "originalPercentileScore" : 7332,
    "businessScope" : "一般项目：信息系统集成服务；仪器仪表销售；办公设备销售；机械设备销售；电子产品销售；通讯设备销售；通信设备销售；办公用品销售；建筑材料销售；信息安全设备销售；安防设备销售；第一类医疗器械销售；第二类医疗器械销售；软件销售；技术服务、技术开发、技术咨询、技术交流、技术转让、技术推广；网络技术服务；信息技术咨询服务；网络与信息安全软件开发；软件开发；信息系统运行维护服务；计算机系统服务；安全技术防范系统设计施工服务；交通及公共管理用金属标牌制造；交通及公共管理用标牌销售；商务代理代办服务（除依法须经批准的项目外，凭营业执照依法自主开展经营活动）许可项目：建筑智能化系统设计；依托实体医院的互联网医院服务；建设工程施工（依法须经批准的项目，经相关部门批准后方可开展经营活动，具体经营项目以相关部门批准文件或许可证件为准）",
    "regCapitalCurrency" : "人民币",
    "isBranch" : false,
    "regCapitalAmount" : "1,000",
    "phoneNumber" : "0379-6519****",
    "staffNumInfoListThreeYear" : [ {
      "showTrendChart" : true,
      "showBranchTrendChart" : true,
      "explainText" : "结合工商照面信息的人员规模及年报信息的参保人数综合计算得出。",
      "route" : "https://nianbao.tianyancha.com/3398690435/2024",
      "year" : 2024,
      "num" : 21,
      "branchNum" : 0,
      "source" : "工商年报",
      "sourceForApp" : "工商年报参保人数",
      "existBranch" : true
    }, {
      "showTrendChart" : true,
      "showBranchTrendChart" : true,
      "explainText" : "结合工商照面信息的人员规模及年报信息的参保人数综合计算得出。",
      "route" : "https://nianbao.tianyancha.com/3398690435/2023",
      "year" : 2023,
      "num" : 19,
      "branchNum" : 0,
      "source" : "工商年报",
      "sourceForApp" : "工商年报参保人数",
      "existBranch" : true
    }, {
      "showTrendChart" : true,
      "showBranchTrendChart" : true,
      "explainText" : "结合工商照面信息的人员规模及年报信息的参保人数综合计算得出。",
      "route" : "https://nianbao.tianyancha.com/3398690435/2022",
      "year" : 2022,
      "num" : 18,
      "branchNum" : 0,
      "source" : "工商年报",
      "sourceForApp" : "工商年报参保人数",
      "existBranch" : true
    } ],
    "name" : "河南中洛佳科技有限公司",
    "percentileScore" : 7332,
    "businessStatusInfo" : null,
    "showCapitalReductionNoticeCount" : false,
    "newCompanyIncome" : null,
    "regLocationTitle" : "注册地址",
    "wechatCount" : 0,
    "newCompanyProfit" : null,
    "link" : 1,
    "updateTime4Index" : 1748312631000,
    "legalTitleName" : "法定代表人",
    "regTitleName" : "注册资本",
    "updateTimes" : 1748312631000,
    "legalPersonName" : "马丽北",
    "operatingProfitInfo" : null,
    "creditCode" : "91410307MA47WC788D",
    "alias" : "中洛佳",
    "staffNumInfoList" : {
      "explainText" : "结合工商照面信息的人员规模及年报信息的参保人数综合计算得出。",
      "isPop" : true,
      "companyStaffs" : [ {
        "explainText" : "结合工商照面信息的人员规模及年报信息的参保人数综合计算得出。",
        "num" : 21,
        "branchNum" : 0,
        "year" : 2024,
        "source" : "工商年报",
        "sourceForApp" : "工商年报参保人数",
        "existBranch" : true,
        "route" : "https://nianbao.tianyancha.com/3398690435/2024",
        "showTrendChart" : true,
        "showBranchTrendChart" : true
      }, {
        "explainText" : "结合工商照面信息的人员规模及年报信息的参保人数综合计算得出。",
        "num" : 19,
        "branchNum" : 0,
        "year" : 2023,
        "source" : "工商年报",
        "sourceForApp" : "工商年报参保人数",
        "existBranch" : true,
        "route" : "https://nianbao.tianyancha.com/3398690435/2023",
        "showTrendChart" : true,
        "showBranchTrendChart" : true
      }, {
        "explainText" : "结合工商照面信息的人员规模及年报信息的参保人数综合计算得出。",
        "num" : 18,
        "branchNum" : 0,
        "year" : 2022,
        "source" : "工商年报",
        "sourceForApp" : "工商年报参保人数",
        "existBranch" : true,
        "route" : "https://nianbao.tianyancha.com/3398690435/2022",
        "showTrendChart" : true,
        "showBranchTrendChart" : true
      } ]
    },
    "showBranchTrendChart" : true,
    "companyType" : 1,
    "companyBizType" : 7,
    "regLocation" : "河南省洛阳市太康路与汇通街交叉口大数据产业园综合楼四楼405室",
    "regCapitalAmountUnit" : "万人民币",
    "tagList" : [ {
      "color" : "#119944",
      "background" : "#ECF7F0",
      "layerArray" : [ "企业依法存在并继续正常运营。" ],
      "sort" : 10,
      "title" : "存续",
      "type" : 1,
      "value" : "存续",
      "layer" : "企业依法存在并继续正常运营。"
    }, {
      "boxinfo" : {
        "imgTitle" : "天眼小课堂—高新技术企业认定",
        "videoImg" : "https://video.tianyancha.com/image/cover/8C68C0A2233C4174A4ACF06FFBE280C0-6-2.png",
        "eventType" : "highTechEnterprises"
      },
      "color" : "#D4AE46",
      "background" : "#FAF5E9",
      "sort" : 80,
      "title" : "高新技术企业",
      "type" : 11,
      "value" : "高新技术企业",
      "layer" : "指在《国家重点支持的高新技术领域》内，持续进行研究开发与技术成果转化，形成企业核心自主知识产权，并以此为基础开展经营活动，在中国境内（不包括港、澳、台地区）注册一年以上的居民企业。"
    }, {
      "color" : "#757DD3",
      "background" : "#EDEEF9",
      "sort" : 260,
      "title" : "小微企业",
      "type" : 12,
      "value" : "小微企业",
      "layer" : "小型微型企业概念来自工信部《中小企业划型标准规定》，具体标准根据企业从业人员、营业收入、资产总额等指标，结合行业特点制定。"
    } ],
    "regCapitalAmt" : 1000000000,
    "legalInfo" : {
      "serviceType" : 1,
      "hid" : 3398690439,
      "role" : null,
      "headUrl" : null,
      "pid" : null,
      "office" : [ {
        "area" : "河南",
        "total" : 2,
        "companyName" : "河南中洛佳科技有限公司",
        "cid" : 3398690435,
        "score" : 0,
        "state" : null
      } ],
      "typeJoin" : null,
      "companyNum" : 2,
      "bossCertificate" : -1,
      "coopCount" : 0,
      "serviceCount" : 2,
      "partners" : null,
      "officeV1" : [ {
        "area" : "河南",
        "total" : 2,
        "companyName" : "河南中洛佳科技有限公司",
        "cid" : 3398690435,
        "score" : 0,
        "state" : null
      } ],
      "companys" : null,
      "name" : "马丽北",
      "alias" : null,
      "event" : null,
      "introduction" : null,
      "partnerNum" : 0,
      "cid" : 3398690435
    },
    "base" : "hen"
  },
  "errorCode" : null,
  "errorMessage" : null
} 
        """.trimIndent()

        val parseObject = responseBody.parseObject<CompanyInfoRes>(
//        JSONReader.Feature.IgnoreNullPropertyValue
        )
        val data = parseObject.data
        val toBaseInfo = data.toBaseInfo2()
        println(toBaseInfo)
    }


}
