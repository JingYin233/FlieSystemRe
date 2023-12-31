package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.*;

import model.sys.Config.FILE_TYPE;
import model.sys.FCB;
import view.*;

import com.google.gson.Gson;

/**
 *
 * @author
 *
 */
public class MainController {

	private MainView view;

	private SystemCore systemCore;

	private boolean isLoggedIn = false;

	/**
	 * 构造函数
	 */
	public MainController() {
		super();

		// Initialize systemCore
		this.systemCore = new SystemCore();

		// UI Methods
		this.configureMainView();
	}

	// UI Methods
	/**
	 * 初始化主界面
	 */
	private void configureMainView() {
		// 根据当前目录文件初始化view
		this.view = new MainView(this.systemCore.currentDir);

		// 添加右键监听
		this.view.addRightClickListener(this.rightClickListener);

		// 添加后退按钮监听
		this.view.addBackButtonActionListener(this.backButtonActionListener);

		// 添加前往按钮监听
		this.view.addGoButtonActionListener(this.goButtonActionListener);

		// 添加关闭事件监听
		this.view.addWindowListener(this.mainWindowListener);

		// 添加登录和注册按钮监听
		this.view.addLoginMenuItemActionListener(this.loginButtonActionListener);
		this.view.addRegisterMenuItemActionListener(this.registerButtonActionListener);

		this.configureContentPanel();

		// 显示当前路径
		this.view.addressTextField.setText(this.systemCore.getCurrentPath());
	}

	/**
	 * 初始化内容面板
	 */
	private void configureContentPanel() {
		// 为每个图标添加监听
		this.addListenerForEachDocumentIcon();
	}

	/**
	 * 显示主界面
	 */
	public void showMainView() {
		this.view.showView();
	}

	/**
	 * 显示编辑界面
	 *
	 * @param fcb
	 *            需要编辑的文件的FCB
	 */
	private void showEditView(FCB fcb) {
		// 弹出Edit View，根据FCB加载
		EditView editView = new EditView(fcb,
				MainController.this.systemCore.readFile(fcb));

		// 如果是创建者且没有读取权限
		if (editView.getDataFCB().CreateName.equals(MainController.this.systemCore.getCurrentUser().getUsername())
				&& !editView.getDataFCB().isCreatorRead()) {
			// 弹出文件已经打开的提示
			JOptionPane.showMessageDialog(null, "权限不足，不能打开（自己）。");
			// 退出方法
			return;
		}

		// 如果是其他用户且没有读取权限
		if (!editView.getDataFCB().CreateName.equals(MainController.this.systemCore.getCurrentUser().getUsername())
				&& !editView.getDataFCB().isOtherRead()) {
			// 弹出文件已经打开的提示
			JOptionPane.showMessageDialog(null, "权限不足，不能打开（其他）。");
			// 退出方法
			return;
		}

		// 如果其他用户正在编辑，无法读取
		if(MainController.this.systemCore.getNewFCB(editView.getDataFCB()).isEdit) {
			// 弹出文件已经打开的提示
			editView.getTextPane().setEditable(false);
			// 退出方法
		}

		// 为Edit Window添加监听
		editView.addWindowListener(MainController.this.editWindowListener);

		// 显示Edit View
		editView.setVisible(true);

	}

	// Listener
	/**
	 * 主界面内容面板右键点击监听
	 */
	private MouseListener rightClickListener = new MouseListener() {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (!isLoggedIn) {
				JOptionPane.showMessageDialog(null, "请先登录！");
				return;
			}
			// Deselect documents
			if (e.getButton() == MouseEvent.BUTTON1) {
				MainController.this.view.deselectDocuments();
			}

			// Popup menu
			if (e.getButton() == MouseEvent.BUTTON3) {
				boolean isRoot = (MainController.this.systemCore.currentDirFCB.fatherBlockId == -1);

				JPopupMenu menu = new JPopupMenu();
				JMenuItem newFileMenu = new JMenuItem("新建文件", KeyEvent.VK_N);
				newFileMenu.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_N, ActionEvent.CTRL_MASK));
				newFileMenu
						.addActionListener(MainController.this.newFileActionListener);
				menu.add(newFileMenu);

				JMenuItem newFolderMenu = new JMenuItem("新建文件夹",
						KeyEvent.VK_F);
				newFolderMenu.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_F, ActionEvent.CTRL_MASK));
				newFolderMenu
						.addActionListener(MainController.this.newFolderActionListener);
				menu.add(newFolderMenu);

				if (isRoot) {
					menu.addSeparator();

					JMenuItem formatMenu = new JMenuItem("格式化");
					formatMenu
							.addActionListener(MainController.this.formatMenuActionListener);
					menu.add(formatMenu);
				}

				menu.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {

		}

		@Override
		public void mouseReleased(MouseEvent e) {

		}

		@Override
		public void mouseEntered(MouseEvent e) {

		}

		@Override
		public void mouseExited(MouseEvent e) {

		}

	};

	/**
	 * 图标点击监听，包括单击选中，双击打开，右键弹出菜单
	 */
	private MouseListener documentIconPanelMouseListener = new MouseListener() {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (!isLoggedIn) {
				JOptionPane.showMessageDialog(null, "请先登录！");
				return;
			}
			// Select current document
			MainController.this.view.deselectDocuments();
			DocumentIconPanel d = (DocumentIconPanel) e.getComponent();
			d.setSelected(true);

			// 获取文件FCB
			FCB fcb = MainController.this.systemCore.getFCB(d.getFilename(),
					d.getType());

			// Double click
			if (e.getClickCount() == 2) {
				// 判断双击的类型
				if (d.getType() == FILE_TYPE.DIRECTORY) {
					// 双击文件夹
					// model进入下一级文件夹
					MainController.this.systemCore.enterDir(d.getFilename());

					// 重绘view
					MainController.this.refreshView();

				} else {
					// 双击文件
					// 显示编辑窗口
					MainController.this.showEditView(fcb);
				}

				System.out.println("Double Click");
			}

			// Right click
			if (e.getButton() == MouseEvent.BUTTON3) {
				JPopupMenu documentMenu = new JPopupMenu();
				JMenuItem openMenuItem = new JMenuItem("打开", KeyEvent.VK_O);
				MainController.this.openMenuActionListener.fcb = fcb;
				openMenuItem
						.addActionListener(MainController.this.openMenuActionListener);
				documentMenu.add(openMenuItem);

				if (d.getType() == FILE_TYPE.FILE) {
					JMenuItem editMenuItem = new JMenuItem("编辑",
							KeyEvent.VK_E);
					MainController.this.editMenuActionListener.fcb = fcb;
					editMenuItem
							.addActionListener(MainController.this.editMenuActionListener);
					documentMenu.add(editMenuItem);
				}

				JMenuItem renameMenuItem = new JMenuItem("重命名",
						KeyEvent.VK_R);
				MainController.this.renameMenuActionListener.fcb = fcb;
				renameMenuItem
						.addActionListener(MainController.this.renameMenuActionListener);
				documentMenu.add(renameMenuItem);



				documentMenu.addSeparator();

				JMenuItem deleteMenuItem = new JMenuItem("删除",
						KeyEvent.VK_D);
				MainController.this.deleteMenuActionListener.fcb = fcb;
				deleteMenuItem
						.addActionListener(MainController.this.deleteMenuActionListener);
				documentMenu.add(deleteMenuItem);
				documentMenu.addSeparator();

				JMenuItem getInfoMenuItem = new JMenuItem("属性",
						KeyEvent.VK_I);
				MainController.this.getInfoMenuActionListener.fcb = fcb;
				getInfoMenuItem
						.addActionListener(MainController.this.getInfoMenuActionListener);
				documentMenu.add(getInfoMenuItem);

				JMenuItem changeAuthorityMenuItem = new JMenuItem("修改权限",
						KeyEvent.VK_A);
				MainController.this.changeAuthorityMenuActionListener.fcb = fcb;
				changeAuthorityMenuItem.addActionListener(MainController.this.changeAuthorityMenuActionListener);
				documentMenu.add(changeAuthorityMenuItem);

				documentMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {

		}

		@Override
		public void mouseReleased(MouseEvent e) {

		}

		@Override
		public void mouseEntered(MouseEvent e) {

		}

		@Override
		public void mouseExited(MouseEvent e) {

		}

	};


	/**
	 * 监听新建文件的按钮
	 */
	private ActionListener newFileActionListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// 新建文件
			// 获取文件名
			String filename = (String) JOptionPane.showInputDialog(
					MainController.this.view,
					"Please enter your new file's name:", "New file",
					JOptionPane.INFORMATION_MESSAGE);

			if (filename == null) {
				// 用户取消
				return;
			}

			// 不允许文件名为空
			while (filename.equals("")) {
				filename = (String) JOptionPane
						.showInputDialog(
								MainController.this.view,
								"文件名不能为空 \n请输入文件名",
								"新文件", JOptionPane.WARNING_MESSAGE);

				if (filename == null) {
					// 用户取消
					return;
				}
			}

			if (MainController.this.systemCore.createFile(filename)) {
				// 添加到model成功，即创建文件成功
				// 添加到view
				DocumentIconPanel d = new DocumentIconPanel(FILE_TYPE.FILE,
						filename);
				d.addMouseListener(MainController.this.documentIconPanelMouseListener);
				MainController.this.view.addDocument(d);
			} else {
				// 创建文件失败
				// 可能是有重名，也可能空间不够
				// 弹出错误信息框
				JOptionPane
						.showMessageDialog(
								MainController.this.view,
								"可能错误的原因:\n1. 文件名 \""
										+ filename
										+ "\" 已存在\n2. 存储空间不足，请清理一些文件",
								"无法新建文件", JOptionPane.ERROR_MESSAGE);
			}

		}
	};

	/**
	 * 监听新建文件夹的按钮
	 */
	private ActionListener newFolderActionListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// 新建文件夹
			// 获取文件名
			String filename = (String) JOptionPane.showInputDialog(
					MainController.this.view,
					"请输入新文件夹名", "新建文件夹",
					JOptionPane.INFORMATION_MESSAGE);

			if (filename == null) {
				// 用户取消
				return;
			}

			// 不允许文件名为空
			while (filename.equals("")) {
				filename = (String) JOptionPane
						.showInputDialog(
								MainController.this.view,
								"文件夹名不能为空！ \n请输入文件夹名:",
								"新建文件夹", JOptionPane.WARNING_MESSAGE);

				if (filename == null) {
					// 用户取消
					return;
				}
			}

			if (MainController.this.systemCore.createDir(filename)) {
				// 添加到model成功，即创建文件夹成功
				// 添加到view
				DocumentIconPanel d = new DocumentIconPanel(
						FILE_TYPE.DIRECTORY, filename);
				d.addMouseListener(MainController.this.documentIconPanelMouseListener);
				MainController.this.view.addDocument(d);
			} else {
				// 创建文件夹失败
				// 可能是有重名，也可能空间不够
				// 弹出错误信息框
				JOptionPane
						.showMessageDialog(
								MainController.this.view,
								"可能错误的原因:\n1. 文件夹名 \""
										+ filename
										+ "\" 已存在\n2.空间不足",
								"新建文件夹失败", JOptionPane.ERROR_MESSAGE);
			}

		}

	};

	private class CustomActionListener implements ActionListener {
		public FCB fcb = null;

		@Override
		public void actionPerformed(ActionEvent e) {

		}
	}

	/**
	 * 监听修改权限的按钮
	 */
	private CustomActionListener changeAuthorityMenuActionListener = new CustomActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			// 创建复选框
			JCheckBox creatorRead = new JCheckBox("创建者读权限");
			JCheckBox creatorWrite = new JCheckBox("创建者写权限");
			JCheckBox otherRead = new JCheckBox("其他用户读权限");
			JCheckBox otherWrite = new JCheckBox("其他用户写权限");

			// 创建一个包含复选框的对象数组
			Object[] message = {
					"请选择新的权限：",
					creatorRead,
					creatorWrite,
					otherRead,
					otherWrite
			};

			// 弹出对话框
			int option = JOptionPane.showConfirmDialog(null, message, "修改权限", JOptionPane.OK_CANCEL_OPTION);

			if (option == JOptionPane.OK_OPTION) {
				// 用户点击了确定按钮，你可以在这里更新FCB的权限
				this.fcb.setAuthority(
						creatorRead.isSelected(),  // 创建者读权限
						creatorWrite.isSelected(),  // 创建者写权限
						otherRead.isSelected(),  // 其他用户读权限
						otherWrite.isSelected()   // 其他用户写权限
				);
			}
		}
	};

	/**
	 * "编辑"按钮按键监听
	 */
	private CustomActionListener editMenuActionListener = new CustomActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			MainController.this.showEditView(this.fcb);
		}

	};

	/**
	 * "打开"按钮按键监听
	 */
	private CustomActionListener openMenuActionListener = new CustomActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (this.fcb.type == FILE_TYPE.DIRECTORY) {
				// model进入下一级文件夹
				MainController.this.systemCore.enterDir(this.fcb.filename);

				// 重绘view
				MainController.this.refreshView();
			} else {
				// 显示编辑窗口
				MainController.this.showEditView(fcb);
			}
		}

	};

	/**
	 * "删除"按钮按键监听
	 */
	private CustomActionListener deleteMenuActionListener = new CustomActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// 获取用户的选择
			int result = JOptionPane.showConfirmDialog(
					MainController.this.view,
					"确认删除？",
					"删除", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);

			if (result == 0) {
				// 确定删除
				if (this.fcb.type == FILE_TYPE.DIRECTORY) {
					// model删除文件夹
					MainController.this.systemCore.deleteDir(this.fcb.filename);
				} else {
					// model删除文件
					MainController.this.systemCore
							.deleteFile(this.fcb.filename);
				}

				// 重绘view
				MainController.this.refreshView();
			}
		}

	};

	/**
	 * "重命名"按钮按键监听
	 */
	private CustomActionListener renameMenuActionListener = new CustomActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// 获取文件名
			String filename = (String) JOptionPane.showInputDialog(
					MainController.this.view, "文件夹名:", "重命名",
					JOptionPane.INFORMATION_MESSAGE, null, null,
					this.fcb.filename);

			if (filename == null) {
				// 用户取消
				return;
			}

			// 不允许文件名为空
			while (filename.equals("")) {
				filename = (String) JOptionPane.showInputDialog(
						MainController.this.view, "文件夹名:", "重命名",
						JOptionPane.INFORMATION_MESSAGE, null, null,
						this.fcb.filename);
			}

			this.fcb.filename = filename;

			Gson gson = new Gson();

			// 更新文件FCB
			MainController.this.systemCore.updateFCB(this.fcb);

			// 更新当前目录的目录文件
			MainController.this.systemCore.updateFile(
					MainController.this.systemCore.currentDirFCB,
					gson.toJson(MainController.this.systemCore.currentDir));

			// 刷新界面
			MainController.this.refreshView();
		}

	};

	/**
	 * "格式化"按钮按键监听
	 */
	private ActionListener formatMenuActionListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// 获取用户的选择
			int result = JOptionPane
					.showConfirmDialog(
							MainController.this.view,
							"所有数据都会被删除且无法恢复\n确定格式化硬盘？",
							"格式化!!", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);

			if (result == 0) {
				// 确定格式化
				MainController.this.systemCore.format();
			}

			// 刷新界面
			MainController.this.refreshView();
		}

	};

	/**
	 * "属性"按钮按键监听
	 */
	private CustomActionListener getInfoMenuActionListener = new CustomActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println(this.fcb);
			// 弹出详细信息框
			JOptionPane.showMessageDialog(MainController.this.view,
					MainController.this.systemCore.getFileInfo(this.fcb),
					"通知", JOptionPane.PLAIN_MESSAGE);
		}
	};

	/**
	 * "后退"按钮按键监听
	 */
	private ActionListener backButtonActionListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// 点击后退按钮

			if (MainController.this.systemCore.leaveDir()) {
				// 确认回到上一级目录
				// 重绘view
				MainController.this.refreshView();
			} else {
				// 根目录
				JOptionPane.showMessageDialog(MainController.this.view,
						"已经是根文件夹，无法后退", "警告",
						JOptionPane.WARNING_MESSAGE);
			}
		}

	};

	/**
	 * "前往"按钮按键监听
	 */
	private ActionListener goButtonActionListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// 获取地址栏地址
			String path = MainController.this.view.addressTextField.getText();

			if (path.charAt(0) != '/') {
				// 路径非法
				JOptionPane.showMessageDialog(MainController.this.view,
						"路径不存在", "警告",
						JOptionPane.WARNING_MESSAGE);

				return;
			}

			// 获取当前地址
			String currentPath = MainController.this.systemCore
					.getCurrentPath();

			// 拆分
			String[] pathArray = path.split("/");
			String[] currentPathArray = currentPath.split("/");

			if (pathArray.length == 0) {
				pathArray = new String[1];
				pathArray[0] = "";
			}

			if (currentPathArray.length == 0) {
				currentPathArray = new String[1];
				currentPathArray[0] = "";
			}

			// 对比
			int length = Math.min(pathArray.length, currentPathArray.length);
			int i = 0;
			for (i = 0; i < length; i++) {
				if (!pathArray[i].equals(currentPathArray[i])) {
					break;
				}
			}

			if (pathArray.length == currentPathArray.length && i == length) {
				// 两拆分后的数组相同，即路径没变化，无需继续下面的步骤
				return;
			}

			// 计算
			// 向后退的步数
			int stepOut = currentPathArray.length - i;
			// 向前进的步数
			int stepIn = pathArray.length - i;

			// 临时保存当前目录FCB和目录文件
			FCB fcb = MainController.this.systemCore.currentDirFCB;
			FCB[] dir = MainController.this.systemCore.currentDir;

			// 开始后退再前进
			boolean success = true;
			// 后退
			for (int j = 0; j < stepOut; j++) {
				MainController.this.systemCore.leaveDir();
			}

			// 前进
			for (int j = 0; j < stepIn; j++) {
				if (!MainController.this.systemCore.enterDir(pathArray[i++])) {
					success = false;
					break;
				}
			}

			if (success) {
				// 成功跳转
				// 刷新界面
				MainController.this.refreshView();
			} else {
				// 目录不存在
				JOptionPane.showMessageDialog(MainController.this.view,
						"目录不存在", "警告",
						JOptionPane.WARNING_MESSAGE);

				// 恢复
				MainController.this.systemCore.currentDirFCB = fcb;
				MainController.this.systemCore.currentDir = dir;
				MainController.this.systemCore.countFiles();
			}

		}

	};

	/**
	 * 主窗口的监听，主要是关闭事件
	 */
	private WindowListener mainWindowListener = new WindowListener() {

		@Override
		public void windowOpened(WindowEvent e) {

		}

		@Override
		public void windowClosing(WindowEvent e) {

		}

		@Override
		public void windowClosed(WindowEvent e) {
			System.out.println("窗口已关闭");

			// 系统核心退出
			MainController.this.systemCore.exit();
		}

		@Override
		public void windowIconified(WindowEvent e) {

		}

		@Override
		public void windowDeiconified(WindowEvent e) {

		}

		@Override
		public void windowActivated(WindowEvent e) {

		}

		@Override
		public void windowDeactivated(WindowEvent e) {

		}

	};

	/**
	 * 编辑窗口的监听
	 */
	private WindowListener editWindowListener = new WindowListener() {

		@Override
		public void windowOpened(WindowEvent e) {

			EditView editView = (EditView) e.getComponent();

			System.out.println("按钮被点击了！");

			editView.getDataFCB().isEdit = true;

			Gson gson = new Gson();

			// 更新文件FCB
			MainController.this.systemCore.updateFCB(editView.getDataFCB());

			// 更新当前目录的目录文件
			MainController.this.systemCore.updateFile(
					MainController.this.systemCore.currentDirFCB,
					gson.toJson(MainController.this.systemCore.currentDir));

			MainController.this.systemCore.update();

			// 获取"编辑"菜单项
			//JMenuItem editMenuItem = editView.getEditMenuItem();
			//
			//JMenuItem noEditMenuItem = editView.getNoEditMenuItem();

			//// 为"编辑"菜单项添加监听器
			//noEditMenuItem.addActionListener(new ActionListener() {
			//	@Override
			//	public void actionPerformed(ActionEvent e) {
			//		// 在这里添加你想要执行的代码
			//		System.out.println("按钮被点击了！");
			//		editView.getTextPane().setEditable(false);
			//
			//		editView.getDataFCB().isEdit = false;
			//
			//		Gson gson = new Gson();
			//
			//		// 更新文件FCB
			//		MainController.this.systemCore.updateFCB(editView.getDataFCB());
			//
			//		// 更新当前目录的目录文件
			//		MainController.this.systemCore.updateFile(
			//				MainController.this.systemCore.currentDirFCB,
			//				gson.toJson(MainController.this.systemCore.currentDir));
			//
			//		MainController.this.systemCore.update();
			//	}
			//});
			//
			//// 为"编辑"菜单项添加监听器
			//editMenuItem.addActionListener(new ActionListener() {
			//	@Override
			//	public void actionPerformed(ActionEvent e) {
			//		// 在这里添加你想要执行的代码
			//		System.out.println("按钮被点击了！");
			//		editView.getTextPane().setEditable(true);
			//
			//		editView.getDataFCB().isEdit = true;
			//
			//		Gson gson = new Gson();
			//
			//		// 更新文件FCB
			//		MainController.this.systemCore.updateFCB(editView.getDataFCB());
			//
			//		// 更新当前目录的目录文件
			//		MainController.this.systemCore.updateFile(
			//				MainController.this.systemCore.currentDirFCB,
			//				gson.toJson(MainController.this.systemCore.currentDir));
			//
			//		MainController.this.systemCore.update();
			//	}
			//});

		}

		@Override
		public void windowClosing(WindowEvent e) {

			EditView editView = (EditView) e.getComponent();

			if (!editView.edited) {
				// 文本没有变化
				return;
			}

			// 获取用户的选择
			int result = JOptionPane.showConfirmDialog(editView,
					"保存退出？", "退出",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);

			if (result == 0) {
				// 退出并保存
				editView.saveOnExit = true;
				editView.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			} else if (result == 1) {
				// 退出不保存
				editView.saveOnExit = false;
				editView.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			} else {
				// 取消
				editView.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			}
		}

		@Override
		public void windowClosed(WindowEvent e) {
			System.out.println("编辑窗口关闭");

			EditView editView = (EditView) e.getComponent();

			editView.getDataFCB().isEdit = false;

			Gson gson = new Gson();

			// 更新文件FCB
			MainController.this.systemCore.updateFCB(editView.getDataFCB());

			// 更新当前目录的目录文件
			MainController.this.systemCore.updateFile(
					MainController.this.systemCore.currentDirFCB,
					gson.toJson(MainController.this.systemCore.currentDir));

			MainController.this.systemCore.update();

			//// 如果是创建者且没有读取权限
			//if (editView.getDataFCB().CreateName.equals(MainController.this.systemCore.getCurrentUser().getUsername())
			//		&& !editView.getDataFCB().isCreatorWrite()) {
			//	// 弹出文件已经打开的提示
			//	JOptionPane.showMessageDialog(null, "权限不足，不能保存（自己）。");
			//	// 退出方法
			//	return;
			//}
			//
			//// 如果是其他用户且没有读取权限
			//if (!editView.getDataFCB().CreateName.equals(MainController.this.systemCore.getCurrentUser().getUsername())
			//		&& !editView.getDataFCB().isOtherWrite()) {
			//	// 弹出文件已经打开的提示
			//	JOptionPane.showMessageDialog(null, "权限不足，不能保存（其他）。");
			//	// 退出方法
			//	return;
			//}
			//
			//if (MainController.this.systemCore.getNewFCB(editView.getDataFCB()).isEdit) {
			//	// 弹出文件已经打开的提示
			//	JOptionPane.showMessageDialog(null, "文件正在被编辑，不能保存。");
			//	// 退出方法
			//	return;
			//}

			if (editView.edited && editView.saveOnExit) {
				// 保存文件
				MainController.this.systemCore.updateFile(
						editView.getDataFCB(), editView.getContent());
			}

		}

		@Override
		public void windowIconified(WindowEvent e) {

		}

		@Override
		public void windowDeiconified(WindowEvent e) {

		}

		@Override
		public void windowActivated(WindowEvent e) {

			EditView editView = (EditView) e.getComponent();

			System.out.println(editView.getDataFCB().filename + "正在编辑");

		}

		@Override
		public void windowDeactivated(WindowEvent e) {

			EditView editView = (EditView) e.getComponent();

			System.out.println(editView.getDataFCB().filename + "退出编辑");

		}

	};

	/**
	 * 根据当前FCB对应的目录文件刷新View
	 */
	private void refreshView() {
		this.view.reloadContent(this.systemCore.currentDir);

		// 重新添加监听
		this.addListenerForEachDocumentIcon();

		// 更新当前路径
		this.view.addressTextField.setText(MainController.this.systemCore
				.getCurrentPath());
	}

	/**
	 * 为每个图标添加监听
	 */
	private void addListenerForEachDocumentIcon() {
		this.view
				.addDocumentIconPanelMouseListener(this.documentIconPanelMouseListener);
	}

	/**
	 * 编辑登录按钮的方法
	 */
	private ActionListener loginButtonActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			showLoginView();
		}
	};

	/**
	 * 编辑注册按钮的方法
	 */
	private ActionListener registerButtonActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			showRegisterView();
		}
	};

	// Show login view
	private void showLoginView() {
		LoginView loginView = new LoginView(view);
		loginView.addLoginButtonActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String username = loginView.getUsername();
				String password = loginView.getPassword();
				if (MainController.this.systemCore.loginUser(username, password)) {
					JOptionPane.showMessageDialog(null, "登录成功！");
					loginView.dispose();
					isLoggedIn = true;
				} else {
					JOptionPane.showMessageDialog(null, "用户名或密码错误！");
				}
			}
		});
		loginView.setVisible(true);
	}

	// Show register view
	private void showRegisterView() {
		RegisterView registerView = new RegisterView(view);
		registerView.addRegisterButtonActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String username = registerView.getUsername();
				String password = registerView.getPassword();
				if (MainController.this.systemCore.registerUser(username, password)) {
					JOptionPane.showMessageDialog(null, "注册成功！");
					registerView.dispose();
				} else {
					JOptionPane.showMessageDialog(null, "注册失败！");
				}
			}
		});
		registerView.setVisible(true);
	}
}
