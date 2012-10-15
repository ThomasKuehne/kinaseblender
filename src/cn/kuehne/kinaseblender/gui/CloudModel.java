/*
 * Copyright (c) 2010, 2011, 2012 Thomas Kühne <thomas@xn--khne-0ra.name>
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

import cn.kuehne.kinaseblender.engine2.CompiledCloud;

public class CloudModel extends AbstractTableModel implements
		ExportableTableModel {
	private static final long serialVersionUID = 1L;
	final CompiledCloud cloud;

	public CloudModel(final CompiledCloud cloud) {
		this.cloud = cloud;
	}

	@Override
	public int getColumnCount() {
		return cloud.getSourceCount() + 1;
	}

	@Override
	public String getColumnName(final int column) {
		if (column == 0) {
			return "Product";
		}
		return "Source_" + column;
	}

	@Override
	public int getRowCount() {
		return cloud.getProductCount() + 1;
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		if (rowIndex == 0) {
			if (columnIndex == 0) {
				return "";
			} else {
				return cloud.getSource(columnIndex - 1);
			}
		} else if (columnIndex == 0) {
			return cloud.getProduct(rowIndex - 1);
		} else {
			final float value = cloud.getValue(columnIndex - 1, rowIndex - 1);
			return (value == 0.0f) ? null : value;
		}
	}

	@Override
	public boolean ignoreColumnForExport(final int col) {
		return false;
	}

	@Override
	public boolean isEmphasized(final int row, final int col) {
		return 0 != (row % 2);
	}

	@Override
	public boolean isExportable() {
		return true;
	}

	@Override
	public String toString() {
		return " Data";
	}
}
