/**
 * Copyright 2013, 2014  by Patrick Nicolas - Scala for Machine Learning - All rights reserved
 *
 * The source code in this file is provided by the author for the only purpose of illustrating the 
 * concepts and algorithms presented in Scala for Machine Learning.
 */
package org.scalaml.workflow.data

import scala.util.{Try, Success, Failure}
import DocumentsSource._
import org.scalaml.util.Display
import org.apache.log4j.Logger



		/**
		 * <p>Class that define the extraction of a document from a corpus
		 * or a list of text file using the following format.
		 * <ul>
		 * <li>date</li>
		 * <li>Title</li>
		 * <li>Content</li>
		 * </ul>
		 * @param pathName relative path for the directory containing the corpus
		 * @throw IllegalArgumentException if the path is not defined
		 * 
		 * @author Patrick Nicolas
		 * @since January 15, 2014
		 * @note Scala for Machine Learning
		 */
class DocumentsSource(val pathName: String) {
   require(pathName != null && pathName.length > 1, "Cannot create a data source with undefined path")
	   
   private val logger = Logger.getLogger("TextSource")
   private val filesList: Array[String] = {   
  	  val file = new java.io.File(pathName)
  	  if( file.isDirectory) file.listFiles.map( x => x.getName) else Array[String](pathName)
   }
    
   def |> : Option[Corpus] = {
  	  import scala.io.Source
  	  import java.io.{FileNotFoundException, IOException}
  	  
  	  Try(filesList.map( fName => {
	  	  val src = Source.fromFile(pathName + fName)	
	  	  val fieldIter = src.getLines
	  	  	  
	  	  val date = nextField(fieldIter)
	  	  val title = nextField(fieldIter)
	  	  val content = fieldIter.foldLeft(new StringBuilder)((b, str) => b.append(str.trim))
	  	  	 
	  	  src.close
	  	  if(date == None || title == None)
	  	  	throw new IllegalStateException("DocumentsSource.|> title or date for " + fName + " is malformatted")
	  	  (date.get, title.get, content.toString)
	  	  
  	  }) ) match {
  	  	 case Success(res) => Some(res)
  	  	 case Failure(e) => Display.error("TextSource", logger, e); None
  	  }
   }
   

   private def nextField(iter: Iterator[String]): Option[String] = 
  	 iter.find( s=> (s != null && s.length > 1))
}



object DocumentsSource {
	type Corpus = Array[(String, String, String)]
	def apply(pathName: String): DocumentsSource = new DocumentsSource(pathName)
}


object DocumentsSourceApp extends App {
  def iterate(iter: Iterator[String]): Option[String] = {
  	 iter.find( s=> (s != null && s .length > 1))
  	 /*
  	 var line: String = null
  	 while( iter.hasNext && (line == null || line.length == 0) )
  		line = iter.next.trim
     line
     * 
     */
  }
	
  import scala.io.Source
  final val pathName = "resources/text/chap5/tesla8.txt"
  	
  val src = Source.fromFile(pathName)	
	  	  val fieldIter = src.getLines
	  	  	  
 val date = iterate(fieldIter)
   val title = iterate(fieldIter)
val content = fieldIter.foldLeft(new StringBuilder)((b, str) => b.append(str.trim))
  if( date != None && title != None && content != None)
  	  println(date.get.toString)
  	  println(title.toString)

	  	  	 
}

// ---------------------------------  EOF --------------------------------------------------