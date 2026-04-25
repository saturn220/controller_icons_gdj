# Kotlin Controller Icons (MVP)

Этот каталог содержит Kotlin-аналог runtime-части `addons/controller_icons`:

- `JvmControllerSettings.kt` - настройки (fallback-девайс, deadzone, custom asset dir/extension).
- `JvmControllerMapper.kt` - маппинг generic `joypad/*` путей в device-specific пути.
- `JvmControllerIcons.kt` - singleton-сервис трекинга типа ввода и парсинга путей в `Texture2D`.
- `JvmControllerIconTexture.kt` - lightweight proxy-ресурс, который держит текущую текстуру и умеет `refresh()`.
- `JvmControllerButton.kt`, `JvmControllerTextureRect.kt`, `JvmControllerSprite2D.kt`, `JvmControllerSprite3D.kt` - runtime-аналоги deprecated-объектов из `addons/controller_icons/objects`.

## Что уже покрыто

- Определение текущего типа ввода (keyboard/mouse vs controller).
- Выбор matching `InputEvent` для `InputMap` action.
- Конвертация key/mouse/joypad событий в asset-пути.
- Маппинг контроллеров: Luna/PlayStation/Stadia/Steam/Switch/Xbox/Steam Deck/Ouya.
- Загрузка текстур из `customAssetDir` и `res://addons/controller_icons/assets/`.
- TTS-строка через `parsePathToTts`.

## Ограничения MVP

- `JvmControllerIconTexture.kt` сделан как `Resource`-прокси (не `Texture2D` draw-proxy как в GDScript/C# версии).
- Используется нативный Godot signal API `input_type_changed` (без промежуточных listener-коллбеков).
- Editor-only объекты (`ControllerIconEditorInspector`, `ControllerIconPathSelector`, `ControllerIconPathEditorProperty`, `ControllerIconPathSelectorPopup`) пока не портированы в Kotlin и проект по-прежнему использует для них GDScript-плагин.

## Быстрый старт

1. Добавьте `JvmControllerIcons` в сцену/autoload, если хотите использовать Kotlin-вариант runtime.
2. Для иконки используйте `JvmControllerIconTexture`, задайте `iconPath`.
3. Получайте текстуру через `getTexture()` и обновляйте `refresh()` при необходимости.

