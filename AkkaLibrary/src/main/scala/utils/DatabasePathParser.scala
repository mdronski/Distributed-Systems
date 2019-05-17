package utils

object DatabasePathParser {
  def getBookPath(db: String, title: String): String ={
    db+"/"+title+".txt"
  }

  def getTitlesBasePath(db: String): String = {
    db+"/titles.txt"
  }

  def getOrdersPath(): String = {
    "/home/mdronski/IET/semestr_6/Distributed-Systems/AkkaLibrary/src/main/resources/database/orders.txt"
  }
}
