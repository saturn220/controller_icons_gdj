package lib.controllerIcons.objects.path_selection

import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterSignal
import godot.api.Button
import godot.api.InputEvent
import godot.api.InputEventJoypadButton
import godot.api.InputEventJoypadMotion
import godot.api.Label
import godot.core.JoyAxis
import godot.core.JoyButton
import godot.core.signal0

@RegisterClass
class JoypadPathSelector : SelectorPanel() {

    @RegisterSignal
    val done by signal0()

    private var buttonLabel: Label? = null
    private val buttonPaths = mutableMapOf<String, String>()

    @RegisterFunction
    override fun _ready() {
        buttonLabel = getNodeOrNull("%ButtonLabel") as? Label

        // Minimal mapping for path selection.
        buttonPaths["%A"] = "joypad/a"
        buttonPaths["%B"] = "joypad/b"
        buttonPaths["%X"] = "joypad/x"
        buttonPaths["%Y"] = "joypad/y"
        buttonPaths["%LT"] = "joypad/lt"
        buttonPaths["%RT"] = "joypad/rt"
        buttonPaths["%LB"] = "joypad/lb"
        buttonPaths["%RB"] = "joypad/rb"
        buttonPaths["%LStick"] = "joypad/l_stick"
        buttonPaths["%RStick"] = "joypad/r_stick"
        buttonPaths["%LStickClick"] = "joypad/l_stick_click"
        buttonPaths["%RStickClick"] = "joypad/r_stick_click"
        buttonPaths["%Select"] = "joypad/select"
        buttonPaths["%Start"] = "joypad/start"
        buttonPaths["%Home"] = "joypad/home"
        buttonPaths["%Share"] = "joypad/share"
        buttonPaths["%DPAD"] = "joypad/dpad"
        buttonPaths["%DPADUp"] = "joypad/dpad_up"
        buttonPaths["%DPADDown"] = "joypad/dpad_down"
        buttonPaths["%DPADLeft"] = "joypad/dpad_left"
        buttonPaths["%DPADRight"] = "joypad/dpad_right"
    }

    fun Populate(editorInterface: Any?) {
        for (nodePath in buttonPaths.keys) {
            (getNodeOrNull(nodePath) as? Button)?.buttonPressed = false
        }
    }

    fun GetIconPath(): String {
        for ((nodePath, value) in buttonPaths) {
            if ((getNodeOrNull(nodePath) as? Button)?.buttonPressed == true) {
                return value
            }
        }
        return ""
    }

    @RegisterFunction
    override fun _input(event: InputEvent?) {
        if (!visible || event == null) return

        when (event) {
            is InputEventJoypadMotion -> {
                if (event.axisValue > 0.5f || event.axisValue < -0.5f) {
                    when (event.axis) {
                        JoyAxis.LEFT_X, JoyAxis.LEFT_Y -> markPressed("%LStick")
                        JoyAxis.RIGHT_X, JoyAxis.RIGHT_Y -> markPressed("%RStick")
                        JoyAxis.TRIGGER_LEFT -> markPressed("%LT")
                        JoyAxis.TRIGGER_RIGHT -> markPressed("%RT")
                        else -> Unit
                    }
                }
            }
            is InputEventJoypadButton -> {
                if (!event.pressed) return
                when (event.buttonIndex) {
                    JoyButton.A -> markPressed("%A")
                    JoyButton.B -> markPressed("%B")
                    JoyButton.X -> markPressed("%X")
                    JoyButton.Y -> markPressed("%Y")
                    JoyButton.LEFT_SHOULDER -> markPressed("%LB")
                    JoyButton.RIGHT_SHOULDER -> markPressed("%RB")
                    JoyButton.LEFT_STICK -> markPressed("%LStickClick")
                    JoyButton.RIGHT_STICK -> markPressed("%RStickClick")
                    JoyButton.BACK -> markPressed("%Select")
                    JoyButton.START -> markPressed("%Start")
                    JoyButton.GUIDE -> markPressed("%Home")
                    JoyButton.MISC1 -> markPressed("%Share")
                    JoyButton.DPAD_UP -> markPressed("%DPADUp")
                    JoyButton.DPAD_DOWN -> markPressed("%DPADDown")
                    JoyButton.DPAD_LEFT -> markPressed("%DPADLeft")
                    JoyButton.DPAD_RIGHT -> markPressed("%DPADRight")
                    else -> Unit
                }
            }
        }
    }

    private fun markPressed(nodePath: String) {
        (getNodeOrNull(nodePath) as? Button)?.buttonPressed = true
        buttonLabel?.text = "[$nodePath]"
    }

    @RegisterFunction
    fun OnButtonPressed() {
        done.emit()
    }

    override fun FocusSelector() {
        (getNodeOrNull("%A") as? Button)?.grabFocus()
    }

    // Compatibility methods referenced by tscn connections.
    @RegisterFunction fun _on_l_stick_pressed() { buttonLabel?.text = "[joypad/l_stick]" }
    @RegisterFunction fun _on_l_stick_click_pressed() { buttonLabel?.text = "[joypad/l_stick_click]" }
    @RegisterFunction fun _on_r_stick_pressed() { buttonLabel?.text = "[joypad/r_stick]" }
    @RegisterFunction fun _on_r_stick_click_pressed() { buttonLabel?.text = "[joypad/r_stick_click]" }
    @RegisterFunction fun _on_lb_pressed() { buttonLabel?.text = "[joypad/lb]" }
    @RegisterFunction fun _on_lt_pressed() { buttonLabel?.text = "[joypad/lt]" }
    @RegisterFunction fun _on_rb_pressed() { buttonLabel?.text = "[joypad/rb]" }
    @RegisterFunction fun _on_rt_pressed() { buttonLabel?.text = "[joypad/rt]" }
    @RegisterFunction fun _on_a_pressed() { buttonLabel?.text = "[joypad/a]" }
    @RegisterFunction fun _on_b_pressed() { buttonLabel?.text = "[joypad/b]" }
    @RegisterFunction fun _on_x_pressed() { buttonLabel?.text = "[joypad/x]" }
    @RegisterFunction fun _on_y_pressed() { buttonLabel?.text = "[joypad/y]" }
    @RegisterFunction fun _on_select_pressed() { buttonLabel?.text = "[joypad/select]" }
    @RegisterFunction fun _on_start_pressed() { buttonLabel?.text = "[joypad/start]" }
    @RegisterFunction fun _on_home_pressed() { buttonLabel?.text = "[joypad/home]" }
    @RegisterFunction fun _on_share_pressed() { buttonLabel?.text = "[joypad/share]" }
    @RegisterFunction fun _on_dpad_pressed() { buttonLabel?.text = "[joypad/dpad]" }
    @RegisterFunction fun _on_dpad_down_pressed() { buttonLabel?.text = "[joypad/dpad_down]" }
    @RegisterFunction fun _on_dpad_right_pressed() { buttonLabel?.text = "[joypad/dpad_right]" }
    @RegisterFunction fun _on_dpad_left_pressed() { buttonLabel?.text = "[joypad/dpad_left]" }
    @RegisterFunction fun _on_dpad_up_pressed() { buttonLabel?.text = "[joypad/dpad_up]" }
}


