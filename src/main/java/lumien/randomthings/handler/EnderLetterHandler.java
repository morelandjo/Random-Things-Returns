package lumien.randomthings.handler;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles the mail delivery system for Ender Letters
 * This is a simplified placeholder implementation - the full system would use
 * world saved data for cross-dimensional delivery
 */
public class EnderLetterHandler {
    private static final Map<UUID, ItemStack> pendingMail = new HashMap<>();

    public static boolean sendLetter(ItemStack letterStack, String receiverName) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return false;
        }

        // Find the target player by name
        ServerPlayer targetPlayer = server.getPlayerList().getPlayerByName(receiverName);
        if (targetPlayer == null) {
            // Player not found or offline - for now we'll just fail
            // In a full implementation, this would queue the letter for when they come online
            return false;
        }

        // Store the letter for the target player
        addIncomingLetter(targetPlayer.getUUID(), letterStack.copy());
        return true;
    }

    public static ItemStack getIncomingLetter(UUID playerUUID) {
        return pendingMail.getOrDefault(playerUUID, ItemStack.EMPTY);
    }

    public static void removeIncomingLetter(UUID playerUUID) {
        pendingMail.remove(playerUUID);
    }

    public static void addIncomingLetter(UUID playerUUID, ItemStack letterStack) {
        pendingMail.put(playerUUID, letterStack);
    }
}