package lib.controllerIcons

import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.api.Texture2D
import godot.api.TextureRect
import godot.core.Vector2

@RegisterClass
class JvmControllerTextureRect : TextureRect() {

    enum class ShowMode {
        ANY,
        KEYBOARD_MOUSE,
        CONTROLLER,
    }

    enum class ForceType {
        NONE,
        KEYBOARD_MOUSE,
        CONTROLLER,
    }

    @Export
    @RegisterProperty
    var iconPath: String = ""
        set(value) {
            field = value
            refreshIcon()
        }

    @Export
    @RegisterProperty
    var showOnly: Int = ShowMode.ANY.ordinal
        set(value) {
            field = value
            refreshIcon()
        }

    @Export
    @RegisterProperty
    var forceType: Int = ForceType.NONE.ordinal
        set(value) {
            field = value
            refreshIcon()
        }

    @Export
    @RegisterProperty
    var maxWidth: Int = 40
        set(value) {
            field = value
            applyMaxWidth()
        }

    private var connectedIcons: JvmControllerIcons? = null

    @RegisterFunction
    override fun _ready() {
        refreshIcon()
        applyMaxWidth()
    }

    fun getTtsString(): String {
        val icons = JvmControllerIcons.instance ?: return ""
        return icons.parsePathToTts(iconPath, resolveInputType(icons), icons.lastController)
    }

    private fun refreshIcon() {
        val icons = JvmControllerIcons.instance ?: return
        ensureSignalConnection(icons)

        if (!canBeShown(icons)) {
            visible = false
            texture = null
            applyMaxWidth()
            return
        }

        visible = true
        texture = resolveTexture(icons)
        applyMaxWidth()
    }

    private fun resolveTexture(icons: JvmControllerIcons): Texture2D? {
        return icons.parsePath(iconPath, resolveInputType(icons), icons.lastController)
    }

    private fun resolveInputType(icons: JvmControllerIcons): JvmControllerIcons.InputType {
        return when (forceType) {
            ForceType.KEYBOARD_MOUSE.ordinal -> JvmControllerIcons.InputType.KEYBOARD_MOUSE
            ForceType.CONTROLLER.ordinal -> JvmControllerIcons.InputType.CONTROLLER
            else -> icons.lastInputType
        }
    }

    private fun canBeShown(icons: JvmControllerIcons): Boolean {
        return when (showOnly) {
            ShowMode.KEYBOARD_MOUSE.ordinal -> icons.lastInputType == JvmControllerIcons.InputType.KEYBOARD_MOUSE
            ShowMode.CONTROLLER.ordinal -> icons.lastInputType == JvmControllerIcons.InputType.CONTROLLER
            else -> true
        }
    }

    private fun applyMaxWidth() {
        if (!isInsideTree()) {
            return
        }

        if (maxWidth < 0) {
            expandMode = ExpandMode.KEEP_SIZE
            return
        }

        expandMode = ExpandMode.IGNORE_SIZE
        val targetHeight = if (texture != null && texture!!.getWidth() > 0) {
            texture!!.getHeight().toDouble() * maxWidth / texture!!.getWidth().toDouble()
        } else {
            maxWidth.toDouble()
        }
        customMinimumSize = Vector2(maxWidth.toDouble(), targetHeight)
    }

    private fun ensureSignalConnection(icons: JvmControllerIcons) {
        if (connectedIcons === icons) {
            return
        }

        connectedIcons?.input_type_changed?.disconnect(this, JvmControllerTextureRect::onInputTypeChanged)
        icons.input_type_changed.connect(this, JvmControllerTextureRect::onInputTypeChanged)
        connectedIcons = icons
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onInputTypeChanged(inputType: JvmControllerIcons.InputType, controller: Int) {
        refreshIcon()
    }
}

