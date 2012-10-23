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

import java.awt.Component;
import java.awt.EventQueue;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Display error messages
 */
public class UnhandledException implements UncaughtExceptionHandler {
	private static final class ShowIt implements Runnable {
		private static final Icon icon = null;
		private static final Object initialValue = null;
		private static final JScrollPane message;
		private static final int messageType = JOptionPane.ERROR_MESSAGE;
		private static final int optionType = JOptionPane.OK_CANCEL_OPTION;
		private static final JTextArea text;
		private static final String title = "Kinaseblender: Error";

		static {
			text = new JTextArea();
			text.setEditable(false);
			message = new JScrollPane(text);
		}

		private final Component parent;
		private final Throwable unhandled;

		ShowIt(final Throwable t, final Component p) {
			unhandled = t;
			parent = p;
		}

		@Override
		public void run() {
			try {
				final StringWriter writer = new StringWriter();
				unhandled.printStackTrace(new PrintWriter(writer));
				text.setText(writer.toString());

				final Component elder;
				final Object[] options;
				if (parent != null && parent.isVisible()) {
					elder = parent;
					options = new String[] { "Ignore", "Exit" };
				} else {
					elder = null;
					options = new String[] { "Exit" };
				}

				final int result = JOptionPane.showOptionDialog(elder, message,
						title, optionType, messageType, icon, options,
						initialValue);
				if ((result != JOptionPane.OK_OPTION) || (1 == options.length)) {
					System.exit(-1);
				}
			} catch (final Throwable thrown) {
				thrown.printStackTrace();
			}
		}

	}

	private static UnhandledException singelton;

	public static UnhandledException getSingelton() {
		synchronized (UnhandledException.class) {
			if (null == singelton) {
				singelton = new UnhandledException();
			}
			return singelton;
		}
	}

	public static synchronized void uncaughtException(final Throwable e,
			final Component parent) {
		try {
			final Runnable r = new ShowIt(e, parent);
			if (EventQueue.isDispatchThread()) {
				r.run();
			} else {
				EventQueue.invokeLater(r);
			}
		} catch (final Throwable thrown) {
			thrown.printStackTrace();
		}
	}

	private Component parent;

	private UnhandledException() {
	}

	public void activate() {
		final Thread current = Thread.currentThread();
		current.setUncaughtExceptionHandler(this);
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	public void setParent(final Component parentComponent) {
		parent = parentComponent;
	}

	@Override
	public synchronized void uncaughtException(final Thread t, final Throwable e) {
		uncaughtException(e, parent);
	}
}
