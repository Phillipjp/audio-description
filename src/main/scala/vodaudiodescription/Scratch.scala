package vodaudiodescription

import scala.xml.{Elem, NodeSeq, XML}
import sys.process._

case class TimeCode(hours: Int, minutes: Int, seconds: Int, milliseconds: Int) {
  def toSecs: Double = {
    hours * 60 * 60 + minutes * 60 + seconds + milliseconds.toDouble / 1000.0
  }
}

object Scratch extends App {

  val originalADFileName = "src/main/resources/1-0694-10425-001.wav"
  val trimmedADFileName = "src/main/resources/1-0694-10425-001-trimmed.wav"
  val leftOnlyTrimmedADFileName = "src/main/resources/1-0694-10425-001-trimmed-left-only.wav"
  val broadcastVideoFileName = "src/main/resources/5203ae73-fad2-48a6-8fd2-a6485c901736_CTV01800_16X9.mp4"
  val broadcastAudioFileName = "src/main/resources/broadcast-audio.aac"
  val broadcastAudioDippedFileName = "src/main/resources/broadcast-audio-dipped.aac"

  val xml: Elem = XML.loadFile("/Users/philperk/Dev/my-stuff/vodudiodescription/src/main/resources/1-0694-10425-001.xml")

  val descriptions: NodeSeq = (xml \\ "Description")

  val times: Seq[(TimeCode, TimeCode)] = descriptions.map(desc => (string2TimeCode((desc \ "@AudioStart").text), string2TimeCode((desc \ "@AudioEnd").text)))
    .tail

  val dipDown = 0.2
  val dipPeriod = 1.5

  val volume = times.map { case (start, end) =>
    s"volume='max(1-(t-${start.toSecs - dipPeriod})*(1-$dipDown)/$dipPeriod,$dipDown)':eval=frame:enable='between(t,${start.toSecs - dipPeriod},${end.toSecs})',volume='max($dipDown+(t-${end.toSecs})*(1-$dipDown)/$dipPeriod,$dipDown)':eval=frame:enable='between(t,${end.toSecs},${end.toSecs + dipPeriod})'"
  }

  val trimADCommand = s"ffmpeg -i $originalADFileName -af atrim=30 $trimmedADFileName"
  val leftOnlyTrimmedADCommand = s"ffmpeg -i $trimmedADFileName -af \"pan=mono|c0=c0\" $leftOnlyTrimmedADFileName"

  val broadcastAudioCommand = s"ffmpeg -i $broadcastVideoFileName -vn -acodec copy $broadcastAudioFileName"

  val volumeCommand = s"ffmpeg -i $broadcastAudioFileName -filter_complex \"${volume.mkString(",")}\" $broadcastAudioDippedFileName"
  val mixCommand = s"ffmpeg -i $broadcastAudioDippedFileName -i $leftOnlyTrimmedADFileName -filter_complex amix=inputs=2:duration=first:dropout_transition=3 src/main/resources/testmix.aac"


  println(trimADCommand)
  s"$trimADCommand -y".!

  println(leftOnlyTrimmedADCommand)
  s"$leftOnlyTrimmedADCommand -y".!

  println(broadcastAudioCommand)
  s"$broadcastAudioCommand -y".!

  println(volumeCommand)
  s"$volumeCommand -y".!

  println(mixCommand)
  s"$mixCommand -y".!

  def string2TimeCode(str: String): TimeCode = {
    val ints = str.split(":").map(_.toInt)
    TimeCode(ints(0) - 10, ints(1), ints(2), 40 * ints(3))
  }

}
