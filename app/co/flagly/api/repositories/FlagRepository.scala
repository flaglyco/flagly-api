package co.flagly.api.repositories

import java.util.UUID

import co.flagly.api.errors.FlaglyError
import co.flagly.data.Flag

class FlagRepository {
  private val testFlags: List[Flag] =
    List(
      Flag.text("test-flag-1", "Test Flag 1", "foo"),
      Flag.number("test-flag-2", "Test Flag 2", 1),
      Flag.boolean("test-flag-3", "Test Flag 3", value = true)
    )

  private var flags: Map[UUID, Flag] = testFlags.map(f => f.id -> f).toMap

  def create(flag: Flag): Either[FlaglyError, Flag] =
    if (flags.contains(flag.id)) {
      Left(FlaglyError.AlreadyExists)
    } else {
      flags += (flag.id -> flag)
      Right(flag)
    }

  def getAll: Either[FlaglyError, List[Flag]] =
    Right(flags.values.toList.sortBy(_.name))

  def get(id: UUID): Either[FlaglyError, Option[Flag]] =
    Right(flags.get(id))

  def update(id: UUID, updater: Flag => Flag): Either[FlaglyError, Flag] =
    flags.get(id) match {
      case None =>
        Left(FlaglyError.DoesNotExist)

      case Some(flag) =>
        val newFlag = updater(flag)
        flags += (id -> newFlag)
        Right(newFlag)
    }

  def delete(id: UUID): Either[FlaglyError, Unit] =
    flags.get(id) match {
      case None =>
        Left(FlaglyError.DoesNotExist)

      case Some(flag) =>
        flags -= flag.id
        Right(())
    }
}
