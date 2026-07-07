package lumien.randomthings.item;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import lumien.randomthings.menu.ChunkAnalyzerMenu;
import lumien.randomthings.util.ChunkAnalyzerResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Right-click to scan the current chunk and list every block in it, sorted by count. */
public class ChunkAnalyzerItem extends Item {
    private static final String RESULT_KEY = "chunk_analyzer_result";

    public ChunkAnalyzerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer serverPlayer) {
            dev.architectury.registry.menu.MenuRegistry.openExtendedMenu(serverPlayer, new ExtendedMenuProvider() {
                @Override
                public void saveExtraData(FriendlyByteBuf buf) {
                }

                @Override
                public Component getDisplayName() {
                    return Component.translatable("item.randomthings.chunk_analyzer");
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player p) {
                    return new ChunkAnalyzerMenu(containerId, inventory, stack);
                }
            });
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.randomthings.chunk_analyzer"));
        ChunkAnalyzerResult result = getResults(stack);
        if (!result.isEmpty()) {
            tooltip.add(Component.translatable("item.randomthings.chunk_analyzer.results", result.size()));
        }
    }

    public static ChunkAnalyzerResult getResults(ItemStack stack) {
        if (stack.getTag() != null && stack.getTag().contains(RESULT_KEY)) {
            return ChunkAnalyzerResult.fromNbt(stack.getTag().getCompound(RESULT_KEY));
        }
        return ChunkAnalyzerResult.empty();
    }

    public static void setResults(ItemStack stack, ChunkAnalyzerResult result) {
        stack.getOrCreateTag().put(RESULT_KEY, result.toNbt());
    }

    public static void clearResults(ItemStack stack) {
        if (stack.getTag() != null) {
            stack.getTag().remove(RESULT_KEY);
        }
    }
}
