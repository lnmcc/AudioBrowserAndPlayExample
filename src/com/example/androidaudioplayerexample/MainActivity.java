package com.example.androidaudioplayerexample;

import java.io.File;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends ListActivity implements LoaderCallbacks<Cursor> {
	
	//设置俩种选项状态：选择专辑或者选择歌曲
	public static int STATE_SELECT_ALBUM = 0;
	public static int STATE_SELECT_SONG = 1;
	//默认当前状态为选者专辑
	int currentState = STATE_SELECT_ALBUM;
	//查询专辑
	static final String[] AlbumColumns = {
		MediaStore.Audio.Albums._ID,
		MediaStore.Audio.Albums.ALBUM,
	};
	//查询歌曲
	static final String[] songColumns = {
		MediaStore.Audio.Media.DATA,
		MediaStore.Audio.Media._ID,
		MediaStore.Audio.Media.TITLE,
		MediaStore.Audio.Media.DISPLAY_NAME,
		MediaStore.Audio.Media.MIME_TYPE,
	};
	
	Cursor cursor;
	SimpleCursorAdapter mAdapter;
	Button returnBtn;
	MediaPlayer mediaPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		returnBtn = (Button)findViewById(R.id.returnBtn);
		returnBtn.setOnClickListener(new View.OnClickListener() {
			//处理返回上级目录
			@Override
			public void onClick(View v) {
				if(currentState == STATE_SELECT_ALBUM) {
					//do nothing
				} else if(currentState == STATE_SELECT_SONG) {
					QueryAlbum();
					currentState = STATE_SELECT_ALBUM;
				}
			}
		});
		
	    //getLoaderManager().initLoader(0, null, this);
		QueryAlbum();
	}
	
	private void QueryAlbum() {
		
		//显示专辑查询结果
		String[] displayFields = new String[] {MediaStore.Audio.Albums.ALBUM};
		int[] displayViews = new int[] {android.R.id.text1};
		
	    mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, null, displayFields, displayViews, 0);
	    Bundle b = new Bundle();
	    b.putInt("state", STATE_SELECT_ALBUM);
	    getLoaderManager().restartLoader(0, b, this);
	    setListAdapter(mAdapter);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		
		Loader<Cursor> loader = null;
		if(args.getInt("state") == STATE_SELECT_ALBUM) {
			loader =  new CursorLoader(this, MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, AlbumColumns, null, null, null);
		} else if(args.getInt("state") == STATE_SELECT_SONG) {
			String where = args.getString("where");
			String whereVal[] = args.getStringArray("whereVal");
			String orderBy = args.getString("orderBy");
			loader =  new CursorLoader(this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songColumns, where, whereVal, orderBy);
		}
		return loader;
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		cursor = data;
		mAdapter.swapCursor(data);
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		if(currentState == STATE_SELECT_ALBUM) {
			
			if(cursor.moveToPosition(position)) {
				String where = MediaStore.Audio.Media.ALBUM + "=?";
				String whereVal[] = {cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM))};
				String orderBy = MediaStore.Audio.Media.TITLE;
				String[] displayFileds = {MediaStore.Audio.Media.DISPLAY_NAME};
				int[] displayViews = new int[] {android.R.id.text1};
				
				mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, null, displayFileds, displayViews, 0);
				Bundle b = new Bundle();
				b.putInt("state", STATE_SELECT_SONG);
				b.putString("where", where);
				b.putStringArray("whereVal", whereVal);
				b.putString("orderBy", orderBy);
				getLoaderManager().restartLoader(0, b, this);
				setListAdapter(mAdapter);
				//进入专辑后，下一次选择模式一定是选择歌曲
				currentState = STATE_SELECT_SONG;
			}
		} else if(currentState == STATE_SELECT_SONG) {
			
			if(cursor.moveToPosition(position)) {
				int fileColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
				int mimeTypeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE);
				String audioFilePath = cursor.getString(fileColumn);
				String mimeType = cursor.getString(mimeTypeColumn);
				File audioFile = new File(audioFilePath);
				
				//打开下面3行可以使用外部播放器播放音频
				//Intent i = new Intent(android.content.Intent.ACTION_VIEW);
				//i.setDataAndType(Uri.fromFile(audioFile), mimeType);
				//startActivity(i);
				
				//使用MediaPlayer播放音频
				if(null != mediaPlayer && mediaPlayer.isPlaying()) {
					mediaPlayer.stop();
					mediaPlayer.release();
				}
				mediaPlayer = MediaPlayer.create(this, Uri.fromFile(audioFile));
				mediaPlayer.start();
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(null != mediaPlayer) {
			mediaPlayer.start();
			mediaPlayer.release();
		}
	}
}
