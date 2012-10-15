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

package cn.kuehne.kinaseblender.engine2;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

public class CompiledCloud {
	final class SearchComb implements Comparable<SearchComb>, Scored {
		final boolean[] prods;
		int score;
		final boolean[] subs;

		SearchComb() {
			subs = new boolean[sources.length];
			prods = new boolean[products.length];
		}

		SearchComb(final SearchComb master) {
			subs = master.subs.clone();
			prods = master.prods.clone();
		}

		@Override
		public int compareTo(final SearchComb other) {
			if (other == null) {
				return -1;
			}

			for (int i = 0; i < subs.length; i++) {
				if (subs[i] ^ other.subs[i]) {
					return subs[i] ? 1 : -1;
				}
			}
			return 0;
		}

		@Override
		public boolean equals(final Object other) {
			if (other instanceof SearchComb) {
				return 0 == compareTo((SearchComb) other);
			}
			return false;
		}

		@Override
		public boolean equals2(Scored raw) {
			if (raw == null || !(raw instanceof SearchComb)) {
				return false;
			}

			final SearchComb other = (SearchComb) raw;
			for (int i = 0; i < subs.length; i++) {
				if (subs[i] ^ other.subs[i]) {
					return false;
				}
			}
			return true;
		}

		@Override
		public int getScore() {
			return score;
		}

		protected int set(final int sourceIndex) {
			boolean changed = false;
			int count = 0;
			subs[sourceIndex] = true;
			for (int pi = 0; pi < prods.length; pi++) {
				if (data[sourceIndex][pi] && !prods[pi]) {
					prods[pi] = true;
					changed = true;
				}
				if (prods[pi]) {
					count++;
				}
			}
			score = count;
			return (changed ? count : 0);
		}
	}

	public class SearchState {
		private final NavigableSet<SearchComb> best;

		private int bestScore;
		private int deepth;

		private final TransferBuffer<SearchComb> transfer2;

		public SearchState() {
			deepth = 0;
			final int s = getSourceCount();
			// Heyristic capacity for combination problem
			final int capacity = (int) (s * Math.log(s + Math.E));

			transfer2 = new TransferBuffer<SearchComb>(capacity);
			best = new TreeSet<SearchComb>();
			bestScore = 0;
		}

		public Combination[] closeCombinations() {
			Scored[] scored = transfer2.copy();
			final Combination[] back = new Combination[scored.length];
			int backIndex = back.length - 1;
			for (int index = 0; index < back.length; index++) {
				SearchComb b = (SearchComb) scored[index];
				back[backIndex--] = new Combination(CompiledCloud.this, b.subs);
			}
			return back;
		}

		public Combination[] combinations() {
			final Combination[] back = new Combination[best.size()];
			int backIndex = 0;
			for (SearchComb b : best) {
				back[backIndex++] = new Combination(CompiledCloud.this, b.subs);
			}
			return back;
		}

		public int getBestSize() {
			return best.size();
		}

		public int getCandidateSize() {
			return transfer2.size();
		}

		public int getDeepth() {
			return deepth;
		}

		public boolean search() {
			final Scored[] old;
			if (deepth == 0) {
				old = new SearchComb[] { new SearchComb() };
			} else {
				old = transfer2.copy();
			}

			best.clear();
			deepth++;
			bestScore++;

			transfer2.setMinScore(bestScore);

			for (final Scored rawTemplate : old) {
				final SearchComb template = (SearchComb) rawTemplate;
				if (template != null) {
					for (int si = 0; si < sources.length; si++) {
						if (!template.subs[si]) {
							final SearchComb test = new SearchComb(template);
							final int score = test.set(si);
							if (transfer2.add(test)) {
								if (bestScore <= score) {
									if (bestScore < score) {
										best.clear();
										bestScore = score;
									}
									best.add(test);
								}
							}
						}
					}
				}
			}

			return !best.isEmpty();
		}
	}

	final boolean[][] data;
	final Product[] products;

	final Source[] sources;

	final float[][] valueData;

	CompiledCloud(final Source[] _sources, final Product[] _products,
			final boolean[][] produces, final float[][] amounts) {
		if (_sources == null || _sources.length < 1) {
			throw new IllegalArgumentException("no sources");
		}
		sources = _sources;

		if (_products == null || _products.length < 1) {
			throw new IllegalArgumentException("no products");
		}
		products = _products;

		if (produces == null) {
			throw new IllegalArgumentException("no data");
		}

		if (produces.length != _sources.length) {
			throw new IllegalArgumentException("data has " + produces.length
					+ " sources, expected " + _sources.length);
		}
		for (int sourceIndex = 0; sourceIndex < sources.length; sourceIndex++) {
			final boolean[] entry = produces[sourceIndex];
			if (entry == null) {
				throw new IllegalArgumentException("no data for source ("
						+ sourceIndex + ") " + sources[sourceIndex]);
			}
			if (entry.length != products.length) {
				throw new IllegalArgumentException("data has " + entry.length
						+ " instead of " + products.length + " for source ("
						+ sourceIndex + ") " + sources[sourceIndex]);
			}
		} // TODO check v
		data = produces;
		valueData = amounts;
	}

	public void debug(final Writer writer) throws IOException {
		writer.append("product");
		for (Source s : sources) {
			writer.append('\t');
			writer.append(s.getName());
		}
		writer.append("\r\n");

		for (int pi = 0; pi < products.length; pi++) {
			writer.append(products[pi].getName());
			for (int si = 0; si < sources.length; si++) {
				writer.append('\t');
				writer.append(data[si][pi] ? '1' : '0');
			}
			writer.append("\r\n");
		}
	}

	public Product getProduct(final int productIndex) {
		return products[productIndex];
	}

	public int getProduct(final Product product) {
		for (int pi = 0; pi < products.length; pi++) {
			if (0 == NamedComparator.SINGELTON.compare(products[pi], product)) {
				return pi;
			}
		}
		return -1;
	}

	public int getProductCount() {
		return products.length;
	}

	public List<Product> getProducts(final Source source) {
		final ArrayList<Product> back = new ArrayList<Product>();
		final int sourceIndex = getSource(source);
		for (int porductIndex = 0; porductIndex < products.length; porductIndex++) {
			if (data[sourceIndex][porductIndex]) {
				back.add(products[porductIndex]);
			}
		}
		return back;
	}

	public Source getSource(final int sourceIndex) {
		return sources[sourceIndex];
	}

	public int getSource(final Source source) {
		for (int si = 0; si < sources.length; si++) {
			if (0 == NamedComparator.SINGELTON.compare(sources[si], source)) {
				return si;
			}
		}
		return -1;
	}

	public int getSourceCount() {
		return sources.length;
	}

	public List<Source> getSources(final int productIndex) {
		final ArrayList<Source> back = new ArrayList<Source>();
		for (int si = 0; si < sources.length; si++) {
			if (data[si][productIndex]) {
				back.add(sources[si]);
			}
		}
		return back;
	}

	public List<Source> getSources(final Product product) {
		return getSources(getProduct(product));
	}

	public float getValue(final int sourceIndex, final int productIndex) {
		return valueData[sourceIndex][productIndex];
	}

	public float getValue(final Source source, final Product product) {
		final int productIndex = getProduct(product);
		final int sourceIndex = getSource(source);
		return getValue(sourceIndex, productIndex);
	}

	public SearchState initSearch() {
		return new SearchState();
	}

}
