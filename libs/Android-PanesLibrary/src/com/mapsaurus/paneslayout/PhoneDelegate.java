package com.mapsaurus.paneslayout;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v4.widget.SlidingPaneLayout.PanelSlideListener;
import android.view.View;

import com.actionbarsherlock.view.MenuItem;

import java.lang.ref.WeakReference;

public class PhoneDelegate extends ActivityDelegate implements OnBackStackChangedListener {

	private SlidingPaneLayout spl;
	
	public PhoneDelegate(PanesActivity a) {
		super(a);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (findViewById(R.id.content_frame) == null)
			setContentView(R.layout.phone_layout);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		
		int menuOffset = getResources().getDimensionPixelSize(R.dimen.menu_offset);
		
		spl = (SlidingPaneLayout) findViewById(R.id.slidingpane_layout);
		spl.setShadowResource(R.drawable.shadow_left);
		spl.setParallaxDistance(menuOffset);
		spl.setCoveredFadeColor(Color.parseColor("#88000000"));
		spl.setSliderFadeColor(Color.parseColor("#00000000"));
		spl.setPanelSlideListener(new PanelSlideListener() {
			
			@Override
			public void onPanelSlide(View panel, float offset) {
				
			}
			
			@Override
			public void onPanelOpened(View panel) {
				
			}
			
			@Override
			public void onPanelClosed(View panel) {
				
			}
		});
		
		FragmentManager fm = getSupportFragmentManager();
		fm.addOnBackStackChangedListener(this);
	}

	/* *********************************************************************
	 * Interactions with menu/back/etc
	 * ********************************************************************* */

	@Override
	public boolean onBackPressed() {
		FragmentManager fm = getSupportFragmentManager();

		if (!spl.isOpen()) {
			if (fm.getBackStackEntryCount() > 0) {
				return false;
			} else {
				spl.openPane();
				return true;
			}
		} else {
			getActivity().finish();
			return true;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (!spl.isOpen())
				spl.openPane();
			else
				onBackPressed();
			return true;
		}
		return false;
	}
	
	@Override
	public void onBackStackChanged() {

	}

	/* *********************************************************************
	 * Adding, removing, getting fragments
	 * ********************************************************************* */

	/**
	 * Save the menu fragment. The reason to do this is because sometimes when
	 * we need to retrieve a fragment, that fragment has not yet been added.
	 */
	private WeakReference<Fragment> wMenuFragment = new WeakReference<Fragment>(null);

	@Override
	public void addFragment(Fragment prevFragment, Fragment newFragment) {
		boolean addToBackStack = false;
		if (prevFragment == getMenuFragment() || prevFragment == null) {
			clearFragments();
		} else {
			addToBackStack = true;
		}

		if (spl.isOpen()) spl.closePane();

		if (newFragment != null) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
					android.R.anim.fade_in, android.R.anim.fade_out);
			ft.replace(R.id.content_frame, newFragment);
			if (addToBackStack) ft.addToBackStack(newFragment.toString());
			ft.commit();
		}
	}

	@Override
	public void clearFragments() {
		FragmentManager fm = getSupportFragmentManager();
		for(int i = 0; i < fm.getBackStackEntryCount(); i ++)    
			fm.popBackStack();
	}

	@Override
	public void setMenuFragment(Fragment f) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.menu_frame, f);
		ft.commit();

		wMenuFragment = new WeakReference<Fragment>(f);
	}

	@Override
	public Fragment getMenuFragment() {
		Fragment f = wMenuFragment.get();
		if (f == null) {
			f = getSupportFragmentManager().findFragmentById(R.id.menu_frame);
			wMenuFragment = new WeakReference<Fragment>(f);
		}
		return f;
	}

	@Override
	public Fragment getTopFragment() {
		return getSupportFragmentManager().findFragmentById(R.id.content_frame);
	}

	@Override
	public void showMenu() {
		spl.openPane();
	}
	
	@Override
	public void showContent() {
		spl.closePane();
	}

}
