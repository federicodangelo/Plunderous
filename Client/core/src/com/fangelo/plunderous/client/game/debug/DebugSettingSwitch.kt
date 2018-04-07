package com.fangelo.plunderous.client.game.debug

class DebugSettingSwitch(val name: String, val value: () -> Boolean, val switchValue: () -> Unit)