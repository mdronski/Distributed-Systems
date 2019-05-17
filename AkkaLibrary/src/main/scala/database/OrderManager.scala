package database

import java.io.{File, FileOutputStream, FileWriter, PrintWriter}
import java.util.concurrent.locks.Lock

import utils.DatabasePathParser

object OrderManager {

  def makeOrder(book: Book):Unit = this.synchronized {
    val path = DatabasePathParser.getOrdersPath()
    val writer = new PrintWriter(new FileOutputStream(new File(path),true))
    writer.write(book.title+"\n")
    writer.flush()
    println(s"Made order for ${book.title}")
  }
}
