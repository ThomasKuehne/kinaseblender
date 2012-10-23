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

package cn.kuehne.kinaseblender.engine2;

import java.util.ArrayList;
import java.util.List;

/**
 * Source combination used by the GUI models
 */
public class Combination {
	private final CompiledCloud cloud;
	private int[] products;
	private Product[] productsObj;

	private final boolean[] sources;

	private Source[] sourcesObj;

	public Combination(final CompiledCloud cloud, final boolean[] sources) {
		if (cloud == null) {
			throw new IllegalArgumentException("no cloud");
		}
		this.cloud = cloud;

		if (sources == null || sources.length < 1) {
			throw new IllegalArgumentException("no sources");
		}
		if (sources.length != cloud.getSourceCount()) {
			throw new IllegalArgumentException("bad source count: "
					+ sources.length + " instead of " + cloud.getSourceCount());
		}

		this.sources = sources.clone();
	}

	/**
	 * if required, fill "products" 
	 */
	private void ensureProducts() {
		if (products == null) {
			products = new int[cloud.products.length];

			for (int sourceIndex = sources.length - 1; -1 < sourceIndex; sourceIndex--) {
				if (sources[sourceIndex]) {
					for (int productIndex = products.length - 1; -1 < productIndex; productIndex--) {
						if (cloud.data[sourceIndex][productIndex]) {
							products[productIndex]++;
						}
					}
				}
			}
		}
	}

	/**
	 * total number of products produced 
	 */
	public int getProductCount() {
		ensureProducts();
		int sum = 0;
		for (int i = products.length - 1; -1 < i; i--) {
			if (0 < products[i]) {
				sum++;
			}
		}
		return sum;
	}

	/**
	 * new array containing all Products 
	 */
	public Product[] getProducts() {
		if (productsObj == null) {
			productsObj = new Product[getProductCount()];
			int obj = productsObj.length - 1;
			for (int i = products.length - 1; -1 < i; i--) {
				if (0 < products[i]) {
					productsObj[obj--] = cloud.products[i];
				}
			}
		}
		return productsObj.clone();
	}

	/**
	 * total number of Sources 
	 */
	public int getSourceCount() {
		int sum = 0;
		for (int i = sources.length - 1; -1 < i; i--) {
			if (sources[i]) {
				sum++;
			}
		}
		return sum;
	}

	/**
	 * new array containing all Sources 
	 */
	public Source[] getSources() {
		if (sourcesObj == null) {
			sourcesObj = new Source[getSourceCount()];
			int obj = sourcesObj.length - 1;
			for (int i = sources.length - 1; -1 < i; i--) {
				if (sources[i]) {
					sourcesObj[obj--] = cloud.sources[i];
				}
			}
		}
		return sourcesObj.clone();
	}

	/**
	 * list all sources producing the given product 
	 */
	public List<Source> getSources(final Product product) {
		final ArrayList<Source> back = new ArrayList<Source>();
		final int productIndex = cloud.getProduct(product);
		if (0 < products[productIndex]) {
			for (int sourceIndex = 0; sourceIndex < sources.length; sourceIndex++) {
				if (sources[sourceIndex]
						&& cloud.data[sourceIndex][productIndex]) {
					back.add(cloud.sources[sourceIndex]);
				}
			}
		}
		return back;
	}

	/**
	 * count all Products only produces by the given source
	 * @return -1 if source isn't part of this combination 
	 */
	public int getUniqueProducts(final Source source) {
		ensureProducts();

		for (int sourceIndex = 0; sourceIndex < sources.length; sourceIndex++) {
			if (cloud.sources[sourceIndex].equals(source)
					&& sources[sourceIndex]) {
				int unique = 0;
				for (int productIndex = 0; productIndex < products.length; productIndex++) {
					if (cloud.data[sourceIndex][productIndex]
							&& 1 == products[productIndex]) {
						unique++;
					}
				}
				return unique;
			}
		}

		return -1;
	}

	/**
	 * check if the given product is produced by this combination 
	 */
	public boolean produces(final int product) {
		return 0 < products[product];
	}

	@Override
	public String toString() {
		return Integer.toString(getProductCount());
	}
}
