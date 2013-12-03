package de.postgresqlinsideout.pagelayout

import scopt.OptionParser


object CommandLineInterface {

  case class Config(db: String, table: String, pageNo: Int)

  val parser = new OptionParser[Config]("PageLayout") {

  }

}
