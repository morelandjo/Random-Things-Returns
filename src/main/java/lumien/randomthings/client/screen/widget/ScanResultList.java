package lumien.randomthings.client.screen.widget;

import lumien.randomthings.util.ChunkAnalyzerResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class ScanResultList extends AbstractWidget {
    private ChunkAnalyzerResult results;
    private int scrollOffset = 0;
    private final int itemHeight = 20;
    private final int visibleItems;
    
    public ScanResultList(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.visibleItems = height / itemHeight;
    }
    
    public void setResults(ChunkAnalyzerResult results) {
        // Only reset scroll if results actually changed
        if (this.results != results) {
            this.results = results;
            this.scrollOffset = 0; // Reset scroll when results change
        }
    }
    
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (results == null || results.isEmpty()) {
            return;
        }
        
        // Draw background
        guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x80000000);
        
        int totalItems = results.size();
        int maxScroll = Math.max(0, totalItems - visibleItems);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        
        // Render visible items
        for (int i = 0; i < visibleItems && (i + scrollOffset) < totalItems; i++) {
            int index = i + scrollOffset;
            int itemY = getY() + i * itemHeight;
            
            renderItem(guiGraphics, index, getX(), itemY, mouseX, mouseY);
        }
        
        // Draw scrollbar if needed
        if (totalItems > visibleItems) {
            drawScrollbar(guiGraphics);
        }
    }
    
    private void renderItem(GuiGraphics guiGraphics, int index, int x, int y, int mouseX, int mouseY) {
        // Safety checks to prevent index out of bounds
        if (index >= results.displayStacks().size() || 
            index >= results.blockDescriptions().size() || 
            index >= results.blockCounts().size()) {
            return; // Skip rendering if index is invalid
        }
        
        ItemStack stack = results.displayStacks().get(index);
        String description = results.blockDescriptions().get(index);
        int count = results.blockCounts().get(index);
        
        // Highlight hovered item
        if (mouseX >= x && mouseX < x + getWidth() && mouseY >= y && mouseY < y + itemHeight) {
            guiGraphics.fill(x, y, x + getWidth(), y + itemHeight, 0x80FFFFFF);
        }
        
        // Render item icon
        if (!stack.isEmpty()) {
            guiGraphics.renderItem(stack, x + 2, y + 2);
        }
        
        // Render count and description
        String countText = count + "x ";
        int countWidth = Minecraft.getInstance().font.width(countText);
        
        guiGraphics.drawString(Minecraft.getInstance().font, countText, 
                              x + 22, y + 6, 0xFFFFFF);
        
        // Truncate description if too long
        String displayText = description;
        int maxDescriptionWidth = getWidth() - 80; // Leave space for icon and count
        if (Minecraft.getInstance().font.width(description) > maxDescriptionWidth) {
            while (Minecraft.getInstance().font.width(displayText + "...") > maxDescriptionWidth && displayText.length() > 1) {
                displayText = displayText.substring(0, displayText.length() - 1);
            }
            displayText += "...";
        }
        
        guiGraphics.drawString(Minecraft.getInstance().font, displayText, 
                              x + 22 + countWidth + 5, y + 6, 0xFFFFFF);
    }
    
    private void drawScrollbar(GuiGraphics guiGraphics) {
        int totalItems = results.size();
        int scrollbarX = getX() + getWidth() - 6;
        int scrollbarY = getY();
        int scrollbarHeight = getHeight();
        
        // Scrollbar track
        guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + 6, scrollbarY + scrollbarHeight, 0xFF000000);
        
        // Scrollbar thumb
        int thumbHeight = Math.max(10, (visibleItems * scrollbarHeight) / totalItems);
        int thumbY = scrollbarY + (scrollOffset * (scrollbarHeight - thumbHeight)) / Math.max(1, totalItems - visibleItems);
        
        guiGraphics.fill(scrollbarX + 1, thumbY, scrollbarX + 5, thumbY + thumbHeight, 0xFFAAAAAA);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (results == null || results.isEmpty()) {
            return false;
        }
        
        int totalItems = results.size();
        if (totalItems <= visibleItems) {
            return false;
        }
        
        int maxScroll = totalItems - visibleItems;
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int)(scrollY * 3)));
        
        return true;
    }
    
    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // Simple narration implementation - provide basic description
        if (results != null && !results.isEmpty()) {
            narrationElementOutput.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, 
                Component.translatable("gui.chunk_analyzer.results", results.size()));
        }
    }
}