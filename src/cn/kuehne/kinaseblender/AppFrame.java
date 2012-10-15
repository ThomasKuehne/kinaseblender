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

package cn.kuehne.kinaseblender;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.net.URL;
import java.util.Stack;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.MutableComboBoxModel;
import javax.swing.SwingUtilities;

import cn.kuehne.kinaseblender.engine2.Cloud;
import cn.kuehne.kinaseblender.gui.ExportableTableModel;
import cn.kuehne.kinaseblender.gui.GuiCloud;
import cn.kuehne.kinaseblender.gui.UnhandledException;

public class AppFrame extends JFrame implements AppInterface {
	class AddItem implements Runnable {
		final Object item;

		AddItem(Object item) {
			this.item = item;
		}

		@Override
		public void run() {
			final ComboBoxModel model = combo.getModel();
			if (model instanceof DefaultComboBoxModel) {
				final String key = item.toString();

				final MutableComboBoxModel mutableModel = (MutableComboBoxModel) model;
				for (int i = 0; i < mutableModel.getSize(); i++) {
					final Object value = mutableModel.getElementAt(i);
					final String valueKey = value.toString();
					if (0 < valueKey.compareTo(key)) {
						mutableModel.insertElementAt(item, i);
						return;
					}
				}
			}
			combo.addItem(item);
			combo.setEnabled(true);
		}

	}

	class EnableExport implements Runnable {
		final boolean enable;

		EnableExport(boolean enable) {
			this.enable = enable;
		}

		@Override
		public void run() {
			exportButton.setEnabled(enable);
		}

	}

	class ExportListener implements ActionListener, Runnable {
		@Override
		public void actionPerformed(final ActionEvent event) {
			progressPush("write clipboard");
			new Thread(this).start();
		}

		@Override
		public void run() {
			final ExportableTableModel tableModel = guiCloud.getTableModel();

			final StringBuilder stringBuilder = new StringBuilder();

			final int columns = tableModel.getColumnCount();
			final int rows = tableModel.getRowCount();

			for (int row = 0; row < rows; row++) {
				boolean firstCol = true;
				for (int col = 0; col < columns; col++) {
					if (tableModel.ignoreColumnForExport(col)) {
						continue;
					}
					final Object value = tableModel.getValueAt(row, col);
					if (!firstCol) {
						stringBuilder.append('\t');
					}
					firstCol = false;
					if (value != null) {
						stringBuilder.append(value.toString());
					}
				}
				stringBuilder.append("\r\n");
			}

			final StringSelection selection = new StringSelection(
					stringBuilder.toString());
			Toolkit.getDefaultToolkit().getSystemClipboard()
					.setContents(selection, null);
			progressPop();
		}
	}

	class FileImporter implements Runnable {

		private final String[] fileNames;
		
		FileImporter(String... fileNames){
			this.fileNames = fileNames.clone();
		}
		
		@Override
		public void run() {
			try {
				progressPush("File importer");
				final Cloud cloud = new Cloud();
				for(final String name : fileNames){
					progressPush("import "+name);
					try{
						final FileReader fileReader = new FileReader(name);
					
						try{
							final BufferedReader bufferedReader = new BufferedReader(fileReader);
						
							cloud.readAll(bufferedReader, Float.MIN_VALUE);
						}finally{
							try{fileReader.close();}catch(Exception e){}
						}
					}finally{
						progressPop();
					}
				}

				progressPush("compile");
				guiCloud.setCloud(cloud.compile());
				progressPop();

				progressPop();
			} catch (Exception e) {
				UnhandledException.getSingelton().uncaughtException(
						Thread.currentThread(), e);
			}
		}
	}
	
	class Importer implements ActionListener, Runnable {
		@Override
		public void actionPerformed(final ActionEvent event) {
			importClipboard();
		}

		void importClipboard() {
			new Thread(this).start();
		}

		@Override
		public void run() {
			try {
				progressPush("read clipboard");
				final Clipboard clipboard = Toolkit.getDefaultToolkit()
						.getSystemClipboard();
				final Transferable transferable = clipboard.getContents(null);
				final Reader reader = DataFlavor.stringFlavor
						.getReaderForText(transferable);
				final BufferedReader bufferedReader = new BufferedReader(reader);

				final Cloud cloud = new Cloud();

				cloud.readAll(bufferedReader, Float.MIN_VALUE);

				progressPush("compile");
				guiCloud.setCloud(cloud.compile());
				progressPop();

				progressPop();
			} catch (Exception e) {
				UnhandledException.getSingelton().uncaughtException(
						Thread.currentThread(), e);
			}
		}
	}

	class ProgressPop implements Runnable {
		@Override
		public void run() {
			synchronized (progressBar) {
				String old = progressMessages.peek();

				progressMessages.pop();

				if (progressMessages.size() < 1) {
					progressBar.setVisible(false);
					progressBar.setIndeterminate(false);
					progressBar.setStringPainted(false);
				} else {
					progressBar.setString(progressMessages.peek());
				}

				if (DEBUG_PROGRESS) {
					System.out.println("pop " + progressMessages.size() + "  "
							+ old);
				}
			}
		}
	}

	class ProgressPush implements Runnable {
		final String message;

		ProgressPush(String message) {
			this.message = message;
		}

		@Override
		public void run() {
			synchronized (progressBar) {
				if (DEBUG_PROGRESS) {
					System.out.println("push " + progressMessages.size() + "  "
							+ message);
				}

				progressMessages.push(message);
				progressBar.setString(progressMessages.peek());
				if (0 < progressMessages.size()) {
					progressBar.setStringPainted(true);
					progressBar.setVisible(true);
					progressBar.setIndeterminate(true);
				}
			}
		}
	}

	class ProgressSwitch implements Runnable {
		final String message;

		ProgressSwitch(String message) {
			this.message = message;
		}

		@Override
		public void run() {
			progressMessages.pop();
			progressMessages.push(message);
			progressBar.setString(progressMessages.peek());
			if (DEBUG_PROGRESS) {
				System.out.println("switch " + progressMessages.size() + "  "
						+ progressMessages.peek());
			}
		}
	}

	class RemoveAllItems implements Runnable {
		@Override
		public void run() {
			combo.removeAllItems();
			combo.setEnabled(false);
		}

	}

	class SetSelectedItem implements Runnable {
		final Object item;

		SetSelectedItem(Object o) {
			item = o;
		}

		@Override
		public void run() {
			combo.setSelectedItem(item);
		}

	}

	static class Starter implements Runnable {
		private String[] args;
		
		public Starter(String[] args) {
			this.args = args;
		}

		@Override
		public void run() {
			final AppFrame appFrame = new AppFrame();
			
			if(args != null && 0 < args.length){
				new Thread(appFrame.new FileImporter(args)).start();
			}
		}
	}

	class StorePreferenceHandler extends WindowAdapter {
		@Override
		public void windowClosing(final WindowEvent evt) {
			storePreferences();
		}
	}

	private final static boolean DEBUG_PROGRESS = false;

	private final static int DEFAULT_HEIGHT = 500;

	private final static int DEFAULT_ORIGIN_X = 20;

	private final static int DEFAULT_ORIGIN_Y = 20;

	private final static int DEFAULT_WIDTH = 600;

	private static ImageIcon icon;

	private final static long serialVersionUID = 1L;

	public static ImageIcon getIcon() {
		synchronized (AppFrame.class) {
			if (icon == null) {
				final URL url = AppFrame.class.getResource("icon.png");
				icon = new ImageIcon(url);
			}
		}
		return icon;
	}

	public static void main(final String[] args) {

		final UnhandledException exceptionHandler = UnhandledException
				.getSingelton();
		exceptionHandler.activate();

		SwingUtilities.invokeLater(new Starter(args));
	}

	private final JComboBox combo;

	private final JButton exportButton;

	final GuiCloud guiCloud;

	private final JProgressBar progressBar;

	private final Stack<String> progressMessages;

	public AppFrame() {
		super();

		progressMessages = new Stack<String>();

		UnhandledException.getSingelton().setParent(this);

		final Container content = getContentPane();

		final BorderLayout layout = new BorderLayout();
		content.setLayout(layout);

		final JPanel top = new JPanel();
		content.add(top, BorderLayout.NORTH);
		top.setLayout(new BorderLayout());

		final JButton importButton = new JButton("» Read from Clipboard");
		importButton.addActionListener(new Importer());

		top.add(importButton, BorderLayout.WEST);

		exportButton = new JButton("Write to Clipboard »");
		exportButton.setEnabled(false);
		exportButton.addActionListener(new ExportListener());

		final JButton aboutButton = new JButton("About");
		new About(aboutButton);

		final JPanel ctr = new JPanel();
		ctr.setLayout(new BorderLayout());
		ctr.add(aboutButton, BorderLayout.WEST);
		ctr.add(exportButton, BorderLayout.CENTER);
		top.add(ctr, BorderLayout.EAST);

		combo = new JComboBox();
		combo.setEnabled(false);
		combo.setModel(new DefaultComboBoxModel());
		top.add(combo, BorderLayout.CENTER);

		guiCloud = new GuiCloud(this);
		content.add(new JScrollPane(guiCloud.getCloudComponent()),
				BorderLayout.CENTER);

		progressBar = new JProgressBar();
		setGlassPane(progressBar);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		loadPreferences();
		addWindowListener(new StorePreferenceHandler());

		guiCloud.setTitleAddon(null);
		setIconImage(getIcon().getImage());
		setVisible(true);
	}

	@Override
	public void addItem(final Object item) {
		invokeInEventDispatchThread(new AddItem(item));
	}

	@Override
	public void addItemListener(ItemListener l) {
		combo.addItemListener(l);
	}

	@Override
	public void enableExport(final boolean enable) {
		invokeInEventDispatchThread(new EnableExport(enable));
	}

	@Override
	public ComboBoxModel getModel() {
		return combo.getModel();
	}

	@Override
	public void invokeInEventDispatchThread(Runnable runnable) {
		if (SwingUtilities.isEventDispatchThread()) {
			runnable.run();
		} else {
			SwingUtilities.invokeLater(runnable);
		}
	}

	private void loadPreferences() {
		int width = DEFAULT_WIDTH;
		int height = DEFAULT_HEIGHT;
		int originX = DEFAULT_ORIGIN_X;
		int originY = DEFAULT_ORIGIN_Y;

		try {
			final Preferences prefs = Preferences
					.userNodeForPackage(getClass());
			if (prefs.nodeExists("frame")) {
				final Preferences framePrefs = prefs.node("frame");
				originX = framePrefs.getInt("x", DEFAULT_ORIGIN_X);
				originY = framePrefs.getInt("y", DEFAULT_ORIGIN_Y);
				width = framePrefs.getInt("width", DEFAULT_WIDTH);
				height = framePrefs.getInt("height", DEFAULT_HEIGHT);
			}
		} catch (BackingStoreException exception) {
			// noop
		}

		setSize(width, height);
		setLocation(originX, originY);
	}

	@Override
	public void progressPop() {
		invokeInEventDispatchThread(new ProgressPop());
	}

	@Override
	public void progressPush(final String message) {
		invokeInEventDispatchThread(new ProgressPush(message));
	}

	@Override
	public void progressSwitch(String message) {
		invokeInEventDispatchThread(new ProgressSwitch(message));
	}

	@Override
	public void removeAllItems() {
		invokeInEventDispatchThread(new RemoveAllItems());
	}

	@Override
	public void setSelectedItem(final Object item) {
		invokeInEventDispatchThread(new SetSelectedItem(item));
	}

	void storePreferences() {
		final Preferences prefs = Preferences.userNodeForPackage(getClass());
		final Preferences framePrefs = prefs.node("frame");
		framePrefs.putInt("width", getWidth());
		framePrefs.putInt("height", getHeight());
		framePrefs.putInt("x", getX());
		framePrefs.putInt("y", getY());
		try {
			framePrefs.flush();
		} catch (final Exception e) { // NOPMD
			// ignore
		}
	}
}
