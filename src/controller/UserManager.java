package controller;

import model.sys.Config;
import model.sys.User;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class UserManager {
    private DiskManager diskManager;
    private int userDatabaseStartBlock; // 用户数据库的起始块ID
    private User currentUser; // 当前用户

    public UserManager(DiskManager diskManager, int userDatabaseStartBlock) {
        this.diskManager = diskManager;
        this.userDatabaseStartBlock = userDatabaseStartBlock;
    }

    public boolean register(String username, String password) {
        // 对密码进行哈希处理
        String hashedPassword = hashPassword(password);

        // 将用户名和密码写入到用户数据库中
        int startBlockId = diskManager.getFreeSpace(1, userDatabaseStartBlock);
        if (startBlockId != 0) {
            diskManager.alloc(startBlockId, 1);
            diskManager.io.writeDisk(startBlockId, 1, username + "\n" + hashedPassword);
            return true;
        } else {
            System.out.println("没有足够的空间来注册新用户");
            return false;
        }
    }

    public boolean login(String username, String password) {
        // 对密码进行哈希处理
        String hashedPassword = hashPassword(password);

        // 从用户数据库中读取用户名和密码
        for (int i = userDatabaseStartBlock; i < Config.BLOCK_COUNT; i++) {
            ByteBuffer buffer = diskManager.io.readDisk(i, 1);
            String content = StandardCharsets.UTF_8.decode(buffer).toString();
            String[] parts = content.split("\n");
            String storedUsername = parts[0];
            String storedPassword = parts[1];

            // 比较用户名和密码
            if (username.equals(storedUsername) && hashedPassword.equals(storedPassword)) {
                this.currentUser = new User(username, hashedPassword); // 登录成功，设置当前用户
                return true;
            }
        }

        return false;
    }

    private String hashPassword(String password) {
        // 这里你可以添加你自己的密码哈希算法
        return password;
    }

    public User getCurrentUser() {
        return this.currentUser;
    }
}
