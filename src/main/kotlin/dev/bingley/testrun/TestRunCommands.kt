package dev.bingley.testrun

import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component

object TestRunCommands {
    fun register() {
        CommandRegistrationCallback.EVENT.register {
            dispatcher, _, _ ->
            
            dispatcher.register(
                Commands.literal("testrun")
                    .requires { source ->
                        Commands.LEVEL_GAMEMASTERS.check(source.permissions())
                    }
                
                    .then(
                        // Check that mod/command is working
                        Commands.literal("ping")
                            .executes { context ->
                                context.source.sendSuccess(
                                    { Component.translatable("message.testrun.pinged")},
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
                                        { Component.translatable("message.testrun.saved", name)},
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
                                ).suggests { context, builder ->
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
                                    
                                    if (ScenarioManager.restore(player, name)) {
                                        context.source.sendSuccess(
                                            { Component.translatable("message.testrun.restored", name)},
                                            false
                                        )
                                        1
                                    } else {
                                        context.source.sendFailure(
                                            Component.translatable("message.testrun.scenario_missing")
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
                        )
                        
                        .then(
                            Commands.literal("delete")
                                .then(
                                Commands.argument(
                                    "name",
                                    StringArgumentType.word()
                                ).suggests { context, builder ->
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
                        )
                        
                        .then(
                            Commands.literal("again")
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
                        )
                        
                        .then(
                            Commands.literal("setup")
                                .then(
                                    Commands.argument(
                                        "name",
                                        StringArgumentType.word()
                                    )
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
                        )

                        .then(
                            Commands.literal("setup")
                                .then(
                                    Commands.argument(
                                        "name",
                                        StringArgumentType.word()
                                    )
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
                        )
            )
        }
    }
}