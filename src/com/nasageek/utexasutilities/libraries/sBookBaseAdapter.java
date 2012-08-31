package com.nasageek.utexasutilities.libraries;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class sBookBaseAdapter extends BaseAdapter {

	//saved Book Base adapter. Very similar to BookBaseAdapter. different in OnClick, and Button text

	public static ArrayList<Book> bookArrayList;
	private LayoutInflater mInflater;
	Context context;

	public sBookBaseAdapter(Context con, ArrayList<Book> results) {
		bookArrayList = results;
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

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.book_layout2, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.titleList);
			// holder.image = (ImageView) convertView
			// .findViewById(R.id.bookImageList);
			// holder.location = (TextView) convertView
			// .findViewById(R.id.locationList);
			holder.publication = (TextView) convertView
			.findViewById(R.id.publicationList);
			// holder.callNo = (TextView) convertView
			// .findViewById(R.id.callNoList);
			// holder.status = (TextView) convertView
			// .findViewById(R.id.statusList);
			holder.other = (TextView) convertView
			.findViewById(R.id.otherInfoList);
			holder.saveBook = (ImageButton) convertView
			.findViewById(R.id.saveBookButton);

			holder.copiesTable = (TableLayout) convertView
			.findViewById(R.id.copiesTable);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final Book b = bookArrayList.get(position);

		// ImageDownloader imgD = new ImageDownloader();
		// imgD.download(b.imageURL, holder.image);

		holder.title.setText(b.title);
		// holder.location.setText(b.location);
		holder.publication.setText(b.publication);
		// holder.callNo.setText(b.callNo);
		// holder.status.setText(b.currentStatus);
		holder.other.setText(b.otherFields);

		int min = (int) Math.min(
				Math.min(b.location.size(), b.currentStatus.size()),
				b.callNo.size());

		holder.copiesTable.removeAllViews();

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
//		holder.saveBook.setText("Delete");
		holder.saveBook.setImageDrawable(context.getResources().getDrawable(R.drawable.cross));
		holder.saveBook.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("unused")
			@Override
			public void onClick(View v) {
				if (shared.LOGGINGLEVEL>0) Log.i("sBookBaseAdapter", "removing on click. the arrayList: " + bookArrayList.toString());
				bookArrayList.remove(position);
				notifyDataSetChanged();
			}
		});

		return convertView;
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
