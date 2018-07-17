package loc

import scala.util.Try
import scala.io.Source
import java.io.File

object Loc {
  val excludeRe = Set(
    "target".r,
    "build".r,
    """\..+""".r,
    "node_modules".r
  )

  val remapRe = Map(
    """.*build.properties\..+""".r -> "build.properties"
  )

  val extRe = """(?:.*)\.(.+)""".r

  def getExt(file: File): String = remap(file.getName) match {
    case extRe(ext) =>
      ext.toLowerCase
    case _ =>
      "unknown"
  }

  def remap(file: String) = {
    val result = remapRe.foldLeft(file) { (b, a) =>
      val (k, v) = a
      b match {
        case k() => v
        case _ => b
      }
    }
    result
  }

  def exclude(file: File) = excludeRe.exists { re =>
    file.getName() match {
      case re() => true
      case _ => false
    }
  }

  case class Info(count: Int,
    totalSize: Long,
    totalLength: Long,
    isBinary: Boolean)

  def compute(file: File, acc: Map[String, Info]): Map[String, Info] = {
    if (exclude(file))
      acc
    else {
      if (file.isDirectory())
        compute(file.listFiles((file: File) => !exclude(file)), acc)
      else if (file.isFile()) {
        val size = file.length()
        val ext = getExt(file)

        val (lineCount, isBinary) = Try {
          val source = Source.fromFile(file.getPath())
          (source.getLines().length.toLong, false)
        }.getOrElse((0L, true))

        acc + (ext -> (acc.get(ext).fold(Info(1, size, lineCount, false)) { info =>
          Info(info.count + 1,
            info.totalSize + size,
            info.totalLength + lineCount,
            isBinary)
        }))
      } else {
        println(s"unknown file $file")
        acc
      }
    }
  }

  def compute(dirs: Seq[File], acc: Map[String, Info] = Map.empty): Map[String, Info] =
    dirs.foldLeft(acc) { (acc, dir) => compute(dir, acc) }

  def main(args: Array[String]): Unit = {
    val argsFile = (if (args.length > 0) args else Array(".")).map { arg => new File(arg) }
    val result = compute(argsFile)

    result foreach println

    val all = result.filter(!_._2.isBinary).foldLeft(Info(0, 0L, 0L, false)) { (r, p) =>
      Info(r.count + p._2.count, r.totalSize + p._2.totalSize, r.totalLength + p._2.totalLength, false)
    }
    println(all)
  }
}
