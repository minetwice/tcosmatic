package com.tcosmatic.network;

import com.tcosmatic.TcosmaticClient;
import com.tcosmatic.cape.CapeManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class NetworkManager {
    public static final Identifier CAPE_SYNC_ID = Identifier.of(TcosmaticClient.MOD_ID, "cape_sync");
    
    public static void init() {
        // Register payload
        PayloadTypeRegistry.playS2C().register(
            CapeSyncPayload.ID,
            CapeSyncPayload.CODEC
        );
        
        // Register receiver
        ClientPlayNetworking.registerGlobalReceiver(
            CapeSyncPayload.ID,
            (payload, context) -> {
                context.client().execute(() -> {
                    if (!payload.capeName().isEmpty()) {
                        CapeManager.setPlayerCape(payload.playerId(), payload.capeName());
                    } else {
                        CapeManager.removePlayerCape(payload.playerId());
                    }
                });
            }
        );
        
        // Send cape on join
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String selectedCape = com.tcosmatic.config.ConfigManager.getConfig().selectedCape;
            if (!selectedCape.isEmpty() && client.player != null) {
                sendCapeUpdate(selectedCape);
            }
        });
        
        TcosmaticClient.LOGGER.info("Network manager initialized");
    }
    
    public static void sendCapeUpdate(String capeName) {
        if (ClientPlayNetworking.canSend(CapeSyncPayload.ID)) {
            UUID playerId = MinecraftClient.getInstance().player.getUuid();
            ClientPlayNetworking.send(new CapeSyncPayload(playerId, capeName));
        }
    }
    
    public record CapeSyncPayload(UUID playerId, String capeName) implements CustomPayload {
        public static final CustomPayload.Id<CapeSyncPayload> ID = 
            new CustomPayload.Id<>(CAPE_SYNC_ID);
        
        public static final PacketCodec<RegistryByteBuf, CapeSyncPayload> CODEC = 
            PacketCodec.tuple(
                createUuidCodec(), CapeSyncPayload::playerId,
                PacketCodecs.STRING, CapeSyncPayload::capeName,
                CapeSyncPayload::new
            );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    // Custom UUID codec since PacketCodecs.UUID isn't available in 1.21.1
    private static PacketCodec<RegistryByteBuf, UUID> createUuidCodec() {
        return new PacketCodec<RegistryByteBuf, UUID>() {
            @Override
            public UUID decode(RegistryByteBuf buf) {
                return new UUID(buf.readLong(), buf.readLong());
            }
            
            @Override
            public void encode(RegistryByteBuf buf, UUID uuid) {
                buf.writeLong(uuid.getMostSignificantBits());
                buf.writeLong(uuid.getLeastSignificantBits());
            }
        };
    }
}
