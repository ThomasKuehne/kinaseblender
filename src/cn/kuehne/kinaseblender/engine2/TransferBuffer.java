package cn.kuehne.kinaseblender.engine2;

import java.util.LinkedList;

public class TransferBuffer<T extends Scored> {

	private int current;
	private final LinkedList<T> data;
	private final int maxSize;
	private int minScore;

	TransferBuffer(int size) {
		data = new LinkedList<T>();
		maxSize = size;
	}

	public boolean add(T comb) {
		int score = comb.getScore();
		if (score < minScore) {
			return false;
		}

		int added = -1;
		for (int index = 0; index < data.size(); index++) {
			final T old = data.get(index);

			if (comb.equals(old)) {
				return false;
			}

			final int oldScore = old.getScore();

			if (score < oldScore) {
				if (score < oldScore && maxSize < data.size()) {
					throw new IllegalStateException("old.score == score "
							+ score + " {" + current + "}" + data.size());

				}
				if (data.size() < maxSize || index != 0) {
					data.add(index, comb);
					added = index;
					break;
				} else {
					return false;
				}
			}
		}

		if (added < 0) {
			data.add(comb);
		}
		if (maxSize < data.size()) {
			T old = data.get(0);
			if (data.getLast().getScore() == old.getScore()) {
				throw new IllegalStateException("last.score == first.score "
						+ score + " {" + current + "}" + data.size() + " "
						+ added);

			}
			data.remove(0);
		}

		return true;
	}

	public Scored[] copy() {
		return data.toArray(new Scored[data.size()]);
	}

	public void setMinScore(int newMinScore) {
		minScore = newMinScore;

		data.clear();
		current = 0;
	}

	public int size() {
		return data.size();
	}
}
