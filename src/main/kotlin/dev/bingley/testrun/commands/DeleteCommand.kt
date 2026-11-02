package dev.bingley.testrun.commands

import dev.bingley.testrun.ScenarioManager
import dev.bingley.testrun.TestScenario

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component

object DeleteCommand {
    fun build(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("delete")
            .then(
            Commands.argument("name", StringArgumentType.word())
                
                .suggests { context, builder ->
                    // Get the executor
                    val player = context.source.player

                    if (player != null) {
                        val names = ScenarioManager.list(player)
                        
                        for (name in names) {
                            // Suggest names matching what was typed
                            if (name.startsWith(builder.remaining, ignoreCase = true)) {
                                builder.suggest(name)
                            }
                        }
                    }
                    
                    builder.buildFuture()
                }
                    
                .executes { context ->
                    val player = context.source.playerOrException
                    val name = StringArgumentType.getString(
                        context, "name"
                    )
                    
                    if (ScenarioManager.delete(player, name)) {
                        context.source.sendSuccess(
                            { Component.translatable("message.testrun.deleted", name)},
                            false
                        )
                        1
                    } else {
                        context.source.sendFailure(
                            Component.translatable("message.testrun.scenario_missing", name)
                        )
                        0
                    }
                }
            )
    }
}