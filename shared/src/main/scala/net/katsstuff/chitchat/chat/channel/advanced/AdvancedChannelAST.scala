package net.katsstuff.chitchat.chat.channel.advanced

object AdvancedChannelAST {

  sealed trait Term {
    def idx: Int
  }
  sealed trait Expression extends Term

  final case class Constant(name: String, idx: Int)                              extends Expression
  final case class Apply(name: String, arguments: Map[String, String], idx: Int) extends Expression
  final case class ApplyVarArgs(name: String, arguments: Seq[String], idx: Int)  extends Expression

  final case class TransformedTerm(term: Term, transformation: Transformation, idx: Int) extends Term

  final case class Combine(method: CombineMethod, first: Term, second: Term, idx: Int) extends Term

  sealed trait CombineMethod
  case object Union      extends CombineMethod
  case object Intersect  extends CombineMethod
  case object Difference extends CombineMethod

  case class Transformation(expression: Expression, next: Option[Transformation], idx: Int)
}
