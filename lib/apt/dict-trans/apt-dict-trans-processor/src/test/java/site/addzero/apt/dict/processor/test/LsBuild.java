
package site.addzero.apt.dict.processor.test;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import site.addzero.aop.dicttrans.anno.Dict;


/**
* <p>
    * 历史建筑
    * </p>
*
* @author zzsoft
* @since 2024-12-09
*/
public class LsBuild  {

      private String buildName;

      private String buildCode;

      @Dict("1871430807323938816")
      private String buildTime;

      @Dict("1866669529686609920")
      @site.addzero.aop.dicttrans.anno.Dict
      private String buildType;

      private String featureDesc;

      @Dict("1869581478762123264")
      private String natInfluence;

      @Dict("1869582488524034048")
      private String humanInfluence;

      @Dict("1869275077477142528")
      private String cityType;

      private String location;

      private BigDecimal floorArea;

      private BigDecimal buildArea;

      private BigDecimal buildHeight;

      private Integer buildStorey;

      private String buildStyle;

      private String saveStyleDesc;

      @Dict("1871434059478208512")
      private String structure;

      @Dict("1866677206542192640")
      private String ownership;

      private String ownerName;

      private String architectName;

      @Dict("1866678229050593280")
      private String quality;

      @Dict("1866679495650709504")
      private String usageStatus;

      private String historyDesc;

      private String saveStatusDesc;

      private String property_change_Desc;

      @Dict("1871432092806811648")
      private String valueEle;

      private String valueEleDesc;

      private String introduction;

      private String remarks;

      private String createuserName;

      private String useUserName;

      private String preparer;

      private LocalDateTime fillDate;

      private String reviewer;

      private String imageUrl;

      private BigDecimal longitude;

      private BigDecimal latitude;

      private String voiceUrl;

      private String docUrl;

      private String panoUrl;

      private String videoUrl;

      private String videoImageUrl;

      private String coverUrl;


      private String attachUrl;

      private List<SysUpload> docList;
}
