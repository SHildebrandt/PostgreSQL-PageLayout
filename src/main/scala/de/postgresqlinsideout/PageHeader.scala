package de.postgresqlinsideout

/**
 * Representation of a page header in PostgreSQL
 * (as described in the PostgreSQL source code)
 *
 * @param lsn next byte after last byte of xlog record for last change to this page
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
class PageHeader(val lsn: Field[String], val checksum: Field[Int], val flags: Field[Int], val lower: Field[Int],
                 val upper: Field[Int], val special: Field[Int], val pagesize: Field[Int], val version: Field[Int],
                 val pruneXid: Field[Int]) {

  override def toString() =
    s"PageHeader($lsn, $checksum, $flags, $lower, $upper, $special, $pagesize, $version, $pruneXid)"
}

object PageHeader {

  def apply(lsn: String, checksum: Int, flags: Int, lower: Int, upper: Int, special: Int,
            pagesize: Int, version: Int, pruneXid: Int) =
    new PageHeader(
      new Field("lsn", lsn, 8),
      new Field("checksum", checksum, 2),
      new Field("flags", flags, 2),
      new Field("lower", lower, 2),
      new Field("upper", upper, 2),
      new Field("special", special, 2),
      new Field("pagesize", pagesize, 1),
      new Field("version", version, 1),
      new Field("pruneXid", pruneXid, 4))

}
