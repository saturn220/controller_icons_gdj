package lib.controllerIcons

import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterSignal
import godot.api.DisplayServer
import godot.api.Engine
import godot.api.FileAccess
import godot.api.Image
import godot.api.ImageTexture
import godot.api.Input
import godot.api.InputEvent
import godot.api.InputEventJoypadButton
import godot.api.InputEventJoypadMotion
import godot.api.InputEventKey
import godot.api.InputEventMouseButton
import godot.api.InputEventMouseMotion
import godot.api.InputEventWithModifiers
import godot.api.InputMap
import godot.api.Node
import godot.api.OS
import godot.api.ProjectSettings
import godot.api.ResourceLoader
import godot.api.Texture2D
import godot.core.JoyAxis
import godot.core.JoyButton
import godot.core.Key
import godot.core.MouseButton
import godot.core.Error
import godot.core.Vector2
import godot.core.signal2
import kotlin.math.abs

@RegisterClass
class JvmControllerIcons : Node() {

    enum class InputType {
        KEYBOARD_MOUSE,
        CONTROLLER,
    }

    enum class PathType {
        INPUT_ACTION,
        JOYPAD_PATH,
        SPECIFIC_PATH,
    }

    companion object {
        var instance: JvmControllerIcons? = null
            private set
    }

    private val cachedIcons = mutableMapOf<String, Texture2D>()

    @RegisterSignal(parameters = ["input_type", "controller"])
    val input_type_changed by signal2<InputType, Int>()

    private val mapper = JvmControllerMapper()
    private val settings = JvmControllerSettings()
    private var baseExtension = "png"

    var lastInputType: InputType = InputType.KEYBOARD_MOUSE
        private set

    var lastController: Int = -1
        private set

    private var t: Double = 0.0
    private var mouseVelocity: Int = 0

    @RegisterFunction
    override fun _enterTree() {
        processMode = ProcessMode.ALWAYS
        instance = this
        if (Engine.isEditorHint()) {
            parseInputActions()
        }
    }

    @RegisterFunction
    override fun _exitTree() {
        if (instance == this) {
            instance = null
        }
    }

    @RegisterFunction
    override fun _ready() {
        if (settings.customFileExtension.isNotBlank()) {
            baseExtension = settings.customFileExtension
        }
        val joypads = Input.getConnectedJoypads()
        if (joypads.isEmpty()) {
            setLastInputType(InputType.KEYBOARD_MOUSE, -1)
        } else {
            setLastInputType(InputType.CONTROLLER, joypads.first().toInt())
        }
    }

    @RegisterFunction
    override fun _process(delta: Double) {
        t += delta
    }

    @RegisterFunction
    override fun _input(event: InputEvent?) {
        if (event == null) return

        var inputType = lastInputType
        var controller = lastController

        when (event) {
            is InputEventKey, is InputEventMouseButton -> inputType = InputType.KEYBOARD_MOUSE
            is InputEventMouseMotion -> {
                if (settings.allowMouseRemap && testMouseVelocity(event.relative)) {
                    inputType = InputType.KEYBOARD_MOUSE
                }
            }
            is InputEventJoypadButton -> {
                inputType = InputType.CONTROLLER
                controller = event.device
            }
            is InputEventJoypadMotion -> {
                if (abs(event.axisValue) > settings.joypadDeadzone) {
                    inputType = InputType.CONTROLLER
                    controller = event.device
                }
            }
        }

        if (inputType != lastInputType || controller != lastController) {
            setLastInputType(inputType, controller)
        }
    }

    fun refresh() {
        emitInputTypeChanged()
    }

    fun getPathType(path: String): PathType {
        return when {
            InputMap.hasAction(path) -> PathType.INPUT_ACTION
            path.startsWith("joypad/") -> PathType.JOYPAD_PATH
            else -> PathType.SPECIFIC_PATH
        }
    }

    fun getMatchingEvent(
        path: String,
        inputType: InputType = lastInputType,
        controller: Int = lastController,
    ): InputEvent? {
        val events = InputMap.actionGetEvents(path)
        val fallbacks = mutableListOf<InputEvent>()

        for (event in events) {
            when (event) {
                is InputEventKey, is InputEventMouseButton, is InputEventMouseMotion -> {
                    if (inputType == InputType.KEYBOARD_MOUSE) {
                        return event
                    }
                }
                is InputEventJoypadButton, is InputEventJoypadMotion -> {
                    if (inputType == InputType.CONTROLLER) {
                        if (event.device == controller) {
                            return event
                        }
                        if (event.device < 0) {
                            fallbacks.add(0, event)
                        } else {
                            fallbacks.add(event)
                        }
                    }
                }
            }
        }

        return fallbacks.firstOrNull()
    }

    fun parsePath(
        path: String,
        inputType: InputType = lastInputType,
        controller: Int = lastController,
        forcedControllerIconStyle: JvmControllerSettings.Devices = JvmControllerSettings.Devices.NONE,
    ): Texture2D? {
        val rootPaths = expandPath(path, inputType, controller, forcedControllerIconStyle)
        for (rootPath in rootPaths) {
            if (loadIcon(rootPath)) {
                return cachedIcons[rootPath]
            }
        }
        return null
    }

    fun parsePathToTts(
        path: String,
        inputType: InputType = lastInputType,
        controller: Int = lastController,
    ): String {
        val ttsPath = convertPathToAssetFile(path, inputType, controller)
        return convertAssetFileToTts(ttsPath.substringAfterLast('/').substringBeforeLast('.'))
    }

    fun parseEventModifiers(event: InputEvent?): List<Texture2D> {
        if (event !is InputEventWithModifiers) {
            return emptyList()
        }

        val modifiers = mutableListOf<String>()
        if (event.commandOrControlAutoremap) {
            modifiers += if (OS.getName() == "macOS") "key/command" else "key/ctrl"
        }
        if (event.ctrlPressed && !event.commandOrControlAutoremap) {
            modifiers += "key/ctrl"
        }
        if (event.shiftPressed) {
            modifiers += "key/shift"
        }
        if (event.altPressed) {
            modifiers += "key/alt"
        }
        if (event.metaPressed && !event.commandOrControlAutoremap) {
            modifiers += if (OS.getName() == "macOS") "key/command" else "key/win"
        }

        val icons = mutableListOf<Texture2D>()
        for (modifier in modifiers) {
            for (iconPath in expandPath(modifier, InputType.KEYBOARD_MOUSE, -1, JvmControllerSettings.Devices.NONE)) {
                if (loadIcon(iconPath)) {
                    cachedIcons[iconPath]?.let { icons += it }
                    break
                }
            }
        }

        return icons
    }

    private fun parseInputActions() {
        // Access ProjectSettings once in tool mode so editor actions are initialized similarly to the addon.
        ProjectSettings.getSetting("input/ui_accept")
    }

    private fun setLastInputType(inputType: InputType, controller: Int) {
        lastInputType = inputType
        lastController = controller
        emitInputTypeChanged()
    }

    private fun emitInputTypeChanged() {
        input_type_changed.emit(lastInputType, lastController)
    }

    private fun testMouseVelocity(relativeVec: Vector2): Boolean {
        if (t > 0.1) {
            t = 0.0
            mouseVelocity = 0
        }
        mouseVelocity += abs(relativeVec.x).toInt() + abs(relativeVec.y).toInt()
        return mouseVelocity / 0.1 > settings.mouseMinMovement
    }

    private fun expandPath(
        path: String,
        inputType: InputType,
        controller: Int,
        forcedControllerIconStyle: JvmControllerSettings.Devices,
    ): List<String> {
        val basePaths = listOf(
            settings.customAssetDir.takeIf { it.isNotBlank() }?.let { "$it/" },
            "res://addons/controller_icons/assets/",
        )

        val assetPath = convertPathToAssetFile(path, inputType, controller, forcedControllerIconStyle)
        return basePaths.filterNotNull().map { "$it$assetPath.$baseExtension" }
    }

    private fun convertPathToAssetFile(
        path: String,
        inputType: InputType,
        controller: Int,
        forcedControllerIconStyle: JvmControllerSettings.Devices = JvmControllerSettings.Devices.NONE,
    ): String {
        return when (getPathType(path)) {
            PathType.INPUT_ACTION -> {
                val event = getMatchingEvent(path, inputType, controller)
                if (event != null) convertEventToPath(event, controller, forcedControllerIconStyle) else path
            }
            PathType.JOYPAD_PATH -> mapper.convertJoypadPath(path, controller, settings.fallbackDevice(), forcedControllerIconStyle)
            PathType.SPECIFIC_PATH -> path
        }
    }

    private fun convertEventToPath(
        event: InputEvent,
        controller: Int = lastController,
        forcedControllerIconStyle: JvmControllerSettings.Devices = JvmControllerSettings.Devices.NONE,
    ): String {
        return when (event) {
            is InputEventKey -> {
                val keycode = if (event.keycode == Key.NONE) {
                    DisplayServer.keyboardGetKeycodeFromPhysical(event.physicalKeycode)
                } else {
                    event.keycode
                }
                convertKeyToPath(keycode)
            }
            is InputEventMouseButton -> convertMouseButtonToPath(event.buttonIndex)
            is InputEventJoypadButton -> convertJoypadButtonToPath(event.buttonIndex, controller, forcedControllerIconStyle)
            is InputEventJoypadMotion -> convertJoypadMotionToPath(event.axis, controller, forcedControllerIconStyle)
            else -> ""
        }
    }

    private fun convertKeyToPath(keycode: Key): String {
        return when (keycode) {
            Key.ESCAPE -> "key/esc"
            Key.TAB -> "key/tab"
            Key.BACKSPACE -> "key/backspace_alt"
            Key.ENTER -> "key/enter_alt"
            Key.KP_ENTER -> "key/enter_tall"
            Key.LEFT -> "key/arrow_left"
            Key.UP -> "key/arrow_up"
            Key.RIGHT -> "key/arrow_right"
            Key.DOWN -> "key/arrow_down"
            Key.SHIFT -> "key/shift_alt"
            Key.CTRL -> "key/ctrl"
            Key.ALT -> "key/alt"
            Key.SPACE -> "key/space"
            Key.A -> "key/a"
            Key.B -> "key/b"
            Key.C -> "key/c"
            Key.D -> "key/d"
            Key.E -> "key/e"
            Key.F -> "key/f"
            Key.G -> "key/g"
            Key.H -> "key/h"
            Key.I -> "key/i"
            Key.J -> "key/j"
            Key.K -> "key/k"
            Key.L -> "key/l"
            Key.M -> "key/m"
            Key.N -> "key/n"
            Key.O -> "key/o"
            Key.P -> "key/p"
            Key.Q -> "key/q"
            Key.R -> "key/r"
            Key.S -> "key/s"
            Key.T -> "key/t"
            Key.U -> "key/u"
            Key.V -> "key/v"
            Key.W -> "key/w"
            Key.X -> "key/x"
            Key.Y -> "key/y"
            Key.Z -> "key/z"
            Key.KEY_0, Key.KP_0 -> "key/0"
            Key.KEY_1, Key.KP_1 -> "key/1"
            Key.KEY_2, Key.KP_2 -> "key/2"
            Key.KEY_3, Key.KP_3 -> "key/3"
            Key.KEY_4, Key.KP_4 -> "key/4"
            Key.KEY_5, Key.KP_5 -> "key/5"
            Key.KEY_6, Key.KP_6 -> "key/6"
            Key.KEY_7, Key.KP_7 -> "key/7"
            Key.KEY_8, Key.KP_8 -> "key/8"
            Key.KEY_9, Key.KP_9 -> "key/9"
            else -> ""
        }
    }

    private fun convertMouseButtonToPath(buttonIndex: MouseButton): String {
        return when (buttonIndex) {
            MouseButton.LEFT -> "mouse/left"
            MouseButton.RIGHT -> "mouse/right"
            MouseButton.MIDDLE -> "mouse/middle"
            MouseButton.WHEEL_UP -> "mouse/wheel_up"
            MouseButton.WHEEL_DOWN -> "mouse/wheel_down"
            MouseButton.XBUTTON1 -> "mouse/side_down"
            MouseButton.XBUTTON2 -> "mouse/side_up"
            else -> "mouse/sample"
        }
    }

    private fun convertJoypadButtonToPath(
        buttonIndex: JoyButton,
        controller: Int,
        forcedControllerIconStyle: JvmControllerSettings.Devices,
    ): String {
        val path = when (buttonIndex) {
            JoyButton.A -> "joypad/a"
            JoyButton.B -> "joypad/b"
            JoyButton.X -> "joypad/x"
            JoyButton.Y -> "joypad/y"
            JoyButton.LEFT_SHOULDER -> "joypad/lb"
            JoyButton.RIGHT_SHOULDER -> "joypad/rb"
            JoyButton.LEFT_STICK -> "joypad/l_stick_click"
            JoyButton.RIGHT_STICK -> "joypad/r_stick_click"
            JoyButton.BACK -> "joypad/select"
            JoyButton.START -> "joypad/start"
            JoyButton.DPAD_UP -> "joypad/dpad_up"
            JoyButton.DPAD_DOWN -> "joypad/dpad_down"
            JoyButton.DPAD_LEFT -> "joypad/dpad_left"
            JoyButton.DPAD_RIGHT -> "joypad/dpad_right"
            JoyButton.GUIDE -> "joypad/home"
            JoyButton.MISC1 -> "joypad/share"
            else -> return ""
        }

        return mapper.convertJoypadPath(path, controller, settings.fallbackDevice(), forcedControllerIconStyle)
    }

    private fun convertJoypadMotionToPath(
        axis: JoyAxis,
        controller: Int,
        forcedControllerIconStyle: JvmControllerSettings.Devices,
    ): String {
        val path = when (axis) {
            JoyAxis.LEFT_X, JoyAxis.LEFT_Y -> "joypad/l_stick"
            JoyAxis.RIGHT_X, JoyAxis.RIGHT_Y -> "joypad/r_stick"
            JoyAxis.TRIGGER_LEFT -> "joypad/lt"
            JoyAxis.TRIGGER_RIGHT -> "joypad/rt"
            else -> return ""
        }
        return mapper.convertJoypadPath(path, controller, settings.fallbackDevice(), forcedControllerIconStyle)
    }

    private fun loadIcon(path: String): Boolean {
        if (cachedIcons.containsKey(path)) {
            return true
        }

        val texture: Texture2D? = if (path.startsWith("res://")) {
            val extension = path.substringAfterLast('.', "").lowercase()
            val canLoadAsImage = extension in setOf("png", "jpg", "jpeg", "webp", "bmp", "tga")

            if (canLoadAsImage && FileAccess.fileExists(path)) {
                val image = Image()
                if (image.load(path) != Error.OK) {
                    null
                } else {
                    ImageTexture.createFromImage(image)
                }
            } else if (!ResourceLoader.exists(path)) {
                null
            } else {
                ResourceLoader.load(path) as? Texture2D
            }
        } else {
            if (!FileAccess.fileExists(path)) {
                null
            } else {
                val image = Image()
                if (image.load(path) != Error.OK) {
                    null
                } else {
                    ImageTexture.createFromImage(image)
                }
            }
        }

        if (texture != null) {
            cachedIcons[path] = texture
            return true
        }
        return false
    }

    private fun convertAssetFileToTts(path: String): String {
        return when (path) {
            "shift_alt" -> "shift"
            "esc" -> "escape"
            "backspace_alt" -> "backspace"
            "enter_alt" -> "enter"
            "enter_tall" -> "keypad enter"
            "arrow_left" -> "left arrow"
            "arrow_right" -> "right arrow"
            "del" -> "delete"
            "arrow_up" -> "up arrow"
            "arrow_down" -> "down arrow"
            "ctrl" -> "control"
            "kp_add" -> "keypad plus"
            "mark_left" -> "left mark"
            "mark_right" -> "right mark"
            "bracket_left" -> "left bracket"
            "bracket_right" -> "right bracket"
            "tilda" -> "tilde"
            "lb" -> "left bumper"
            "rb" -> "right bumper"
            "lt" -> "left trigger"
            "rt" -> "right trigger"
            "l_stick_click" -> "left stick click"
            "r_stick_click" -> "right stick click"
            "l_stick" -> "left stick"
            "r_stick" -> "right stick"
            else -> path
        }
    }
}






