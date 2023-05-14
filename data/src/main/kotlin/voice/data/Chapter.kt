package voice.data

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import voice.logging.core.Logger
import java.time.Instant

@Entity(tableName = "chapters2")
data class Chapter(
  @PrimaryKey
  val id: ChapterId,
  val name: String?,
  val duration: Long,
  val fileLastModified: Instant,
  val markData: List<MarkData>,
) : Comparable<Chapter> {

  @Ignore
  val chapterMarks: List<ChapterMark> = parseMarkData()

  override fun compareTo(other: Chapter): Int {
    return id.compareTo(other.id)
  }
}

private fun Chapter.parseMarkData(): List<ChapterMark> {
  return if (markData.isEmpty()) {
    listOf(ChapterMark(name, 0L, duration - 1))
  } else {
    try {
      val cleanedMarks = markData.associate { it.startMs to it.name }
        .toMutableMap()
      val keys = cleanedMarks.keys
      keys.filter { it - 1 in keys }
        .forEach { cleanedMarks.remove(it) }

      val sorted = cleanedMarks.toSortedMap().map { MarkData(it.key, it.value) }

      val result = mutableListOf<ChapterMark>()
      for ((index, mark) in sorted.withIndex()) {
        val name = mark.name
        val previous = result.lastOrNull()
        val next = sorted.getOrNull(index + 1)

        val endMs = if (next != null && next.startMs <= duration - 2) {
          next.startMs - 1
        } else {
          duration - 1
        }

        if (previous == null) {
          result += ChapterMark(
            name = name,
            startMs = 0L,
            endMs = endMs,
          )
        } else {
          val startMs = previous.endMs + 1
          if (startMs < duration && startMs < endMs) {
            result += ChapterMark(
              name = name,
              startMs = startMs,
              endMs = endMs,
            )
          }
        }
      }
      result
    } catch (e: Exception) {
      Logger.e(e, "Could not parse marks from $this")
      listOf(ChapterMark(name, 0L, duration - 1))
    }
  }
}

object ChapterIdSerializer : KSerializer<ChapterId> {

  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("chapterId", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): ChapterId = ChapterId(decoder.decodeString())

  override fun serialize(encoder: Encoder, value: ChapterId) {
    encoder.encodeString(value.value)
  }
}

fun ChapterId.toUri(): Uri = value.toUri()
