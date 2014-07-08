package powercrystals.powerconverters.renderer;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.Container;

/**
 * A GuiContainer wrapper to expose some protected fields
 * @author samrg472
 */
public abstract class ExposedGuiContainer extends GuiContainer {

    public ExposedGuiContainer(Container container) {
        super(container);
    }

    @Override
    public void drawGradientRect(int par1, int par2, int par3, int par4, int par5, int par6) {
        super.drawGradientRect(par1, par2, par3, par4, par5, par6);
    }

    public RenderItem getItemRenderer() {
        return itemRender;
    }

    public int getGuiTop() {
        return guiTop;
    }

    public void setZLevel(float zLevel) {
        this.zLevel = zLevel;
    }

    public FontRenderer getFontRenderer() {
        return fontRendererObj;
    }

    // Temporary. Maps function from old name.to srg name
    public boolean isPointInRegion(int p1, int p2, int p3, int p4, int p5, int p6) {
        return func_146978_c(p1, p2, p3, p4, p5, p6);
    }
}
