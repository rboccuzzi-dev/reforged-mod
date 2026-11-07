package com.cuzzi.reforged.network;

import com.cuzzi.reforged.Reforged;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Reforged.MODID).versioned("1.0.0");

        // --- Clientbound Packets (Server -> Client) ---
        registrar.playToClient(
                ClientboundOpenNamingScreenPayload.TYPE,
                ClientboundOpenNamingScreenPayload.STREAM_CODEC,
                ClientboundOpenNamingScreenPayload::handle
        );

        registrar.playToClient(
                ClientboundSyncTitleRegistryPayload.TYPE,
                ClientboundSyncTitleRegistryPayload.STREAM_CODEC,
                ClientboundSyncTitleRegistryPayload::handle
        );

        registrar.playToClient(
                SyncStatsPayload.TYPE,
                SyncStatsPayload.STREAM_CODEC,
                SyncStatsPayload::handle
        );

        // --- Serverbound Packets (Client -> Server) ---
        registrar.playToServer(
                ApplyStatsPayload.TYPE,
                ApplyStatsPayload.STREAM_CODEC,
                ApplyStatsPayload::handle
        );

        registrar.playToServer(
                ServerboundAssignSkillPayload.TYPE,
                ServerboundAssignSkillPayload.STREAM_CODEC,
                ServerboundAssignSkillPayload::handle
        );
        registrar.playToServer(
                ServerboundCastSkillPayload.TYPE,
                ServerboundCastSkillPayload.STREAM_CODEC,
                ServerboundCastSkillPayload::handle
        );

        registrar.playToServer(
                ServerboundSubmitLegendaryNamePayload.TYPE,
                ServerboundSubmitLegendaryNamePayload.STREAM_CODEC,
                ServerboundSubmitLegendaryNamePayload::handle
        );

        registrar.playToServer(
                ServerboundSelectTitlePayload.TYPE,
                ServerboundSelectTitlePayload.STREAM_CODEC,
                ServerboundSelectTitlePayload::handle
        );
    }

    private ModNetwork() {}
}