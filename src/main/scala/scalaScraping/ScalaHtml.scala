package scalaScraping

import java.io.{File, PrintWriter}

import scala.io.Source
import com.ibm.icu.text.Transliterator

/**
  * Created by kenta-yoshinaga on 2016/10/19.
  */
class ScalaHtml {

  val transliterator = Transliterator.getInstance("Halfwidth-Fullwidth")

  def getURLfromSource(url: String): List[String] = {
    val src  = try {
      Source.fromURL(url, "utf-8").getLines.toList
    } catch {
      case e: Exception => {
        println(e.getMessage)
        throw e.getCause
      }
    }
    var cnt: Int = 0
    var urls = List.empty[String]

    for(line <- src){
      if( line.contains("<div class=\"listArea\">"))  cnt = 1
      if( line.contains("</div><!-- /.listArea -->")) cnt = 0

      if(cnt == 1){
        if(line.contains("pickup")) {
          val ss = line.split('"')
          urls = ss(1) :: urls
        }
      }
    }
    urls
  }

  def getTextfromSource(urls: List[String], dirName: String): Unit = {
    var newFile = new File(dirName)
    if (!newFile.exists())  newFile.mkdir()

    for ( i <- urls){
      println(i)
      var durl = i.split('/')
      var filename:String = durl.last

      val src = try {
        Source.fromURL(i, "utf-8").getLines.toList
      }
      catch{
        case e:Exception => {
          println("get url error %s".format(e.getMessage))
          throw e.getCause
        }
      }
      var cnt: Int = 0
      var tmp:String = null

      for (j <- src){
        if(j.contains("hbody"))                     cnt = 1
        if(j.contains("</div><!--/.headline -->"))  cnt = 0
        if(cnt == 1)
          tmp += j
      }

      var tmp2 = tmp.replaceAll("<br>", "")
      var ss = tmp2.split('>')
      var ss2 = ss(1).replaceAll("</p", "")

      var ss3: String = ss2
      if(ss2.endsWith("p")) ss3 = ss2.init
      var last: String = ss3
      if(ss3.endsWith(("/"))) last = ss3.init

      if(last.trim != null && last.trim != "\n"){
        val out = new PrintWriter( dirName + "/" + filename)
        val outString = transliterator.transliterate(last.replace("　","").stripLineEnd)
        out.write(outString)
        out.close
      }
    }
  }
}

object ScalaHtml{

  def prefix(category: String, page: Int):String =
    "http://news.yahoo.co.jp/list/?c=%s&p=%d".format(category, page)

  def main(args: Array[String]): Unit = {
    val html = new ScalaHtml
    val url = prefix(args(0), args(1).toInt)
    println(url)
    try {
      val src = html.getURLfromSource(url)
      html.getTextfromSource(src, "./data/%s".format(args(0)))
    }
    catch {
      case e:Exception => e.getMessage
    }
  }
}
