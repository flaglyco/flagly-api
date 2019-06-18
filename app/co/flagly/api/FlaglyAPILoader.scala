package co.flagly.api

import play.api.ApplicationLoader.Context
import play.api.{Application, ApplicationLoader, LoggerConfigurator}

class FlaglyAPILoader extends ApplicationLoader {
  override def load(context: Context): Application = {
    LoggerConfigurator(context.environment.classLoader).foreach { loggerConfigurator =>
      loggerConfigurator.configure(context.environment)
    }
    
    new FlaglyAPIComponents(context).application
  }
}
