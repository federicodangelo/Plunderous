package com.fangelo.plunderous.client.game.debug

class GameDebug {

    private val _settings = mutableListOf<DebugSettingSwitch>()

    val settings: List<DebugSettingSwitch>
        get() = _settings

    fun addSwitch(name: String, value: () -> Boolean, switchValue: () -> Unit) {
        _settings.add(DebugSettingSwitch(name, value, switchValue))
    }
}