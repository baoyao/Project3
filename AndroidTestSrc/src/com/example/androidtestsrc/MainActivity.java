package com.example.androidtestsrc;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class MainActivity extends Activity implements
		android.view.GestureDetector.OnGestureListener, SurfaceHolder.Callback {

	private final String DIR_URL = "mnt/usb_storage/USB_DISK1/udisk0/ad_play";
	private final String VIDEO_URL = DIR_URL + "/videos";
	private final String IMAGE_URL = DIR_URL + "/images";
	private final String TEXT_DIR = DIR_URL + "/text";
	private final String TEXT_URL = TEXT_DIR + "/ad.txt";

	private String[] videos = null;// 视频列表
	private List<Bitmap> images = new ArrayList<Bitmap>();// 图片列表
	private String adText = "";

	private int videoIndex = -1;

	private GestureDetector gestureDetector = null;

	private ViewFlipper viewFlipper = null;

	private Activity mActivity = null;

	private SurfaceView mSurfaceView;

	private MediaPlayer mMediaPlayer;

	private TextView adTextView, dateTextView1, dateTextView2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getImages();

		mActivity = this;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		readText();
		initView();
		initdate();
		getTimeInnerval();
		super.onResume();
		
		MediaScannerConnection.scanFile(this, new String[] { VIDEO_URL,IMAGE_URL,TEXT_DIR }, null, null);
	}

	@Override
	protected void onPause() {
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		stopInnerval();
		super.onPause();
	}

	private void initView() {
		adTextView = (TextView) findViewById(R.id.ad_text);
		adTextView.setText(adText);
		dateTextView1 = (TextView) findViewById(R.id.date1);
		dateTextView2 = (TextView) findViewById(R.id.date2);

		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);

		mSurfaceView.getHolder().addCallback(this);

		viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
		gestureDetector = new GestureDetector(this); // 声明检测手势事件

		for (int i = 0; i < images.size(); i++) { // 添加图片源
			ImageView iv = new ImageView(this);
			// iv.setImageResource(images[i]);
			iv.setImageBitmap(images.get(i));
			iv.setScaleType(ImageView.ScaleType.FIT_XY);
			viewFlipper.addView(iv, new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT));
		}

		viewFlipper.setAutoStart(true); // 设置自动播放功能（点击事件，前自动播放）
		viewFlipper.setFlipInterval(3000);
		if (viewFlipper.isAutoStart() && !viewFlipper.isFlipping()) {
			viewFlipper.startFlipping();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		viewFlipper.stopFlipping(); // 点击事件后，停止自动播放
		viewFlipper.setAutoStart(false);
		return gestureDetector.onTouchEvent(event); // 注册手势事件
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (e2.getX() - e1.getX() > 120) { // 从左向右滑动（左进右出）
			Animation rInAnim = AnimationUtils.loadAnimation(mActivity,
					R.anim.push_right_in); // 向右滑动左侧进入的渐变效果（alpha 0.1 -> 1.0）
			Animation rOutAnim = AnimationUtils.loadAnimation(mActivity,
					R.anim.push_right_out); // 向右滑动右侧滑出的渐变效果（alpha 1.0 -> 0.1）

			viewFlipper.setInAnimation(rInAnim);
			viewFlipper.setOutAnimation(rOutAnim);
			viewFlipper.showPrevious();
			return true;
		} else if (e2.getX() - e1.getX() < -120) { // 从右向左滑动（右进左出）
			Animation lInAnim = AnimationUtils.loadAnimation(mActivity,
					R.anim.push_left_in); // 向左滑动左侧进入的渐变效果（alpha 0.1 -> 1.0）
			Animation lOutAnim = AnimationUtils.loadAnimation(mActivity,
					R.anim.push_left_out); // 向左滑动右侧滑出的渐变效果（alpha 1.0 -> 0.1）

			viewFlipper.setInAnimation(lInAnim);
			viewFlipper.setOutAnimation(lOutAnim);
			viewFlipper.showNext();
			return true;
		}
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		play();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	private void play() {
		File file = getVideosUrl();
		if (file == null) {
			return;
		}

		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
		mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
		mMediaPlayer.setDisplay(mSurfaceView.getHolder());
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			mMediaPlayer.setDataSource(file.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		mMediaPlayer.prepareAsync();
	}

	private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {
		@Override
		public void onPrepared(MediaPlayer mp) {
			mp.start();
		}
	};

	private OnCompletionListener mOnCompletionListener = new OnCompletionListener() {

		@Override
		public void onCompletion(MediaPlayer mp) {
			play();
		}

	};

	private File getVideosUrl() {
		Log.v("tt", "VIDEO_URL: " + VIDEO_URL + " videoIndex: " + videoIndex);
		File dir = new File(VIDEO_URL);
		if (!dir.exists()) {
			dir.mkdirs();
			MediaScannerConnection.scanFile(this, new String[] { VIDEO_URL,IMAGE_URL,TEXT_DIR }, null, null);
			return null;
		}
		videos = dir.list();
		Log.v("tt", "videos: " + Arrays.toString(videos));
		if ((videoIndex + 1) >= videos.length) {
			videoIndex = -1;
		}

		videoIndex++;
		if (videos != null && videos.length != 0) {
			return new File(VIDEO_URL + "/" + videos[videoIndex]);
		} else {
			return null;
		}
	}

	private void getImages() {
		Log.v("tt", "IMAGE_URL: " + IMAGE_URL);
		File dir = new File(IMAGE_URL);
		if (!dir.exists()) {
			dir.mkdirs();
			MediaScannerConnection.scanFile(this, new String[] { VIDEO_URL,IMAGE_URL,TEXT_DIR }, null, null);
			return;
		}
		String[] files = dir.list();
		if (files != null && files.length != 0) {
			images.clear();
			for (int i = 0; i < files.length; i++) {
				String fileUrl = IMAGE_URL + "/" + files[i];
				Bitmap bitmap = BitmapFactory.decodeFile(fileUrl);
				images.add(bitmap);
			}
			System.gc();
		}
	}

	public void readText() {
		Log.v("tt", "TEXT_DIR: " + TEXT_DIR);
		File dir = new File(TEXT_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
			MediaScannerConnection.scanFile(this, new String[] { VIDEO_URL,IMAGE_URL,TEXT_DIR }, null, null);
			return;
		}
		Log.v("tt", "TEXT_URL: " + TEXT_URL);

		String textUrl="";
		if(dir.list().length>0){
			textUrl=TEXT_DIR+"/"+dir.list()[0];
			adText = readSDFile(textUrl);
		}
	}

	public String readSDFile(String fileUrl) {

		File file = new File(fileUrl);
		BufferedReader reader = null;
		FileInputStream fis = null;
		BufferedInputStream in = null;
		String text = "";

		try {
			fis = new FileInputStream(file);
			in = new BufferedInputStream(fis);
			in.mark(4);
			byte[] first3bytes = new byte[3];
			in.read(first3bytes);// 找到文档的前三个字节并自动判断文档类型。
			in.reset();
			if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB
					&& first3bytes[2] == (byte) 0xBF) {// utf-8

				reader = new BufferedReader(new InputStreamReader(in, "utf-8"));

			} else if (first3bytes[0] == (byte) 0xFF
					&& first3bytes[1] == (byte) 0xFE) {

				reader = new BufferedReader(
						new InputStreamReader(in, "unicode"));
			} else if (first3bytes[0] == (byte) 0xFE
					&& first3bytes[1] == (byte) 0xFF) {

				reader = new BufferedReader(new InputStreamReader(in,
						"utf-16be"));
			} else if (first3bytes[0] == (byte) 0xFF
					&& first3bytes[1] == (byte) 0xFF) {

				reader = new BufferedReader(new InputStreamReader(in,
						"utf-16le"));
			} else {

				reader = new BufferedReader(new InputStreamReader(in, "GBK"));
			}
			String str = reader.readLine();
			/*
			 * int line = 0; while (str != null) { while (line < 1) {//line < 3
			 * text = text + str + "\r\n"; str = reader.readLine(); line++; }
			 * line = 0; }
			 */
			while (str != null) {
				text = text + str + "\r\n";
				str = reader.readLine();
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (fis != null) {
					fis.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return text;
	}

	private void initdate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		String date1 = sdf.format(new Date());
		dateTextView1.setText(date1);

		SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
		String date2 = sdf2.format(new Date());

		Calendar calendar = Calendar.getInstance();
		int week = calendar.get(Calendar.DAY_OF_WEEK);
		String weekStr = "";
		switch (week) {
		case Calendar.MONDAY:
			weekStr = "Monday";
			break;
		case Calendar.TUESDAY:
			weekStr = "Tuesday";
			break;
		case Calendar.WEDNESDAY:
			weekStr = "Wednesday";
			break;
		case Calendar.THURSDAY:
			weekStr = "Thursday";
			break;
		case Calendar.FRIDAY:
			weekStr = "Friday";
			break;
		case Calendar.SATURDAY:
			weekStr = "Saturday";
			break;
		case Calendar.SUNDAY:
			weekStr = "Sunday";
			break;
		default:
			weekStr = "Sunday";
			break;
		}

		dateTextView2.setText(weekStr + "  " + date2);
	}

	private void getTimeInnerval() {
		mGetTimeHandler.sendEmptyMessageDelayed(MSG_GET_TIME, 1000);
	}

	private void stopInnerval() {
		mGetTimeHandler.removeMessages(MSG_GET_TIME);
	}

	private final int MSG_GET_TIME = 1;

	Handler mGetTimeHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			initdate();
			mGetTimeHandler.sendEmptyMessageDelayed(MSG_GET_TIME, 1000);
		};
	};

}
