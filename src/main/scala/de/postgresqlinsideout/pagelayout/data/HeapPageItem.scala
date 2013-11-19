package de.postgresqlinsideout.pagelayout.data

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
 * @param tField3 two fields:
 *                - t_cid   inserting or deleting command ID, or both
 *                - t_xvac  old-style VACUUM FULL xact ID
 * @param tCtid current TID of this or newer tuple
 * @param tInfomask2 number of attributes + various flags
 * @param tInfomask various flag bits
 * @param tHoff offset to user data
 * @param tBits
 * @param tOid
 *
 * @author Steffen Hildebrandt
 */
class HeapPageItem(val lp: Field[Int], val lpOff: Field[Int], val lpFlags: Field[Int], val lpLen: Field[Int],
                   val tXmin: Field[Int], val tXmax: Field[Int], val tField3: Field[Int], val tCtid: Field[(Int, Int)],
                   val tInfomask2: Field[Int], val tInfomask: Field[Int], val tHoff: Field[Int], val tBits: Field[Int],
                   val tOid: Field[Int])
  extends FieldList {

  def toList() = List(lp, lpOff, lpFlags, lpLen, tXmin, tXmax, tField3,
                      tCtid, tInfomask2, tInfomask, tHoff, tBits, tOid)

  override def toString() = "HeapPageItem" + itemString
}

object HeapPageItem {

  def apply(lp: Int, lpOff: Int, lpFlags: Int, lpLen: Int, tXmin: Int, tXmax: Int, tField3: Int, tCtid: (Int, Int),
             tInfomask2: Int, tInfomask: Int, tHoff: Int, tBits: Int, tOid: Int) =
    new HeapPageItem(
      new Field("lp", lp, 0),  // size ??
      new Field("lpOff", lpOff, 2),     // 15 bit
      new Field("lpFlags", lpFlags, 0), //  2 bit
      new Field("lpLen", lpLen, 2),     // 15 bit
      new Field("tXmin", tXmin, 4),
      new Field("tXmax", tXmax, 4),
      new Field("tField3", tField3, 4),
      new Field("tCtid", tCtid, 6),
      new Field("tInfomask2", tInfomask2, 2),
      new Field("tInfomask", tInfomask, 2),
      new Field("tHoff", tHoff, 1),
      new Field("tBits", tBits, -1), // undefined length(?!!)
      new Field("tOid", tOid, 0)) // size ????
}

