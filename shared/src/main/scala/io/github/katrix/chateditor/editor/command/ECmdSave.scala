package io.github.katrix.chateditor.editor.command

import java.nio.file.Path

import scala.util.{Failure, Success}

import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.chateditor.EditorPlugin
import io.github.katrix.chateditor.editor.Editor
import io.github.katrix.chateditor.editor.component.text.FileEditorHelper
import io.github.katrix.chateditor.lib.LibPerm
import io.github.katrix.katlib.helper.Implicits._

class ECmdSave(implicit plugin: EditorPlugin) extends EditorCommand {

  override def execute(raw: String, editor: Editor, player: Player): Editor =
    if (player.hasPermission(LibPerm.UnsafeFile)) {
      editor.text.data("path") match {
        case Some(path: Path) =>
          FileEditorHelper.save(path, editor) match {
            case Success(_) =>
              player.sendMessage(plugin.config.text.fileSaved.value)
              editor
            case Failure(e) =>
              player.sendMessage(t"$RED${e.getMessage}")
              editor
          }
        case _ =>
          player.sendMessage(plugin.config.text.fileNotOpen.value)
          editor
      }
    } else {
      player.sendMessage(plugin.config.text.fileMissingPerm.value)
      editor.copy(text = editor.text.dataRemove("path"))
    }

  override def aliases:    Seq[String] = Seq("saveFile")
  override def help:       Text        = t"If you have a file loaded, saves the file to disk"
  override def permission: String      = LibPerm.UnsafeFile
}
