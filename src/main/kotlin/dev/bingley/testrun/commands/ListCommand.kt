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

object ListCommand {
    fun build(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("list")
            .executes { context ->
                val player = context.source.playerOrException
                val names = ScenarioManager.list(player)
                
                val message = if (names.isEmpty()) {
                    Component.translatable("message.testrun.list_empty")
                } else {
                    Component.translatable("message.testrun.list", names.joinToString(", "))
                }
                
                context.source.sendSuccess(
                    { message },
                        false
                )
                1
            }
    }
}