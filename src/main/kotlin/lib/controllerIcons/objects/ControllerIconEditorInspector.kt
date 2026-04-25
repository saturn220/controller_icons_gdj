package lib.controllerIcons.objects

import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.api.RefCounted

@RegisterClass
class ControllerIconEditorInspector : RefCounted() {

    @RegisterFunction
    fun CanHandleControllerIconTexture(): Boolean {
        return true
    }
}


