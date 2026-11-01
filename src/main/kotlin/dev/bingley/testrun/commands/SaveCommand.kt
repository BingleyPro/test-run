package dev.bingley.testrun

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component

object SaveCommand {
    fun build(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("save")
            // Save a scenario
            .then(
                Commands.argument("name", StringArgumentType.word())
                
                .executes { context ->
                    
                    val player = context.source.playerOrException
                    val name = StringArgumentType.getString(
                        context, "name"
                    )
                    
                    ScenarioManager.save(player, name)
                    
                    context.source.sendSuccess(
                        { Component.translatable("message.testrun.saved", name)},
                        false
                    )
                    1
                }
            )
    }
}