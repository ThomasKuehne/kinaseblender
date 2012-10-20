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

import java.util.ArrayList;
import java.util.List;

import cn.kuehne.kinaseblender.engine2.Combination;
import cn.kuehne.kinaseblender.engine2.Source;

public class CombinationCollectionModel extends AbstractBasicTableModel {
	private static final long serialVersionUID = 1L;

	private final Object[][] data;

	public CombinationCollectionModel(final List<Combination[]> combinations) {
		this(combinations, " Unique Count");
	}

	public CombinationCollectionModel(final List<Combination[]> combinations,
			final String title) {
		super(title);

		final List<Source> sources = new ArrayList<Source>();
		int combinationCount = 0;
		for (final Combination[] cs : combinations) {
			for (final Combination c : cs) {
				combinationCount++;
				for (final Source s : c.getSources()) {
					if (!sources.contains(s)) {
						sources.add(s);
					}
				}
			}
		}

		data = new Object[sources.size() + 4][combinationCount + 1];
		data[0][0] = "Sources";
		data[1][0] = "Products";
		data[2][0] = "Unique";
		data[3][0] = "Shared";

		int col = 4;
		int row = 0;
		for (final Source s : sources) {
			data[col++][row] = s;
		}

		for (final Combination[] cs : combinations) {
			for (final Combination c : cs) {

				row++;
				data[0][row] = c.getSourceCount();
				data[1][row] = c;

				col = 4;
				int sharedSum = c.getProductCount();
				for (final Source s : sources) {
					final int u = c.getUniqueProducts(s);

					if (u > 0) {
						data[col][row] = u;
						sharedSum -= u;
					}
					col++;
				}
				data[3][row] = sharedSum;
				data[2][row] = c.getProductCount() - sharedSum;
			}
		}

	}

	@Override
	protected Object[][] getData() {
		return data;
	}

	@Override
	public boolean isEmphasized(final int row, final int col) {
		Object raw = data[0][row];
		if (raw != null) {
			Class<?> c = raw.getClass();
			if (Number.class.isAssignableFrom(c)) {
				final Number number = (Number) raw;
				final long value = number.longValue();
				if ((value % 2) != 0) {
					return true;
				}
			}
		}

		return false;
	}
}
