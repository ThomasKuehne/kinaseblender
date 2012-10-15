package cn.kuehne.kinaseblender.gui;

import java.util.Collections;

import cn.kuehne.kinaseblender.engine2.Combination;

public class NextBestModel extends CombinationCollectionModel {
	private static final long serialVersionUID = 1L;

	static String genTitle(final Combination[] combinations) {
		final StringBuilder builder = new StringBuilder();

		final int sources;
		if (combinations == null || combinations.length < 1) {
			sources = 0;
		} else {
			sources = combinations[0].getSourceCount();
		}

		builder.append(" Next Best: ");
		for (int i = sources; i < 1000; i *= 10) {
			builder.append(' ');
		}
		builder.append(sources);
		builder.append(" Sources");
		return builder.toString();

	}

	private final boolean[] emphasized;

	public NextBestModel(final Combination[] combinations) {
		super(Collections.singletonList(combinations), genTitle(combinations));

		emphasized = new boolean[combinations.length];

		boolean em = false;
		int old = combinations[0].getProductCount();
		for (int index = 1; index < emphasized.length; index++) {
			final int current = combinations[index].getProductCount();
			if (current != old) {
				em = !em;
				old = current;
			}
			emphasized[index] = em;
		}
	}

	@Override
	public boolean isEmphasized(final int row, final int col) {
		if (row == 0) {
			return false;
		} else {
			return emphasized[row - 1];
		}
	}
}
