package lib.controllerIcons.objects

import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.api.Texture2D
import godot.core.Color
import godot.core.Rect2
import godot.core.RID
import godot.core.Vector2
import lib.controllerIcons.ControllerIcons
import lib.controllerIcons.ControllerSettings

@RegisterClass
class ControllerIconTexture : Texture2D() {

    enum class EShowMode {
        ANY,
        KEYBOARD_MOUSE,
        CONTROLLER,
    }

    enum class EInputType {
        NONE,
        KEYBOARD_MOUSE,
        CONTROLLER,
    }

    enum class EForceDevice {
        DEVICE_0,
        DEVICE_1,
        DEVICE_2,
        DEVICE_3,
        DEVICE_4,
        DEVICE_5,
        DEVICE_6,
        DEVICE_7,
        DEVICE_8,
        DEVICE_9,
        DEVICE_10,
        DEVICE_11,
        DEVICE_12,
        DEVICE_13,
        DEVICE_14,
        DEVICE_15,
        ANY,
    }

    @Export
    @RegisterProperty
    var iconPath: String = ""
        set(value) {
            field = value
            loadTexturePath()
        }

    @Export
    @RegisterProperty
    var show_mode: Int = EShowMode.ANY.ordinal
        set(value) {
            field = value
            loadTexturePath()
        }

    @Export
    @RegisterProperty
    var force_controller_icon_style: Int = ControllerSettings.Devices.NONE.ordinal
        set(value) {
            field = value
            loadTexturePath()
        }

    @Export
    @RegisterProperty
    var force_type: Int = EInputType.NONE.ordinal
        set(value) {
            field = value
            loadTexturePath()
        }

    @Export
    @RegisterProperty
    var force_device: Int = EForceDevice.ANY.ordinal
        set(value) {
            field = value
            loadTexturePath()
        }

    private var texture: Texture2D? = null
    private var connectedIcons: ControllerIcons? = null

    fun GetTTSString(): String {
        val icons = ControllerIcons.instance ?: return ""
        return icons.parsePathToTts(iconPath, resolveInputType(icons), resolveController(icons))
    }

    private fun canBeShown(icons: ControllerIcons): Boolean {
        return when (show_mode) {
            EShowMode.KEYBOARD_MOUSE.ordinal -> icons.lastInputType == ControllerIcons.InputType.KEYBOARD_MOUSE
            EShowMode.CONTROLLER.ordinal -> icons.lastInputType == ControllerIcons.InputType.CONTROLLER
            else -> true
        }
    }

    private fun resolveInputType(icons: ControllerIcons): ControllerIcons.InputType {
        return when (force_type) {
            EInputType.KEYBOARD_MOUSE.ordinal -> ControllerIcons.InputType.KEYBOARD_MOUSE
            EInputType.CONTROLLER.ordinal -> ControllerIcons.InputType.CONTROLLER
            else -> icons.lastInputType
        }
    }

    private fun resolveController(icons: ControllerIcons): Int {
        return if (force_device == EForceDevice.ANY.ordinal) icons.lastController else force_device
    }

    private fun resolveStyle(): ControllerSettings.Devices {
        val values = ControllerSettings.Devices.entries
        return values.getOrElse(force_controller_icon_style) { ControllerSettings.Devices.NONE }
    }

    private fun loadTexturePath() {
        val icons = ControllerIcons.instance ?: return
        ensureSignalConnection(icons)

        texture = if (!canBeShown(icons)) {
            null
        } else {
            icons.parsePath(iconPath, resolveInputType(icons), resolveController(icons), resolveStyle())
        }

        emitChanged()
    }

    private fun ensureSignalConnection(icons: ControllerIcons) {
        if (connectedIcons === icons) return
        connectedIcons?.input_type_changed?.disconnect(this, ControllerIconTexture::OnInputTypeChanged)
        icons.input_type_changed.connect(this, ControllerIconTexture::OnInputTypeChanged)
        connectedIcons = icons
    }

    @Suppress("UNUSED_PARAMETER")
    fun OnInputTypeChanged(inputType: Int, controller: Int) {
        loadTexturePath()
    }

    @RegisterFunction
    override fun _getWidth(): Int = texture?.getWidth() ?: 2

    @RegisterFunction
    override fun _getHeight(): Int = texture?.getHeight() ?: 2

    @RegisterFunction
    override fun _hasAlpha(): Boolean = texture?.hasAlpha() ?: true

    @RegisterFunction
    override fun _isPixelOpaque(x: Int, y: Int): Boolean = true

    @RegisterFunction
    override fun _draw(toCanvasItem: RID, pos: Vector2, modulate: Color, transpose: Boolean) {
        texture?.draw(toCanvasItem, pos, modulate, transpose)
    }

    @RegisterFunction
    override fun _drawRect(toCanvasItem: RID, rect: Rect2, tile: Boolean, modulate: Color, transpose: Boolean) {
        texture?.drawRect(toCanvasItem, rect, tile, modulate, transpose)
    }

    @RegisterFunction
    override fun _drawRectRegion(
        toCanvasItem: RID,
        rect: Rect2,
        srcRect: Rect2,
        modulate: Color,
        transpose: Boolean,
        clipUv: Boolean,
    ) {
        texture?.drawRectRegion(toCanvasItem, rect, srcRect, modulate, transpose, clipUv)
    }
}


