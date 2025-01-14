package com.getjenny.analyzer.operators

import com.getjenny.analyzer.expressions._
import scalaz._
import Scalaz._

/**
  * Created by mal on 21/02/2017.
  */

class BooleanAndOperator(children: List[Expression]) extends AbstractOperator(children: List[Expression]) {
  override def toString: String = "BooleanAndOperator(" + children.mkString(", ") + ")"
  def add(e: Expression, level: Int = 0): AbstractOperator = {
    if (level === 0) new BooleanAndOperator(e :: children)
    else {
      children.headOption match {
        case Some(t) =>
          t match {
            case c: AbstractOperator => new BooleanAndOperator(c.add(e, level - 1) :: children.tail)
            case _ => throw OperatorException("BooleanAndOperator: trying to add to smt else than an operator")
          }
        case _ =>
          throw OperatorException("BooleanAndOperator: trying to add None instead of an operator")
      }
    }
  }

  def evaluate(query: String, data: AnalyzersDataInternal = AnalyzersDataInternal()): Result = {
    def loop(l: List[Expression]): Result = {
      val firstRes = l.headOption match {
        case Some(t) => t.matches(query, data)
        case _ => throw OperatorException("BooleanAndOperator: operator argument is empty")
      }
      if (firstRes.score < 1.0d) {
        Result(score=0.0d, data = firstRes.data)
      } else if (l.tail.isEmpty) {
        Result(score=1.0d, data = firstRes.data)
      } else {
        val res = loop(l.tail)
        Result(score = res.score,
          AnalyzersDataInternal(
            traversedStates = data.traversedStates,
            extractedVariables = res.data.extractedVariables ++ firstRes.data.extractedVariables,
            data = res.data.data ++ firstRes.data.data
          )
        )
      }
    }
    loop(children)
  }
}
