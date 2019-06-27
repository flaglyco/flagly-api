package co.flagly.api.auth

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

import scala.util.Random

object PasswordUtils {
  private val md: MessageDigest = MessageDigest.getInstance("SHA-256")

  def generateSalt(): String = {
    val chars = for (_ <- 1 to 32) yield "%02x".format(Random.nextPrintableChar.toByte)
    chars.mkString
  }

  def hash(plainPassword: String, salt: String): String = {
    (1 to 3)
      .foldLeft(s"$plainPassword$salt".getBytes(StandardCharsets.UTF_8)) { case (message, _) => md.digest(message) }
      .map(b => "%02x".format(b))
      .mkString
  }
}
