package dev.bingley.testrun

import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

object TestRunCommands {
    fun register() {
        CommandRegistrationCallback.EVENT.register {
            dispatcher, _, _ ->
            
            dispatcher.register(
                Commands.literal("testrun")
                
                    .then(
                        // Check that mod/command is working
                        Commands.literal("ping")
                            .executes { context ->
                                context.source.sendSuccess(
                                    { Component.literal("TestRun is working.")},
                                        false
                                )
                                1
                            }
                    )
                    
                    .then(
                        // Save a scenario
                        Commands.literal("save")
                            .then(
                                Commands.argument(
                                    "name",
                                        StringArgumentType.word()
                                ).executes { context ->
                                    
                                    val player = context.source.playerOrException
                                    val name = StringArgumentType.getString(
                                        context, "name"
                                    )
                                    
                                    ScenarioManager.save(player, name)
                                    
                                    context.source.sendSuccess(
                                        { Component.literal("Saved scenario: $name")},
                                        false
                                    )
                                    1
                                }
                            )
                    )

                    .then(
                        // Run a scenario
                        Commands.literal("run")
                            .then(
                                Commands.argument(
                                    "name",
                                    StringArgumentType.word()
                                ).executes { context ->
                                    val player = context.source.playerOrException
                                    val name = StringArgumentType.getString(
                                        context, "name"
                                    )
                                    
                                    if (ScenarioManager.restore(player, name)) {
                                        context.source.sendSuccess(
                                            { Component.literal("Restored the scenario $name.")},
                                            false
                                        )
                                        1
                                    } else {
                                        context.source.sendFailure(
                                            Component.literal(
                                                "The scenario not found."
                                            )
                                        )
                                        0
                                    }
                                }
                            )
                        )
                        .then(
                            // List all scenarios
                            Commands.literal("list")
                                .executes { context ->
                                    val player = context.source.playerOrException
                                    val names = ScenarioManager.list(player)
                                    
                                    val message = if (names.isEmpty()) {
                                        "No saved scenarios."
                                    } else {
                                        "Scenarios: ${names.joinToString(", ")}"
                                    }
                                    
                                    context.source.sendSuccess(
                                        { Component.literal(message)},
                                            false
                                    )
                                    1
                                }
                        )
                        
                        .then(
                            Commands.literal("again")
                                .executes { context ->
                                    val player = context.source.playerOrException
                                    
                                    if (ScenarioManager.runAgain(player)) {
                                        context.source.sendSuccess(
                                            { Component.literal("Scenario restarted.")},
                                            false
                                        )
                                        1
                                    } else {
                                        context.source.sendFailure(
                                            Component.literal("No previous scenario is available.")
                                        )
                                        0
                                    }
                                }
                        )
            )
        }
    }
}