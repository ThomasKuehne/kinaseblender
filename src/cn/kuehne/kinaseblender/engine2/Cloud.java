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

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

/*
 * slow intermediate storage for Source-Product data
 */
public class Cloud {
	private static final Pattern TAB = Pattern.compile("\t");

	private static String rtrim(final String raw) {
		for (int end = raw.length() - 1; -1 < end; end--) {
			if (' ' < raw.charAt(end)) {
				if (end != raw.length() + 1) {
					return raw.substring(0, end + 1);
				}
				return raw;
			}
		}
		return null;
	}

	private final NamedComparator comparator;

	private final NavigableMap<Source, TreeMap<Product, Float>> map;

	private final NavigableSet<Product> products;

	private final NavigableSet<Source> sources;

	public Cloud() {
		comparator = new NamedComparator();
		sources = new TreeSet<Source>(comparator);
		products = new TreeSet<Product>(comparator);
		map = new TreeMap<Source, TreeMap<Product, Float>>(comparator);
	}

	/**
	 * add product, if it isn't already part of this Cloud
	 */
	public boolean addProduct(final Product product) {
		if (product == null) {
			throw new IllegalArgumentException("product is null");
		}
		final String name = product.getName();
		if (name == null) {
			throw new IllegalArgumentException("products's name is null");
		}
		return products.add(product);
	}

	/**
	 * add source, if it isn't already part of this Cloud
	 */
	public boolean addSource(final Source source) {
		if (source == null) {
			throw new IllegalArgumentException("source is null");
		}
		final String name = source.getName();
		if (name == null) {
			throw new IllegalArgumentException("source's name is null");
		}

		return sources.add(source);
	}

	/**
	 * transform into an optimized, read-only CompiledCloud 
	 */
	public CompiledCloud compile() {
		final Source[] sBack = getSources();
		final Product[] pBack = getProducts();

		boolean[][] data = new boolean[sBack.length][pBack.length];
		float[][] valueData = new float[sBack.length][pBack.length];

		for (int sourceIndex = 0; sourceIndex < sBack.length; sourceIndex++) {
			final TreeMap<Product, Float> produceMap = map
					.get(sBack[sourceIndex]);
			if (produceMap != null) {
				for (final Entry<Product, Float> entry : produceMap.entrySet()) {
					final int productIndex = Arrays.binarySearch(pBack,
							entry.getKey(), comparator);
					data[sourceIndex][productIndex] = true;
					valueData[sourceIndex][productIndex] = entry.getValue();
				}
			}
		}

		return new CompiledCloud(sBack, pBack, data, valueData);
	}

	public Product createProduct(final String name) {
		return new Product(name);
	}

	public Source createSource(final String name) {
		return new Source(name);
	}

	/**
	 * map product name to product
	 */
	public Product getProduct(final String name) {
		if (name == null) {
			throw new IllegalArgumentException("products's name is null");
		}
		for (Product p : products) {
			if (name.equals(p.getName())) {
				return p;
			}
		}
		return null;
	}

	/**
	 * total number of products
	 */
	public int getProductCount() {
		return products.size();
	}

	/**
	 * new array containing all products
	 */
	public Product[] getProducts() {
		return products.toArray(new Product[products.size()]);
	}

	/**
	 * map source name to source
	 */
	public Source getSource(final String name) {
		if (name == null) {
			throw new IllegalArgumentException("source's name is null");
		}
		for (Source s : sources) {
			if (name.equals(s.getName())) {
				return s;
			}
		}
		return null;
	}

	/**
	 * total number of sources
	 */
	public int getSourceCount() {
		return sources.size();
	}

	/**
	 * new array containing all sources
	 */
	public Source[] getSources() {
		final Source[] back = new Source[sources.size()];
		return sources.toArray(back);
	}

	/**
	 * set production value for the given source
	 */
	public void produces(final Source source, final Product product,
			final float value) {
		addSource(source);
		addProduct(product);

		producesImpl(source, product, value);
	}

	protected void producesImpl(final Source source, final Product product,
			float value) {
		TreeMap<Product, Float> set = map.get(source);
		if (set == null) {
			set = new TreeMap<Product, Float>(comparator);
			map.put(source, set);
		}else{
			final Float oldValue  = set.get(product);
			if(oldValue != null && !Float.isNaN(oldValue)){
				value += oldValue;
			}
		}
		set.put(product, value);
	}

	/**
	 * parse, filter and add tab separated data
	 */
	public void readAll(final Reader input, final double minValue)
			throws IOException {
		final LineNumberReader reader = new LineNumberReader(input);

		String line = reader.readLine();

		if (line != null) {
			String[] tok = TAB.split(rtrim(line));
			final Source[] sources = new Source[tok.length];
			for (int i = 1; i < sources.length; i++) {
				sources[i] = createSource(tok[i]);
				addSource(sources[i]);
			}

			for (line = reader.readLine(); null != line; line = reader
					.readLine()) {
				if (0 < line.length()) {

					tok = TAB.split(rtrim(line));
					if (1 < tok.length) {
						final Product product = createProduct(tok[0]);
						addProduct(product);
						for (int si = 1; si < tok.length; si++) {
							try {
								final float value = Float.parseFloat(tok[si]);
								if (minValue <= value) {
									producesImpl(sources[si], product, value);
								}
							} catch (final Exception e) {
								// noop
							}
						}
					}
				}
			}
		}
	}
}
