package cn.kuehne.kinaseblender.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import cn.kuehne.kinaseblender.AppInterface;
import cn.kuehne.kinaseblender.engine2.Combination;
import cn.kuehne.kinaseblender.engine2.CompiledCloud;
import cn.kuehne.kinaseblender.engine2.Source;

public class WhatIfModel implements ExportableTableModel {
	private class Creator extends TableAction {
		final AppInterface appInterface;

		private Creator(AppInterface appInterface) {
			super("Show >>");
			this.appInterface = appInterface;
		}

		@Override
		public Object getTableActionItem() {
			appInterface.progressPush("create Combination");
			Combination c = new Combination(cloud, include);
			appInterface.progressPop();
			return c;
		}
	}

	private final CompiledCloud cloud;
	private final Creator creator;
	private final boolean[] include;

	private final List<TableModelListener> listeners;

	public WhatIfModel(AppInterface appInterface, final CompiledCloud cloud) {
		this.cloud = cloud;
		listeners = new ArrayList<TableModelListener>();
		include = new boolean[cloud.getSourceCount()];
		creator = new Creator(appInterface);
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
		if (l != null) {
			listeners.add(l);
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Creator.class;
		case 1:
			return String.class;
		case 2:
			return Boolean.class;
		default:
			throw new IllegalArgumentException("missing col: " + columnIndex);
		}
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return "";
		case 1:
			return "Source";
		case 2:
			return "Include";
		default:
			throw new IllegalArgumentException("missing col: " + columnIndex);
		}
	}

	@Override
	public int getRowCount() {
		return include.length;
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		switch (columnIndex) {
		case 0:
			return (rowIndex % 10 == 0) ? creator : null;
		case 1:
			return cloud.getSource(rowIndex);
		case 2:
			return include[rowIndex];
		default:
			throw new IllegalArgumentException("missing col: " + rowIndex + " "
					+ columnIndex);
		}
	}

	@Override
	public boolean ignoreColumnForExport(final int col) {
		return col == 0;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 2;
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
	public void removeTableModelListener(TableModelListener l) {
		int i = listeners.indexOf(l);
		if (-1 < i) {
			listeners.remove(i);
		}
	}

	public void set(Source[] sources) {
		for (int i = 0; i < include.length; i++) {
			include[i] = false;
		}
		for (Source s : sources) {
			int sourceIndex = cloud.getSource(s);
			include[sourceIndex] = true;
		}
		final TableModelEvent event = new TableModelEvent(this);
		for (final TableModelListener listener : listeners) {
			listener.tableChanged(event);
		}
	}

	@Override
	public void setValueAt(Object rawValue, int rowIndex, int columnIndex) {
		if (!isCellEditable(rowIndex, columnIndex)) {
			throw new IllegalArgumentException("Cell isn't editable: row="
					+ rowIndex + " col=" + columnIndex);
		}
		if (rawValue instanceof Boolean
				|| (rawValue != null && Boolean.TYPE.isAssignableFrom(rawValue
						.getClass()))) {
			Boolean value = (Boolean) rawValue;
			include[rowIndex] = value;
		} else {
			throw new IllegalArgumentException("can't set value " + rawValue);
		}
	}

	@Override
	public String toString() {
		return " What If?";
	}
}