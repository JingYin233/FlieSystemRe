package view;

import model.sys.FCB;
import view.Config;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class EditView extends JFrame implements DocumentListener {

	private static final long serialVersionUID = 5359647733388619559L;
	private JTextPane textPane;
	private JMenu editMenu; // 编辑菜单项
	JMenuItem editMenuItem;
	JMenuItem noEditMenuItem;
	private FCB dataFCB;

	public boolean saveOnExit = true;
	public boolean edited = false;

	public EditView(FCB fcb, String content) {
		super();

		// initialize
		this.dataFCB = fcb;
		this.textPane = new JTextPane();

		this.configureMenuBar();
		this.configureTextPane(content);

		// Main View
		this.configureJFrame();
	}

	// UI Method
	private void configureJFrame() {
		this.setTitle(this.dataFCB.filename + " - 可编辑");
		this.setSize(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setBackground(Color.WHITE);

		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private void configureMenuBar() {
		// Components
		JMenuBar menuBar;
		JMenu fileMenu;
		JMenu helpMenu;
		JMenuItem helpMenuItem;

		// Create the Menu Bar
		menuBar = new JMenuBar();

		// Build File Menu
		fileMenu = new JMenu("开始");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.setEnabled(false);

		// Build Edit Menu
		//editMenu = new JMenu("编辑");
		//editMenu.setMnemonic(KeyEvent.VK_E);
		//
		//editMenuItem = new JMenuItem("可编辑", KeyEvent.VK_H);
		//editMenu.add(editMenuItem);
		//
		//noEditMenuItem = new JMenuItem("不可编辑", KeyEvent.VK_H);
		//editMenu.add(noEditMenuItem);

		// Build About Menu
		helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);

		// Add Menu Items to Menu "About"
		helpMenuItem = new JMenuItem("Help", KeyEvent.VK_H);
		helpMenu.add(helpMenuItem);

		// Add Menus "File" and "Edit" to Menu Bar
		menuBar.add(fileMenu);
		//menuBar.add(editMenu); // 添加到菜单栏
		menuBar.add(helpMenu);

		// Add Components
		this.setJMenuBar(menuBar);
	}

	private void configureTextPane(String content) {
		this.textPane.setText(content);
		this.textPane.getDocument().addDocumentListener(this);
		this.textPane.setEditable(true); // 默认可编辑
		this.add(this.textPane, BorderLayout.CENTER);
	}

	public String getContent() {
		return this.textPane.getText();
	}

	public FCB getDataFCB() {
		return dataFCB;
	}

	// 新的方法，用于添加监听器到编辑菜单项
	public void addEditMenuActionListener(ActionListener listener) {
		editMenu.addActionListener(listener);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		this.edited = true;
		this.setTitle(this.dataFCB.filename + " - *未保存");
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		this.edited = true;
		this.setTitle(this.dataFCB.filename + " - *未保存");
	}

	@Override
	public void changedUpdate(DocumentEvent e) {

	}

	public JMenu getEditMenu() {
		return editMenu;
	}

	public JTextPane getTextPane() {
		return textPane;
	}

	public JMenuItem getEditMenuItem() {
		return editMenuItem;
	}

	public JMenuItem getNoEditMenuItem() {
		return noEditMenuItem;
	}
}
