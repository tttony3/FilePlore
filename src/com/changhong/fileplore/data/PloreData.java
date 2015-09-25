package com.changhong.fileplore.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PloreData {
	int nDirectory;
	int nFile;
	List<File> files = new ArrayList<File>();

	/**
	 * 
	 * @return 子文件夹个数
	 */
	public int getnDirectory() {
		return nDirectory;
	}

	/**
	 * 
	 * @return 子文件个数
	 */
	public int getnFile() {
		return nFile;
	}

	/**
	 * 
	 * @param folder
	 *            父文件夹
	 * @return 文件夹内的子文件夹和文件，按字母排序
	 */
	public List<File> lodaData(File folder) {
		files.clear();
		nDirectory = 0;
		nFile = 0;
		String path = folder.getPath();
		String[] names = folder.list();
		for (int i = 0; i < names.length; i++) {
			files.add(new File("/" + path + "/" + names[i]));
		}

		for (int i = 0, j = 0; j < files.size(); j++, i++) {
			if (!files.get(i).isDirectory()) {
				files.add(files.remove(i));
				i--;
				continue;
			}
			nDirectory++;
		}

		for (int i = 0; i < nDirectory - 1; i++)
			for (int j = i + 1; j < nDirectory; j++) {
				if (files.get(i).getName().charAt(0) > files.get(j).getName().charAt(0)) {
					File tmp = files.remove(i);
					files.add(i, files.remove(j - 1));
					files.add(j, tmp);
				}
			}
		for (int i = nDirectory; i < files.size() - 1; i++)
			for (int j = i + 1; j < files.size(); j++) {
				if (files.get(i).getName().charAt(0) > files.get(j).getName().charAt(0)) {
					File tmp = files.remove(i);
					files.add(i, files.remove(j - 1));
					files.add(j, tmp);
				}
			}
		nFile = files.size() - nDirectory;
		return files;
	}
}
