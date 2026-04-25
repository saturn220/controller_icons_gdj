package lib.controllerIcons.objects.path_selection

import godot.annotation.RegisterClass
import godot.api.Panel

@RegisterClass
open class SelectorPanel : Panel() {
    open fun FocusSelector() {
        // Override in child selectors.
    }
}

