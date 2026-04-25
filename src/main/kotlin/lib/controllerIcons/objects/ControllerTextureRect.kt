package lib.controllerIcons.objects

import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.api.TextureRect
import godot.core.Vector2
import lib.controllerIcons.ControllerIcons

@RegisterClass
class ControllerTextureRect : TextureRect() {

    @Export
    @RegisterProperty
    var path: String = ""
        set(value) {
            field = value
            refreshTexture()
        }

    @Export
    @RegisterProperty
    var show_only: Int = 0
        set(value) {
            field = value
            refreshTexture()
        }

    @Export
    @RegisterProperty
    var force_type: Int = 0
        set(value) {
            field = value
            refreshTexture()
        }

    @Export
    @RegisterProperty
    var max_width: Int = 40
        set(value) {
            field = value
            applyMaxWidth()
        }

    private var connectedIcons: ControllerIcons? = null

    @RegisterFunction
    override fun _ready() {
        refreshTexture()
        applyMaxWidth()
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

    private fun refreshTexture() {
        val icons = ControllerIcons.instance ?: return
        ensureSignalConnection(icons)

        val show = when (show_only) {
            1 -> icons.lastInputType == ControllerIcons.InputType.KEYBOARD_MOUSE
            2 -> icons.lastInputType == ControllerIcons.InputType.CONTROLLER
            else -> true
        }
        if (!show) {
            visible = false
            texture = null
            applyMaxWidth()
            return
        }

        visible = true
        val inputType = when (force_type) {
            1 -> ControllerIcons.InputType.KEYBOARD_MOUSE
            2 -> ControllerIcons.InputType.CONTROLLER
            else -> icons.lastInputType
        }
        texture = icons.parsePath(path, inputType, icons.lastController)
        applyMaxWidth()
    }

    private fun applyMaxWidth() {
        if (!isInsideTree()) return
        if (max_width < 0) {
            expandMode = ExpandMode.KEEP_SIZE
            return
        }

        expandMode = ExpandMode.IGNORE_SIZE
        val h = if (texture != null && texture!!.getWidth() > 0) {
            texture!!.getHeight().toDouble() * max_width / texture!!.getWidth().toDouble()
        } else {
            max_width.toDouble()
        }
        customMinimumSize = Vector2(max_width.toDouble(), h)
    }

    private fun ensureSignalConnection(icons: ControllerIcons) {
        if (connectedIcons === icons) return
        connectedIcons?.input_type_changed?.disconnect(this, ControllerTextureRect::OnInputTypeChanged)
        icons.input_type_changed.connect(this, ControllerTextureRect::OnInputTypeChanged)
        connectedIcons = icons
    }

    @Suppress("UNUSED_PARAMETER")
    fun OnInputTypeChanged(inputType: Int, controller: Int) {
        refreshTexture()
    }
}


