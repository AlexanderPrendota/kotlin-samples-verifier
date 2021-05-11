package com.samples.verifier.internal.utils

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

import org.jsoup.nodes.Element

// [+] Classes for storing AST of a custom filter
sealed class BooleanExpression<T> {
  abstract fun evaluate(element: T): Boolean
}

data class Not<T>(val body: BooleanExpression<T>) : BooleanExpression<T>() {
  override fun evaluate(element: T): Boolean = !body.evaluate(element)
}

data class And<T>(val left: BooleanExpression<T>, val right: BooleanExpression<T>) : BooleanExpression<T>() {
  override fun evaluate(element: T): Boolean = left.evaluate(element) && right.evaluate(element)
}

data class Or<T>(val left: BooleanExpression<T>, val right: BooleanExpression<T>) : BooleanExpression<T>() {
  override fun evaluate(element: T): Boolean = left.evaluate(element) || right.evaluate(element)
}

data class Variable(val name: String) {
  fun evaluate(element: Element): String {
    if (name == "tag") {
      return element.tagName()
    }
    return "[UNKNOWN]"
  }
}

data class Atribute(val name: String) : BooleanExpression<Element>() {
  override fun evaluate(element: Element): Boolean {
    return element.attributes().hasKey(name)
  }
}

data class CompareAttrOp(val left: Atribute, val right: String) : BooleanExpression<Element>() {
  override fun evaluate(element: Element): Boolean {
    val value = element.attributes().get(left.name) ?: return false
    return value.equals(right)
  }
}

data class CompareVarOp(val left: Variable, val right: String) : BooleanExpression<Element>() {
  override fun evaluate(element: Element): Boolean = left.evaluate(element).equals(right)
}
// [-] Classes for storing AST of a custom filter

/**
 * Describe the grammar for parsing a user filter in Kotlin DSL ( https://github.com/h0tk3y/better-parse ).
 * Grammar in BNF:
 * <attr> ::= [A-Za-z]\w*
 * <variable> ::= #[A-Za-z]\w*
 *
 * <bracedExpression> = '(' <orChain> ')'
 * <negation>  ::= '!' <term>
 * <atom> ::= <variable> '=' '"\w*"' |  <attr> '=' '"\w*"' | <attr>
 * <term> ::= <atom> | <negation> | <bracedExpression>
 * <andChain> ::=  <andChain> '&' <term> | <term>
 * <orChain> ::=   <orChain> '|' <andChain> | <andChain>
 */
private object BooleanGrammar : Grammar<BooleanExpression<Element>>() {

  // [+] tokens
  val equ by regexToken("={1,2}")
  val id by regexToken("[A-Za-z][\\w-]*")
  val varname by regexToken("#[A-Za-z]\\w*")
  val quote by regexToken("\"\\w*\"")
  val lpar by literalToken("(")
  val rpar by literalToken(")")
  val not by literalToken("!")
  val and by regexToken("&{1,2}")
  val or by regexToken("\\|{1,2}")
  // [-] tokens

  val attr: Parser<Atribute> by id map { Atribute(it.text) }
  val variable: Parser<Variable> by varname map { Variable(it.text.substring(1)) }

  val negation by -not * parser(this::term) map { Not(it) }
  val bracedExpression by -lpar * parser(this::orChain) * -rpar


  val atom: Parser<BooleanExpression<Element>> by
    (variable * -equ * quote).map { (v, e) -> CompareVarOp(v, e.text.substring(1, e.text.length - 1)) } or
    (attr * -equ * quote).map { (v, e) -> CompareAttrOp(v, e.text.substring(1, e.text.length - 1)) } or
    attr

  val term: Parser<BooleanExpression<Element>> by
    atom or
    negation or
    bracedExpression

  val andChain by leftAssociative(term, and) { a, _, b -> And(a, b) }
  val orChain by leftAssociative(andChain, or) { a, _, b -> Or(a, b) }

  override val rootParser by orChain
}

/**
 * Build AST by the grammar given above
 */
internal fun compileFilter(s: String) = BooleanGrammar.parseToEnd(s)