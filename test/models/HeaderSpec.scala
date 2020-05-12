package models
import generators.MessagesModelGenerators
import org.scalatest.{FreeSpec, MustMatchers, StreamlinedXmlEquality}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class HeaderSpec extends FreeSpec with MustMatchers with ScalaCheckPropertyChecks with MessagesModelGenerators with StreamlinedXmlEquality {

}
