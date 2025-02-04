package com.getjenny.analyzer.operators

import com.getjenny.analyzer.expressions._
import scalaz._
import Scalaz._

/** Compare Operator
  *
  * It compare the result of two Expressions
  *
  * Created by Angelo Leto on 19/10/2018.
  */

class EqOperator(child: List[Expression]) extends AbstractOperator(child: List[Expression]) {
  require(child.length <= 2, "EqOperator can only have one Expression and a double number")
  override def toString: String = "equal(" + child.mkString(", ") + ")"

  def add(e: Expression, level: Int = 0): AbstractOperator = {
    if (level === 0) new EqOperator(e :: child)
    else if (level === 1) {
      child.headOption match {
        case Some(t) =>
          t match {
            case c: AbstractOperator => new EqOperator(c.add(e, level - 1) :: child.tail)
            case _ => throw OperatorException("EqOperator: trying to add to smt else than an operator")
          }
        case _ =>
          throw OperatorException("EqOperator: trying to add None instead of an operator")
      }
    } else {
      throw OperatorException("EqOperator: trying to add more than two expression")
    }
  }

  def evaluate(query: String, data: AnalyzersDataInternal = new AnalyzersDataInternal): Result = {
    val firstArgument = child.headOption match {
      case Some(t) => t
      case _ =>
        throw OperatorException("EqOperator: requires an expression as first argument")
    }

    val secondArgument = child.tail.headOption match {
      case Some(t) => t
      case _ =>
        throw OperatorException("EqOperator: requires an expression as second argument")
    }

    val res1: Result = firstArgument.evaluate(query = query, data = data)
    val res2: Result = secondArgument.evaluate(query = query, data = data)
    val score = if(res1.score == res2.score) 1.0 else 0.0
    val resData = AnalyzersDataInternal(
      traversedStates = res1.data.traversedStates,
      extractedVariables = res1.data.extractedVariables ++ res2.data.extractedVariables,
      data = res1.data.data ++ res2.data.data
    )
    Result(score=score, data = resData)
  }
}
