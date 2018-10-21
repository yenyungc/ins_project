package ins.com.ins_project.Home;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eschao.android.widget.elasticlistview.ElasticListView;
import com.eschao.android.widget.elasticlistview.LoadFooter;
import com.eschao.android.widget.elasticlistview.OnLoadListener;
import com.eschao.android.widget.elasticlistview.OnUpdateListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ins.com.ins_project.R;
import ins.com.ins_project.Utils.MainFeedListAdapter;
import ins.com.ins_project.models.Comment;
import ins.com.ins_project.models.Photo;
import ins.com.ins_project.models.UserAccountSettings;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class HomeFragment extends Fragment implements OnUpdateListener, OnLoadListener {

    private static final String TAG = "HomeFragment";

    @Override
    public void onUpdate() {
        Log.d(TAG, "ElasticListView: updating list view...");
        getFollowing();
    }

    @Override
    public void onLoad() {
        Log.d(TAG, "ElasticListView: loading...");
        mListView.notifyLoaded();
    }

    //vars
    private ArrayList<Photo> mPhotos;
    private ArrayList<Photo> mPaginatedPhotos;
    private ArrayList<String> mFollowing;
    private Context mContext;
    private Location geoPoint;
    private FusedLocationProviderClient mFusedLocationClient;

    private ElasticListView mListView;
    private MainFeedListAdapter adapter;
    private int resultsCount = 0;
    private ArrayList<UserAccountSettings> mUserAccountSettings;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mListView = (ElasticListView) view.findViewById(R.id.listView);
        initListViewRefresh();
        getFollowing();
        return view;
    }

    private void initListViewRefresh() {
        mListView.setHorizontalFadingEdgeEnabled(true);
        mListView.setAdapter(adapter);
        mListView.enableLoadFooter(true)
                .getLoadFooter().setLoadAction(LoadFooter.LoadAction.RELEASE_TO_LOAD);
        mListView.setOnUpdateListener(this)
                .setOnLoadListener(this);
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                geoPoint = task.getResult();
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void getFriendsAccountSettings() {
        Log.d(TAG, "getFriendsAccountSettings: getting friends account settings.");

        for (int i = 0; i < mFollowing.size(); i++) {
            Log.d(TAG, "getFriendsAccountSettings: user: " + mFollowing.get(i));
            final int count = i;
            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbname_user_account_settings))
                    .orderByKey()
                    .equalTo(mFollowing.get(i));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Log.d(TAG, "getFriendsAccountSettings: got a user: " + snapshot.getValue(UserAccountSettings.class).getDisplay_name());
                        mUserAccountSettings.add(snapshot.getValue(UserAccountSettings.class));

                        if (count == 0) {
                            JSONObject userObject = new JSONObject();
                            try {
                                userObject.put(getString(R.string.field_display_name), mUserAccountSettings.get(count).getDisplay_name());
                                userObject.put(getString(R.string.field_username), mUserAccountSettings.get(count).getUsername());
                                userObject.put(getString(R.string.field_profile_photo), mUserAccountSettings.get(count).getProfile_photo());
                                userObject.put(getString(R.string.field_user_id), mUserAccountSettings.get(count).getUser_id());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void clearAll() {
        if (mFollowing != null) {
            mFollowing.clear();
        }

        if (mPhotos != null) {
            mPhotos.clear();
            if (adapter != null) {
                adapter.clear();
                adapter.notifyDataSetChanged();
            }
        }

        if (mUserAccountSettings != null) {
            mUserAccountSettings.clear();
        }

        if (mPaginatedPhotos != null) {
            mPaginatedPhotos.clear();
        }

        mFollowing = new ArrayList<>();
        mPhotos = new ArrayList<>();
        mPaginatedPhotos = new ArrayList<>();
        mUserAccountSettings = new ArrayList<>();
    }

    /**
     * //     * Retrieve all user id's that current user is following
     * //
     */
    private void getFollowing() {
        Log.d(TAG, "getFollowing: searching for following");

        clearAll();
        //also add your own id to the list
        try {
            mFollowing.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        try {
            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getActivity().getString(R.string.dbname_following))
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Log.d(TAG, "getFollowing: found user: " + singleSnapshot
                                .child(getString(R.string.field_user_id)).getValue());

                        mFollowing.add(singleSnapshot
                                .child(getString(R.string.field_user_id)).getValue().toString());
                    }
                    getPhotos();
                    getFriendsAccountSettings();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void getPhotos() {
        Log.d(TAG, "getPhotos: getting list of photos");
        try {
            mFusedLocationClient = getFusedLocationProviderClient(mContext);
            getLastLocation();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < mFollowing.size(); i++) {
            final int count = i;
            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getActivity().getString(R.string.dbname_user_photos))
                    .child(mFollowing.get(i))
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(mFollowing.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                        Photo newPhoto = new Photo();
                        Location loc1 = new Location("");
                        String lat1 = singleSnapshot.child("location").child("latitude").getValue().toString();
                        String lon1 = singleSnapshot.child("location").child("longitude").getValue().toString();


                        loc1.setLatitude(Double.parseDouble(lat1));
                        loc1.setLongitude(Double.parseDouble(lon1));

                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                        newPhoto.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        newPhoto.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        newPhoto.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        newPhoto.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        newPhoto.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        newPhoto.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                        Log.d(TAG, "getPhotos: photo: " + newPhoto.getPhoto_id());
                        List<Comment> commentsList = new ArrayList<Comment>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(getString(R.string.field_comments)).getChildren()) {
                            Map<String, Object> object_map = (HashMap<String, Object>) dSnapshot.getValue();
                            Comment comment = new Comment();
                            comment.setUser_id(object_map.get(getString(R.string.field_user_id)).toString());
                            comment.setComment(object_map.get(getString(R.string.field_comment)).toString());
                            comment.setDate_created(object_map.get(getString(R.string.field_date_created)).toString());
                            commentsList.add(comment);
                        }
                        newPhoto.setComments(commentsList);
                        mPhotos.add(newPhoto);
                    }
                    if (count >= mFollowing.size() - 1) {
                        displayPhotos();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: query cancelled.");
                }
            });
        }
    }

    private void displayPhotos() {
        if (mPhotos != null) {

            try {
                //sort for newest to oldest
                Collections.sort(mPhotos, new Comparator<Photo>() {
                    public int compare(Photo o1, Photo o2) {
                        //int i = o2.getDate_created().compareTo(o1.getDate_created());
                        //return i;
                        //return o2.getDate_created().compareTo(o1.getDate_created());
                        float distanceMeters1 = geoPoint.distanceTo(o1.getLocation())/ 1000f;
                        float distanceMeters2 = geoPoint.distanceTo(o2.getLocation())/ 1000f;
                        //return (int) (distanceMeters2-distanceMeters1);
                        int diff = (int) (distanceMeters2-distanceMeters1);
                        return diff;
                    }
                });

                //we want to load 10 at a time. So if there is more than 10, just load 10 to start
                int iterations = mPhotos.size();
                if (iterations > 10) {
                    iterations = 10;
                }

                resultsCount = 0;
                for (int i = 0; i < iterations; i++) {
                    mPaginatedPhotos.add(mPhotos.get(i));
                    resultsCount++;
                    Log.d(TAG, "displayPhotos: adding a photo to paginated list: " + mPhotos.get(i).getPhoto_id());
                }

                adapter = new MainFeedListAdapter(getActivity(), R.layout.layout_mainfeed_listitem, mPaginatedPhotos);
                mListView.setAdapter(adapter);

                // Notify update is done
                mListView.notifyUpdated();
            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "displayPhotos: IndexOutOfBoundsException:" + e.getMessage());
            } catch (NullPointerException e) {
                Log.e(TAG, "displayPhotos: NullPointerException:" + e.getMessage());
            }
        }
    }

    public void displayMorePhotos() {
        Log.d(TAG, "displayMorePhotos: displaying more photos");

        try {
            if (mPhotos.size() > resultsCount && mPhotos.size() > 0) {
                int iterations;
                if (mPhotos.size() > (resultsCount + 10)) {
                    Log.d(TAG, "displayMorePhotos: there are greater than 10 more photos");
                    iterations = 10;
                } else {
                    Log.d(TAG, "displayMorePhotos: there is less than 10 more photos");
                    iterations = mPhotos.size() - resultsCount;
                }

                //add the new photos to the paginated list
                for (int i = resultsCount; i < resultsCount + iterations; i++) {
                    mPaginatedPhotos.add(mPhotos.get(i));
                }
                resultsCount = resultsCount + iterations;
                adapter.notifyDataSetChanged();
            }
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException:" + e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG, "displayPhotos: NullPointerException:" + e.getMessage());
        }
    }
}





















