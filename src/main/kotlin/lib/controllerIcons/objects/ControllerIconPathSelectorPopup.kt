package lib.controllerIcons.objects

import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterSignal
import godot.api.ConfirmationDialog
import godot.core.signal1

@RegisterClass
class ControllerIconPathSelectorPopup : ConfirmationDialog() {

    @RegisterSignal(parameters = ["path"])
    val pathSelected by signal1<String>()

    var editorInterface: Any? = null

    private var selector: ControllerIconPathSelector? = null

    @RegisterFunction
    override fun _ready() {
        selector = getNodeOrNull("ControllerIconPathSelector") as? ControllerIconPathSelector
    }

    fun Populate() {
        selector?.Populate(editorInterface)
    }

    @RegisterFunction
    fun OnConfirmed() {
        pathSelected.emit(selector?.GetIconPath() ?: "")
        selector?.Cleanup()
    }

    @RegisterFunction
    fun OnCancelled() {
        selector?.Cleanup()
    }

    fun OnControllerIconPathSelectorPathSelected(path: String) {
        pathSelected.emit(path)
        selector?.Cleanup()
        hide()
    }
}


