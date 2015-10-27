package com.changhong.fileplore.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class PloreData {
	public static int NAME =1;
	public static int TIME = 2;
	int nDirectory;
	int nFile;
	List<File> files ;
	List<File> nfolders ;
	List<File> nfiles ;

	public PloreData(File folder, boolean hide) {
		this(folder,hide,NAME);
	}
	

	public PloreData(File folder, boolean hide,int type) {
		files = new ArrayList<File>();
		nfolders = new ArrayList<File>();
		nfiles = new ArrayList<File>();
		lodaData(folder, hide,type);
	}

	/**
	 * 
	 * @return 子文件列表
	 */
	public List<File> getfiles() {
		return files;
	}

	/**
	 * 
	 * @return 子文件夹个数
	 */
	public int getnDirectory() {
		return nfolders.size();
	}

	/**
	 * 
	 * @return 子文件个数
	 */
	public int getnFile() {
		return nfiles.size();
	}

	/**
	 * 
	 * @param folder
	 *            父文件夹
	 * @param hide
	 *            是否显示隐藏文件
	 * @return 文件夹内的子文件夹和文件，按字母排序
	 */
	private void lodaData(File folder, boolean hide,int type) {
		File[] names = folder.listFiles();

		for (int i = 0; i < names.length; i++) {
			if (!hide) {
				if (names[i].getName().startsWith("."))
					continue;
			}
			if (names[i].isDirectory()) {
				nfolders.add(names[i]);
			} else
				nfiles.add(names[i]);
		}
		
		if(type ==NAME){
			SortByName sortByName =new SortByName();
			Collections.sort(nfolders, sortByName);
			Collections.sort(nfiles, sortByName);
		}
		else if(type ==TIME){
			SortByTime sortByTime =new SortByTime();
			Collections.sort(nfolders, sortByTime);
			Collections.sort(nfiles, sortByTime);
		}
		
		files.addAll(nfolders);
		files.addAll(nfiles);

	}


	class SortByName implements Comparator<File> {
		@Override
		public int compare(File file1, File file2) {
			if (file1.getName().charAt(0) > file2.getName().charAt(0)) {
				return 1;
			}		
			else return -1;
		}		
	}
	
	
	class SortByTime implements Comparator<File> {
		@Override
		public int compare(File file1, File file2) {
			if (file1.lastModified() > file2.lastModified()) {
				return -1;
			}		
			else return 1;
		}
		
	}
}
