package lila.tv

import scala.concurrent.duration._

import akka.actor._
import akka.actor.ActorSelection
import akka.pattern.{ ask, pipe }

import lila.common.LightUser
import lila.game.{ Game, GameRepo }

final class Tv(actor: ActorRef) {

  import Tv._

  implicit private def timeout = makeTimeout(200 millis)

  def getGame(channel: Tv.Channel): Fu[Option[Game]] =
    (actor ? TvActor.GetGameId(channel) mapTo manifest[Option[String]]) recover {
      case e: Exception =>
        logwarn("[TV]" + e.getMessage)
        none
    } flatMap { _ ?? GameRepo.game }

  def getBest = getGame(Tv.Channel.Best)

  def getChampions: Fu[Champions] =
    actor ? TvActor.GetChampions mapTo manifest[Champions]
}

object Tv {
  import chess.{ Speed => S, variant => V }
  import lila.rating.{ PerfType => P }

  case class Champion(user: LightUser, rating: Int)
  case class Champions(channels: Map[Channel, Champion]) {
    def get = channels.get _
  }

  sealed abstract class Channel(val name: String, val icon: String, filters: Seq[Game => Boolean]) {
    def filter(g: Game) = filters forall { _(g) }
    val key = toString.head.toLower + toString.drop(1)
  }
  object Channel {
    case object Best extends Channel(
      name = "Top Rated",
      icon = "C",
      filters = Seq(standard, freshBlitz))
    case object Bullet extends Channel(
      name = S.Bullet.name,
      icon = P.Bullet.iconChar.toString,
      filters = Seq(standard, speed(S.Bullet), fresh(15)))
    case object Blitz extends Channel(
      name = S.Blitz.name,
      icon = P.Blitz.iconChar.toString,
      filters = Seq(standard, speed(S.Blitz), freshBlitz))
    case object Classical extends Channel(
      name = S.Classical.name,
      icon = P.Classical.iconChar.toString,
      filters = Seq(standard, speed(S.Classical), fresh(60 * 3)))
    case object Chess960 extends Channel(
      name = V.Chess960.name,
      icon = P.Chess960.iconChar.toString,
      filters = Seq(variant(V.Chess960), freshBlitz))
    case object KingOfTheHill extends Channel(
      name = V.KingOfTheHill.name,
      icon = P.KingOfTheHill.iconChar.toString,
      filters = Seq(variant(V.KingOfTheHill), freshBlitz))
    case object ThreeCheck extends Channel(
      name = V.ThreeCheck.name,
      icon = P.ThreeCheck.iconChar.toString,
      filters = Seq(variant(V.ThreeCheck), freshBlitz))
    case object Antichess extends Channel(
      name = V.Antichess.name,
      icon = P.Antichess.iconChar.toString,
      filters = Seq(variant(V.Antichess), freshBlitz))
    case object Atomic extends Channel(
      name = V.Atomic.name,
      icon = P.Atomic.iconChar.toString,
      filters = Seq(variant(V.Atomic), freshBlitz))
    case object Horde extends Channel(
      name = V.Horde.name,
      icon = P.Horde.iconChar.toString,
      filters = Seq(variant(V.Horde), freshBlitz))
    val all = List(
      Best,
      Bullet, Blitz, Classical,
      Chess960, KingOfTheHill, ThreeCheck, Antichess, Atomic, Horde)
    val byKey = all.map { c => c.key -> c }.toMap
  }

  private def speed(speed: chess.Speed) = (g: Game) => g.speed == speed
  private def variant(variant: chess.variant.Variant) = (g: Game) => g.variant == variant
  private val standard = variant(V.Standard)
  private def fresh(seconds: Int) = (g: Game) => {
    g.isBeingPlayed && !g.olderThan(seconds)
  } || {
    g.finished && !g.olderThan(7)
  } // rematch time
  private val freshBlitz = fresh(40)
}
