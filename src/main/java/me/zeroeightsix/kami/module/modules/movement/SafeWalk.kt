package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BaritoneUtils

@Module.Info(
        name = "SafeWalk",
        category = Module.Category.MOVEMENT,
        description = "Keeps you from walking off edges"
)
object SafeWalk : Module() {
    private val baritoneCompat = register(Settings.b("BaritoneCompatibility", true))

    fun shouldSafewalk(): Boolean {
        return isEnabled && (baritoneCompat.value && BaritoneUtils.primary?.customGoalProcess!!.goal == null || !baritoneCompat.value)
    }
}