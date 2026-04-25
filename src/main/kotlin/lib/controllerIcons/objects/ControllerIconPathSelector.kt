package lib.controllerIcons.objects

import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterSignal
import godot.api.PanelContainer
import godot.api.TabContainer
import godot.core.signal1
import lib.controllerIcons.objects.path_selection.InputActionSelector
import lib.controllerIcons.objects.path_selection.JoypadPathSelector
import lib.controllerIcons.objects.path_selection.SelectorPanel
import lib.controllerIcons.objects.path_selection.SpecificPathSelector

@RegisterClass
class ControllerIconPathSelector : PanelContainer() {

    @RegisterSignal(parameters = ["path"])
    val pathSelected by signal1<String>()

    var editorInterface: Any? = null

    private var tabContainer: TabContainer? = null
    private var inputActionSelector: InputActionSelector? = null
    private var joypadPathSelector: JoypadPathSelector? = null
    private var specificPathSelector: SpecificPathSelector? = null

    @RegisterFunction
    override fun _ready() {
        tabContainer = getNodeOrNull("%TabContainer") as? TabContainer
        inputActionSelector = getNodeOrNull("%Input Action") as? InputActionSelector
        joypadPathSelector = getNodeOrNull("%Joypad Path") as? JoypadPathSelector
        specificPathSelector = getNodeOrNull("%Specific Path") as? SpecificPathSelector
    }

    fun Populate(interfaceInstance: Any?) {
        editorInterface = interfaceInstance
        tabContainer?.currentTab = 0
    }

    fun GetIconPath(): String {
        val current = tabContainer?.getCurrentTabControl()
        return when (current) {
            is InputActionSelector -> current.GetIconPath()
            is JoypadPathSelector -> current.GetIconPath()
            is SpecificPathSelector -> current.GetIconPath()
            else -> ""
        }
    }

    fun Cleanup() {
        specificPathSelector?.CleanupSignals()
    }

    @RegisterFunction
    fun OnTabContainerTabSelected(tab: Int) {
        val current = tabContainer?.getCurrentTabControl()
        (current as? SelectorPanel)?.FocusSelector()
    }
}


