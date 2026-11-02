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

object RunCommand {
    fun build(): LiteralArgumentBuilder<CommandSourceStack> {
        val nameArgument = Commands.argument("name", StringArgumentType.word())
                    
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
                runScenario(context, "all") 
            }
                
        val parts = listOf(
            "all",
            "position",
            "inventory",
            "player",
            "world"
        )
        
        for (part in parts) {
            nameArgument.then(
                Commands.literal(part)
                    .executes { context ->
                        runScenario(context, part)
                    }
            )
        }

        return Commands.literal("run").then(nameArgument)
    }
    
    private fun runScenario(context: CommandContext<CommandSourceStack>, part: String): Int {
        val player = context.source.playerOrException
        val name = StringArgumentType.getString(
            context, "name"
        )
        
        if (ScenarioManager.restore(player, name, part)) {
            
            val message = if (part == "all") {
                Component.translatable("message.testrun.restored", name)
            } else {
                Component.translatable("message.testrun.restored_part", name, part)
            }
            
            context.source.sendSuccess(
                { message },
                false
            )
            return 1
        } else {
            context.source.sendFailure(
                Component.translatable("message.testrun.scenario_missing", name)
            )
            return 0
        }
    }
}