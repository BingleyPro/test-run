package dev.bingley.testrun.commands

import dev.bingley.testrun.ScenarioManager
import dev.bingley.testrun.TestScenario

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component

object CopyCommand {
    fun build(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("copy")
            // Save a scenario
            .then(
                Commands.argument("old_name", StringArgumentType.word())
                
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
            .then(
            Commands.argument("new_name", StringArgumentType.word())
            
            .executes { context ->
                
                val player = context.source.playerOrException
                val oldName = StringArgumentType.getString(
                    context, "old_name"
                )
                val newName = StringArgumentType.getString(
                    context, "new_name"
                )
                
                ScenarioManager.copy(player, oldName, newName)
                
                context.source.sendSuccess(
                    { Component.translatable("message.testrun.copied", oldName, newName)},
                    false
                )
                1
            }
        )
        )
    }
}