package site.addzero.ide.config.ui

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import site.addzero.ide.config.registry.ConfigRegistry
import site.addzero.ide.config.registry.ConfigRouteInfo
import site.addzero.ide.ui.form.DynamicFormBuilder
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeSelectionModel

/**
 * 基础的可配置树形设置面板类
 * 支持自定义显示名称和配置扫描逻辑
 */
abstract class BaseConfigurableTreeUI(
    protected val labelName: String,

    protected val configScanner: () -> Unit = {
        // 默认扫描逻辑
        // ConfigScanner.scanAndRegisterConfigs()
    }
) : Configurable {

    private var mainPanel: JPanel? = null
    private var treePanel: JBScrollPane? = null
    private var configPanel: JPanel? = null
    private var currentConfigInfo: ConfigRouteInfo? = null
    override fun getDisplayName(): @NlsContexts.ConfigurableName String? {
        return labelName
    }

    init {
        // 执行配置扫描
        configScanner()
    }

    // 移除了显式的 getDisplayName() 方法，因为属性的 getter 已经提供了相同的功能

    override fun getHelpTopic(): String? = null

    override fun createComponent(): JComponent? {
        if (mainPanel == null) {
            mainPanel = JPanel(BorderLayout())

            // 创建左右分割面板
            val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
            splitPane.dividerSize = 3

            // 左侧树面板
            treePanel = createTreePanel()
            splitPane.leftComponent = treePanel

            // 右侧配置面板
            configPanel = JPanel(BorderLayout())
            configPanel?.add(JLabel("请选择一个配置项"), BorderLayout.CENTER)
            splitPane.rightComponent = configPanel

            mainPanel?.add(splitPane, BorderLayout.CENTER)

            // 设置分割比例
            SwingUtilities.invokeLater {
                splitPane.dividerLocation = 200
            }
        }
        return mainPanel
    }

    /**
     * 创建树形面板
     */
    open fun createTreePanel(): JBScrollPane {
        val root = createTreeModel()
        val tree = Tree(root)
        tree.isRootVisible = true
        tree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION

        // 添加节点选择监听器
        tree.addTreeSelectionListener { e ->
            val node = tree.lastSelectedPathComponent as? DefaultMutableTreeNode
            if (node != null && node.userObject is ConfigRouteInfo) {
                val configInfo = node.userObject as ConfigRouteInfo
                showConfigPanel(configInfo)
            }
        }

        // 添加双击展开/折叠功能
        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val selectedNode = tree.lastSelectedPathComponent as? DefaultMutableTreeNode
                    if (selectedNode != null) {
                        if (tree.isExpanded(tree.selectionPath)) {
                            tree.collapsePath(tree.selectionPath)
                        } else {
                            tree.expandPath(tree.selectionPath)
                        }
                    }
                }
            }
        })

        return JBScrollPane(tree)
    }

    /**
     * 创建树模型
     */
    private fun createTreeModel(): DefaultMutableTreeNode {
        val root = DefaultMutableTreeNode(displayName)
        val configs = ConfigRegistry.getRegisteredConfigs()

        // 按路径组织配置项
        val pathMap = mutableMapOf<String, DefaultMutableTreeNode>()
        pathMap[""] = root

        configs.values.forEach { configInfo ->
            val pathKey = configInfo.path.joinToString(".")
            val parentNode = getOrCreatePathNode(pathMap, configInfo.path, root)
            val configNode = DefaultMutableTreeNode(configInfo)
            parentNode.add(configNode)
        }

        return root
    }

    /**
     * 获取或创建路径节点
     */
    private fun getOrCreatePathNode(
        pathMap: MutableMap<String, DefaultMutableTreeNode>,
        path: List<String>,
        root: DefaultMutableTreeNode
    ): DefaultMutableTreeNode {
        if (path.isEmpty()) return root

        val currentPath = path.joinToString(".")
        if (pathMap.containsKey(currentPath)) {
            return pathMap[currentPath]!!
        }

        val parentPath = path.dropLast(1).joinToString(".")
        val parentNode = getOrCreatePathNode(pathMap, path.dropLast(1), root)
        val currentNode = DefaultMutableTreeNode(path.last())
        parentNode.add(currentNode)
        pathMap[currentPath] = currentNode

        return currentNode
    }

    /**
     * 显示配置面板
     */
    private fun showConfigPanel(configInfo: ConfigRouteInfo) {
        currentConfigInfo = configInfo
        val formBuilder = DynamicFormBuilder(configInfo.configItems)
        val panel = createConfigPanel(configInfo, formBuilder)
        configPanel?.removeAll()
        configPanel?.add(panel, BorderLayout.CENTER)
        configPanel?.revalidate()
        configPanel?.repaint()
    }

    /**
     * 创建配置面板
     */
    open fun createConfigPanel(
        configInfo: ConfigRouteInfo,
        formBuilder: DynamicFormBuilder
    ): JPanel {
        // 使用 DynamicFormBuilder 构建表单
        return formBuilder.build()
    }

    override fun isModified(): Boolean = false

    @Throws(ConfigurationException::class)
    override fun apply() {
        // 应用配置更改
    }

    override fun reset() {
        // 重置配置
    }

    override fun disposeUIResources() {
        mainPanel = null
        treePanel = null
        configPanel = null
    }
}
