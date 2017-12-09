//package com.honeywell.iaq.fragment;
//
//import android.app.Fragment;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.res.Configuration;
//import android.os.Bundle;
//import android.preference.PreferenceManager;
//import android.support.v4.view.GravityCompat;
//import android.support.v4.widget.DrawerLayout;
//import android.support.v7.app.ActionBarDrawerToggle;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.support.v7.widget.Toolbar;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.honeywell.iaq.activity.AboutActivity;
//import com.honeywell.iaq.activity.LoginActivity;
//import com.honeywell.iaq.activity.NetworkSetup1Activity;
//import com.honeywell.iaq.adapter.NavigationDrawerAdapter;
//import com.honeywell.iaq.utils.Constants;
//import com.honeywell.iaq.net.HttpClientHelper;
//import com.honeywell.iaq.R;
//import com.honeywell.iaq.utils.Utils;
//import com.loopj.android.http.AsyncHttpResponseHandler;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import cz.msebera.android.httpclient.Header;
//
//
//public class NavigationDrawerFragment extends Fragment {
//
//    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
//    private static final String STATE_TITLE = "title";
//    //    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learn";
//    private ActionBarDrawerToggle mDrawerToggle;
//
//    private DrawerLayout mDrawerLayout;
//    private RecyclerView mRecyclerView;
//    private NavigationDrawerAdapter mAdapter;
//    private View mFragmentContainerView;
//    private int mCurrentSelectedPosition = -1;
//    private boolean mFromSavedInstanceState;
//    private CharSequence mTitle;
//    private Context mMainActivity;
//
//    public NavigationDrawerFragment() {
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        if (savedInstanceState != null) {
//            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
//            mTitle = savedInstanceState.getCharSequence(STATE_TITLE);
//            mFromSavedInstanceState = true;
//        }
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        setHasOptionsMenu(true);
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        mRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_drawer, container, false);
//        mRecyclerView.setClipToPadding(false);
//        mAdapter = new NavigationDrawerAdapter(getActivity(), new NavigationDrawerAdapter.ClickListener() {
//            @Override
//            public void onClick(int index) {
//                selectItem(index);
//            }
//
//            @Override
//            public boolean onLongClick(final int index) {
//                selectItem(index);
//                /*Pins.Item item = mAdapter.getItem(index);
//                Utils.showConfirmDialog(getActivity(), R.string.remove_shortcut,
//                        R.string.confirm_remove_shortcut, item.getDisplay(getActivity()), new CustomDialog.SimpleClickListener() {
//                            @Override
//                            public void onPositive(int which, View view) {
//                                Pins.remove(getActivity(), index);
//                                mAdapter.reload(getActivity());
//                            }
//                        }
//                );*/
//                return false;
//            }
//        });
//        mRecyclerView.setAdapter(mAdapter);
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        mAdapter.setCheckedPos(mCurrentSelectedPosition);
//        return mRecyclerView;
//    }
//
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//    }
//
//    public void setUp(int fragmentId, DrawerLayout drawerLayout, boolean selectDefault, Context mainActivity, final Toolbar toolbar) {
//        mFragmentContainerView = getActivity().findViewById(fragmentId);
//        mDrawerLayout = drawerLayout;
//        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
//        mMainActivity = mainActivity;
//
////        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
////        if (actionBar != null) {
////            actionBar.setDisplayHomeAsUpEnabled(true);
////            actionBar.setHomeButtonEnabled(true);
////        }
//
//        // ActionBarDrawerToggle ties together the the proper interactions
//        // between the navigation drawer and the action bar app icon.
//        mDrawerToggle = new ActionBarDrawerToggle(
//                getActivity(),                    /* host Activity */
//                mDrawerLayout,                    /* DrawerLayout object */
//                toolbar,
//                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
//                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */) {
//
//            @Override
//            public void onDrawerClosed(View drawerView) {
//                super.onDrawerClosed(drawerView);
//                if (!isAdded()) {
//                    return;
//                }
////                getActivity().setTitle(mTitle);
////                getActivity().invalidateOptionsMenu();
//            }
//
//            @Override
//            public void onDrawerOpened(View drawerView) {
//                super.onDrawerOpened(drawerView);
//                if (!isAdded()) {
//                    return;
//                }
//
////                mTitle = getActivity().getTitle();
////                getActivity().setTitle(R.string.navigation_to);
////                CharSequence title = getActivity().getTitle();
//
////                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
////                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
//
////                getActivity().invalidateOptionsMenu();
//
//            }
//        };
//
//        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
//        // per the navigation drawer design guidelines.
//        if (!mFromSavedInstanceState) {
//            mDrawerLayout.openDrawer(mFragmentContainerView);
//        }
//
//        mDrawerLayout.post(new Runnable() {
//            @Override
//            public void run() {
//                mDrawerToggle.syncState();
//                toolbar.setNavigationIcon(R.mipmap.ic_menu);
//            }
//        });
//        mDrawerLayout.setDrawerListener(mDrawerToggle);
//        if (selectDefault) selectItem(mCurrentSelectedPosition);
//    }
//
//    private void selectItem(int position) {
//        if (position < 0) position = 0;
//
//        if (mRecyclerView != null) {
//            mAdapter.setCheckedPos(position);
//        }
//        if (mDrawerLayout != null) {
//            mDrawerLayout.closeDrawer(mFragmentContainerView);
//        }
//
//        if (mCurrentSelectedPosition >= 0) {
//            if (position == 0) {
//                Intent intent = new Intent(mMainActivity, NetworkSetup1Activity.class);
//                startActivity(intent);
//            } else if (position == 1) {
//                Intent intent = new Intent(mMainActivity, AboutActivity.class);
//                startActivity(intent);
//            } else if (position == 2) {
//                logout();
//            }
//        }
//        mCurrentSelectedPosition = position;
//        if (mDrawerLayout != null) {
//            mDrawerLayout.closeDrawers();
//        }
//    }
//
//    /*public void selectFile(File file) {
//        mCurrentSelectedPosition = mAdapter.setCheckedFile(file);
//    }*/
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
//        outState.putCharSequence(STATE_TITLE, mTitle);
//    }
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        mDrawerToggle.onConfigurationChanged(newConfig);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        return mDrawerLayout.getDrawerLockMode(Gravity.START) == DrawerLayout.LOCK_MODE_LOCKED_CLOSED ||
//                mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
//    }
//
//    //    public void reload(boolean open) {
////        Activity act = getActivity();
////        if (act != null) {
////            mAdapter.reload(act);
////            if (open) mDrawerLayout.openDrawer(Gravity.START);
////        }
////    }
//    private void logout() {
//        if (Utils.isNetworkAvailable(mMainActivity)) {
//            final String cookie = Utils.getSharedPreferencesValue(mMainActivity, Constants.KEY_COOKIE, Constants.DEFAULT_COOKIE_VALUE);
//            if (cookie.length() > 0) {
//                final AsyncHttpResponseHandler callback = new AsyncHttpResponseHandler() {
//                    @Override
//                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                        Log.d("logout", "onSuccess");
//                        doLogout();
//                    }
//
//                    @Override
//                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                        Log.d("logout", "onFailure");
//                        if (responseBody != null) {
//                            String responseStr = new String(responseBody, 0, responseBody.length);
//                            Log.d("logout", "responseStr=" + responseStr);
//                        }
////                        new Thread(new Runnable() {
////                            @Override
////                            public void run() {
////                                if (Utils.isNetworkConnected(getActivity())) {
////                                    Utils.sendBroadcast(getActivity(), Const.ACTION_LOGOUT_FAIL);
////                                } else {
////                                    Utils.sendBroadcast(getActivity(), Const.ACTION_INVALID_NETWORK);
////                                }
////                            }
////                        }).start();
////                        Utils.showToast(mMainActivity, getString(R.string.logout_fail));
//                        doLogout();
//                    }
//                };
//
////                new Thread(new Runnable() {
////                    @Override
////                    public void run() {
//                Map<String, String> params = new HashMap<>();
//                params.put(Constants.KEY_TYPE, Constants.TYPE_LOGOUT_USER);
//                HttpClientHelper.newInstance().httpRequest(mMainActivity, Constants.USER_URL, params, HttpClientHelper.COOKIE, callback, HttpClientHelper.POST, cookie);
////                    }
////                }).start();
//            }
//        } else {
//            Utils.showToast(mMainActivity, getString(R.string.no_network));
//        }
//    }
//
//    private void doLogout() {
//        Log.d("doLogout", "Logout");
//        Utils.setSharedPreferencesValue(mMainActivity, Constants.KEY_ACCOUNT, "");
//        Utils.setSharedPreferencesValue(mMainActivity, Constants.KEY_PASSWORD, "");
//
//        Utils.startServiceByAction(mMainActivity, Constants.ACTION_DISCONNECT);
//
//        Intent intent = new Intent(mMainActivity, LoginActivity.class);
//        mMainActivity.startActivity(intent);
//        getActivity().finish();
//    }
//}