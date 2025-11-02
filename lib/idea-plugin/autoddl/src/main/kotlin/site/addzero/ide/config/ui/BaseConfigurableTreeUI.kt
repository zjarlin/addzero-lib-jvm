package site.addzero.ide.config.ui

import site.addzero.ide.config.registry.ConfigRouteInfo
import site.addzero.ide.ui.form.DynamicFormBuilder
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree

/**
 * 基础的可配置树形设置面板类
 * 支持自定义显示名称和配置扫描逻辑
 */
abstract class BaseConfigurableTreeUI(
    protected val displayName: String,
    protected val configScanner: () -> Unit = {
        // 默认扫描逻辑
        // ConfigScanner.scanAndRegisterConfigs()
    }
) {
    
    init {
        // 执行配置扫描
        configScanner()
    }
    
    /**
     * 创建树形面板
     */
    open fun createTreePanel(): JBScrollPane {
        val root = DefaultMutableTreeNode(displayName)
        val treeModel = DefaultTreeModel(root)
        val tree = Tree(treeModel)
        return JBScrollPane(tree)
    }
    
    /**
     * 创建配置面板
     */
    open fun createConfigPanel(
        configInfo: ConfigRouteInfo,
        formBuilder: DynamicFormBuilder
    ): JPanel {
        return JPanel()
    }
}