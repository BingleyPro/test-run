package dev.bingley.testrun

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.KeyMapping
import net.minecraft.resources.Identifier
import com.mojang.blaze3d.platform.InputConstants

object TestRunClient : ClientModInitializer {
    override fun onInitializeClient() {
        val category = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("testrun", "controls")
        )
        
        val restartKey = KeyMappingHelper.registerKeyMapping(
            KeyMapping(
                "key.testrun.again",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_K,
                category
            )
        )
        
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (restartKey.consumeClick()) {
                client.connection?.sendCommand("testrun again")
            }
        }
    }
}