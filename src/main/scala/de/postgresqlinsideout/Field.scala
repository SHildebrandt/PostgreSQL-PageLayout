package de.postgresqlinsideout

/**
 *
 *
 * @author Steffen Hildebrandt
 * @version 14.1
 */
class Field[T](val name: String, val value: T, val size: Int)  {

  override def toString = s"$name=$value"

}
