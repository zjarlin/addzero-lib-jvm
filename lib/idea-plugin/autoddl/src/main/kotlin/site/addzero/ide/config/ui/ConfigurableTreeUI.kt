package site.addzero.ide.config.ui

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import site.addzero.ide.config.core.SingletonConfigManager
import site.addzero.ide.config.registry.ConfigRegistry
import site.addzero.ide.config.scanner.ConfigScanner
import site.addzero.ide.ui.form.DynamicFormBuilder
import java.awt.BorderLayout
import java.awt.CardLayout
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel
import kotlin.reflect.full.primaryConstructor

/**
 * 树形配置界面
 */
class ConfigurableTreeUI : Configurable {
    private var panel: JPanel? = null
    private var tree: Tree? = null
    private var cardPanel: JPanel? = null
    private var cards: CardLayout? = null
    private val formBuilders = mutableMapOf<String, DynamicFormBuilder>()
    
    override fun getDisplayName(): String = "AutoDDL 配置"
    
    override fun getHelpTopic(): String? = null
    
    override fun createComponent(): JComponent? {
        // 扫描并注册配置
        ConfigScanner.scanAndRegisterConfigs()
        
        if (panel == null) {
            panel = JPanel(BorderLayout())
            
            // 创建左侧树形结构
            val treePanel = createTreePanel()
            
            // 创建右侧配置面板
            cardPanel = JPanel().apply {
                cards = CardLayout()
                layout = cards
            }
            
            // 添加分割面板
            val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePanel, cardPanel).apply {
                dividerLocation = 200
                isOneTouchExpandable = true
            }
            
            panel?.add(splitPane, BorderLayout.CENTER)
            
            // 初始化树和面板
            initializeTreeAndPanels()
        }
        
        return panel
    }
    
    private fun createTreePanel(): JPanel {
        val treePanel = JPanel(BorderLayout())
        treePanel.border = BorderFactory.createTitledBorder("配置项")
        
        // 创建根节点
        val root = DefaultMutableTreeNode("AutoDDL 配置")
        val treeModel = DefaultTreeModel(root)
        tree = Tree(treeModel).apply {
            selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
            isRootVisible = true
            showsRootHandles = true
            addTreeSelectionListener { event ->
                val node = event.path.lastPathComponent as? DefaultMutableTreeNode
                node?.userObject?.let { nodeName ->
                    // 构造完整路径
                    val path = buildPathFromNode(node)
                    val pathKey = path.joinToString(".")
                    
                    // 检查是否存在对应的配置面板
                    if (cardPanel?.getComponentCount() ?: 0 > 0) {
                        // 尝试显示对应的面板
                        try {
                            cards?.show(cardPanel, pathKey)
                        } catch (e: Exception) {
                            // 如果找不到对应的面板，显示第一个面板或默认面板
                            val configMap = ConfigRegistry.getRegisteredConfigs()
                            if (configMap.isNotEmpty()) {
                                val firstConfig = configMap.values.first()
                                val firstPathKey = firstConfig.path.joinToString(".")
                                cards?.show(cardPanel, firstPathKey)
                            }
                        }
                    }
                }
            }
        }
        
        val scrollPane = JBScrollPane(tree).apply {
            horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
            verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
        }
        
        treePanel.add(scrollPane, BorderLayout.CENTER)
        return treePanel
    }
    
    private fun buildPathFromNode(node: DefaultMutableTreeNode): List<String> {
        val path = mutableListOf<String>()
        var currentNode: DefaultMutableTreeNode? = node
        
        // 从当前节点向上遍历到根节点，构建路径
        while (currentNode != null && currentNode.userObject != "AutoDDL 配置") {
            path.add(0, currentNode.userObject.toString())
            currentNode = currentNode.parent as? DefaultMutableTreeNode
        }
        
        return path
    }
    
    private fun initializeTreeAndPanels() {
        val root = (tree?.model as? DefaultTreeModel)?.root as? DefaultMutableTreeNode
        root?.removeAllChildren()
        
        // 获取所有注册的配置
        val configMap = ConfigRegistry.getRegisteredConfigs()
        
        // 构建树结构
        buildTreeStructure(root, emptyList(), configMap)
        
        // 更新树模型
        (tree?.model as? DefaultTreeModel)?.reload()
        
        // 为每个配置创建面板
        configMap.forEach { (id, configInfo) ->
            val pathKey = configInfo.path.joinToString(".")
            val formBuilder = DynamicFormBuilder()
            val configPanel = createConfigPanel(configInfo, formBuilder)
            formBuilders[pathKey] = formBuilder
            
            // 加载配置数据
            val configInstance = SingletonConfigManager.getConfig(configInfo.configClass)
            formBuilder.setFormDataFromConfig(configInstance)
            
            cardPanel?.add(configPanel, pathKey)
        }
        
        // 如果有配置项，展开树并选择第一个叶节点
        if (configMap.isNotEmpty()) {
            // 展开所有节点
            for (i in 0 until (tree?.rowCount ?: 0)) {
                tree?.expandRow(i)
            }
            
            // 选择第一个配置项
            val firstConfig = configMap.values.first()
            val firstPathKey = firstConfig.path.joinToString(".")
            cards?.show(cardPanel, firstPathKey)
        }
    }
    
    private fun buildTreeStructure(
        parent: DefaultMutableTreeNode?,
        currentPath: List<String>,
        configMap: Map<String, site.addzero.ide.config.registry.ConfigRouteInfo>
    ) {
        if (parent == null) return
        
        // 获取当前路径层级的所有配置
        val configsAtThisLevel = configMap.values.filter { configInfo ->
            configInfo.path.size > currentPath.size && 
            configInfo.path.subList(0, currentPath.size) == currentPath
        }
        
        // 获取当前层级的唯一节点名称
        val nodeNamesAtThisLevel = configsAtThisLevel.map { 
            it.path[currentPath.size] 
        }.distinct()
        
        nodeNamesAtThisLevel.forEach { nodeName ->
            val node = DefaultMutableTreeNode(nodeName)
            parent.add(node)
            
            // 检查是否为叶节点（即有对应的实际配置）
            val isLeafConfig = configsAtThisLevel.any { 
                it.path.size == currentPath.size + 1 && it.path.last() == nodeName 
            }
            
            if (!isLeafConfig) {
                // 如果不是叶节点，继续递归构建子树
                val childPath = currentPath + nodeName
                buildTreeStructure(node, childPath, configMap)
            } else {
                // 如果是叶节点，确保该配置的面板已创建
                val configInfo = configsAtThisLevel.find { 
                    it.path.size == currentPath.size + 1 && it.path.last() == nodeName 
                }
                if (configInfo != null) {
                    val pathKey = configInfo.path.joinToString(".")
                    if (!formBuilders.containsKey(pathKey)) {
                        val formBuilder = DynamicFormBuilder()
                        val configPanel = createConfigPanel(configInfo, formBuilder)
                        formBuilders[pathKey] = formBuilder
                        
                        // 加载配置数据
                        val configInstance = SingletonConfigManager.getConfig(configInfo.configClass)
                        formBuilder.setFormDataFromConfig(configInstance)
                        
                        cardPanel?.add(configPanel, pathKey)
                    }
                }
            }
        }
    }
    
    private fun createConfigPanel(
        configInfo: site.addzero.ide.config.registry.ConfigRouteInfo, 
        formBuilder: DynamicFormBuilder
    ): JPanel {
        val panel = JPanel().apply {
            layout = BorderLayout()
            border = JBUI.Borders.empty(10)
        }
        
        try {
            // 确保配置项不为空
            if (configInfo.configItems.isNotEmpty()) {
                val formPanel = formBuilder.buildFormPanel(configInfo.configItems)
                panel.add(formPanel, BorderLayout.CENTER)
            } else {
                // 如果没有配置项，显示提示信息
                val emptyLabel = JLabel("该配置项没有可编辑的参数").apply {
                    horizontalAlignment = SwingConstants.CENTER
                }
                panel.add(emptyLabel, BorderLayout.CENTER)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val errorLabel = JLabel("无法创建配置面板: ${e.message}").apply {
                horizontalAlignment = SwingConstants.CENTER
            }
            panel.add(errorLabel, BorderLayout.CENTER)
        }
        
        return panel
    }
    
    override fun isModified(): Boolean {
        // 检查所有表单是否有修改
        return formBuilders.values.any { it.isModified() }
    }
    
    @Throws(ConfigurationException::class)
    override fun apply() {
        // 保存所有配置更改
        formBuilders.forEach { (pathKey, formBuilder) ->
            val configData = formBuilder.getFormData()
            // 这里应该保存配置数据到持久化存储
            // 例如：ConfigPersistenceManager.getInstance().saveConfig(pathKey, configData)
        }
    }
    
    override fun reset() {
        // 重置配置到初始状态
        val configMap = ConfigRegistry.getRegisteredConfigs()
        formBuilders.forEach { (pathKey, formBuilder) ->
            val configInfo = configMap.values.find { 
                it.path.joinToString(".") == pathKey 
            }
            if (configInfo != null) {
                val configInstance = SingletonConfigManager.getConfig(configInfo.configClass)
                formBuilder.setFormDataFromConfig(configInstance)
            }
        }
    }
    
    override fun disposeUIResources() {
        panel = null
        tree = null
        cardPanel = null
        formBuilders.clear()
    }
}