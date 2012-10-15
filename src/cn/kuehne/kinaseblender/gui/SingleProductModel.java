/*
 * Copyright (c) 2010, 2011, 2012 Thomas Kühne <thomas@kuehne.cn>
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

import cn.kuehne.kinaseblender.engine2.CompiledCloud;
import cn.kuehne.kinaseblender.engine2.Product;
import cn.kuehne.kinaseblender.engine2.Source;

public class SingleProductModel extends AbstractBasicTableModel {
	private static final long serialVersionUID = 1L;

	private final Object[][] data;

	public SingleProductModel(final CompiledCloud compiled,
			final Product product) {
		super("Product: " + product.getName());

		final int productIndex = compiled.getProduct(product);
		final List<Source> sources = compiled.getSources(product);

		data = new Object[3][sources.size() + 1];

		data[0][0] = "Count";
		data[1][0] = "Source";
		data[2][0] = "Amount";

		for (int si = 0; si < sources.size(); si++) {
			final Source source = sources.get(si);
			data[0][si + 1] = (si + 1);
			data[1][si + 1] = source;
			data[2][si + 1] = compiled.getValue(compiled.getSource(source),
					productIndex);
		}
	}

	@Override
	protected Object[][] getData() {
		return data; // NOPMD
	}

	@Override
	public boolean isCloseable() {
		return true;
	}
}
