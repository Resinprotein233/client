package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.module.modules.client.GuiColors
import me.zeroeightsix.kami.setting.impl.other.BindSetting
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter
import me.zeroeightsix.kami.util.math.Vec2f
import org.lwjgl.input.Keyboard

class BindButton(
        private val setting: BindSetting
) : AbstractSlider(setting.name, 0.0, setting.description) {

    override val renderProgress: Double = 0.0

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        listening = !listening
    }

    override fun onKeyInput(keyCode: Int, keyState: Boolean) {
        super.onKeyInput(keyCode, keyState)
        if (listening && !keyState) {
            setting.value.apply {
                if (keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE) clear()
                else setBind(keyCode)
                listening = false
            }
        }
    }

    override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        super.onRender(vertexHelper, absolutePos)

        val valueText = if (listening) "Listening"
        else setting.value.toString()

        protectedWidth = FontRenderAdapter.getStringWidth(valueText, 0.75f).toDouble()
        val posX = (renderWidth - protectedWidth - 2.0f).toFloat()
        val posY = renderHeight - 2.0f - FontRenderAdapter.getFontHeight(0.75f)
        FontRenderAdapter.drawString(valueText, posX, posY, color = GuiColors.text, scale = 0.75f)
    }
}