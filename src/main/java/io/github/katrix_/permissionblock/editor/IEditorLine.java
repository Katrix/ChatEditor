/**
 * This file is part of PermissionBlock, licensed under the MIT License (MIT).
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
package io.github.katrix_.permissionblock.editor;

/**
 * An editor that that have a concept of lines.
 */
public interface IEditorLine extends IEditor {

	/**
	 * Get the location of the cursor.
	 *
	 * @return The cursors position.
	 */
	int getLine();

	/**
	 * Sets the line being worked on.
	 *
	 * @param location The new line position.
	 * @return The new location after modification. This might be different from the value in the
	 * input. Same as calling {@link IEditorLine#getLine()}.
	 */
	int setLinePos(int location);

	/**
	 * Moves the cursor line being worked on forward.
	 *
	 * @param add The amount to move forward.
	 * @return The new location after modification. This might be different from the value in the
	 * input. Same as calling {@link IEditorLine#getLine()}.
	 */
	default int addLinePos(int add) {
		setLinePos(getLine() + add);
		return getLine();
	}

	/**
	 * Moves the line being worked on backwards.
	 *
	 * @param subtract The amount to move backwards.
	 * @return The new location after modification. This might be different from the value in the
	 * input. Same as calling {@link IEditorLine#getLine()}.
	 */
	default int subtractLinePos(int subtract) {
		setLinePos(getLine() - subtract);
		return getLine();
	}

	/**
	 * Adds a new line to the editor at the currently selected line.
	 *
	 * @return If success.
	 */
	boolean addLine();

	/**
	 * Remove the currently selected line from the editor.
	 *
	 * @return If Success.
	 */
	boolean removeLine();

	/**
	 * @return The currently selected lines content.
	 */
	String getCurrentLineContent();
}
