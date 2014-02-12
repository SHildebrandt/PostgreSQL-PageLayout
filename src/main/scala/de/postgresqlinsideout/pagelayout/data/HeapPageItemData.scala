/**
 * Copyright (c) 2013, Steffen Hildebrandt
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.postgresqlinsideout.pagelayout.data

import scala.slick.session.Database


/**
 * Representation of a heap page item in PostgreSQL
 * (as described in the PostgreSQL source code)
 *
 * @param lp
 * @param lpOff offset to tuple (from start of page)
 * @param lpFlags state of item pointer
 * @param lpLen byte length of tuple
 * @param tXmin inserting xact ID
 * @param tXmax deleting or locking xact ID
 * @param tField3 one of two fields (depending on the state of the item)
 *                - t_cid   inserting or deleting command ID, or both
 *                - t_xvac  old-style VACUUM FULL xact ID
 * @param tCtid current TID of this or newer tuple
 * @param tInfomask2 number of attributes + various flags
 * @param tInfomask various flag bits
 * @param tHoff size of header incl. bitmap, oid, padding
 * @param tBits bitmap of NULLs -- variable length
 * @param tOid object id
 *
 * @author Steffen Hildebrandt
 */
class HeapPageItemData(val lp: Field[Int], val lpOff: Field[Int], val lpFlags: Field[Int], val lpLen: Field[Int],
                       val tXmin: Field[Int], val tXmax: Field[Int], val tField3: Field[Int], val tCtid: Field[Option[(Int, Int)]],
                       val tInfomask2: Field[String], val tInfomask: Field[String], val tHoff: Field[Int], val tBits: Field[String],
                       val tOid: Field[Int])
        extends FieldList {

  /**
   * This is a var, not a val.
   * The reason is that the object should be creatable within a GetResult, which I can't pass any further parameters to.
   * So this variable has to be set after the actual instantiation of the object.
   * Another possibility would have been to create a wrapper class with this table field, but that's probably overkill.
   */
  var fromDB: Option[Database] = None
  var fromTable: Option[String] = None

  lazy val lpList = List(lp, lpOff, lpFlags, lpLen)
  lazy val headerDataList = List(tXmin, tXmax, tField3, tCtid, tInfomask2, tInfomask, tHoff, tBits, tOid)

  override def toList() = lpList ++ headerDataList

  override def toString() = "HeapPageItem" + itemString

  val headerSize = tHoff.value

  val firstByte = lpOff.value // first Byte of this item (== first Byte of ItemHeader)
  val lastByte = firstByte + lpLen.value - 1 // last Byte of this item
  val itemIdDataStart = Page.ITEM_ID_DATA_START + (lp.value - 1) * 4
  val itemIdDataEnd = itemIdDataStart + 3
  val itemHeaderStart = lpOff.value
  val itemHeaderEnd = firstByte + headerSize - 1
  val itemDataStart = firstByte + headerSize
  val itemDataEnd = lastByte
}

object HeapPageItemData {

  def apply(lp: Int, lpOff: Int, lpFlags: Int, lpLen: Int, tXmin: Int, tXmax: Int, tField3: Int, tCtid: Option[(Int, Int)],
            tInfomask2: Int, tInfomask: Int, tHoff: Int, tBits: String, tOid: Int) =
    new HeapPageItemData(
      new Field("lp", lp, 0, "Implicit sequence number of this item pointer (is not stored in the page but created on-the-fly by the getHeapPageItems-function)"),
      new Field("lpOff", lpOff, 2, "Byte offset to tuple (from start of page)"), // 15 bit
      new Field("lpFlags", lpFlags, 0, "State of the item pointer (1 or 2 = alive)"), //  2 bit
      new Field("lpLen", lpLen, 2, "Byte length of tuple"), // 15 bit
      new Field("tXmin", tXmin, 4, "Inserting xact ID"),
      new Field("tXmax", tXmax, 4, "Deleting or locking xact ID"),
      new Field("tField3", tField3, 4, "During insert/delete operations: Command IDs cmin/cmax,  Otherwise: old-style VACUUM FULL xact ID"),
      new Field("tCtid", tCtid, 6, "Current TID of this or newer tuple") {
        override def valueToString = value.getOrElse("").toString
        override def toString = s"$name=${value.getOrElse("")}"
      },
      new Field("tInfomask2", bitString(tInfomask2), 2, "Various flag bits"),
      new Field("tInfomask", bitString(tInfomask), 2, "Various flag bits"),
      new Field("tHoff", tHoff, 1, "Size of header incl. bitmap, oid, padding"),
      new Field("tBits", tBits, -1, "Bitmap of NULLs -- variable length"), // undefined length (depending on number of fields, padding, etc)
      new Field("tOid", tOid, 4, "Object ID"))

  def bitString(n: Int) = {
    (0 to 15) map (i => if (((1 << i) & n) > 0) "1" else "0") mkString ""
  }
}

