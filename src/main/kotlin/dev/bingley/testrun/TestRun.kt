package dev.bingley.testrun

import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TestRun : ModInitializer {
    companion object {
        const val MOD_ID = "testrun"
        val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)
    }

    override fun onInitialize() {
        LOGGER.info("Initializing TestRun...")
        
        TestRunCommands.register()
        
        LOGGER.info("TestRun loaded successfully.")
    }
}