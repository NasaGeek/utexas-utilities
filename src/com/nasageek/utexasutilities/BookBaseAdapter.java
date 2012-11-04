package com.nasageek.utexasutilities;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.nasageek.utexasutilities.R;

public class BookBaseAdapter extends BaseAdapter {

	private static ArrayList<Book> bookArrayList;
	private static ArrayList<Book> savedBooks;
	private LayoutInflater mInflater;
	Context context;

	public BookBaseAdapter(Context con, ArrayList<Book> results) {
		bookArrayList = results;
		savedBooks = new ArrayList<Book>();
		mInflater = LayoutInflater.from(con);
		context = con;

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return bookArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return bookArrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@SuppressWarnings("unused")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		try{
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.book_layout2, null);
				holder = new ViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.titleList);
				// holder.image = (ImageView) convertView
				// .findViewById(R.id.bookImageList);
				// holder.location = (TextView) convertView
				// .findViewById(R.id.locationList);
				holder.publication = (TextView) convertView.findViewById(R.id.publicationList);
				// holder.callNo = (TextView) convertView
				// .findViewById(R.id.callNoList);
				// holder.status = (TextView) convertView
				// .findViewById(R.id.statusList);
				holder.other = (TextView) convertView.findViewById(R.id.otherInfoList);
				holder.saveBook = (ImageButton) convertView.findViewById(R.id.saveBookButton);

				holder.copiesTable = (TableLayout) convertView.findViewById(R.id.copiesTable);

				convertView.setTag(holder);
				// convertView.setClickable(true);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final Book b = bookArrayList.get(position);
			b.cleanUp();

			// ImageDownloader imgD = new ImageDownloader();
			// imgD.download(b.imageURL, holder.image);

			holder.title.setText(b.numberinorder + ". " + b.title);
			// holder.location.setText(b.location);
			holder.publication.setText(b.publication);
			// holder.callNo.setText(b.callNo);
			// holder.status.setText(b.currentStatus);
			holder.other.setText(b.otherFields);

			int min = (int) Math.min(
					Math.min(b.location.size(), b.currentStatus.size()),
					b.callNo.size());

			holder.copiesTable.removeAllViews();
			// holder.copiesTable.setStretchAllColumns(true);
			// holder.copiesTable.setShrinkAllColumns(true);
			holder.copiesTable.setGravity(Gravity.CENTER);

			for (int i = 0; i < min; i++) {

				TableRow row = (TableRow) mInflater.inflate(
						R.layout.single_copy_info_row, null);
				((TextView) row.findViewById(R.id.location)).setText(b.location
						.get(i));
				((TextView) row.findViewById(R.id.callNo)).setText(b.callNo.get(i));
				((TextView) row.findViewById(R.id.status)).setText(b.currentStatus
						.get(i));

				holder.copiesTable.addView(row);
			}
			holder.saveBook.setFocusable(false); // needed to make listview
			// clickable
			holder.saveBook.setOnClickListener(new OnClickListener() {
				@SuppressWarnings({ "unchecked" })
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					try {
						String FILENAME = "Saved_Books";
//						boolean first = true;

						try {
							FileInputStream fileIn = context
							.openFileInput(FILENAME);
							ObjectInputStream in = new ObjectInputStream(fileIn);
							Object nextObject = null;
							// if (in.available()>0)
							nextObject = in.readObject();
							savedBooks.clear();
							// instead of using savedBooks, just read from file
							// everytime
							if (nextObject != null
									&& nextObject instanceof ArrayList<?>) {
								if (shared.LOGGINGLEVEL>0) Log.i("BookBaseAdapter", "class of nextObject: "
										+ nextObject.getClass().toString());
								savedBooks = (ArrayList<Book>) nextObject;
								if (shared.LOGGINGLEVEL>0) Log.i("BookBaseAdapter", savedBooks.toString());
							}
							in.close();
							fileIn.close();
						} catch (java.io.FileNotFoundException e) {
							// do nothing. just make new file - do not have read
							// from old
						}
						savedBooks.add(b);
						FileOutputStream fos = null;
						ObjectOutputStream oos = null;
						fos = context
						.openFileOutput(FILENAME, Context.MODE_PRIVATE); // change
						// this
						// back
						// to
						// append
						oos = new ObjectOutputStream(fos);
						oos.writeObject(savedBooks);
						oos.close();
						fos.close();

					} catch (Exception e) {
						if (shared.LOGGINGLEVEL>0) Log.e("BookBaseAdapter", "exception in onClick", e);
					}
				}
			});

			return convertView;
		}
		catch(Exception e)
		{
			if (shared.LOGGINGLEVEL>0) Log.e("BookBaseAdapter", "inside getView", e);
			return null;
		}
	}

	static class ViewHolder {
		TableLayout copiesTable;
		// ImageView image;
		TextView title;
		TextView location;
		TextView publication;
		TextView callNo;
		TextView status;
		TextView other;
		TextView number;
		ImageButton saveBook;

	}

}
