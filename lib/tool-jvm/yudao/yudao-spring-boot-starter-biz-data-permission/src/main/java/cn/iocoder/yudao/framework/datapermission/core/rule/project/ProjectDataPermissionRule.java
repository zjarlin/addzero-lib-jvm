package cn.iocoder.yudao.framework.datapermission.core.rule.project;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;
import cn.iocoder.yudao.framework.common.biz.system.permission.dto.ProjectDataPermissionRespDTO;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.datapermission.core.rule.DataPermissionRule;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@Slf4j
public class ProjectDataPermissionRule implements DataPermissionRule {
    /**
     * LoginUser 的 Context 缓存 Key
     */
    protected static final String CONTEXT_KEY = ProjectDataPermissionRule.class.getSimpleName();

    private static final String PROJECT_COLUMN_NAME = "project_id";

    static final Expression EXPRESSION_NULL = new NullValue();

    private final PermissionCommonApi permissionApi;


    /**
     * 基于项目的表字段配置
     * 一般情况下，每个表的项目编号字段是 project_id，通过该配置自定义。
     *
     * key：表名
     * value：字段名
     */
    private final Map<String, String> projectColumns = new HashMap<>();

    /**
     * 所有表名
     */
    private final Set<String> TABLE_NAMES = new HashSet<>();

    @Override
    public Set<String> getTableNames() {
        return TABLE_NAMES;
    }

    @Override
    public Expression getExpression(String tableName, Alias tableAlias) {
        // 只有有登陆用户的情况下，才进行数据权限的处理
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null) {
            return null;
        }
        // 只有管理员类型的用户，才进行数据权限的处理
        if (ObjectUtil.notEqual(loginUser.getUserType(), UserTypeEnum.ADMIN.getValue())) {
            return null;
        }

        // 获得数据权限
        ProjectDataPermissionRespDTO projectDataPermission = loginUser.getContext(CONTEXT_KEY, ProjectDataPermissionRespDTO.class);
        // 从上下文中拿不到，则调用逻辑进行获取
        if (projectDataPermission == null) {
            projectDataPermission = permissionApi.getProjectDataPermission(loginUser.getId()).getCheckedData();
            if (projectDataPermission == null) {
                log.error("[getExpression][LoginUser({}) 获取数据权限为 null]", JsonUtils.toJsonString(loginUser));
                throw new NullPointerException(String.format("LoginUser(%d) Table(%s/%s) 未返回数据权限",
                        loginUser.getId(), tableName, tableAlias.getName()));
            }
            // 添加到上下文中，避免重复计算
            loginUser.setContext(CONTEXT_KEY, projectDataPermission);
        }

        // 情况一，如果是 ALL 可查看全部，则无需拼接条件
        if (projectDataPermission.getAll()) {
            return null;
        }

        // 情况二，拼接条件
        Expression projectExpression = buildProjectExpression(tableName,tableAlias, projectDataPermission.getProjectIds());
        if (projectExpression == null) {
            // TODO 芋艿：获得不到条件的时候，暂时不抛出异常，而是不返回数据
            log.warn("[getExpression][LoginUser({}) Table({}/{}) DeptDataPermission({}) 构建的条件为空]",
                    JsonUtils.toJsonString(loginUser), tableName, tableAlias, JsonUtils.toJsonString(projectDataPermission));
//            throw new NullPointerException(String.format("LoginUser(%d) Table(%s/%s) 构建的条件为空",
//                    loginUser.getId(), tableName, tableAlias.getName()));
            return EXPRESSION_NULL;
        }

        return projectExpression;
    }

    private Expression buildProjectExpression(String tableName, Alias tableAlias, Set<Long> deptIds) {
        // 如果不存在配置，则无需作为条件
        String columnName = projectColumns.get(tableName);
        if (StrUtil.isEmpty(columnName)) {
            return null;
        }
        // 如果为空，则无条件
        if (CollUtil.isEmpty(deptIds)) {
            return null;
        }
        // 拼接条件
        return new InExpression(MyBatisUtils.buildColumn(tableName, tableAlias, columnName),
                // Parenthesis 的目的，是提供 (1,2,3) 的 () 左右括号
                new ParenthesedExpressionList(new ExpressionList<LongValue>(CollectionUtils.convertList(deptIds, LongValue::new))));
    }

    public void addProjectColumn(Class<? extends BaseDO> entityClass) {
        addProjectColumn(entityClass, PROJECT_COLUMN_NAME);
    }

    public void addProjectColumn(Class<? extends BaseDO> entityClass, String columnName) {
        String tableName = TableInfoHelper.getTableInfo(entityClass).getTableName();
        addProjectColumn(tableName, columnName);
    }

    public void addProjectColumn(String tableName, String columnName) {
        projectColumns.put(tableName, columnName);
        TABLE_NAMES.add(tableName);
    }
}
