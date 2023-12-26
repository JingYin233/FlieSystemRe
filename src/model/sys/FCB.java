package model.sys;

import java.io.Serializable;
import java.util.Date;

import model.sys.Config.FILE_TYPE;

/**
 * 
 * @author Tom Hu
 * 
 */
public class FCB implements Serializable {

	private static final long serialVersionUID = -5262771405010721496L;

	public String filename; // 文件名
	public FILE_TYPE type; // FILE 或 DORECTORY
	public String address; // 文件地址
	public Date createdDate;
	public Date updatedDate;
	public int fatherBlockId; // 指向上一级FCB
	public int size; // 当前文件控制块所对应的数据区块个数，即占用空间
	public int dataStartBlockId; // 数据区Block的id
	public int blockId; // 当前FCB所在Block的id
	public String CreateName=""; // 创建者账户名
	private int authority=15;  // 权限
	public boolean isRead=false; // 文件是否可以阅读
	public boolean isEdit=false; // 文件是否可以编辑
	public FCB(String filename, int fatherBlockId, FILE_TYPE type, int size,
			int dataStartBlockId, int blockId) {
		this.filename = filename;
		this.fatherBlockId = fatherBlockId;
		this.type = type;
		this.size = size;
		this.createdDate = new Date();
		this.updatedDate = (Date) this.createdDate.clone();
		this.dataStartBlockId = dataStartBlockId;
		this.blockId = blockId;
		this.isRead = isRead;
		this.isEdit = isEdit;
	}

	// 设置权限
	public void setAuthority(boolean creatorRead, boolean creatorWrite, boolean otherRead, boolean otherWrite) {
		authority = 0;
		if (creatorRead) {
			authority |= 8;  // 1000
		}
		if (creatorWrite) {
			authority |= 4;  // 0100
		}
		if (otherRead) {
			authority |= 2;  // 0010
		}
		if (otherWrite) {
			authority |= 1;  // 0001
		}
	}

	// 设置权限
	public void setupAuthority(boolean creatorRead, boolean creatorWrite, boolean otherRead, boolean otherWrite) {
		authority = 0;
		if (creatorRead) {
			authority |= 8;  // 1000
		}
		if (creatorWrite) {
			authority |= 4;  // 0100
		}
		if (otherRead) {
			authority |= 2;  // 0010
		}
		if (otherWrite) {
			authority |= 1;  // 0001
		}
	}

	// 获取权限
	public Integer getAuthority() {
		return authority;
	}

	// 判断权限
	public boolean isCreatorRead() {
		return (authority & 8) != 0;  // 1000
	}

	public boolean isCreatorWrite() {
		return (authority & 4) != 0;  // 0100
	}

	public boolean isOtherRead() {
		return (authority & 2) != 0;  // 0010
	}

	public boolean isOtherWrite() {
		return (authority & 1) != 0;  // 0001
	}
}
