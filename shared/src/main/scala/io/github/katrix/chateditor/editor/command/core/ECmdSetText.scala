/*
 * This file is part of ChatEditor, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016 Katrix
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.katrix.chateditor.editor.command.core

import java.nio.file.Paths

import scala.util.{Failure, Success, Try}

import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.chateditor.editor.Editor
import io.github.katrix.chateditor.editor.command.EditorCommand
import io.github.katrix.chateditor.editor.component.end.CompEndSave
import io.github.katrix.chateditor.editor.component.text.{CompTextCursor, CompTextFile, CompTextLine}
import io.github.katrix.chateditor.lib.LibPerm
import io.github.katrix.katlib.helper.Implicits._

object ECmdSetText extends EditorCommand {

	override def execute(raw: String, editor: Editor, player: Player): Editor = {
		val args = raw.split(' ')
		if(args.length >= 2) {
			val behavior = args(1)
			behavior match {
				case "cursor" =>
					player.sendMessage(t"${GREEN}Set text behavior to cursor")
					editor.copy(text = CompTextCursor(0, 0, editor.text.builtString))(editor.listener)
				case "line" =>
					player.sendMessage(t"${GREEN}Set text behavior to line")
					editor.copy(text = CompTextLine(0, 0, editor.text.builtString.split(' ')))(editor.listener)
				case "file" if player.hasPermission(LibPerm.UnsafeFile) =>
					if(args.length > 2) {
						Try(Paths.get(args(2))) match {
							case Success(path) =>
								val newText = new CompTextFile(0, 0, editor.text.builtString.split('\n'), path)
								player.sendMessage(t"${GREEN}Set text behavior to file, set end behavior to save")
								editor.copy(text = newText, end = CompEndSave)(editor.listener)
							case Failure(e) =>
								player.sendMessage(t"Invalid path: ${e.getMessage}")
								editor
						}
					}
					else {
						player.sendMessage(t"${RED}Please specify a file path")
						editor
					}
				case "file" =>
					player.sendMessage(t"${RED}You don't have permission to use that text behavior")
					editor
				case _ =>
					player.sendMessage(t"${RED}Unrecognized text behavior")
					editor
			}
		}
		else {
			player.sendMessage(t"${RED}You need to specify a new text behavior")
			editor
		}
	}

	override def aliases: Seq[String] = Seq("setText", "changeText")
	override def help: Text = ???
	override def permission: String = LibPerm.Editor
}