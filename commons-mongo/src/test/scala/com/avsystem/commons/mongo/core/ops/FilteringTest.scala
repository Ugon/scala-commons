package com.avsystem.commons
package mongo.core.ops

import java.util.regex.Pattern

import com.avsystem.commons.mongo.BsonRef
import com.avsystem.commons.serialization.GenCodec
import com.mongodb.client.model.Filters
import org.bson.BsonType
import org.bson.conversions.Bson
import org.scalatest.FunSuite

class FilteringTest extends FunSuite {

  import Filtering._
  import FilteringTest._

  private def testCase(name: String)(filter: (BsonRef[String]) => Bson)(verify: (String) => Bson): Unit = {
    import BsonEquality.bsonEquality

    test(name) {
      assert(filter(sRef) === verify("s"))
    }
  }

  private def testValue(name: String)(filter: (BsonRef[String], String) => Bson)(verify: (String, String) => Bson): Unit = {
    val someValue = "someValue"
    testCase(name)(filter(_, someValue))(verify(_, someValue))
  }

  testValue("equal")(_ equal _)(Filters.eq)
  testValue("notEqual")(_ notEqual _)(Filters.ne)

  testValue("gt")(_ gt _)(Filters.gt)
  testValue("lt")(_ lt _)(Filters.lt)
  testValue("gte")(_ gte _)(Filters.gte)
  testValue("lte")(_ lte _)(Filters.lte)

  testValue("in")(_ in _)(Filters.in(_, _))
  testValue("nin")(_ nin _)(Filters.nin(_, _))

  testCase("exists")(_.exists())(Filters.exists)
  testCase("notExists")(_.exists(false))(Filters.exists(_, false))

  testCase("ofType")(_.ofType("someTypeName"))(Filters.`type`(_, "someTypeName"))
  testCase("ofTypeEnum")(_.ofType(BsonType.STRING))(Filters.`type`(_, BsonType.STRING))

  testCase("mod")(_.mod(313, 131))(Filters.mod(_, 313, 131))

  private val regexString = "\\d"
  private val regexScala = regexString.r
  private val regexJava = Pattern.compile(regexString)
  testCase("regexScala")(_ regex regexScala)(Filters.regex(_, regexString))
  testCase("regexJava")(_ regex regexJava)(Filters.regex(_, regexJava))
  testCase("regexString")(_ regex regexString)(Filters.regex(_, regexString))
  testCase("regexOptions")(_.regex(regexString, "ops"))(Filters.regex(_, regexString, "ops"))

  private val simpleFilter = Filters.eq("key", "value")
  testCase("elemMatch")(_ elemMatch simpleFilter)(Filters.elemMatch(_, simpleFilter))

  testCase("size")(_ size 131)(Filters.size(_, 131))
}

object FilteringTest extends BsonRef.Creator[SomeEntity] {
  implicit val codec: GenCodec[SomeEntity] = GenCodec.materialize
  val sRef: BsonRef[String] = ref(_.s)
}
