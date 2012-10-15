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

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import cn.kuehne.kinaseblender.engine2.Combination;
import cn.kuehne.kinaseblender.engine2.Product;
import cn.kuehne.kinaseblender.engine2.Source;

public class RenderJTable extends JTable {
	class ClickListener extends MouseAdapter {
		private final CellClickListener listener;

		ClickListener(final CellClickListener listener) {
			this.listener = listener;
		}

		@Override
		public void mouseClicked(final MouseEvent event) {
			final Point point = event.getPoint();
			final int row = rowAtPoint(point);
			final int column = columnAtPoint(point);
			if (-1 < row && -1 < column) {
				if (!isCellEditable(row, column)) {
					listener.onClick(row, column);
				}
			}
		}
	}

	private static final long serialVersionUID = 1L;

	public RenderJTable(final CellClickListener listener) {
		TableActionRenderer actionRenderer = new TableActionRenderer();

		setDefaultRenderer(Source.class, actionRenderer);
		setDefaultRenderer(Product.class, actionRenderer);
		setDefaultRenderer(Combination.class, new CombinationRenderer());
		setDefaultRenderer(TableAction.class, actionRenderer);
		setAutoResizeMode(AUTO_RESIZE_OFF);

		addMouseListener(new ClickListener(listener));
	}

	@Override
	public TableCellRenderer getCellRenderer(final int row, final int column) {
		final Object obj = getValueAt(row, column);
		if (obj != null) {
			final Class<?> c = obj.getClass();
			return getDefaultRenderer(c);
		}
		return super.getCellRenderer(row, column);
	}

	@Override
	public Component prepareRenderer(final TableCellRenderer renderer,
			final int row, final int col) {
		final Component comp = super.prepareRenderer(renderer, row, col);
		final TableModel model = getModel();
		final Class<?> modelClass = model.getClass();
		final boolean emphasize;

		if (ExportableTableModel.class.isAssignableFrom(modelClass)) {
			final ExportableTableModel exportModel = (ExportableTableModel) model;
			emphasize = exportModel.isEmphasized(row, col);
		} else {
			emphasize = false;
		}

		if (emphasize) {
			comp.setBackground(Color.LIGHT_GRAY);
		} else {
			comp.setBackground(Color.WHITE);
		}
		return comp;
	}
}
