package lumien.randomthings.event;

import lumien.randomthings.blockentity.ChatDetectorBlockEntity;
import lumien.randomthings.blockentity.GlobalChatDetectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber
public class ChatEventHandler {
    
    // Static registry to track active chat detectors
    public static final Set<ChatDetectorBlockEntity> CHAT_DETECTORS = ConcurrentHashMap.newKeySet();
    public static final Set<GlobalChatDetectorBlockEntity> GLOBAL_CHAT_DETECTORS = ConcurrentHashMap.newKeySet();
    
    public static void registerChatDetector(ChatDetectorBlockEntity detector) {
        CHAT_DETECTORS.add(detector);
    }
    
    public static void unregisterChatDetector(ChatDetectorBlockEntity detector) {
        CHAT_DETECTORS.remove(detector);
    }
    
    public static void registerGlobalChatDetector(GlobalChatDetectorBlockEntity detector) {
        GLOBAL_CHAT_DETECTORS.add(detector);
    }
    
    public static void unregisterGlobalChatDetector(GlobalChatDetectorBlockEntity detector) {
        GLOBAL_CHAT_DETECTORS.remove(detector);
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String message = event.getRawText();
        
        // Use the static registry - much more efficient
        List<ChatDetectorBlockEntity> chatDetectors = new ArrayList<>(CHAT_DETECTORS);
        List<GlobalChatDetectorBlockEntity> globalChatDetectors = new ArrayList<>(GLOBAL_CHAT_DETECTORS);
        
        // Clean up invalid entries (detectors that have been removed)
        chatDetectors.removeIf(detector -> detector.isRemoved());
        globalChatDetectors.removeIf(detector -> detector.isRemoved());
        CHAT_DETECTORS.removeIf(detector -> detector.isRemoved());
        GLOBAL_CHAT_DETECTORS.removeIf(detector -> detector.isRemoved());
        
        // Check if any detectors want to consume this message
        boolean shouldConsume = false;
        
        // Check chat detectors first
        for (ChatDetectorBlockEntity detector : chatDetectors) {
            if (detector.shouldConsumeMessage(message, player.getUUID())) {
                shouldConsume = true;
                break;
            }
        }
        
        // Check global chat detectors
        if (!shouldConsume) {
            for (GlobalChatDetectorBlockEntity detector : globalChatDetectors) {
                if (detector.shouldConsumeMessage(message, player.getUUID())) {
                    shouldConsume = true;
                    break;
                }
            }
        }
        
        // Cancel the event if any detector wants to consume the message
        if (shouldConsume) {
            event.setCanceled(true);
        }
        
        // Trigger all detectors regardless of whether message is consumed
        for (ChatDetectorBlockEntity detector : chatDetectors) {
            detector.onChatMessage(message, player.getUUID());
        }
        
        for (GlobalChatDetectorBlockEntity detector : globalChatDetectors) {
            detector.onChatMessage(message, player.getUUID());
        }
    }
}