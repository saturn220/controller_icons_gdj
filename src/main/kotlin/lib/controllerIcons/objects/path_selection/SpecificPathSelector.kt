package lib.controllerIcons.objects.path_selection

import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterSignal
import godot.api.Button
import godot.api.HFlowContainer
import godot.api.LineEdit
import godot.api.Tree
import godot.api.TreeItem
import godot.core.signal0

@RegisterClass
class SpecificPathSelector : SelectorPanel() {

    @RegisterSignal
    val done by signal0()

    private var nameFilter: LineEdit? = null
    private var baseAssetNames: Tree? = null
    private var assetsContainer: HFlowContainer? = null

    private val iconButtons = mutableMapOf<String, Button>()
    private var root: TreeItem? = null

    var IsSignalsConnected: Boolean = false
        private set

    @RegisterFunction
    override fun _ready() {
        nameFilter = getNodeOrNull("%NameFilter") as? LineEdit
        baseAssetNames = getNodeOrNull("%BaseAssetNames") as? Tree
        assetsContainer = getNodeOrNull("%AssetsContainer") as? HFlowContainer
    }

    fun Populate(editorInterface: Any?) {
        nameFilter?.text = ""
        baseAssetNames?.clear()
        iconButtons.clear()

        for (child in assetsContainer?.getChildren() ?: emptyList()) {
            assetsContainer?.removeChild(child)
            child.queueFree()
        }

        root = baseAssetNames?.createItem()
        val noCategory = baseAssetNames?.createItem(root)
        noCategory?.setText(0, "<no category>")
        noCategory?.select(0)

        // Minimal static examples for selector preview.
        createIcon("joypad/a")
        createIcon("joypad/b")
        createIcon("key/space")

        IsSignalsConnected = true
        OnBaseAssetNamesItemSelected()
    }

    fun HookupSignals() {
        IsSignalsConnected = true
    }

    fun CleanupSignals() {
        IsSignalsConnected = false
    }

    private fun createIcon(path: String) {
        val button = Button()
        button.text = path.substringAfter('/')
        button.customMinimumSize = godot.core.Vector2(100.0, 100.0)
        button.toggleMode = true
        button.pressed.connect(this, SpecificPathSelector::onIconPressed)
        button.setMeta("icon_path", path)
        iconButtons[path] = button
        assetsContainer?.addChild(button)
    }

    private fun onIconPressed() {
        done.emit()
    }

    fun GetIconPath(): String {
        for ((path, button) in iconButtons) {
            if (button.buttonPressed) return path
        }
        return ""
    }

    override fun FocusSelector() {
        nameFilter?.grabFocus()
    }

    @RegisterFunction
    fun OnBaseAssetNamesItemSelected() {
        // Single category in MVP implementation.
        for (button in iconButtons.values) {
            button.visible = true
        }
    }

    @RegisterFunction
    fun OnNameFilterTextChanged(newText: String) {
        for ((path, button) in iconButtons) {
            val filtered = newText.isEmpty() || path.contains(newText, ignoreCase = true)
            button.visible = filtered
            button.disabled = !filtered
        }
    }
}


