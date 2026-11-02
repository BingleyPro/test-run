package dev.bingley.testrun

import dev.bingley.testrun.commands.*

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
                    
                    // Register other commands
                    .then(SaveCommand.build())
                    .then(RunCommand.build())
                    .then(DeleteCommand.build())
                    .then(SetupCommand.build())
                    .then(InfoCommand.build())
                    .then(ListCommand.build())
                    .then(AgainCommand.build())    
                    .then(RunFromCommand.build())      
                    .then(FinishCommand.build())
                    .then(CopyCommand.build())
            )
        }
    }
}