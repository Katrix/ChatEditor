package io.github.katrix.chateditor.listener

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.ref.WeakReference

import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.block.InteractBlockEvent
import org.spongepowered.api.event.command.{SendCommandEvent, TabCompleteEvent}
import org.spongepowered.api.event.filter.cause.First
import org.spongepowered.api.event.message.MessageChannelEvent
import org.spongepowered.api.event.{Listener, Order}

import io.github.katrix.chateditor.EditorPlugin
import io.github.katrix.chateditor.editor.Editor
import io.github.katrix.chateditor.editor.command.EditorCommandRegistry
import io.github.katrix.chateditor.editor.component.end.CompEndCommandBlock
import io.github.katrix.chateditor.editor.component.text.{CompTextCursor, CompTextLine}
import io.github.katrix.chateditor.lib.LibPerm
import io.github.katrix.katlib.helper.Implicits._

class EditorHandler(editorCommandRegistry: EditorCommandRegistry)(implicit plugin: EditorPlugin) {

	private val editorPlayers = new mutable.WeakHashMap[Player, Editor]

	/**
		* Adds a new editor player binding
		*
		* @return The old editor if there was one
		*/
	def addEditorPlayer(player: Player, editor: Editor): Option[Editor] = editorPlayers.put(player, editor)

	/**
		* Removes a editor player binding
		*
		* @return The editor used by the player, if any
		*/
	def removeEditorPlayer(player: Player): Option[Editor] = editorPlayers.remove(player)

	@Listener
	def interactCommandBlock(event: InteractBlockEvent.Secondary, @First player: Player): Unit = {
		if(player.get(Keys.IS_SNEAKING).orElse(false) && !event.getCause.contains(BypassEditor)) {
			val blockSnapshot = event.getTargetBlock
			if(blockSnapshot.getState.getType == BlockTypes.COMMAND_BLOCK) {
				val permCmdBlock = blockSnapshot.get(Keys.DISPLAY_NAME).toOption match {
					case Some(name) => s"${LibPerm.CommandBlock}.${name.toPlain}"
					case None => LibPerm.CommandBlock
				}

				if(player.hasPermission(permCmdBlock)) {
					blockSnapshot.getLocation.toOption match {
						case Some(location) => editorPlayers.get(player) match {
							case Some(editor) => editor.end match {
								case componentEnd: CompEndCommandBlock =>
									event.setCancelled(true)

									val newEditor = editor.copy(end = new CompEndCommandBlock(location))
									editorPlayers.put(player, newEditor)
									player.sendMessage(plugin.config.text.commandBlockLocationSet.value(Map(plugin.config.text.Location -> location.getBlockPosition)
										.asJava).build())
								case _ =>
							}
							case None =>
								location.getTileEntity.toOption match {
									case Some(tileEntity) =>
										event.setCancelled(true)

										val commandString = tileEntity.get(Keys.COMMAND).orElse("")
										player.sendMessage(plugin.config.text.commandBlockStart.value(Map(plugin.config.text.Location -> location.getBlockPosition)
											.asJava).build())
										val text = CompTextCursor(0, 0, commandString)
										val end = new CompEndCommandBlock(location)
										editorPlayers.put(player, Editor(text, end, WeakReference(player), this))
									case None => player.sendMessage(plugin.config.text.commandBlockErrorTileEntity.value)
								}
						}
						case None => player.sendMessage(plugin.config.text.commandBlockErrorLocation.value)
					}
				}
			}
		}
	}

	@Listener(order = Order.FIRST)
	def onChat(event: MessageChannelEvent.Chat, @First player: Player): Unit = {
		if(!event.getCause.contains(BypassEditor)) {
			editorPlayers.get(player) match {
				case Some(editor) =>
					event.setCancelled(true)

					val rawText = event.getRawMessage.toPlain
					editorCommandRegistry.getCommand(rawText) match {
						case Some(command) if player.hasPermission(command.permission) =>
							val commandText = if(rawText.startsWith("!")) rawText.substring(1) else rawText
							editorPlayers.put(player, command.execute(commandText, editor, player))
						case Some(command) =>
							player.sendMessage(plugin.config.text.eCommandMissingPerm.value)
						case None =>
							player.sendMessage(plugin.config.text.eCommandNotFound.value)
					}
				case None =>
			}
		}
	}

	@Listener(order = Order.FIRST)
	def onCommand(event: SendCommandEvent, @First player: Player): Unit = {
		if(!event.getCause.contains(BypassEditor) && event.getCommand != "sponge:callback") {
			editorPlayers.get(player) match {
				case Some(editor) =>
					val newText = editor.text.addString(s"/${event.getCommand} ${event.getArguments}")
					val newEditor = editor.copy(text = newText)
					newText.sendPreview(newEditor, player)
					editorPlayers.put(player, newEditor)
					event.setCancelled(true)
				case None =>
			}
		}
	}

	@Listener
	def onTabComplete(event: TabCompleteEvent, @First player: Player): Unit = {
		if(!event.getCause.contains(BypassEditor)) {
			editorPlayers.get(player) match {
				case Some(editor) =>
					val rawMessage = event.getRawMessage
					val suggestions = event.getTabCompletions
					if(rawMessage.startsWith("!")) {
						val withoutExclamation = rawMessage.drop(1)
						val relevantCommands = editorCommandRegistry.registeredCommands.keys.filter(s => s.startsWith(withoutExclamation)).map(str => s"!$str")
						suggestions.addAll(relevantCommands.toSeq.asJava)
					}
					else editor.text match {
						case lineEditor: CompTextLine =>
							if(suggestions.isEmpty) {
								suggestions.add(lineEditor.currentLine)
							}
						case _ =>
					}
				case None =>
			}
		}
	}
}

/**
	* The bypass object. If an event wants to escape the
	* editor processing, it needs to include this in it's caused.
	*/
object BypassEditor