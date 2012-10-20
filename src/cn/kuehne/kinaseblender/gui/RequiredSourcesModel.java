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

import java.util.List;

import cn.kuehne.kinaseblender.engine2.Combination;

public class RequiredSourcesModel extends AbstractBasicTableModel {
	private static final long serialVersionUID = 1L;
	private final Object[][] data;

	public RequiredSourcesModel(final List<Combination[]> combi) {
		super(" Required Sources");

		data = new Object[3][combi.size() + 1];

		data[0][0] = "Sources";
		data[1][0] = "Products";
		data[2][0] = "Combinations";

		final int max = combi.size();
		for (int i = 0; i < max; i++) {
			data[0][i + 1] = i + 1;
			final Combination[] combs = combi.get(i);
			if (combs != null) {
				if (0 < combs.length) {
					data[1][i + 1] = combs[0].getProductCount();
				}
				data[2][i + 1] = combs.length;
			}
		}
	}

	@Override
	protected Object[][] getData() {
		return data;
	}
}
