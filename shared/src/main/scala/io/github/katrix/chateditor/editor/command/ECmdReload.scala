package io.github.katrix.chateditor.editor.command

import java.nio.file.Path

import scala.util.{Failure, Success}

import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.chateditor.editor.Editor
import io.github.katrix.chateditor.editor.component.text.FileEditorHelper
import io.github.katrix.chateditor.lib.LibPerm
import io.github.katrix.katlib.helper.Implicits._

object ECmdReload extends EditorCommand {

	override def execute(raw: String, editor: Editor, player: Player): Editor = editor.text.data("path") match {
		case Some(path: Path) => FileEditorHelper.load(path) match {
			case Success(newText) =>
				player.sendMessage(t"${GREEN}Reloaded file successfully")
				editor.copy(text = newText)
			case Failure(e) =>
				player.sendMessage(t"$RED${e.getMessage}")
				editor
		}
		case _ =>
			player.sendMessage(t"${RED}You don't have a file open")
			editor
	}

	override def aliases: Seq[String] = Seq("saveFile")
	override def help: Text = ???
	override def permission: String = LibPerm.UnsafeFile
}