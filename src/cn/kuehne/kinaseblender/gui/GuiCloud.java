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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JTable;

import cn.kuehne.kinaseblender.AppInterface;
import cn.kuehne.kinaseblender.engine2.Combination;
import cn.kuehne.kinaseblender.engine2.CompiledCloud;
import cn.kuehne.kinaseblender.engine2.CompiledCloud.SearchState;
import cn.kuehne.kinaseblender.engine2.Product;
import cn.kuehne.kinaseblender.engine2.Source;

public class GuiCloud {
	class CombinationToModel implements Runnable {
		final Combination combi;

		public CombinationToModel(Combination combi) {
			this.combi = combi;
		}

		@Override
		public void run() {
			appInterface.progressPush("create Combination model");
			SingleCombinationModel model = new SingleCombinationModel(cloud,
					combCounter++, combi);
			models.put(combi, model);
			appInterface.addItem(model);
			appInterface.setSelectedItem(model);
			appInterface.progressPop();
		}

	}

	class ModelChangedRunneable implements Runnable {
		final ExportableTableModel model;
		final SelListener selListener;

		ModelChangedRunneable(final SelListener selListener,
				final ExportableTableModel model) {
			this.selListener = selListener;
			this.model = model;
		}

		@Override
		public void run() {
			if (selListener.redirected) {
				selListener.redirected = false;
				appInterface.setSelectedItem(model);
			} else {
				table.setModel(model);
				setTitleAddon(model.toString());
				appInterface.enableExport(model.isExportable());
			}
		}

	}

	class SelListener implements CellClickListener, ItemListener {
		class TableActionChanged implements Runnable {
			final TableAction action;

			TableActionChanged(TableAction action) {
				this.action = action;
			}

			@Override
			public void run() {
				itemStateChanged(action.getTableActionItem());
			}
		}

		boolean redirected = false;

		@Override
		public void itemStateChanged(final ItemEvent event) {
			final Object item = event.getItem();
			itemStateChanged(item);
		}

		void itemStateChanged(final Object object) {
			if (object == null || object instanceof Double
					|| object instanceof Float || object instanceof Integer
					|| object instanceof String) {
				// noop
				return;
			}

			if (object instanceof ExportableTableModel) {
				modelChanged((ExportableTableModel) object);
				return;
			}
			redirected = true;
			if (object instanceof TableAction) {
				TableAction action = (TableAction) object;
				new Thread(new TableActionChanged(action)).start();
				return;
			}

			ExportableTableModel model = models.get(object);
			if (model != null) {
				modelChanged(model);
			} else if (object instanceof Combination) {
				Combination combi = (Combination) object;
				new Thread(new CombinationToModel(combi)).start();
				return;
			} else if (object instanceof Source) {
				model = new SingleSourceModel(cloud, (Source) object);
				models.put(object, model);
				appInterface.addItem(model);
				modelChanged(model);
			} else if (object instanceof Product) {
				model = new SingleProductModel(cloud, (Product) object);
				models.put(object, model);
				appInterface.addItem(model);
				modelChanged(model);
			} else if (object instanceof Source[]) {
				Source[] sources = (Source[]) object;
				whatIf.set(sources);
				modelChanged(whatIf);
			} else {
				throw new RuntimeException("itemStateChanged: "
						+ object.getClass() + " " + object);
			}
		}

		void modelChanged(final ExportableTableModel model) {
			appInterface.invokeInEventDispatchThread(new ModelChangedRunneable(
					this, model));
		}

		@Override
		public void onClick(final int row, final int column) {
			final Object value = table.getValueAt(row, column);
			itemStateChanged(value);
		}
	}

	final AppInterface appInterface;
	private final List<Combination[]> bestCombinations;
	CompiledCloud cloud;
	int combCounter;
	// final JComboBox combo;
	// final JButton exportButton;
	final Map<Object, ExportableTableModel> models;

	private final List<Combination[]> nextBestCombinaitons;

	final JTable table;

	WhatIfModel whatIf;

	public GuiCloud(AppInterface appInterface) {
		this.appInterface = appInterface;
		final SelListener selListener = new SelListener();
		appInterface.addItemListener(selListener);
		table = new RenderJTable(selListener);

		models = new HashMap<Object, ExportableTableModel>();
		bestCombinations = new ArrayList<Combination[]>();
		nextBestCombinaitons = new ArrayList<Combination[]>();
	}

	private void calcCombinations(final CompiledCloud compiled) {
		final SearchState searchState = compiled.initSearch();
		bestCombinations.clear();
		nextBestCombinaitons.clear();

		while (searchState.search()) {
			final Combination[] combs = searchState.combinations();
			bestCombinations.add(combs);

			final Combination[] closeCombinations = searchState
					.closeCombinations();
			nextBestCombinaitons.add(closeCombinations);

			final String message = "find Combinations... " + "(Sources: "
					+ searchState.getDeepth() + ",  Best: "
					+ searchState.getBestSize() + ", NextBest: "
					+ searchState.getCandidateSize() + ")";
			appInterface.progressSwitch(message);

		}
	}

	public JComponent getCloudComponent() {
		return table;
	}

	public ExportableTableModel getTableModel() {
		return (ExportableTableModel) table.getModel();
	}

	public void setCloud(final CompiledCloud compiled) {
		cloud = compiled;
		appInterface.removeAllItems();
		models.clear();
		combCounter = 1;
		if (cloud != null) {
			appInterface.progressPush("find Combinations");
			calcCombinations(compiled);
			appInterface.progressPop();

			appInterface.progressPush("generate Data model");
			appInterface.addItem(new CloudModel(cloud));
			appInterface.progressPop();

			appInterface.progressPush("generate Product model");
			appInterface.addItem(new ProductModel(cloud));
			appInterface.progressPop();

			appInterface.progressPush("generate Source model");
			appInterface.addItem(new SourcesModel(cloud));
			appInterface.progressPop();

			appInterface.progressPush("generate Required Sources model");
			appInterface.addItem(new RequiredSourcesModel(bestCombinations));
			appInterface.progressPop();

			appInterface.progressPush("generate Combination model");
			appInterface.addItem(new CombinationCollectionModel(
					bestCombinations));
			appInterface.progressPop();

			appInterface.progressPush("generate Next Best models");
			for (final Combination[] nextBest : nextBestCombinaitons) {
				appInterface.addItem(new NextBestModel(nextBest));
			}
			appInterface.progressPop();

			appInterface.progressPush("generate What If? model");
			whatIf = new WhatIfModel(appInterface, cloud);
			appInterface.addItem(whatIf);
			appInterface.progressPop();
		}
	}

	public void setTitleAddon(final String addon) {
		if (addon == null) {
			appInterface.setTitle("Kinaseblender Big");
		} else {
			appInterface.setTitle("Kinaseblender Big - " + addon);
		}
	}
}
