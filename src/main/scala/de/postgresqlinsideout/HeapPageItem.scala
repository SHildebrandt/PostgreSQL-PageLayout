package de.postgresqlinsideout

/**
 * Representation of a heap page item in PostgreSQL
 * (as described in the PostgreSQL source code)
 *
 * @param lp
 * @param lpOff
 * @param lpFlags
 * @param lpLen
 * @param tXmin insert XID stamp
 * @param tXmax delete XID stamp
 * @param tField3
 * @param tCtid current TID of this or newer tuple
 * @param tInfomask2 number of attributes + various flags
 * @param tInfomask various flag bits
 * @param tHoff offset to user data
 * @param tBits
 * @param tOid
 *
 * @author Steffen Hildebrandt
 */
case class HeapPageItem(lp: Int, lpOff: Int, lpFlags: Int, lpLen: Int, tXmin: Int, tXmax: Int, tField3: Int,
                        tCtid: (Int, Int), tInfomask2: Int, tInfomask: Int, tHoff: Int, tBits: Int, tOid: Int)
