package dev.bingley.testrun.commands

import dev.bingley.testrun.ScenarioManager
import dev.bingley.testrun.TestScenario

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.bingley.testrun.PracticeManager
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component

object FinishCommand {
    fun build(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("finish")
            .executes { context ->
                val player = context.source.playerOrException
                
                val seconds = PracticeManager.finish(player)
                
                if (seconds == null) {
                    context.source.sendFailure(
                        Component.translatable("message.testrun.no_active_attempt")
                    )
                    return@executes 0
                }
                
                val message = Component.translatable("message.testrun.finished", seconds)
                
                context.source.sendSuccess(
                    { message },
                    false
                )
                1
            }
    }
}