package lib.controllerIcons

import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterProperty
import godot.api.Resource
import godot.api.Texture2D

@RegisterClass
class JvmControllerIconTexture : Resource() {

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

    enum class ForceDevice {
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
            reload()
        }

    @Export
    @RegisterProperty
    var showMode: Int = ShowMode.ANY.ordinal
        set(value) {
            field = value
            reload()
        }

    @Export
    @RegisterProperty
    var forceType: Int = ForceType.NONE.ordinal
        set(value) {
            field = value
            reload()
        }

    @Export
    @RegisterProperty
    var forceDevice: Int = ForceDevice.ANY.ordinal
        set(value) {
            field = value
            reload()
        }

    @Export
    @RegisterProperty
    var forceControllerIconStyle: Int = JvmControllerSettings.Devices.NONE.ordinal
        set(value) {
            field = value
            reload()
        }

    private var texture: Texture2D? = null
    private var lastInputType: JvmControllerIcons.InputType? = null
    private var lastController: Int = Int.MIN_VALUE
    private var connectedIcons: JvmControllerIcons? = null

    fun getTexture(): Texture2D? {
        if (texture == null) {
            reload()
        }
        return texture
    }

    fun refresh() {
        reload()
    }

    fun getTtsString(): String {
        val icons = JvmControllerIcons.instance ?: return ""
        val targetType = forcedInputType(icons)
        return icons.parsePathToTts(iconPath, targetType)
    }

    private fun canBeShown(icons: JvmControllerIcons): Boolean {
        return when (showMode) {
            ShowMode.KEYBOARD_MOUSE.ordinal -> icons.lastInputType == JvmControllerIcons.InputType.KEYBOARD_MOUSE
            ShowMode.CONTROLLER.ordinal -> icons.lastInputType == JvmControllerIcons.InputType.CONTROLLER
            else -> true
        }
    }

    private fun forcedInputType(icons: JvmControllerIcons): JvmControllerIcons.InputType {
        return when (forceType) {
            ForceType.KEYBOARD_MOUSE.ordinal -> JvmControllerIcons.InputType.KEYBOARD_MOUSE
            ForceType.CONTROLLER.ordinal -> JvmControllerIcons.InputType.CONTROLLER
            else -> icons.lastInputType
        }
    }

    private fun forcedDevice(icons: JvmControllerIcons): Int {
        return if (forceDevice == ForceDevice.ANY.ordinal) {
            icons.lastController
        } else {
            forceDevice
        }
    }

    private fun forcedStyle(): JvmControllerSettings.Devices {
        val values = JvmControllerSettings.Devices.entries
        return values.getOrElse(forceControllerIconStyle) { JvmControllerSettings.Devices.NONE }
    }

    private fun reload() {
        val icons = JvmControllerIcons.instance ?: return
        ensureSignalConnection(icons)

        lastInputType = icons.lastInputType
        lastController = icons.lastController

        texture = if (!canBeShown(icons)) {
            null
        } else {
            icons.parsePath(iconPath, forcedInputType(icons), forcedDevice(icons), forcedStyle())
        }

        emitChanged()
    }

    private fun ensureSignalConnection(icons: JvmControllerIcons) {
        if (connectedIcons === icons) {
            return
        }

        connectedIcons?.input_type_changed?.disconnect(this, JvmControllerIconTexture::onInputTypeChanged)
        icons.input_type_changed.connect(this, JvmControllerIconTexture::onInputTypeChanged)
        connectedIcons = icons
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onInputTypeChanged(inputType: Int, controller: Int) {
        reload()
    }
}
