package de.postgresqlinsideout.pagelayout


object CommandLineInterface {

  case class Config(db: String, table: String, pageNo: Int)

  val parser = new scopt.OptionParser[Config]("PageLayout") {

  }

}
