package de.innfactory.afps

import scala.annotation.tailrec

sealed abstract class FList[+T] {
  def head: T
  def tail: FList[T]

  def isEmpty: Boolean = this match {
    case FLEmpty => true
    case FLNonEmpty(head, tail) => false
  }

  def length: Int = {
    @tailrec
    def loop(list: FList[T], acc: Int): Int = list match {
      case FLEmpty          => acc
      case FLNonEmpty(_, t) => loop(t, acc + 1)
    }
    loop(this, 0)
  }

  def filter(predicate: T => Boolean): FList[T] = {
    @tailrec
    def loop(list: FList[T], acc: FList[T]): FList[T] = list match {
      case FLEmpty => acc.reverse
      case FLNonEmpty(h, t) =>
        if (predicate(h)) loop(t, FLNonEmpty(h, acc))
        else loop(t, acc)
    }
    loop(this, FLEmpty)
  }

  def foreach(f: T => Unit): Unit = {
    @tailrec
    def loop(list: FList[T]): Unit = list match {
      case FLEmpty => ()
      case FLNonEmpty(h, t) =>
        f(h)
        loop(t)
    }
    loop(this)
  }

  def reverse: FList[T] = {
    @tailrec
    def loop(list: FList[T], acc: FList[T]): FList[T] = list match {
      case FLEmpty          => acc
      case FLNonEmpty(h, t) => loop(t, FLNonEmpty(h, acc))
    }
    loop(this, FLEmpty)
  }
}

case object FLEmpty extends FList[Nothing] {
  def head: Nothing = throw new NoSuchElementException("Empty list")
  def tail: FList[Nothing] = throw new NoSuchElementException("Empty list")
}

case class FLNonEmpty[+T](head: T, tail: FList[T]) extends FList[T]

object Main {
  def main(args: Array[String]): Unit = {
    val testlist = FLNonEmpty(1, FLNonEmpty(2, FLNonEmpty(3, FLNonEmpty(4, FLEmpty))))
    println(s"head: ${testlist.head}")
    println(s"tail: ${testlist.tail}")
    println(s"isEmpty: ${testlist.isEmpty}")
    println(s"length: ${testlist.length}")
    println(s"even: ${testlist.filter(_ % 2 == 0)}")
    print("foreach: ")
    testlist.foreach(v => print(s"$v "))
  }
}
