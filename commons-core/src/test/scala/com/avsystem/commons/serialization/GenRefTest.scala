package com.avsystem.commons
package serialization

import org.scalatest.FunSuite

@flatten sealed trait Seal {
  def id: String
}
case class Klass(@name("_id") id: String) extends Seal
case object Objekt extends Seal {
  @generated @name("_id") def id: String = "O"
}

case class Bottom(mapa: Map[String, Int])
case class Middle(@name("bot") bottom: Bottom)
case class Toplevel(middle: Middle, seal: Seal = Objekt)
@transparent case class TransparentToplevel(toplevel: Toplevel)

case class CodecRef[S, T](ref: GenRef[S, T])(implicit targetCodec: GenCodec[T])

class GenRefTest extends FunSuite {
  test("simple raw ref") {
    val path = RawRef[TransparentToplevel](_.toplevel.middle.bottom.mapa("str")).normalize.toList
    assert(path == List("middle", "bot", "mapa", "str").map(RawRef.Field))
  }

  test("simple gen ref") {
    val ref = GenRef[TransparentToplevel](_.toplevel.middle.bottom.mapa("str"))
    val obj = TransparentToplevel(Toplevel(Middle(Bottom(Map("str" -> 42)))))
    assert(ref(obj) == 42)
  }

  test("gen ref splicing") {
    val subRef = GenRef[TransparentToplevel](_.toplevel.middle)
    val ref1 = GenRef[TransparentToplevel](t => subRef(t).bottom.mapa("str"))
    val ref2 = GenRef[TransparentToplevel](subRef andThen (_.bottom.mapa("str")))
    val obj = TransparentToplevel(Toplevel(Middle(Bottom(Map("str" -> 42)))))
    assert(ref1(obj) == 42)
    assert(ref1.rawRef.normalize.toList == List("middle", "bot", "mapa", "str").map(RawRef.Field))
    assert(ref2(obj) == 42)
    assert(ref2.rawRef.normalize.toList == List("middle", "bot", "mapa", "str").map(RawRef.Field))
  }

  test("gen ref implicits test") {
    import GenRef.Implicits._

    val codecRef = CodecRef((_: TransparentToplevel).toplevel.middle.bottom.mapa("str"))
    val obj = TransparentToplevel(Toplevel(Middle(Bottom(Map("str" -> 42)))))
    assert(codecRef.ref(obj) == 42)
  }

  test("sealed trait common field test") {
    val ref = GenRef[Toplevel](_.seal.id)
    assert(ref.rawRef.normalize.toList == List("seal", "_id").map(RawRef.Field))
  }
}
