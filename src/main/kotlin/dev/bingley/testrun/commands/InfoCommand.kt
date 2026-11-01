package dev.bingley.testrun

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component

object InfoCommand {
    fun build(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("info")
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
                    
                    val scenario = ScenarioManager.get(player, name)
                    
                    if (scenario == null) {
                            context.source.sendFailure(
                                Component.translatable("message.testrun.scenario_missing", name)
                            )
                            return@executes 0
                    } else {
                        val message = """
                            Scenario: ${scenario.name}
                            Dimension: ${scenario.dimension}
                            Coordinates: ${scenario.x}, ${scenario.y}, ${scenario.z}
                            Health: ${scenario.health}
                            Gamemode: ${scenario.gamemode}
                            Experience Level: ${scenario.experienceLevel}
                            Reset Function: ${scenario.resetFunction ?: "None"}
                        """.trimIndent()
                        
                        context.source.sendSuccess( {
                            Component.literal(message) },
                            false
                        )
                        1
                    }
                }
            )
    }
}