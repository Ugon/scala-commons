package com.avsystem.commons
package mongo

import java.util.NoSuchElementException

import com.avsystem.commons.serialization.GenCodec.ReadFailure
import com.avsystem.commons.serialization.{FieldInput, InputType, ListInput, ObjectInput}
import com.google.common.collect.AbstractIterator
import org.bson.types.ObjectId
import org.bson.{BsonReader, BsonType}

class BsonReaderInput(br: BsonReader) extends BsonInput {
  override def inputType: InputType = br.getCurrentBsonType match {
    case BsonType.NULL => InputType.Null
    case BsonType.ARRAY => InputType.List
    case BsonType.DOCUMENT => InputType.Object
    case _ => InputType.Simple
  }

  override def readNull(): Null = {
    br.readNull()
    null
  }
  override def readString(): String = br.readString()
  override def readBoolean(): Boolean = br.readBoolean()
  override def readInt(): Int = br.readInt32()
  override def readLong(): Long = br.readInt64()
  override def readTimestamp(): Long = br.readDateTime()
  override def readDouble(): Double = br.readDouble()
  override def readBinary(): Array[Byte] = br.readBinaryData().getData
  override def readList(): BsonReaderListInput = BsonReaderListInput.startReading(br)
  override def readObject(): BsonReaderObjectInput = BsonReaderObjectInput.startReading(br)
  override def readObjectId(): ObjectId = br.readObjectId()
  override def skip(): Unit = br.skipValue()
}

class BsonReaderFieldInput(name: String, private[mongo] val br: BsonReader) extends BsonReaderInput(br) with FieldInput {
  override def fieldName: String = name
}

abstract class BsonIdFieldInput(val inputType: InputType, bsonType: BsonType) extends BsonInput with FieldInput {
  override def fieldName: String = "_id"
  override def skip(): Unit = ()

  override def readList(): ListInput = throw new UnsupportedOperationException //can not be id

  override def readObjectId(): ObjectId = throw new ReadFailure(s"Id of different type: ${bsonType.name()}")
  override def readNull(): Null = throw new ReadFailure(s"Id of different type: ${bsonType.name()}")
  override def readString(): String = throw new ReadFailure(s"Id of different type: ${bsonType.name()}")
  override def readBoolean(): Boolean = throw new ReadFailure(s"Id of different type: ${bsonType.name()}")
  override def readInt(): Int = throw new ReadFailure(s"Id of different type: ${bsonType.name()}")
  override def readLong(): Long = throw new ReadFailure(s"Id of different type: ${bsonType.name()}")
  override def readDouble(): Double = throw new ReadFailure(s"Id of different type: ${bsonType.name()}")
  override def readBinary(): Array[Byte] = throw new ReadFailure(s"Id of different type: ${bsonType.name()}")
  override def readObject(): ObjectInput = throw new ReadFailure(s"Id of different type: ${bsonType.name()}")

}

class BsonReaderIterator[T](br: BsonReader, endCallback: BsonReader => Unit, readElement: BsonReader => T)
  extends AbstractIterator[T] {
  override def computeNext(): T = {
    if (br.readBsonType() == BsonType.END_OF_DOCUMENT) {
      endCallback(br)
      endOfData()
    } else {
      readElement(br)
    }
  }
}

class BsonReaderListInput private(br: BsonReader) extends ListInput {
  private val it = new BsonReaderIterator(br, _.readEndArray(), new BsonReaderInput(_))

  override def hasNext: Boolean = it.hasNext
  override def nextElement(): BsonReaderInput = it.next()
}
object BsonReaderListInput {
  def startReading(br: BsonReader): BsonReaderListInput = {
    br.readStartArray()
    new BsonReaderListInput(br)
  }
}


class BsonReaderObjectInput private(br: BsonReader) extends ObjectInput {
  private val it = new BsonReaderIterator(br, _.readEndDocument(),
    br => new BsonReaderFieldInput(KeyEscaper.unescape(br.readName()), br)
  )

  private val id: Opt[BsonIdFieldInput] =
    if (it.hasNext && it.peek().fieldName == "_id") Opt.some(readAccordingInput(it.next().br))
    else Opt.empty

  private var idToRead = id.isDefined

  override def hasNext: Boolean = it.hasNext || idToRead
  override def nextField(): FieldInput =
    if (it.hasNext) {
      it.next()
    }
    else if (idToRead) {
      idToRead = false
      id.get
    } else {
      throw new NoSuchElementException
    }


  def readAccordingInput(bsonReader: BsonReader): BsonIdFieldInput = {
    import InputType._

    //@formatter:off
    bsonReader.getCurrentBsonType match {
      case BsonType.DOUBLE =>
        val readValue = bsonReader.readDouble()
        new BsonIdFieldInput(Simple, BsonType.DOUBLE) {
          override def readDouble() = readValue
        }
      case BsonType.STRING =>
        val readValue = bsonReader.readString()
        new BsonIdFieldInput(Simple, BsonType.STRING) {
          override def readString() = readValue
        }
      case BsonType.DOCUMENT =>
        val readValue = BsonReaderObjectInput.startReading(bsonReader)
        new BsonIdFieldInput(Simple, BsonType.DOCUMENT) {
          override def readObject() = readValue
        }
      case BsonType.BINARY =>
        val readValue = bsonReader.readBinaryData().getData
        new BsonIdFieldInput(Simple, BsonType.BINARY) {
          override def readBinary() = readValue
        }
      case BsonType.UNDEFINED =>
        val readValue = bsonReader.readUndefined()
        new BsonIdFieldInput(Simple, BsonType.UNDEFINED) {
          override def readNull() = null
        }
      case BsonType.OBJECT_ID =>
        val readValue = bsonReader.readObjectId()
        new BsonIdFieldInput(Simple, BsonType.OBJECT_ID) {
          override def readObjectId() = readValue
        }
      case BsonType.BOOLEAN =>
        val readValue = bsonReader.readBoolean()
        new BsonIdFieldInput(Simple, BsonType.BOOLEAN) {
          override def readBoolean() = readValue
        }
      case BsonType.DATE_TIME =>
        val readValue = bsonReader.readDateTime()
        new BsonIdFieldInput(Simple, BsonType.DATE_TIME) {
          override def readTimestamp() = readValue
        }
      case BsonType.NULL =>
        val readValue = bsonReader.readNull()
        new BsonIdFieldInput(Simple, BsonType.NULL) {
          override def readNull() = null
        }
      case BsonType.REGULAR_EXPRESSION =>
        val readValue = bsonReader.readRegularExpression().asString().getValue
        new BsonIdFieldInput(Simple, BsonType.REGULAR_EXPRESSION) {
          override def readString() = readValue
        }
      case BsonType.DB_POINTER =>
        val readValue = bsonReader.readDBPointer()
        new BsonIdFieldInput(Simple, BsonType.DB_POINTER) {
          override def readNull() = null
        }
      case BsonType.JAVASCRIPT =>
        val readValue = bsonReader.readJavaScript()
        new BsonIdFieldInput(Simple, BsonType.JAVASCRIPT) {
          override def readString() = readValue
        }
      case BsonType.SYMBOL =>
        val readValue = bsonReader.readSymbol()
        new BsonIdFieldInput(Simple, BsonType.SYMBOL) {
          override def readString() = readValue
        }
      case BsonType.JAVASCRIPT_WITH_SCOPE =>
        val readValue = bsonReader.readJavaScriptWithScope()
        new BsonIdFieldInput(Simple, BsonType.JAVASCRIPT_WITH_SCOPE) {
          override def readString() = readValue
        }
      case BsonType.INT32 =>
        val readValue = bsonReader.readInt32()
        new BsonIdFieldInput(Simple, BsonType.INT32) {
          override def readInt() = readValue
        }
      case BsonType.TIMESTAMP =>
        val readValue = bsonReader.readTimestamp().getTime
        new BsonIdFieldInput(Simple, BsonType.TIMESTAMP) {
          override def readTimestamp() = readValue
        }
      case BsonType.INT64 =>
        val readValue = bsonReader.readInt64()
        new BsonIdFieldInput(Simple, BsonType.INT64) {
          override def readLong() = readValue
        }
      case BsonType.MIN_KEY =>
        val readValue = bsonReader.readMinKey()
        new BsonIdFieldInput(Simple, BsonType.MIN_KEY) {
          override def readNull() = null
        }
      case BsonType.MAX_KEY =>
        val readValue = bsonReader.readMaxKey()
        new BsonIdFieldInput(Simple, BsonType.MAX_KEY) {
          override def readNull() = null
        }
      case BsonType.END_OF_DOCUMENT => throw new IllegalArgumentException()
      case BsonType.ARRAY => throw new IllegalArgumentException()
    }
  }
}

object BsonReaderObjectInput {
  def startReading(br: BsonReader): BsonReaderObjectInput = {
    br.readStartDocument()
    new BsonReaderObjectInput(br)
  }
}
