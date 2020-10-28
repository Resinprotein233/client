package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.gui.rgui.InteractiveComponent
import me.zeroeightsix.kami.module.modules.client.ClickGUI
import me.zeroeightsix.kami.module.modules.client.GuiColors
import me.zeroeightsix.kami.module.modules.client.Tooltips
import me.zeroeightsix.kami.util.TimedFlag
import me.zeroeightsix.kami.util.graphics.AnimationUtils
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.RenderUtils2D
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter
import me.zeroeightsix.kami.util.graphics.font.TextComponent
import me.zeroeightsix.kami.util.math.Vec2d
import me.zeroeightsix.kami.util.math.Vec2f
import org.lwjgl.opengl.GL11.*

abstract class AbstractSlider(
        name: String,
        valueIn: Double,
        private val descriptionIn: String = ""
) : InteractiveComponent(name, 0.0f, 0.0f, 40.0f, 10.0f, false) {
    protected var value = valueIn
        set(value) {
            if (value != field) {
                prevValue.value = renderProgress
                field = value
            }
        }

    protected val prevValue = TimedFlag(value)
    protected open val renderProgress: Double
        get() = AnimationUtils.linear(AnimationUtils.toDeltaTimeDouble(prevValue.lastUpdateTime), 50.0, prevValue.value, value)

    override val maxHeight
        get() = FontRenderAdapter.getFontHeight() + 3.0f
    protected var protectedWidth = 0.0

    private val description = TextComponent(" ")
    private var descriptionPosX = 0.0f
    private var shown = false

    var listening = false; protected set

    override fun onClosed() {
        super.onClosed()
        listening = false
    }

    override fun onDisplayed() {
        super.onDisplayed()
        prevValue.value = 0.0
        value = 0.0
        setupDescription()
    }

    private fun setupDescription() {
        description.clear()
        if (descriptionIn.isNotBlank()) {
            val spaceWidth = FontRenderAdapter.getStringWidth(" ")
            var lineWidth = -spaceWidth
            var lineString = ""

            for (string in descriptionIn.split(' ')) {
                lineWidth += FontRenderAdapter.getStringWidth(string) + spaceWidth
                if (lineWidth > 169) {
                    description.addLine(lineString.trimEnd())
                    lineWidth = -spaceWidth
                    lineString = ""
                } else {
                    lineString += "$string "
                }
            }

            if (lineString.isNotBlank()) description.addLine(lineString)
        }
    }

    override fun onTick() {
        super.onTick()
        height.value = maxHeight
    }

    override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        // Slider bar
        if (renderProgress > 0.0) RenderUtils2D.drawRectFilled(vertexHelper, Vec2d(0.0, 0.0), Vec2d(renderWidth * renderProgress, renderHeight.toDouble()), GuiColors.primary)

        // Slider hover overlay
        val overlayColor = getStateColor(mouseState).interpolate(getStateColor(prevState), AnimationUtils.toDeltaTimeDouble(lastStateUpdateTime), 200.0)
        RenderUtils2D.drawRectFilled(vertexHelper, Vec2d(0.0, 0.0), Vec2d(renderWidth, renderHeight), overlayColor)

        // Slider frame
        RenderUtils2D.drawRectOutline(vertexHelper, Vec2d(0.0, 0.0), Vec2d(renderWidth, renderHeight), 1.5f, GuiColors.outline)

        // Slider name
        GlStateUtils.pushScissor()
        /*if (protectedWidth > 0.0) {
            GlStateUtils.scissor(
                    ((absolutePos.x + renderWidth - protectedWidth) * ClickGUI.getScaleFactor()).roundToInt(),
                    (mc.displayHeight - (absolutePos.y + renderHeight) * ClickGUI.getScaleFactor()).roundToInt(),
                    (protectedWidth * ClickGUI.getScaleFactor()).roundToInt(),
                    (renderHeight * ClickGUI.getScaleFactor()).roundToInt()
            )
        }*/
        FontRenderAdapter.drawString(name.value, 2f, 1.0f, color = GuiColors.text)
        GlStateUtils.popScissor()

        // Tooltips
        if (Tooltips.isEnabled && descriptionIn.isNotBlank()) drawToolTips(vertexHelper, absolutePos)
    }

    private fun drawToolTips(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        var deltaTime = AnimationUtils.toDeltaTimeFloat(lastStateUpdateTime)

        if (mouseState == MouseState.HOVER && deltaTime > 500L || prevState == MouseState.HOVER && shown) {

            if (mouseState == MouseState.HOVER) {
                if (descriptionPosX == 0.0f) descriptionPosX = lastMousePos.x
                deltaTime -= 500L
                shown = true
            } else if (deltaTime > 250.0f) {
                descriptionPosX = 0.0f
                shown = false
                return
            }

            val alpha = (if (mouseState == MouseState.HOVER) AnimationUtils.exponentInc(deltaTime, 250.0f, 0.0f, 1.0f)
            else AnimationUtils.exponentDec(deltaTime, 250.0f, 0.0f, 1.0f))
            val textWidth = description.getWidth()
            val textHeight = description.getHeight(2)

            val relativeCorner = Vec2f(mc.displayWidth.toFloat(), mc.displayHeight.toFloat()).divide(ClickGUI.getScaleFactorFloat()).subtract(absolutePos)

            val posX = descriptionPosX.coerceIn(-absolutePos.x, relativeCorner.x - textWidth - 10.0f)
            val posY = (renderHeight + 4.0f).coerceIn(-absolutePos.y, relativeCorner.y - textHeight - 10.0f)

            glDisable(GL_SCISSOR_TEST)
            glPushMatrix()
            glTranslatef(posX, posY, 696.0f)

            RenderUtils2D.drawRectFilled(vertexHelper, posEnd = Vec2d(textWidth, textHeight).add(4.0), color = GuiColors.backGround.apply { a = (a * alpha).toInt() })
            RenderUtils2D.drawRectOutline(vertexHelper, posEnd = Vec2d(textWidth, textHeight).add(4.0), lineWidth = 2.0f, color = GuiColors.primary.apply { a = (a * alpha).toInt() })

            description.draw(Vec2d(2.0f, 2.0f), 2, alpha)

            glEnable(GL_SCISSOR_TEST)
            glPopMatrix()
        }
    }

    private fun getStateColor(state: MouseState) = when (state) {
        MouseState.NONE -> GuiColors.idle
        MouseState.HOVER -> GuiColors.hover
        else -> GuiColors.click
    }
}