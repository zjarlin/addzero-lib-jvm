package cn.iocoder.yudao.framework.datapermission.core.rule.project;

/**
 * {@link ProjectDataPermissionRule} 的自定义配置接口
 *
 * @author 芋道源码
 */
@FunctionalInterface
public interface ProjectDataPermissionRuleCustomizer {

    /**
     * 自定义该权限规则
     * 1. 调用 {@link ProjectDataPermissionRule#addProjectColumn(Class, String)} 方法，配置基于 project_id 的过滤规则
     *
     * @param rule 权限规则
     */
    void customize(ProjectDataPermissionRule rule);

}
