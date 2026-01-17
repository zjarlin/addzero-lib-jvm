package site.addzero.network.call.suno.model;

import com.alibaba.fastjson2.annotation.JSONField;

/**
 * 统一的 Suno 提交请求模型，支持灵感模式、自定义模式、续写模式、歌手风格、二次创作和拼接。
 */
public class SunoSubmitRequest {

  // --- 通用与基础字段 ---
  private String prompt;
  //  模型版本号
//支持模型
//chirp-v3-0 (对应版本 v3.0)
//chirp-v3-5 (对应版本 v3.5 )
//chirp-v4 (对应版本 v4.0 )
//chirp-auk (对应版本 v4.5 )
//chirp-v5 (对应版本 v5.0 )
  private String mv = "chirp-v4";
  //标题名称
  private String title;
  //  风格标签
  private String tags;
  //  任务类型，默认“延展”
  private String task; // "extend", "artist_consistency", "upload_extend", etc.

  // --- 灵感模式 (Inspiration Mode) ---
  @JSONField(name = "gpt_description_prompt")
//  创作描述提示词，仅用于灵感模式
  private String gptDescriptionPrompt;

  //是否生成纯音乐版本，true 表示生成纯音乐
  @JSONField(name = "make_instrumental")
  private Boolean makeInstrumental;

  // --- 续写/二次创作模式 (Extend / Remix Mode) ---
//  续写起始时间点
  @JSONField(name = "continue_at")
  private Integer continueAt;


  //需要续写的歌曲ID
  @JSONField(name = "continue_clip_id")
  private String continueClipId;

  // --- 歌手风格 (Artist Consistency) ---
  @JSONField(name = "generation_type")
  private String generationType; // e.g., "TEXT"

  /**
   * 负面标签，不希望出现的元素
   */
  @JSONField(name = "negative_tags")
  private String negativeTags;

/*
角色id

 B. 新建 Persona
clip_id 需要系统内存在的，非 uploader
不能跨账号，所以可能账号下线用不了
C. 使用 persona_id 创作
注意事项：
mv 为 chirp-v3-5-tau 或者 chirp-v4-tau
task 为 artist_consistency
persona_id 为 B 步骤得到的
artist_clip_id 就是 A 步骤中的 clip_id
可跨账号
*/

  @JSONField(name = "persona_id")
  private String personaId;
  //一个参考音频片段的 ID
  @JSONField(name = "artist_clip_id")
  private String artistClipId;

  //人声性别
  @JSONField(name = "vocal_gender")
  private String vocalGender;

  // --- 拼接歌曲 (Concat Mode) ---
  @JSONField(name = "clip_id")
  private String clipId;

  @JSONField(name = "is_infill")
  private Boolean isInfill;

  public SunoSubmitRequest() {
  }

  // Getters and Setters

  public String getPrompt() {
    return prompt;
  }

  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }

  public String getMv() {
    return mv;
  }

  public void setMv(String mv) {
    this.mv = mv;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTags() {
    return tags;
  }

  public void setTags(String tags) {
    this.tags = tags;
  }

  public String getTask() {
    return task;
  }

  public void setTask(String task) {
    this.task = task;
  }

  public String getGptDescriptionPrompt() {
    return gptDescriptionPrompt;
  }

  public void setGptDescriptionPrompt(String gptDescriptionPrompt) {
    this.gptDescriptionPrompt = gptDescriptionPrompt;
  }

  public Boolean getMakeInstrumental() {
    return makeInstrumental;
  }

  public void setMakeInstrumental(Boolean makeInstrumental) {
    this.makeInstrumental = makeInstrumental;
  }

  public Integer getContinueAt() {
    return continueAt;
  }

  public void setContinueAt(Integer continueAt) {
    this.continueAt = continueAt;
  }

  public String getContinueClipId() {
    return continueClipId;
  }

  public void setContinueClipId(String continueClipId) {
    this.continueClipId = continueClipId;
  }

  public String getGenerationType() {
    return generationType;
  }

  public void setGenerationType(String generationType) {
    this.generationType = generationType;
  }

  public String getNegativeTags() {
    return negativeTags;
  }

  public void setNegativeTags(String negativeTags) {
    this.negativeTags = negativeTags;
  }

  public String getPersonaId() {
    return personaId;
  }

  public void setPersonaId(String personaId) {
    this.personaId = personaId;
  }

  public String getArtistClipId() {
    return artistClipId;
  }

  public void setArtistClipId(String artistClipId) {
    this.artistClipId = artistClipId;
  }

  public String getVocalGender() {
    return vocalGender;
  }

  public void setVocalGender(String vocalGender) {
    this.vocalGender = vocalGender;
  }

  public String getClipId() {
    return clipId;
  }

  public void setClipId(String clipId) {
    this.clipId = clipId;
  }

  public Boolean getIsInfill() {
    return isInfill;
  }

  public void setIsInfill(Boolean isInfill) {
    this.isInfill = isInfill;
  }
}
