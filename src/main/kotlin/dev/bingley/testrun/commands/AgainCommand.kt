package dev.bingley.testrun

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component

object AgainCommand {
    fun build(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("again")
            .executes { context ->
                val player = context.source.playerOrException
                
                if (ScenarioManager.runAgain(player)) {
                    context.source.sendSuccess(
                        { Component.translatable("message.testrun.again")},
                        false
                    )
                    1
                } else {
                    context.source.sendFailure(
                        Component.translatable("message.testrun.last_scenario_missing"))
                    0
                }
            }
    }
}