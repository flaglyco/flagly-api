package com.github.makiftutuncu.switchboard.akkahttpserver

import com.github.makiftutuncu.switchboard.Flag

object Flags {
  val maintenanceMode: Flag[Boolean] = Flag("maintenance-mode", "Whether or not system is in maintenance mode", false, false)
  val httpTimeout: Flag[Int]         = Flag("http-timeout",     "HTTP timeout in seconds",                      10,    10)
}
