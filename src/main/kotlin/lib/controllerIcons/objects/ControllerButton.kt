package lib.controllerIcons.objects

import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.api.Button
import lib.controllerIcons.ControllerIcons

@RegisterClass
class ControllerButton : Button() {

    @Export
    @RegisterProperty
    var path: String = ""
        set(value) {
            field = value
            refreshIcon()
        }

    @Export
    @RegisterProperty
    var show_only: Int = 0
        set(value) {
            field = value
            refreshIcon()
        }

    @Export
    @RegisterProperty
    var force_type: Int = 0
        set(value) {
            field = value
            refreshIcon()
        }

    private var connectedIcons: ControllerIcons? = null

    @RegisterFunction
    override fun _ready() {
        refreshIcon()
    }

    fun GetTTSString(): String {
        val icons = ControllerIcons.instance ?: return ""
        val inputType = when (force_type) {
            1 -> ControllerIcons.InputType.KEYBOARD_MOUSE
            2 -> ControllerIcons.InputType.CONTROLLER
            else -> icons.lastInputType
        }
        return icons.parsePathToTts(path, inputType, icons.lastController)
    }

    private fun refreshIcon() {
        val icons = ControllerIcons.instance ?: return
        ensureSignalConnection(icons)

        val show = when (show_only) {
            1 -> icons.lastInputType == ControllerIcons.InputType.KEYBOARD_MOUSE
            2 -> icons.lastInputType == ControllerIcons.InputType.CONTROLLER
            else -> true
        }
        if (!show) {
            visible = false
            icon = null
            return
        }

        visible = true
        val inputType = when (force_type) {
            1 -> ControllerIcons.InputType.KEYBOARD_MOUSE
            2 -> ControllerIcons.InputType.CONTROLLER
            else -> icons.lastInputType
        }
        icon = icons.parsePath(path, inputType, icons.lastController)
    }

    private fun ensureSignalConnection(icons: ControllerIcons) {
        if (connectedIcons === icons) return
        connectedIcons?.input_type_changed?.disconnect(this, ControllerButton::OnInputTypeChanged)
        icons.input_type_changed.connect(this, ControllerButton::OnInputTypeChanged)
        connectedIcons = icons
    }

    @Suppress("UNUSED_PARAMETER")
    fun OnInputTypeChanged(inputType: Int, controller: Int) {
        refreshIcon()
    }
}


