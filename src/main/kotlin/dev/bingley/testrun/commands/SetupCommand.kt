package dev.bingley.testrun

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component

object SetupCommand {
    fun build(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("setup")
            .then(
                Commands.argument("name",StringArgumentType.word())
                
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
                    Commands.argument(
                    "resetFunction",
                    StringArgumentType.greedyString()
                )
                    .suggests { context, builder ->
                        // Get datapack function names
                        val functions = context.source.server.functions.functionNames
                        
                        SharedSuggestionProvider.suggestResource(functions, builder)
                    }
                
                    .executes { context ->
                        val player = context.source.playerOrException
                        val name = StringArgumentType.getString(
                            context, "name"
                        )
                        val resetFunction = StringArgumentType.getString(
                            context, "resetFunction"
                        )
                        
                        if (ScenarioManager.setResetFunction(player, name, resetFunction)) {
                            context.source.sendSuccess( {
                                Component.translatable(
                                    "message.testrun.set_reset_function", name, resetFunction)
                                },
                                false
                            )
                            1
                        } else {
                            context.source.sendFailure(
                                Component.translatable(
                                    "message.testrun.scenario_or_function_missing"
                                )
                            )
                            0
                        }
                    }
                )
            )
    }
}