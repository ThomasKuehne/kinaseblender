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

import java.util.ArrayList;
import java.util.List;

import cn.kuehne.kinaseblender.engine2.Combination;
import cn.kuehne.kinaseblender.engine2.CompiledCloud;
import cn.kuehne.kinaseblender.engine2.Product;
import cn.kuehne.kinaseblender.engine2.Source;

public class SingleCombinationModel extends AbstractBasicTableModel {
	private class ToWhatIf extends TableAction {
		private final Combination comb;

		private ToWhatIf(Combination comb) {
			super("What if >>");
			this.comb = comb;
		}

		@Override
		public Source[] getTableActionItem() {
			return comb.getSources();
		}
	}

	private static final long serialVersionUID = 1L;

	private final Object[][] data;

	private final ToWhatIf toWhatIf;

	public SingleCombinationModel(final CompiledCloud compiled, final int id,
			final Combination comb) {
		super("Combination: " + id);
		toWhatIf = new ToWhatIf(comb);

		final ArrayList<List<Source>> sources = new ArrayList<List<Source>>();

		int maxSources = 0;

		final Product[] products = comb.getProducts();
		for (final Product p : products) {
			final List<Source> producers = comb.getSources(p);
			sources.add(producers);
			if (producers.size() > maxSources) {
				maxSources = producers.size();
			}
		}

		data = new Object[4 + maxSources][1 + compiled.getProductCount()];

		data[0][0] = toWhatIf;
		data[1][0] = "Product";
		data[2][0] = "Amount";
		data[3][0] = "Count";
		for (int i = 4; i < data.length; i++) {
			data[i][0] = "Source_" + (i - 3);
		}

		int rowIndex;
		for (rowIndex = 0; rowIndex < products.length; rowIndex++) {
			if ((rowIndex + 1) % 10 == 0) {
				data[0][rowIndex + 1] = toWhatIf;
			}
			data[1][rowIndex + 1] = products[rowIndex];
			int columnIndex = 4;
			int count = 0;
			float amount = 0;
			for (final Source s : sources.get(rowIndex)) {
				data[columnIndex][rowIndex + 1] = s;
				columnIndex++;
				count++;
				amount += compiled.getValue(s, products[rowIndex]);
			}
			data[2][rowIndex + 1] = amount;
			data[3][rowIndex + 1] = count;
		}

		for (int pi = 0; pi < compiled.getProductCount(); pi++) {
			if (!comb.produces(pi)) {
				if ((rowIndex + 1) % 10 == 0) {
					data[0][rowIndex + 1] = toWhatIf;
				}
				final Product product = compiled.getProduct(pi);
				data[1][rowIndex + 1] = product;
				data[2][rowIndex + 1] = "Missing";
				rowIndex++;
			}
		}
	}

	@Override
	protected Object[][] getData() {
		return data; // NOPMD
	}

	@Override
	public boolean ignoreColumnForExport(final int col) {
		return col == 0;
	}

	@Override
	public boolean isCloseable() {
		return true;
	}
}
