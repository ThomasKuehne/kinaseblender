/*
 * Copyright (c) 2010, 2011, 2012 Thomas Kuehne <thomas@kuehne.cn>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package cn.kuehne.kinaseblender.gui;

import javax.swing.table.AbstractTableModel;

/**
 * common table base class 
 */
public abstract class AbstractBasicTableModel extends AbstractTableModel
		implements ExportableTableModel {
	private static final long serialVersionUID = 1L;

	private final String title;

	public AbstractBasicTableModel(final String title) {
		super();
		if (title == null || title.length() < 1) {
			throw new IllegalArgumentException("title is null or empty");
		}
		this.title = title;
	}

	public final int getColumnCount() {
		final Object[][] data = getData();
		return (data == null) ? 0 : data.length;
	}

	@Override
	public final String getColumnName(final int column) {
		return " ";
	}

	protected abstract Object[][] getData();

	public final int getRowCount() {
		final Object[][] data = getData();
		return (data == null || data.length < 1) ? 0 : data[0].length;
	}

	public final Object getValueAt(final int rowIndex, final int columnIndex) {
		return getData()[columnIndex][rowIndex];
	}

	public boolean ignoreColumnForExport(final int col) {
		return false;
	}

	public boolean isEmphasized(final int row, final int col) {
		return 0 != (row % 2);
	}

	public boolean isExportable() {
		return true;
	}

	@Override
	public String toString() {
		return title;
	}
}
