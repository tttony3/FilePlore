package com.changhong.fileplore.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.format.Formatter;

public class Utils {
	static ArrayList<Content> results = new ArrayList<Content>();
	static ExecutorService pool;

	/**
	 * 
	 * @return 手机内部存储空间字符串数组（总空间，可用空间，可用百分比）
	 * 
	 */
	public static String[] getPhoneSpace(Context context) {
		StatFs fs = new StatFs("/storage/sdcard0");
		String totalSize = Formatter.formatFileSize(context, (fs.getTotalBytes()));
		String availableSize = Formatter.formatFileSize(context, (fs.getAvailableBytes()));

		return (new String[] { totalSize, availableSize,
				(double) fs.getAvailableBytes() / (double) fs.getTotalBytes() * 100 + "" });
	}

	/**
	 * 
	 * @return sd卡存储空间字符串数组（总空间，可用空间，可用百分比）
	 * 
	 */
	public static String[] getSdSpace(Context context) {
		File file = new File("/storage/sdcard1");
		if (file.exists() && null != file.list() && file.list().length != 0) {
			// if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			StatFs fs = new StatFs("/storage/sdcard1");

			// StatFs fs = new
			// StatFs(Environment.getExternalStorageDirectory().getPath());
			// Android API18之前：fs.getAvailableBlocks()*fs.getBlockSize()
			// Log.e("11", Environment.getExternalStorageDirectory().getPath());
			String totalSize = Formatter.formatFileSize(context, (fs.getTotalBytes()));
			String availableSize = Formatter.formatFileSize(context, (fs.getAvailableBytes()));
			// Log.e("11", fs.getAvailableBytes() + fs.getTotalBytes() + "");
			return (new String[] { totalSize, availableSize,
					(double) fs.getAvailableBytes() / (double) fs.getTotalBytes() * 100 + "" });
		} else
			return (new String[] { "未装载", "未装载", "000" });

	}

	/**
	 * 
	 * @param context
	 * @return Arraylist<content> content包含路径，标题，缩略图
	 */
	public static ArrayList<Content> getVideo(Context context) {
		ArrayList<Content> results = new ArrayList<Content>();
		ContentResolver contentResolver = context.getContentResolver();
		Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Video.Media.DEFAULT_SORT_ORDER);
		cursor.moveToFirst();
		int counter = cursor.getCount();
		for (int j = 0; j < counter; j++) {
			int origId = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));

			Content video = new Content(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)),
					cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)),
					MediaStore.Video.Thumbnails.getThumbnail(contentResolver, origId, Images.Thumbnails.MICRO_KIND,
							null));

			results.add(video);
			cursor.moveToNext();

		}
		cursor.close();
		return results;
	}

	/**
	 * 
	 * @param context
	 * @return Arraylist<content> content包含路径，标题，演唱者
	 */
	public static ArrayList<Content> getMusic(Context context) {
		ArrayList<Content> results = new ArrayList<Content>();
		ContentResolver contentResolver = context.getContentResolver();
		Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		cursor.moveToFirst();
		int counter = cursor.getCount();
		for (int j = 0; j < counter; j++) {
			String albumArt = cursor.getString(0);
			Bitmap bm = null;
			if (albumArt == null) {
				Content content = new Content(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
						cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
						cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)), null);

				results.add(content);
				cursor.moveToNext();
			} else {
				bm = BitmapFactory.decodeFile(albumArt);
				@SuppressWarnings("deprecation")
				BitmapDrawable bmpDraw = new BitmapDrawable(bm);

				Content video = new Content(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
						cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
						cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
						bmpDraw.getBitmap());

				results.add(video);
				cursor.moveToNext();

			}

		}
		cursor.close();
		return results;
	}

	/**
	 * 获取系统内图片相册
	 * 
	 * @param context
	 * @return 返回相册列表的Arraylist<content> content包含 dir:路径，(title:相册名)，缩略图
	 */
	public static ArrayList<Content> getPhotoCata(Context context) {

		ArrayList<Content> results = new ArrayList<Content>();
		ContentResolver contentResolver = context.getContentResolver();
		Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Images.Media.DEFAULT_SORT_ORDER);
		cursor.moveToFirst();
		int counter = cursor.getCount();
		String tmp = "";
		for (int j = 0; j < counter; j++) {
			String dir = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

			if (tmp.equalsIgnoreCase(dir.substring(0, dir.lastIndexOf("/")))) {
				// Log.e("tmp","tmp"+tmp);
				cursor.moveToNext();
				continue;
			}

			tmp = dir.substring(0, dir.lastIndexOf("/"));
			// Log.e("dir", dir+"");
			int origId = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
			String[] path = dir.split("/");
			// Log.e("222",
			// cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
			Content photo = new Content(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)),
					// cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE)),
					path[path.length - 2], MediaStore.Images.Thumbnails.getThumbnail(contentResolver, origId,
							Images.Thumbnails.MICRO_KIND, null),
					origId);

			results.add(photo);
			cursor.moveToNext();

		}
		cursor.close();
		return results;

	}

	public static ArrayList<Content> getDoc(Context context, Boolean reseach) {
		if (!reseach) {
			try {
				results = getObject("result_doc");
			} catch (Exception e2) {
				reSeach("doc");
			} 
				return results;
			

		} else {
			reSeach("doc");
			return results;
		}
	}

	private static void reSeach(String type) {

		pool = Executors.newFixedThreadPool(200);
		results.clear();
		File root = new File("/storage");
		File file = root;
		if (type.equals("doc")) {
			GetResult gr1 = new Utils().new GetResult(file, "doc", 0);
			pool.submit(gr1);
		} else if (type.equals("zip")) {
			GetResult gr1 = new Utils().new GetResult(file, "zip", 0);
			pool.submit(gr1);
		}
		if (type.equals("apk")) {
			GetResult gr1 = new Utils().new GetResult(file, "apk", 0);
			pool.submit(gr1);
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {

			e1.printStackTrace();
		}
		pool.shutdown();
		try {// 等待直到所有任务完成
			pool.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		sortList(results);
		if (type.equals("doc")) {
			saveObject("result_doc", results);
		} else if (type.equals("zip")) {
			saveObject("result_zip", results);
		}
		if (type.equals("apk")) {
			saveObject("result_apk", results);
		}

	}

	public static ArrayList<Content> getApk(Context context, Boolean reseach) {
		if (!reseach) {
			try {
				results = getObject("result_apk");
			} catch (Exception e2) {
				reSeach("apk");

			}
			return results;
		} else
			reSeach("apk");
		return results;
	}

	public static ArrayList<Content> getZip(Context context, Boolean reseach) {
		if (!reseach) {
			try {
				results = getObject("result_zip");
			} catch (Exception e2) {
				reSeach("zip");

			}
			return results;
		} else {
			reSeach("zip");
			return results;
		}
	}

	private static void sortList(List<Content> results) {
		int n = results.size();
		for (int i = 0; i < n - 1; i++)
			for (int j = i + 1; j < n; j++) {
				if (results.get(i).getTitle().charAt(0) > results.get(j).getTitle().charAt(0)) {
					Content tmp = results.remove(i);
					results.add(i, results.remove(j - 1));
					results.add(j, tmp);
				}
			}
		for (int i = 0; i < results.size() - 1; i++)
			for (int j = i + 1; j < results.size(); j++) {
				if (results.get(i).getTitle().equals(results.get(j).getTitle())) {
					results.remove(j);
					j--;
				}
			}

	}

	public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高，注意此处的bitmap为null
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		options.inJustDecodeBounds = false; // 设为 false
		// 计算缩放比
		int h = options.outHeight;
		int w = options.outWidth;
		int beWidth = w / width;
		int beHeight = h / height;
		int be = 1;
		if (beWidth < beHeight) {
			be = beWidth;
		} else {
			be = beHeight;
		}
		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;
		// 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	/**
	 * 使用系统工具打开文件
	 * 
	 * @param file
	 * @return 对应的Intent
	 */
	public static Intent openFile(File file) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		String type = getMIMEType(file);
		intent.setDataAndType(Uri.fromFile(file), type);
		return intent;
	}

	public static String getMIMEType(File file) {
		String type = "";
		String name = file.getName();

		String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
		if (end.equals("m4a") || end.equals("mp3") || end.equals("wav")) {
			type = "audio";
		} else if (end.equals("mp4") || end.equals("3gp")) {
			type = "video";
		} else if (end.equals("jpg") || end.equals("png") || end.equals("jpeg") || end.equals("bmp")
				|| end.equals("gif")) {
			type = "image";
		} else {
			type = "*";
		}
		type += "/*";
		return type;
	}

	class GetResult implements Runnable {
		File file;
		String type;
		int n = 0;

		public GetResult(File file, String type, int n) {
			this.file = file;
			this.type = type;
			this.n = n;
		}

		public void run() {
			if (file.isDirectory() && file.canRead()) {
				String[] files = file.list();
				if (null == files)
					return;
				for (int i = 0; i < files.length; i++) {
					if (n < 2) {
						try {
							pool.execute(new GetResult(new File(file.getPath() + "/" + files[i]), type, n + 1));
						} catch (RejectedExecutionException e) {
							getresult(new File(file.getPath() + "/" + files[i]), type);
						}
					} else
						getresult(new File(file.getPath() + "/" + files[i]), type);
				}

			} else if (file.canRead()) {
				String name = file.getName();
				String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
				if (type.equals("zip")) {
					if (end.equals("zip") || end.equals("rar") || end.equals("7z")) {
						synchronized (results) {
							results.add(new Content(file.getPath(), file.getName(), null,
									new SimpleDateFormat("yyyy/MM/dd").format(file.lastModified()), null));
						}

					}
				} else if (type.equals("doc")) {
					if (end.equals("txt") || end.equals("doc") || end.equals("docx")) {
						synchronized (results) {
							results.add(new Content(file.getPath(), file.getName(), null,
									new SimpleDateFormat("yyyy/MM/dd").format(file.lastModified()), null));
						}
					}
				} else if (type.equals("apk")) {
					if (end.equals("apk")) {
						synchronized (results) {
							results.add(new Content(file.getPath(), file.getName(), null,
									new SimpleDateFormat("yyyy/MM/dd").format(file.lastModified()), null));
						}
					}
				}
			}
		}
	}

	/**
	 * 从指定文件递归获取文件列表
	 * 
	 * @param file
	 * @param type
	 *            "zip","doc","apk"其中之一
	 */
	public static void getresult(File file, String type) {

		if (type.equals("zip")) {
			if (file.isDirectory() && file.canRead()) {
				String[] files = file.list();
				if (null == files)
					return;
				for (int i = 0; i < files.length; i++)
					getresult(new File(file.getPath() + "/" + files[i]), type);

			} else if (file.canRead()) {
				String name = file.getName();
				String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
				if (end.equals("zip") || end.equals("rar") || end.equals("7z")) {
					results.add(new Content(file.getPath(), file.getName(), null,
							new SimpleDateFormat("yyyy/MM/dd").format(file.lastModified()), null));
				}
			}

		} else if (type.equals("doc")) {
			if (file.isDirectory() && file.canRead()) {
				String[] files = file.list();
				if (null == files)
					return;
				for (int i = 0; i < files.length; i++) {
					getresult(new File(file.getPath() + "/" + files[i]), type);

				}

			} else if (file.canRead()) {

				String name = file.getName();
				String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
				if (end.equals("doc") || end.equals("docx") || end.equals("txt")) {
					results.add(new Content(file.getPath(), file.getName(), null,
							new SimpleDateFormat("yyyy/MM/dd").format(file.lastModified()), null));
				}
			}

		} else if (type.equals("apk")) {
			if (file.isDirectory() && file.canRead()) {
				String[] files = file.list();
				if (null == files)
					return;
				for (int i = 0; i < files.length; i++)
					getresult(new File(file.getPath() + "/" + files[i]), type);

			} else if (file.canRead()) {
				String name = file.getName();
				String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
				if (end.equals("apk")) {
					results.add(new Content(file.getPath(), file.getName(), null,
							new SimpleDateFormat("yyyy/MM/dd").format(file.lastModified()), null));
				}
			}

		}

	}

	/**
	 * 从指定文件获得对象
	 * 
	 * @param name
	 *            文件名
	 * 
	 */
	@SuppressWarnings("unchecked")
	static ArrayList<Content> getObject(String name) throws Exception {
		ArrayList<Content> savedArrayList = null;
		File file = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "fileplore"
				+ File.separator + name);
		FileInputStream fileInputStream = null;
		ObjectInputStream objectInputStream = null;

		fileInputStream = new FileInputStream(file.toString());
		objectInputStream = new ObjectInputStream(fileInputStream);
		savedArrayList = (ArrayList<Content>) objectInputStream.readObject();
		objectInputStream.close();
		fileInputStream.close();
		return savedArrayList;

	}

	/**
	 * 
	 * @param name
	 *            : file's name
	 * @param obj
	 *            : Object
	 * 
	 */
	static void saveObject(String name, Object obj) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		File file = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "fileplore"
				+ File.separator + name);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				//
				e.printStackTrace();
			}
		}

		try {
			fos = new FileOutputStream(file);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(obj);
		} catch (Exception e) {
			e.printStackTrace();
			// 这里是保存文件产生异常
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// fos流关闭异常
					e.printStackTrace();
				}
			}
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					// oos流关闭异常
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param type
	 *            : one of
	 *            "result_movie","result_music","result_photo","result_apk",
	 *            "result_doc"，"result_zip"
	 * @param context
	 *            : context
	 * @return counts of type
	 */
	public static int getCount(String type, Context context) {
		ArrayList<Content> results = null;
		int count;
		if (type.equals("result_movie")) {
			results = new ArrayList<Content>();
			ContentResolver contentResolver = context.getContentResolver();
			Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null,
					MediaStore.Video.Media.DEFAULT_SORT_ORDER);
			cursor.moveToFirst();
			count = cursor.getCount();
		} else if (type.equals("result_music")) {
			results = new ArrayList<Content>();
			ContentResolver contentResolver = context.getContentResolver();
			Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
					MediaStore.Video.Media.DEFAULT_SORT_ORDER);
			cursor.moveToFirst();
			count = cursor.getCount();
		} else if (type.equals("result_photo")) {
			results = new ArrayList<Content>();
			ContentResolver contentResolver = context.getContentResolver();
			Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,
					MediaStore.Video.Media.DEFAULT_SORT_ORDER);
			cursor.moveToFirst();
			count = cursor.getCount();
		}

		else {
			try {
				results = getObject(type);
				count = results.size();

			} catch (Exception e) {
				count = 0;
			}
		}
		return count;
	}
/**
 * 
 * @param name 文件名
 * @return 文件类型   one of {"audio","video","image","*"}
 */
	public static String getMIMEType(String name) {
		String type;
		String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
		if (end.equals("m4a") || end.equals("mp3") || end.equals("wav")) {
			type = "audio";
		} else if (end.equals("mp4") || end.equals("3gp")) {
			type = "video";
		} else if (end.equals("jpg") || end.equals("png") || end.equals("jpeg") || end.equals("bmp")
				|| end.equals("gif")) {
			type = "image";
		} else {
			type = "*";
		}
		return type;
		
	}
}
