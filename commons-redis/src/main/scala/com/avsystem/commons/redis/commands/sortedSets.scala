package com.avsystem.commons
package redis.commands

import akka.util.ByteString
import com.avsystem.commons.misc.{NamedEnum, NamedEnumCompanion, Opt, OptArg}
import com.avsystem.commons.redis.CommandEncoder.CommandArg
import com.avsystem.commons.redis._
import com.avsystem.commons.redis.commands.ReplyDecoders._
import com.avsystem.commons.redis.util.SingletonSeq

trait SortedSetsApi extends ApiSubset {
  def zadd(key: Key, score: Double, members: Value*): Result[Long] = zadd(key, members.map((_, score)))
  def zadd(key: Key, memberScores: (Value, Double)*): Result[Long] = zadd(key, memberScores)
  def zadd(key: Key, memberScores: Seq[(Value, Double)], existence: OptArg[Boolean] = OptArg.Empty, changed: Boolean = false): Result[Long] =
    execute(new Zadd(key, memberScores, existence.toOpt, changed))
  def zaddIncr(key: Key, member: Value, score: Double, existence: OptArg[Boolean] = OptArg.Empty): Result[Opt[Double]] =
    execute(new ZaddIncr(key, member, score, existence.toOpt))
  def zcard(key: Key): Result[Long] =
    execute(new Zcard(key))
  def zcount(key: Key, min: ScoreLimit = ScoreLimit.MinusInf, max: ScoreLimit = ScoreLimit.PlusInf): Result[Long] =
    execute(new Zcount(key, min, max))
  def zincrby(key: Key, increment: Double, member: Value): Result[Double] =
    execute(new Zincrby(key, increment, member))
  def zinterstore(destination: Key, keys: Key*): Result[Long] = zinterstore(destination, keys)
  def zinterstore(destination: Key, keys: Seq[Key], aggregation: OptArg[Aggregation] = OptArg.Empty): Result[Long] =
    execute(new Zinterstore(destination, keys, Opt.Empty, aggregation.toOpt))
  def zinterstoreWeights(destination: Key, keysWeights: (Key, Double)*): Result[Long] = zinterstoreWeights(destination, keysWeights)
  def zinterstoreWeights(destination: Key, keysWeights: Seq[(Key, Double)], aggregation: OptArg[Aggregation] = OptArg.Empty): Result[Long] =
    execute(new Zinterstore(destination, keysWeights.map(_._1), keysWeights.map(_._2).opt, aggregation.toOpt))
  def zlexcount(key: Key, min: LexLimit[Value] = LexLimit.MinusInf, max: LexLimit[Value] = LexLimit.PlusInf): Result[Long] =
    execute(new Zlexcount(key, min, max))
  def zrange(key: Key, start: Long = 0, stop: Long = -1): Result[Seq[Value]] =
    execute(new Zrange(key, start, stop))
  def zrangeWithscores(key: Key, start: Long = 0, stop: Long = -1): Result[Seq[(Value, Double)]] =
    execute(new ZrangeWithscores(key, start, stop))
  def zrangebylex(key: Key, min: LexLimit[Value] = LexLimit.MinusInf, max: LexLimit[Value] = LexLimit.PlusInf, limit: OptArg[Limit] = OptArg.Empty): Result[Seq[Value]] =
    execute(new Zrangebylex(key, min, max, limit.toOpt))
  def zrangebyscore(key: Key, min: ScoreLimit = ScoreLimit.MinusInf, max: ScoreLimit = ScoreLimit.PlusInf, limit: OptArg[Limit] = OptArg.Empty): Result[Seq[Value]] =
    execute(new Zrangebyscore(key, min, max, limit.toOpt))
  def zrangebyscoreWithscores(key: Key, min: ScoreLimit = ScoreLimit.MinusInf, max: ScoreLimit = ScoreLimit.PlusInf, limit: OptArg[Limit] = OptArg.Empty): Result[Seq[(Value, Double)]] =
    execute(new ZrangebyscoreWithscores(key, min, max, limit.toOpt))
  def zrank(key: Key, member: Value): Result[Opt[Long]] =
    execute(new Zrank(key, member))
  def zrem(key: Key, members: Value*): Result[Long] =
    execute(new Zrem(key, members))
  def zremrangebylex(key: Key, min: LexLimit[Value] = LexLimit.MinusInf, max: LexLimit[Value] = LexLimit.PlusInf): Result[Long] =
    execute(new Zremrangebylex(key, min, max))
  def zremrangebyrank(key: Key, start: Long = 0, stop: Long = -1): Result[Long] =
    execute(new Zremrangebyrank(key, start, stop))
  def zremrangebyscore(key: Key, min: ScoreLimit = ScoreLimit.MinusInf, max: ScoreLimit = ScoreLimit.PlusInf): Result[Long] =
    execute(new Zremrangebyscore(key, min, max))
  def zrevrange(key: Key, start: Long = 0, stop: Long = -1): Result[Seq[Value]] =
    execute(new Zrevrange(key, start, stop))
  def zrevrangeWithscores(key: Key, start: Long = 0, stop: Long = -1): Result[Seq[(Value, Double)]] =
    execute(new ZrevrangeWithscores(key, start, stop))
  def zrevrangebylex(key: Key, max: LexLimit[Value] = LexLimit.PlusInf, min: LexLimit[Value] = LexLimit.MinusInf, limit: OptArg[Limit] = OptArg.Empty): Result[Seq[Value]] =
    execute(new Zrevrangebylex(key, max, min, limit.toOpt))
  def zrevrangebyscore(key: Key, max: ScoreLimit = ScoreLimit.PlusInf, min: ScoreLimit = ScoreLimit.MinusInf, limit: OptArg[Limit] = OptArg.Empty): Result[Seq[Value]] =
    execute(new Zrevrangebyscore(key, max, min, limit.toOpt))
  def zrevrangebyscoreWithscores(key: Key, max: ScoreLimit = ScoreLimit.PlusInf, min: ScoreLimit = ScoreLimit.MinusInf, limit: OptArg[Limit] = OptArg.Empty): Result[Seq[(Value, Double)]] =
    execute(new ZrevrangebyscoreWithscores(key, max, min, limit.toOpt))
  def zrevrank(key: Key, member: Value): Result[Opt[Long]] =
    execute(new Zrevrank(key, member))
  def zscan(key: Key, cursor: Cursor, matchPattern: OptArg[Value] = OptArg.Empty, count: OptArg[Long] = OptArg.Empty): Result[(Cursor, Seq[(Value, Double)])] =
    execute(new Zscan(key, cursor, matchPattern.toOpt, count.toOpt))
  def zscore(key: Key, member: Value): Result[Opt[Double]] =
    execute(new Zscore(key, member))
  def zunionstore(destination: Key, keys: Key*): Result[Long] = zunionstore(destination, keys)
  def zunionstore(destination: Key, keys: Seq[Key], aggregation: OptArg[Aggregation] = OptArg.Empty): Result[Long] =
    execute(new Zunionstore(destination, keys, Opt.Empty, aggregation.toOpt))
  def zunionstoreWeights(destination: Key, keysWeights: (Key, Double)*): Result[Long] = zunionstoreWeights(destination, keysWeights)
  def zunionstoreWeights(destination: Key, keysWeights: Seq[(Key, Double)], aggregation: OptArg[Aggregation] = OptArg.Empty): Result[Long] =
    execute(new Zunionstore(destination, keysWeights.map(_._1), keysWeights.map(_._2).opt, aggregation.toOpt))

  private abstract class AbstractZadd[T](decoder: ReplyDecoder[T])
    (key: Key, memberScores: Seq[(Value, Double)], existence: Opt[Boolean], changed: Boolean, incr: Boolean)
    extends AbstractRedisCommand[T](decoder) with NodeCommand {
    val encoded = encoder("ZADD").key(key).add(existence.map(e => if (e) "XX" else "NX"))
      .addFlag("CH", changed).addFlag("INCR", incr).argDataPairs(memberScores.iterator.map(_.swap)).result
  }

  private final class Zadd(key: Key, memberScores: Seq[(Value, Double)], existence: Opt[Boolean], changed: Boolean)
    extends AbstractZadd[Long](integerLong)(key, memberScores, existence, changed, incr = false)

  private final class ZaddIncr(key: Key, member: Value, score: Double, existence: Opt[Boolean])
    extends AbstractZadd[Opt[Double]](nullBulkOr(bulkDouble))(key, new SingletonSeq((member, score)), existence, changed = false, incr = true)

  private final class Zcard(key: Key) extends RedisLongCommand with NodeCommand {
    val encoded = encoder("ZCARD").key(key).result
  }

  private final class Zcount(key: Key, min: ScoreLimit, max: ScoreLimit) extends RedisLongCommand with NodeCommand {
    val encoded = encoder("ZCOUNT").key(key).add(min.repr).add(max.repr).result
  }

  private final class Zincrby(key: Key, increment: Double, member: Value) extends RedisDoubleCommand with NodeCommand {
    val encoded = encoder("ZINCRBY").key(key).add(increment).data(member).result
  }

  private final class Zinterstore(destination: Key, keys: Seq[Key], weights: Opt[Seq[Double]], aggregation: Opt[Aggregation])
    extends RedisLongCommand with NodeCommand {

    val encoded = encoder("ZINTERSTORE").key(destination).add(keys.size).keys(keys)
      .optAdd("WEIGHTS", weights).optAdd("AGGREGATE", aggregation).result
  }

  private final class Zlexcount(key: Key, min: LexLimit[Value], max: LexLimit[Value])
    extends RedisLongCommand with NodeCommand {
    val encoded = encoder("ZLEXCOUNT").key(key).add(LexLimit.repr(min)).add(LexLimit.repr(max)).result
  }

  private abstract class AbstractZrange[T](cmd: String, decoder: ReplyDecoder[Seq[T]])(key: Key, start: Long, stop: Long, withscores: Boolean)
    extends AbstractRedisCommand[Seq[T]](decoder) with NodeCommand {
    val encoded = encoder(cmd).key(key).add(start).add(stop).addFlag("WITHSCORES", withscores).result
  }

  private final class Zrange(key: Key, start: Long, stop: Long)
    extends AbstractZrange[Value]("ZRANGE", multiBulk[Value])(key, start, stop, withscores = false)

  private final class ZrangeWithscores(key: Key, start: Long, stop: Long)
    extends AbstractZrange[(Value, Double)]("ZRANGE", pairedMultiBulk(bulk[Value], bulkDouble))(
      key, start, stop, withscores = true)

  private final class Zrangebylex(key: Key, min: LexLimit[Value], max: LexLimit[Value], limit: Opt[Limit])
    extends RedisDataSeqCommand[Value] with NodeCommand {
    val encoded = encoder("ZRANGEBYLEX").key(key).add(LexLimit.repr(min)).add(LexLimit.repr(max)).optAdd("LIMIT", limit).result
  }

  private abstract class AbstractZrangebyscore[T](cmd: String, decoder: ReplyDecoder[Seq[T]])(
    key: Key, firstLimit: ScoreLimit, secondLimit: ScoreLimit, withscores: Boolean, limit: Opt[Limit])
    extends AbstractRedisCommand[Seq[T]](decoder) with NodeCommand {
    val encoded = encoder(cmd).key(key).add(firstLimit.repr).add(secondLimit.repr).addFlag("WITHSCORES", withscores).optAdd("LIMIT", limit).result
  }

  private final class Zrangebyscore(key: Key, min: ScoreLimit, max: ScoreLimit, limit: Opt[Limit])
    extends AbstractZrangebyscore[Value]("ZRANGEBYSCORE", multiBulk[Value])(key, min, max, withscores = false, limit)

  private final class ZrangebyscoreWithscores(key: Key, min: ScoreLimit, max: ScoreLimit, limit: Opt[Limit])
    extends AbstractZrangebyscore[(Value, Double)]("ZRANGEBYSCORE", pairedMultiBulk(bulk[Value], bulkDouble))(
      key, min, max, withscores = true, limit)

  private final class Zrank(key: Key, member: Value) extends RedisOptLongCommand with NodeCommand {
    val encoded = encoder("ZRANK").key(key).data(member).result
  }

  private final class Zrem(key: Key, members: Seq[Value]) extends RedisLongCommand with NodeCommand {
    val encoded = encoder("ZREM").key(key).datas(members).result
  }

  private final class Zremrangebylex(key: Key, min: LexLimit[Value], max: LexLimit[Value])
    extends RedisLongCommand with NodeCommand {
    val encoded = encoder("ZREMRANGEBYLEX").key(key).add(LexLimit.repr(min)).add(LexLimit.repr(max)).result
  }

  private final class Zremrangebyrank(key: Key, start: Long, stop: Long)
    extends RedisLongCommand with NodeCommand {
    val encoded = encoder("ZREMRANGEBYRANK").key(key).add(start).add(stop).result
  }

  private final class Zremrangebyscore(key: Key, min: ScoreLimit, max: ScoreLimit)
    extends RedisLongCommand with NodeCommand {
    val encoded = encoder("ZREMRANGEBYSCORE").key(key).add(min.repr).add(max.repr).result
  }

  private final class Zrevrange(key: Key, start: Long, stop: Long)
    extends AbstractZrange[Value]("ZREVRANGE", multiBulk[Value])(key, start, stop, withscores = false)

  private final class ZrevrangeWithscores(key: Key, start: Long, stop: Long)
    extends AbstractZrange[(Value, Double)]("ZREVRANGE", pairedMultiBulk(bulk[Value], bulkDouble))(key, start, stop, withscores = true)

  private final class Zrevrangebylex(key: Key, max: LexLimit[Value], min: LexLimit[Value], limit: Opt[Limit])
    extends RedisDataSeqCommand[Value] with NodeCommand {
    val encoded = encoder("ZREVRANGEBYLEX").key(key).add(LexLimit.repr(max)).add(LexLimit.repr(min)).optAdd("LIMIT", limit).result
  }

  private final class Zrevrangebyscore(key: Key, max: ScoreLimit, min: ScoreLimit, limit: Opt[Limit])
    extends AbstractZrangebyscore[Value]("ZREVRANGEBYSCORE", multiBulk[Value])(key, max, min, withscores = false, limit)

  private final class ZrevrangebyscoreWithscores(key: Key, max: ScoreLimit, min: ScoreLimit, limit: Opt[Limit])
    extends AbstractZrangebyscore[(Value, Double)]("ZREVRANGEBYSCORE", pairedMultiBulk(bulk[Value], bulkDouble))(
      key, max, min, withscores = true, limit)

  private final class Zrevrank(key: Key, member: Value) extends RedisOptLongCommand with NodeCommand {
    val encoded = encoder("ZREVRANK").key(key).data(member).result
  }

  private final class Zscan(key: Key, cursor: Cursor, matchPattern: Opt[Value], count: Opt[Long])
    extends RedisScanCommand[(Value, Double)](pairedMultiBulk(bulk[Value], bulkDouble)) with NodeCommand {
    val encoded = encoder("ZSCAN").key(key).add(cursor.raw).optData("MATCH", matchPattern).optAdd("COUNT", count).result
  }

  private final class Zscore(key: Key, member: Value) extends RedisOptDoubleCommand with NodeCommand {
    val encoded = encoder("ZSCORE").key(key).data(member).result
  }

  private final class Zunionstore(destination: Key, keys: Seq[Key], weights: Opt[Seq[Double]], aggregation: Opt[Aggregation])
    extends RedisLongCommand with NodeCommand {

    val encoded = encoder("ZUNIONSTORE").key(destination).add(keys.size).keys(keys)
      .optAdd("WEIGHTS", weights).optAdd("AGGREGATE", aggregation).result
  }
}

case class ScoreLimit(value: Double, inclusive: Boolean) {
  def repr = (if (!inclusive) "(" else "") + (value match {
    case Double.NegativeInfinity => "-inf"
    case Double.PositiveInfinity => "+inf"
    case _ => value.toString
  })
}
object ScoreLimit {
  def incl(value: Double) = ScoreLimit(value, inclusive = true)
  def excl(value: Double) = ScoreLimit(value, inclusive = false)

  val MinusInf = ScoreLimit.incl(Double.NegativeInfinity)
  val PlusInf = ScoreLimit.incl(Double.PositiveInfinity)
}

sealed trait LexLimit[+V]
object LexLimit {
  def apply[V](value: V, inclusive: Boolean): LexLimit[V] = Finite(value, inclusive)

  def incl[V](value: V): LexLimit[V] = Finite(value, inclusive = true)
  def excl[V](value: V): LexLimit[V] = Finite(value, inclusive = false)

  case class Finite[+V](value: V, inclusive: Boolean) extends LexLimit[V]
  object MinusInf extends LexLimit[Nothing]
  object PlusInf extends LexLimit[Nothing]

  def repr[V: RedisDataCodec](limit: LexLimit[V]) = limit match {
    case Finite(value, incl) =>
      (if (incl) '[' else '(').toByte +: RedisDataCodec.write(value)
    case MinusInf => ByteString('-'.toByte)
    case PlusInf => ByteString('+'.toByte)
  }
}

case class Limit(offset: Long, count: Long)
object Limit {
  implicit val commandArg: CommandArg[Limit] =
    CommandArg((e, l) => e.add(l.offset).add(l.count))
}

sealed abstract class Aggregation(val name: String) extends NamedEnum
object Aggregation extends NamedEnumCompanion[Aggregation] {
  case object Sum extends Aggregation("SUM")
  case object Min extends Aggregation("MIN")
  case object Max extends Aggregation("MAX")
  val values: List[Aggregation] = caseObjects
}