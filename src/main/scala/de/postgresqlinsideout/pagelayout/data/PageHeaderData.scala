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

/**
 * Representation of a page header in PostgreSQL
 * (as described in the PostgreSQL source code)
 *
 * @param lsn Xlog record for last change to this page
 * @param checksum page checksum, if set
 * @param flags flag bits
 * @param lower offset to start of free space
 * @param upper offset to end of free space
 * @param special offset to start of special space
 * @param pagesize size in bytes
 * @param version page layout version number
 * @param pruneXid oldest XID among potentially prunable tuples on page
 *
 * @author Steffen Hildebrandt
 */
class PageHeaderData(val lsn: Field[String], val checksum: Field[Int], val flags: Field[Int], val lower: Field[Int],
                 val upper: Field[Int], val special: Field[Int], val pagesize: Field[Int], val version: Field[Int],
                 val pruneXid: Field[Int]) extends FieldList {

  override def toString() = "PageHeader" + itemString

  override def toList() = List(lsn, checksum, flags, lower, upper, special, pagesize, version, pruneXid)
}

object PageHeaderData {

  def apply(lsn: String, checksum: Int, flags: Int, lower: Int, upper: Int, special: Int,
            pagesize: Int, version: Int, pruneXid: Int) =
    new PageHeaderData(
      new Field("lsn", lsn, 8, "Xlog record for last change to this page"),
      new Field("checksum", checksum, 2, "Page checksum, if set"),
      new Field("flags", flags, 2, "Flag bits"),
      new Field("lower", lower, 2, "Offset to start of free space"),
      new Field("upper", upper, 2, "Offset to end of free space"),
      new Field("special", special, 2, "Offset to start of special space"),
      new Field("pagesize", pagesize, 1, "Page size in bytes (will always be the same within a database)"),
      new Field("version", version, 1, "Page layout version number"),
      new Field("pruneXid", pruneXid, 4, "Oldest XID among potentially prunable tuples on page"))
}
