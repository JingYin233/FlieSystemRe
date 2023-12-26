package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginView extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;

    public LoginView(JFrame parent) {
        super(parent, "登录", true);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        JLabel usernameLabel = new JLabel("用户名: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(usernameLabel, cs);

        usernameField = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(usernameField, cs);

        JLabel passwordLabel = new JLabel("密码: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(passwordLabel, cs);

        passwordField = new JPasswordField(20);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(passwordField, cs);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        loginButton = new JButton("登录");

        cancelButton = new JButton("取消");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        JPanel bp = new JPanel();
        bp.add(loginButton);
        bp.add(cancelButton);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    public String getUsername() {
        return usernameField.getText().trim();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    public void addLoginButtonActionListener(ActionListener listener) {
        loginButton.addActionListener(listener);
    }
}
