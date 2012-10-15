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

import cn.kuehne.kinaseblender.engine2.CompiledCloud;
import cn.kuehne.kinaseblender.engine2.Product;
import cn.kuehne.kinaseblender.engine2.Source;

public class SingleSourceModel extends AbstractBasicTableModel {
	private static final long serialVersionUID = 1L;

	private final Object[][] data;

	public SingleSourceModel(final CompiledCloud compiled, final Source source) {
		super("Source: " + source.getName());

		data = new Object[3][compiled.getProductCount() + 1];

		data[0][0] = "Count";
		data[1][0] = "Product";
		data[2][0] = "Amount";

		final int sourceIndex = compiled.getSource(source);
		int produced = 1;
		final ArrayList<Product> products = new ArrayList<Product>();
		for (int pi = 0; pi < compiled.getProductCount(); pi++) {
			final double amount = compiled.getValue(sourceIndex, pi);
			if (0 < amount) {
				data[0][produced] = produced;
				data[1][produced] = compiled.getProduct(pi);
				data[2][produced] = amount;
				produced++;
			} else {
				products.add(compiled.getProduct(pi));
			}
		}

		for (final Product p : products) {
			data[1][produced] = p;
			data[2][produced] = "Missing";
			produced++;
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
