/*
 * Copyright 2001-2015 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalactic

trait LazySeq[+T] extends LazyBag[T] {
  def map[U](f: T => U): LazySeq[U]
  def flatMap[U](f: T => LazyBag[U]): LazySeq[U]
  def toEquaSet[U >: T](toPath: EquaPath[U]): toPath.EquaSet
  def toSortedEquaSet[U >: T](toPath: SortedEquaPath[U]): toPath.SortedEquaSet
  def toList: List[T]
  def size: Int
}

object LazySeq {
  private class BasicLazySeq[T](private val args: List[T]) extends LazySeq[T] { thisLazySeq =>
    def map[U](f: T => U): BasicLazySeq[U] = new BasicLazySeq[U](args.map(f)) // TODO: Bug, should be lazy
    def flatMap[U](f: T => LazyBag[U]): LazySeq[U] = new FlatMappedLazySeq(thisLazySeq, f)
    def toEquaSet[U >: T](toPath: EquaPath[U]): toPath.FastEquaSet = toPath.FastEquaSet(args: _*)
    def toSortedEquaSet[U >: T](toPath: SortedEquaPath[U]): toPath.SortedEquaSet = toPath.TreeEquaSet(args: _*)
    def toList: List[T] = args
    def size: Int = args.size
    override def toString = args.mkString("LazySeq(", ",", ")")
  }

  private class MappedLazySeq[T, U](lazySeq: LazySeq[T], f: T => U) extends LazySeq[U] { thisLazySeq => 
    def map[V](g: U => V): LazySeq[V] = new MappedLazySeq[T, V](lazySeq, f andThen g)
    def flatMap[V](f: U => LazyBag[V]): LazySeq[V] = ???
    def toEquaSet[V >: U](toPath: EquaPath[V]): toPath.FastEquaSet = {
      toPath.FastEquaSet(toList: _*)
    }
    def toSortedEquaSet[V >: U](toPath: SortedEquaPath[V]): toPath.SortedEquaSet = ???
    def toList: List[U] = lazySeq.toList.map(f)
    def size: Int = toList.size
    override def toString: String = toList.mkString("LazySeq(", ",", ")")
  }

  private class FlatMappedLazySeq[T, U](lazySeq: LazySeq[T], f: T => LazyBag[U]) extends LazySeq[U] { thisLazySeq => 
    def map[V](g: U => V): LazySeq[V] = new MappedLazySeq[U, V](thisLazySeq, g)
    def flatMap[V](f: U => LazyBag[V]): LazySeq[V] = ???
    def toEquaSet[V >: U](toPath: EquaPath[V]): toPath.FastEquaSet = {
      toPath.FastEquaSet(toList: _*)
    }
    def toSortedEquaSet[V >: U](toPath: SortedEquaPath[V]): toPath.SortedEquaSet = ???
    def toList: List[U] = lazySeq.toList.flatMap(f.andThen(_.toList))
    def size: Int = toList.size
    override def toString: String = toList.mkString("LazySeq(", ",", ")")
  }
  
  def apply[T](args: T*): LazySeq[T] = new BasicLazySeq(args.toList)
}

