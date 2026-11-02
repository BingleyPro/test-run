package dev.bingley.testrun.commands

import dev.bingley.testrun.ScenarioManager
import dev.bingley.testrun.TestScenario

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component

object SaveCommand {
    fun build(): LiteralArgumentBuilder<CommandSourceStack> {
        val nameArgument = Commands.argument("name", StringArgumentType.word())
        
            .suggests { context, builder ->
                val player = context.source.player
                
                if (player != null) {
                    for (name in ScenarioManager.list(player)) {
                        if (name.startsWith(builder.remaining, ignoreCase = true)) {
                            builder.suggest(name)
                        }
                    }
                }
                
                builder.buildFuture()
            }
                
            .executes { context ->
                saveScenario(context, "all")
    
            }
            
        val parts = listOf(
            "all", "position", "inventory", "player", "world"
        )
        
        for (part in parts) {
            nameArgument.then(
                Commands.literal(part)
                    .executes { context ->
                        saveScenario(context, part)
                    }
            )
        }

        return Commands.literal("save").then(nameArgument)
    }
    
    private fun saveScenario(context: CommandContext<CommandSourceStack>, part: String): Int {
        val player = context.source.playerOrException
        val name = StringArgumentType.getString(
            context, "name"
        )
        
        if (!ScenarioManager.save(player, name, part)) {
            context.source.sendFailure(
                Component.translatable("message.testrun.scenario_missing", name)
            )
            return 0
        }
        
        val message = if (part == "all") {
            Component.translatable("message.testrun.saved", name)
        } else {
            Component.translatable("message.testrun.saved_part", name)
        }
        
        context.source.sendSuccess(
            { message },
            false
        )
        return 1
    }
}