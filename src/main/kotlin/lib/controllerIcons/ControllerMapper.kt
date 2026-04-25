package lib.controllerIcons

import godot.annotation.RegisterClass
import godot.api.Input
import godot.api.RefCounted

@RegisterClass
class ControllerMapper : RefCounted() {

    fun convertJoypadPath(
        path: String,
        device: Int,
        fallback: ControllerSettings.Devices,
        forceControllerIconStyle: ControllerSettings.Devices = ControllerSettings.Devices.NONE,
    ): String {
        return when (getJoypadType(device, fallback, forceControllerIconStyle)) {
            ControllerSettings.Devices.LUNA -> convertJoypadToLuna(path)
            ControllerSettings.Devices.PS3 -> convertJoypadToPs3(path)
            ControllerSettings.Devices.PS4 -> convertJoypadToPs4(path)
            ControllerSettings.Devices.PS5 -> convertJoypadToPs5(path)
            ControllerSettings.Devices.STADIA -> convertJoypadToStadia(path)
            ControllerSettings.Devices.STEAM -> convertJoypadToSteam(path)
            ControllerSettings.Devices.SWITCH -> convertJoypadToSwitch(path)
            ControllerSettings.Devices.JOYCON -> convertJoypadToJoycon(path)
            ControllerSettings.Devices.XBOX360 -> convertJoypadToXbox360(path)
            ControllerSettings.Devices.XBOXONE -> convertJoypadToXboxOne(path)
            ControllerSettings.Devices.XBOXSERIES -> convertJoypadToXboxSeries(path)
            ControllerSettings.Devices.STEAM_DECK -> convertJoypadToSteamDeck(path)
            ControllerSettings.Devices.OUYA -> convertJoypadToOuya(path)
            ControllerSettings.Devices.NONE -> ""
        }
    }

    fun getJoypadType(
        device: Int,
        fallback: ControllerSettings.Devices,
        forceControllerIconStyle: ControllerSettings.Devices = ControllerSettings.Devices.NONE,
    ): ControllerSettings.Devices {
        if (forceControllerIconStyle != ControllerSettings.Devices.NONE) {
            return forceControllerIconStyle
        }

        val available = Input.getConnectedJoypads()
        if (available.isEmpty()) {
            return fallback
        }

        var deviceToUse = device
        if (!available.contains(deviceToUse.toLong())) {
            deviceToUse = ControllerIcons.instance?.lastController ?: deviceToUse
        }
        if (!available.contains(deviceToUse.toLong())) {
            deviceToUse = available.first().toInt()
        }

        val controllerName = Input.getJoyName(deviceToUse)
        return when {
            controllerName.contains("Luna Controller") -> ControllerSettings.Devices.LUNA
            controllerName.contains("PS3 Controller") -> ControllerSettings.Devices.PS3
            controllerName.contains("PS4 Controller") || controllerName.contains("DUALSHOCK 4") -> ControllerSettings.Devices.PS4
            controllerName.contains("PS5 Controller") || controllerName.contains("DualSense") -> ControllerSettings.Devices.PS5
            controllerName.contains("Stadia Controller") -> ControllerSettings.Devices.STADIA
            controllerName.contains("Steam Controller") -> ControllerSettings.Devices.STEAM
            controllerName.contains("Switch Controller") || controllerName.contains("Switch Pro Controller") -> ControllerSettings.Devices.SWITCH
            controllerName.contains("Joy-Con") -> ControllerSettings.Devices.JOYCON
            controllerName.contains("Xbox 360 Controller") -> ControllerSettings.Devices.XBOX360
            controllerName.contains("Xbox One") || controllerName.contains("X-Box One") || controllerName.contains("Xbox Wireless Controller") -> ControllerSettings.Devices.XBOXONE
            controllerName.contains("Xbox Series") -> ControllerSettings.Devices.XBOXSERIES
            controllerName.contains("Steam Deck") || controllerName.contains("Steam Virtual Gamepad") -> ControllerSettings.Devices.STEAM_DECK
            controllerName.contains("OUYA Controller") -> ControllerSettings.Devices.OUYA
            else -> fallback
        }
    }

    private fun pathSuffix(path: String): String = path.substringAfter('/', "")

    private fun convertJoypadToLuna(path: String): String {
        val p = path.replace("joypad", "luna")
        return when (pathSuffix(p)) {
            "select" -> p.replace("/select", "/circle")
            "start" -> p.replace("/start", "/menu")
            "share" -> p.replace("/share", "/microphone")
            else -> p
        }
    }

    private fun convertJoypadToPlaystation(path: String): String {
        return when (pathSuffix(path)) {
            "a" -> path.replace("/a", "/cross")
            "b" -> path.replace("/b", "/circle")
            "x" -> path.replace("/x", "/square")
            "y" -> path.replace("/y", "/triangle")
            "lb" -> path.replace("/lb", "/l1")
            "rb" -> path.replace("/rb", "/r1")
            "lt" -> path.replace("/lt", "/l2")
            "rt" -> path.replace("/rt", "/r2")
            else -> path
        }
    }

    private fun convertJoypadToPs3(path: String): String = convertJoypadToPlaystation(path.replace("joypad", "ps3"))

    private fun convertJoypadToPs4(path: String): String {
        val p = convertJoypadToPlaystation(path.replace("joypad", "ps4"))
        return when (pathSuffix(p)) {
            "select" -> p.replace("/select", "/share")
            "start" -> p.replace("/start", "/options")
            "share" -> p.replace("/share", "/")
            else -> p
        }
    }

    private fun convertJoypadToPs5(path: String): String {
        val p = convertJoypadToPlaystation(path.replace("joypad", "ps5"))
        return when (pathSuffix(p)) {
            "select" -> p.replace("/select", "/share")
            "start" -> p.replace("/start", "/options")
            "home" -> p.replace("/home", "/assistant")
            "share" -> p.replace("/share", "/microphone")
            else -> p
        }
    }

    private fun convertJoypadToStadia(path: String): String {
        val p = path.replace("joypad", "stadia")
        return when (pathSuffix(p)) {
            "lb" -> p.replace("/lb", "/l1")
            "rb" -> p.replace("/rb", "/r1")
            "lt" -> p.replace("/lt", "/l2")
            "rt" -> p.replace("/rt", "/r2")
            "select" -> p.replace("/select", "/dots")
            "start" -> p.replace("/start", "/menu")
            "share" -> p.replace("/share", "/select")
            else -> p
        }
    }

    private fun convertJoypadToSteam(path: String): String {
        val p = path.replace("joypad", "steam")
        return when (pathSuffix(p)) {
            "r_stick_click" -> p.replace("/r_stick_click", "/right_track_center")
            "select" -> p.replace("/select", "/back")
            "home" -> p.replace("/home", "/system")
            "dpad" -> p.replace("/dpad", "/left_track")
            "dpad_up" -> p.replace("/dpad_up", "/left_track_up")
            "dpad_down" -> p.replace("/dpad_down", "/left_track_down")
            "dpad_left" -> p.replace("/dpad_left", "/left_track_left")
            "dpad_right" -> p.replace("/dpad_right", "/left_track_right")
            "l_stick" -> p.replace("/l_stick", "/stick")
            "r_stick" -> p.replace("/r_stick", "/right_track")
            else -> p
        }
    }

    private fun convertJoypadToSwitch(path: String): String {
        val p = path.replace("joypad", "switch")
        return when (pathSuffix(p)) {
            "a" -> p.replace("/a", "/b")
            "b" -> p.replace("/b", "/a")
            "x" -> p.replace("/x", "/y")
            "y" -> p.replace("/y", "/x")
            "lb" -> p.replace("/lb", "/l")
            "rb" -> p.replace("/rb", "/r")
            "lt" -> p.replace("/lt", "/zl")
            "rt" -> p.replace("/rt", "/zr")
            "select" -> p.replace("/select", "/minus")
            "start" -> p.replace("/start", "/plus")
            "share" -> p.replace("/share", "/square")
            else -> p
        }
    }

    private fun convertJoypadToJoycon(path: String): String {
        val p = convertJoypadToSwitch(path)
        return when (pathSuffix(p)) {
            "dpad_up" -> p.replace("/dpad_up", "/up")
            "dpad_down" -> p.replace("/dpad_down", "/down")
            "dpad_left" -> p.replace("/dpad_left", "/left")
            "dpad_right" -> p.replace("/dpad_right", "/right")
            else -> p
        }
    }

    private fun convertJoypadToXbox360(path: String): String {
        val p = path.replace("joypad", "xbox360")
        return if (pathSuffix(p) == "select") p.replace("/select", "/back") else p
    }

    private fun convertJoypadToXboxModern(path: String): String {
        return when (pathSuffix(path)) {
            "select" -> path.replace("/select", "/view")
            "start" -> path.replace("/start", "/menu")
            else -> path
        }
    }

    private fun convertJoypadToXboxOne(path: String): String = convertJoypadToXboxModern(path.replace("joypad", "xboxone"))

    private fun convertJoypadToXboxSeries(path: String): String = convertJoypadToXboxModern(path.replace("joypad", "xboxseries"))

    private fun convertJoypadToSteamDeck(path: String): String {
        val p = path.replace("joypad", "steamdeck")
        return when (pathSuffix(p)) {
            "lb" -> p.replace("/lb", "/l1")
            "rb" -> p.replace("/rb", "/r1")
            "lt" -> p.replace("/lt", "/l2")
            "rt" -> p.replace("/rt", "/r2")
            "select" -> p.replace("/select", "/inventory")
            "start" -> p.replace("/start", "/menu")
            "home" -> p.replace("/home", "/steam")
            "share" -> p.replace("/share", "/dots")
            else -> p
        }
    }

    private fun convertJoypadToOuya(path: String): String {
        val p = path.replace("joypad", "ouya")
        return when (pathSuffix(p)) {
            "a" -> p.replace("/a", "/o")
            "x" -> p.replace("/x", "/u")
            "b" -> p.replace("/b", "/a")
            "lb" -> p.replace("/lb", "/l1")
            "rb" -> p.replace("/rb", "/r1")
            "lt" -> p.replace("/lt", "/l2")
            "rt" -> p.replace("/rt", "/r2")
            "start" -> p.replace("/start", "/menu")
            "share" -> p.replace("/share", "/microphone")
            else -> p
        }
    }
}



