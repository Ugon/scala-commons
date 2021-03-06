package com.avsystem.commons
package serialization

import com.avsystem.commons.annotation.explicitGenerics

import scala.annotation.implicitNotFound

/**
  * Typeclass which implements two-directional conversion between values of some type and field names
  * used in [[ObjectOutput.writeField]] and [[ObjectInput.nextField]] ([[FieldInput.fieldName]]).
  * Every type which has a natural, unambiguous string representation should have a `GenKeyCodec`.
  */
@implicitNotFound("Can't convert between map key and ${T} - GenKeyCodec not found")
trait GenKeyCodec[T] {
  def read(key: String): T
  def write(value: T): String
}

object GenKeyCodec {
  /**
    * Materializes a `GenKeyCodec` for a "sealed enum" (sealed hierarchy with case objects at the bottom).
    * The generated codec uses object name by default as key value.
    * This can be adjusted with `@name` annotation.
    */
  def forSealedEnum[T]: GenKeyCodec[T] = macro macros.serialization.GenKeyCodecMacros.forSealedEnum[T]

  @explicitGenerics
  def read[T](key: String)(implicit keyCodec: GenKeyCodec[T]): T = keyCodec.read(key)
  def write[T](value: T)(implicit keyCodec: GenKeyCodec[T]): String = keyCodec.write(value)

  def create[T](readFun: String => T, writeFun: T => String): GenKeyCodec[T] =
    new GenKeyCodec[T] {
      def read(key: String): T = readFun(key)
      def write(value: T): String = writeFun(value)
    }

  implicit val BooleanKeyCodec: GenKeyCodec[Boolean] = create(_.toBoolean, _.toString)
  implicit val CharKeyCodec: GenKeyCodec[Char] = create(_.charAt(0), _.toString)
  implicit val ByteKeyCodec: GenKeyCodec[Byte] = create(_.toByte, _.toString)
  implicit val ShortKeyCodec: GenKeyCodec[Short] = create(_.toShort, _.toString)
  implicit val IntKeyCodec: GenKeyCodec[Int] = create(_.toInt, _.toString)
  implicit val LongKeyCodec: GenKeyCodec[Long] = create(_.toLong, _.toString)

  implicit val JBooleanKeyCodec: GenKeyCodec[JBoolean] = create(_.toBoolean, _.toString)
  implicit val JCharacterKeyCodec: GenKeyCodec[JCharacter] = create(_.charAt(0), _.toString)
  implicit val JByteKeyCodec: GenKeyCodec[JByte] = create(_.toByte, _.toString)
  implicit val JShortKeyCodec: GenKeyCodec[JShort] = create(_.toShort, _.toString)
  implicit val JIntKeyCodec: GenKeyCodec[JInteger] = create(_.toInt, _.toString)
  implicit val JLongKeyCodec: GenKeyCodec[JLong] = create(_.toLong, _.toString)

  implicit val StringKeyCodec: GenKeyCodec[String] = create(identity, identity)

  implicit def jEnumKeyCodec[E <: Enum[E]](implicit ct: ClassTag[E]): GenKeyCodec[E] =
    GenKeyCodec.create(
      string => Enum.valueOf(ct.runtimeClass.asInstanceOf[Class[E]], string),
      enum => enum.name()
    )
}
