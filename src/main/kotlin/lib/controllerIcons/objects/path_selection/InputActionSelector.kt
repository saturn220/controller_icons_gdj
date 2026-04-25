package lib.controllerIcons.objects.path_selection

import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterSignal
import godot.api.CheckButton
import godot.api.InputMap
import godot.api.LineEdit
import godot.api.Tree
import godot.api.TreeItem
import godot.core.signal0

@RegisterClass
class InputActionSelector : SelectorPanel() {

    @RegisterSignal
    val done by signal0()

    private var nameFilter: LineEdit? = null
    private var builtinActionButton: CheckButton? = null
    private var tree: Tree? = null
    private var root: TreeItem? = null

    @RegisterFunction
    override fun _ready() {
        nameFilter = getNodeOrNull("%NameFilter") as? LineEdit
        builtinActionButton = getNodeOrNull("%BuiltinActionButton") as? CheckButton
        tree = getNodeOrNull("%Tree") as? Tree
    }

    fun Populate(editorInterface: Any?) {
        tree?.clear()
        root = tree?.createItem()

        for (action in InputMap.getActions()) {
            val item = tree?.createItem(root)
            item?.setText(0, action.toString())
        }

        OnNameFilterTextChanged(nameFilter?.text ?: "")
    }

    fun GetIconPath(): String {
        return tree?.getSelected()?.getText(0) ?: ""
    }

    override fun FocusSelector() {
        nameFilter?.grabFocus()
    }

    @RegisterFunction
    fun OnBuiltInActionButtonToggled(toggledOn: Boolean) {
        // Kept for tscn signal compatibility.
    }

    @RegisterFunction
    fun OnTreeItemActivated() {
        done.emit()
    }

    @RegisterFunction
    fun OnNameFilterTextChanged(newText: String) {
        var item = root?.getFirstChild()
        while (item != null) {
            val text = item.getText(0)
            item.visible = newText.isEmpty() || text.contains(newText, ignoreCase = true)
            item = item.getNext()
        }
    }
}


