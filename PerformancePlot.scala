package de.fosd.typechef.parser.c

import scala.sys.process._
import java.io.File

/**
 * create an R plot for the performance stored in report repository
 */
object PerformancePlot extends App {

    val all = (args.length > 0 && args(1) == "--all")

    val revisions = Process("git log --format=%h").lines
    val lastRevisions = if (all) revisions else revisions.takeRight(10)
println(lastRevisions)
    var knownCols = List[String]()
   // val lastRevisions = Seq("a")
    var rdata:Map[(String,String),(Double,Double)]=Map()//from col,revision to avg,sdev
    for (revision <- lastRevisions) {
        var success=Process("git checkout %s -- timing.csv".format(revision)).! == 0
        success &&= Process("git checkout %s -- timing.format".format(revision)).! == 0
	success &&= new File("timing.csv").exists()

	if (success) {
        val header = io.Source.fromFile("timing.format").getLines().next()
        val cols = "total" +: header.trim.drop(8).dropRight(1).split(",").map(_.trim)
        for (c<-cols)
            if (!(knownCols contains c))
                knownCols = (knownCols :+ c)

        val table = io.Source.fromFile("timing.csv").getLines()
        var data: List[List[Int]] = Nil
        for (line <- table) {
            val row = line.split(";").map(_.trim).map(x => if (x.isEmpty) 0 else x.toInt)
            if (row.length+1 == cols.length) {
                data = (sum(row) +: row.toList ) :: data
            }
        }

        for (colIdx <- 0 until cols.length) {
            val name = cols(colIdx)
            val a = avg(data.map(_(colIdx)))
            val s = sdev(data.map(_(colIdx)), a)

            rdata = rdata + ((name,revision)->(a,s))

//            println(name + ": " + a + " +/- " + s)

        }
}
    }

    val cols = knownCols

    val max = rdata.values.map(v=>v._1+v._2).max

    def sums(col:String): String = (for (r<-lastRevisions) yield rdata.getOrElse((col,r),(0,0))._1).mkString("c(",",",")")
    def sdevs(col:String): String = (for (r<-lastRevisions) yield rdata.getOrElse((col,r),(0,0))._2).mkString("c(",",",")")


    println("l<-c(%s)".format(cols.map("\""+_+"\"").mkString(",")))
    println("x<-c(%s)".format((1 to lastRevisions.size).mkString(",")))
    println("pchs<-c(%s)".format((1 to cols.size).mkString(",")))
    println("legend<-c(%s)".format(cols.map("\""+_+"\"").mkString(",")))
    println("plot(x=x,y=%s,ylim=c(0,%f),pch=pchs[%d],labels=%s,at=%s)".format(sums("total"),max,cols.indexOf("total")+1,lastRevisions.mkString("c(\"","\",\"","\")"),(1 to lastRevisions.size).mkString("c(",",",")")))
    for (col<-cols) {
        println("lines(x=x,y=%s)#%s".format(sums(col),col))
        println("points(x=x,y=%s,pch=pchs[%d])".format(sums(col),cols.indexOf(col)+1))
        println("avg<-%s;sdev<-%s;arrows(x, avg-sdev, x, avg+sdev, length=0.05, angle=90, code=3)".format(sums(col),sdevs(col)))
    }
    println("legend(\"right\",l,pch=pchs)")


//    l<-c("a","b")
//    r<-c("x","y")
//    sdev<-c(1,2)
//    avg<-c(5,6)
//    x<-c(1,2)
//
//    plot(x=x,y=c(5,6),ylim=c(0,6))
//    lines(x=x,y=c(1,2),pch=5)



    def sum(data: Seq[Int]): Int =
        if (data.isEmpty) 0
        else
            data.reduce(_ + _)

    def avg(data: List[Int]): Double = sum(data) / data.length.toDouble

    def sdev(xs: List[Int], avg: Double): Double = xs match {
        case Nil => 0.0
        case ys => math.sqrt((0.0 /: ys) {
            (a, e) => a + math.pow(e - avg, 2.0)
        } / xs.size)
    }

}
