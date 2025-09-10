package lumien.randomthings.item;

import lumien.randomthings.menu.ChunkAnalyzerMenu;
import lumien.randomthings.util.ChunkAnalyzerResult;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ChunkAnalyzerItem extends Item {
    public ChunkAnalyzerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new ChunkAnalyzerMenuProvider(stack));
            return InteractionResultHolder.success(stack);
        }
        
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        
        // Add the main tooltip
        tooltipComponents.add(Component.translatable("tooltip.randomthings.chunk_analyzer"));
        
        // If there are scan results, show summary
        ChunkAnalyzerResult result = stack.get(ModDataComponents.CHUNK_ANALYZER_RESULT.get());
        if (result != null && !result.isEmpty()) {
            tooltipComponents.add(Component.translatable("item.randomthings.chunk_analyzer.results", result.size()));
        }
    }

    public static ChunkAnalyzerResult getResults(ItemStack stack) {
        return stack.get(ModDataComponents.CHUNK_ANALYZER_RESULT.get());
    }

    public static void setResults(ItemStack stack, ChunkAnalyzerResult result) {
        stack.set(ModDataComponents.CHUNK_ANALYZER_RESULT.get(), result);
    }

    public static void clearResults(ItemStack stack) {
        stack.remove(ModDataComponents.CHUNK_ANALYZER_RESULT.get());
    }

    private static class ChunkAnalyzerMenuProvider implements MenuProvider {
        private final ItemStack analyzerStack;

        public ChunkAnalyzerMenuProvider(ItemStack analyzerStack) {
            this.analyzerStack = analyzerStack;
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("item.randomthings.chunk_analyzer");
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
            return new ChunkAnalyzerMenu(containerId, playerInventory, analyzerStack);
        }
    }
}