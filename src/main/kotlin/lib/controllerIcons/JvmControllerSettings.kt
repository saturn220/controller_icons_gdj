package lib.controllerIcons

import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterProperty
import godot.api.LabelSettings
import godot.api.Resource

@RegisterClass
class JvmControllerSettings : Resource() {

    enum class Devices {
        NONE,
        LUNA,
        OUYA,
        PS3,
        PS4,
        PS5,
        STADIA,
        STEAM,
        SWITCH,
        JOYCON,
        XBOX360,
        XBOXONE,
        XBOXSERIES,
        STEAM_DECK,
    }

    @Export
    @RegisterProperty
    var joypadFallback: Int = Devices.XBOX360.ordinal

    @Export
    @RegisterProperty
    var joypadDeadzone: Double = 0.5

    @Export
    @RegisterProperty
    var allowMouseRemap: Boolean = true

    @Export
    @RegisterProperty
    var mouseMinMovement: Int = 200

    @Export
    @RegisterProperty
    var customAssetDir: String = ""

    @Export
    @RegisterProperty
    var customFileExtension: String = ""

    @Export
    @RegisterProperty
    var customLabelSettings: LabelSettings? = null

    fun fallbackDevice(): Devices {
        val values = Devices.entries
        return values.getOrElse(joypadFallback) { Devices.XBOX360 }
    }
}


