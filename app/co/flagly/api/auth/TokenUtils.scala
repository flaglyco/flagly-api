package co.flagly.api.auth

import scala.util.Random

object TokenUtils {
  def generateToken(): String = {
    val chars = for (_ <- 1 to 32) yield "%02x".format(Random.nextPrintableChar.toByte)
    chars.mkString
  }
}
