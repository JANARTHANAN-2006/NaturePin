package com.example.naturepin;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.tabs.TabLayout;

import org.maplibre.android.MapLibre;
import org.maplibre.android.WellKnownTileServer;
import org.maplibre.android.annotations.Icon;
import org.maplibre.android.annotations.IconFactory;
import org.maplibre.android.annotations.MarkerOptions;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.location.LocationComponentActivationOptions;
import org.maplibre.android.location.modes.CameraMode;
import org.maplibre.android.location.modes.RenderMode;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;

import java.util.ArrayList;
import java.util.List;

import io.github.jan.supabase.auth.user.UserInfo;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private MapLibreMap map;
    private RecyclerView recyclerView, rvNotifications, rvProfileGrid;
    private PostAdapter postAdapter;
    private NotificationAdapter notificationAdapter;
    private ProfileGridAdapter profileGridAdapter;
    private FloatingActionButton btnAddPin, btnMyLocation;
    private List<Post> allPosts = new ArrayList<>();
    private List<Post> userPosts = new ArrayList<>();
    private String currentUserUid;
    private BottomNavigationView bottomNav;
    private ViewGroup contentFrame;
    private TabLayout profileTabs;

    // Profile UI
    private View profileLayout;
    private ImageView ivProfilePic;
    private TextView tvProfileUsername, tvProfileBio, tvPostCount, tvFriendCount;
    private Button btnEditProfile, btnActionFriend;
    private ImageButton ibProfileOptions;
    private String profilePicUri = "";
    private String viewedUserUid; 

    // Filter
    private Spinner spinnerFilter;
    private String currentFilter = "All";

    private String uri1 = "", uri2 = "", uri3 = "";
    private ImageView iv1, iv2, iv3, ivEditProfilePreview;
    private int currentPickingImage = 0;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        String uriString = imageUri.toString();
                        if (currentPickingImage == 1) {
                            uri1 = uriString;
                            Glide.with(this).load(imageUri).into(iv1);
                        } else if (currentPickingImage == 2) {
                            uri2 = uriString;
                            Glide.with(this).load(imageUri).into(iv2);
                        } else if (currentPickingImage == 3) {
                            uri3 = uriString;
                            Glide.with(this).load(imageUri).into(iv3);
                        } else if (currentPickingImage == 100) {
                            profilePicUri = uriString;
                            if (ivEditProfilePreview != null) {
                                Glide.with(this).load(imageUri).circleCrop().into(ivEditProfilePreview);
                            }
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserInfo user = SupabaseHelper.INSTANCE.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        currentUserUid = user.getId();
        viewedUserUid = currentUserUid;

        MapLibre.getInstance(this, null, WellKnownTileServer.MapLibre);
        setContentView(R.layout.activity_main);

        contentFrame = findViewById(R.id.content_frame);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(allPosts, this);
        postAdapter.setOnProfileClickListener(uid -> {
            viewedUserUid = uid;
            bottomNav.setSelectedItemId(R.id.nav_profile);
        });
        recyclerView.setAdapter(postAdapter);

        SupabaseHelper.INSTANCE.getMyFriends(currentUserUid, friendUids -> {
            postAdapter.setFriendUids(friendUids);
            return null;
        });

        SupabaseHelper.INSTANCE.getUserProfile(currentUserUid, profile -> {
            boolean needsProfile = profile == null
                    || profile.getUsername() == null || profile.getUsername().trim().isEmpty()
                    || profile.getBio() == null || profile.getBio().trim().isEmpty();
            if (needsProfile) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Finish your profile to continue", Toast.LENGTH_SHORT).show();
                    showEditProfileBottomSheet();
                });
            }
            return null;
        });

        rvNotifications = findViewById(R.id.rv_notifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        notificationAdapter = new NotificationAdapter(new ArrayList<>());
        rvNotifications.setAdapter(notificationAdapter);

        profileLayout = findViewById(R.id.profile_layout);
        initProfileUI();
        initFilterUI();

        btnAddPin = findViewById(R.id.btn_add_pin);
        btnMyLocation = findViewById(R.id.btn_my_location);

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId != R.id.nav_profile) {
                viewedUserUid = currentUserUid;
            }
            showFragmentWithAnimation(itemId);
            return true;
        });

        btnMyLocation.setOnClickListener(v -> requestFreshLocation());
        btnAddPin.setOnClickListener(v -> showAddPostDialog());

        loadPosts();
    }
    
    private void requestFreshLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        if (map != null && map.getLocationComponent().isLocationComponentActivated()) {
            Location lastKnown = map.getLocationComponent().getLastKnownLocation();
            if (lastKnown != null) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(lastKnown.getLatitude(), lastKnown.getLongitude()), 16), 1200);
            } else {
                LocationServices.getFusedLocationProviderClient(this)
                        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(location.getLatitude(), location.getLongitude()), 16), 1200);
                            } else {
                                Toast.makeText(this, "Could not find your location. Please ensure GPS is on.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    private void showFragmentWithAnimation(int itemId) {
        View targetView = null;
        if (itemId == R.id.nav_map) targetView = mapView;
        else if (itemId == R.id.nav_posts) targetView = recyclerView;
        else if (itemId == R.id.nav_notifications) targetView = rvNotifications;
        else if (itemId == R.id.nav_profile) targetView = profileLayout;

        if (targetView != null && targetView.getVisibility() == View.VISIBLE && itemId != R.id.nav_profile) return;

        TransitionSet set = new TransitionSet()
                .addTransition(new Fade())
                .setDuration(300);
        TransitionManager.beginDelayedTransition(contentFrame, set);
        
        mapView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        rvNotifications.setVisibility(View.GONE);
        profileLayout.setVisibility(View.GONE);

        if (itemId == R.id.nav_map) {
            if (btnAddPin.getVisibility() != View.VISIBLE) {
                btnAddPin.show();
                btnMyLocation.show();
                spinnerFilter.setVisibility(View.VISIBLE);
                spinnerFilter.setAlpha(0f);
                spinnerFilter.animate().alpha(1f).setDuration(300).start();
            }
        } else {
            btnAddPin.hide();
            btnMyLocation.hide();
            spinnerFilter.animate().alpha(0f).setDuration(200).withEndAction(() -> spinnerFilter.setVisibility(View.GONE)).start();
        }

        if (targetView != null) {
            targetView.setVisibility(View.VISIBLE);
        }

        if (itemId == R.id.nav_map) {
            loadPosts();
        } else if (itemId == R.id.nav_posts) {
            loadPosts();
        } else if (itemId == R.id.nav_notifications) {
            loadNotifications();
        } else if (itemId == R.id.nav_profile) {
            loadUserProfile(viewedUserUid);
        }
    }

    private void initFilterUI() {
        spinnerFilter = findViewById(R.id.spinner_filter);
        String[] filters = {"All", "#nature", "#hiking", "#sunset", "#adventure", "#waterfall"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, filters) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setTextColor(ContextCompat.getColor(getContext(), R.color.text_black));
                return v;
            }
        };
        spinnerFilter.setAdapter(adapter);
        
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = filters[position];
                displayMarkers();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void initProfileUI() {
        ivProfilePic = profileLayout.findViewById(R.id.iv_profile_pic);
        tvProfileUsername = profileLayout.findViewById(R.id.tv_profile_username);
        tvProfileBio = profileLayout.findViewById(R.id.tv_profile_bio);
        tvPostCount = profileLayout.findViewById(R.id.tv_post_count);
        tvFriendCount = profileLayout.findViewById(R.id.tv_friend_count);
        btnEditProfile = profileLayout.findViewById(R.id.btn_edit_profile);
        btnActionFriend = profileLayout.findViewById(R.id.btn_action_friend);
        ibProfileOptions = profileLayout.findViewById(R.id.ib_profile_options);
        rvProfileGrid = profileLayout.findViewById(R.id.rv_profile_grid);
        profileTabs = profileLayout.findViewById(R.id.profile_tabs);

        rvProfileGrid.setLayoutManager(new GridLayoutManager(this, 3));
        profileGridAdapter = new ProfileGridAdapter(new ArrayList<>(), post -> showPostPreview(post));
        rvProfileGrid.setAdapter(profileGridAdapter);

        profileTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterUserPosts(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnEditProfile.setOnClickListener(v -> showEditProfileBottomSheet());
        btnActionFriend.setOnClickListener(v -> handleBefriendAction());
        ibProfileOptions.setOnClickListener(this::showProfileOptionsMenu);
    }

    private void filterUserPosts(int tabIndex) {
        List<Post> filtered = new ArrayList<>();
        for (Post p : userPosts) {
            boolean hasLocation = p.latitude != 0 || p.longitude != 0;
            if (tabIndex == 0 && !hasLocation) {
                filtered.add(p);
            } else if (tabIndex == 1 && hasLocation) {
                filtered.add(p);
            }
        }
        profileGridAdapter.setPosts(filtered);
    }

    private void showProfileOptionsMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenu().add("Delete Post");
        popup.getMenu().add("Sign Out");
        
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Sign Out")) {
                SupabaseHelper.INSTANCE.signOut();
                getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                return true;
            } else if (item.getTitle().equals("Delete Post")) {
                showDeletePostList();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showDeletePostList() {
        SupabaseHelper.INSTANCE.getUserPosts(currentUserUid, posts -> {
            List<String> titles = new ArrayList<>();
            List<String> ids = new ArrayList<>();
            for (Post p : posts) {
                titles.add(p.title);
                ids.add(p.id);
            }

            if (titles.isEmpty()) {
                Toast.makeText(this, "No posts to delete.", Toast.LENGTH_SHORT).show();
                return null;
            }

            new AlertDialog.Builder(this)
                .setTitle("Select post to delete")
                .setItems(titles.toArray(new String[0]), (dialog, which) -> {
                    confirmDeletion(ids.get(which));
                })
                .show();
            return null;
        });
    }

    private void confirmDeletion(String postId) {
        new AlertDialog.Builder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete this pin?")
            .setPositiveButton("Delete", (dialog, which) -> {
                SupabaseHelper.INSTANCE.deletePost(postId, success -> {
                    if (success) {
                        Toast.makeText(this, "Pin deleted", Toast.LENGTH_SHORT).show();
                        loadPosts();
                    }
                    return null;
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void handleBefriendAction() {
        if (viewedUserUid == null || viewedUserUid.equals(currentUserUid)) return;
        SupabaseHelper.INSTANCE.befriend(currentUserUid, viewedUserUid, success -> {
            if (success) {
                Toast.makeText(this, "Befriended", Toast.LENGTH_SHORT).show();
                SupabaseHelper.INSTANCE.getMyFriends(currentUserUid, friendUids -> {
                    postAdapter.setFriendUids(friendUids);
                    return null;
                });
                loadUserProfile(viewedUserUid);
            } else {
                Toast.makeText(this, "Could not befriend right now", Toast.LENGTH_SHORT).show();
            }
            return null;
        });
    }

    private void showEditProfileBottomSheet() {
        profilePicUri = ""; // Reset stale value to avoid accidental upload of old selected photos
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottomsheet_edit_profile, null);

        com.google.android.material.imageview.ShapeableImageView ivPic = view.findViewById(R.id.iv_edit_profile_pic);
        EditText etUsername = view.findViewById(R.id.et_edit_username);
        EditText etBio = view.findViewById(R.id.et_edit_bio);
        com.google.android.material.button.MaterialButton btnSave = view.findViewById(R.id.btn_edit_profile_save);

        ivEditProfilePreview = ivPic;

        SupabaseHelper.INSTANCE.getUserProfile(currentUserUid, profile -> {
            if (profile != null) {
                etUsername.setText(profile.getUsername());
                etBio.setText(profile.getBio());
                if (profile.getProfile_pic_url() != null && !profile.getProfile_pic_url().isEmpty()) {
                    Glide.with(this).load(profile.getProfile_pic_url()).circleCrop().into(ivPic);
                }
            }
            return null;
        });

        ivPic.setOnClickListener(v -> pickImage(100));

        btnSave.setOnClickListener(v -> {
            String newUsername = etUsername.getText().toString().trim();
            String newBio = etBio.getText().toString().trim();
            if (newUsername.isEmpty() || newBio.isEmpty()) {
                Toast.makeText(this, "Username and bio are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Saving profile...");
            pd.show();

            SupabaseHelper.INSTANCE.getUserProfile(currentUserUid, existing -> {
                String existingUrl = existing != null ? existing.getProfile_pic_url() : "";
                String pending = profilePicUri != null ? profilePicUri : "";

                // If user picked a local content:// image, upload it to Supabase Storage first.
                if (pending.startsWith("content://")) {
                    SupabaseHelper.INSTANCE.uploadProfilePicture(getContentResolver(), pending, uploadedUrl -> {
                        if (uploadedUrl == null) {
                            pd.dismiss();
                            Toast.makeText(this, "Could not read/upload selected photo (check permissions)", Toast.LENGTH_LONG).show();
                            return null;
                        }
                        String finalUrl = uploadedUrl != null ? uploadedUrl : existingUrl;
                        SupabaseHelper.INSTANCE.updateUserProfile(currentUserUid, newUsername, newBio, finalUrl, ok -> {
                            pd.dismiss();
                            if (ok) {
                                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                                profilePicUri = finalUrl;
                                loadUserProfile(currentUserUid);
                                dialog.dismiss();
                            } else {
                                Toast.makeText(this, "Could not update profile", Toast.LENGTH_SHORT).show();
                            }
                            return null;
                        });
                        return null;
                    });
                } else {
                    // Otherwise treat it as already-a-URL (or empty -> keep existing).
                    String finalUrl = !pending.isEmpty() ? pending : existingUrl;
                    SupabaseHelper.INSTANCE.updateUserProfile(currentUserUid, newUsername, newBio, finalUrl, ok -> {
                        pd.dismiss();
                        if (ok) {
                            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                            profilePicUri = finalUrl;
                            loadUserProfile(currentUserUid);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, "Could not update profile", Toast.LENGTH_SHORT).show();
                        }
                        return null;
                    });
                }
                return null;
            });
        });

        dialog.setContentView(view);
        dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        dialog.show();
    }

    private void loadUserProfile(String uid) {
        SupabaseHelper.INSTANCE.getUserProfile(uid, profile -> {
            if (profile != null) {
                tvProfileUsername.setText(profile.getUsername());
                tvProfileBio.setText(profile.getBio());
                if (profile.getProfile_pic_url() != null && !profile.getProfile_pic_url().isEmpty()) {
                    Glide.with(this).load(profile.getProfile_pic_url()).circleCrop().into(ivProfilePic);
                }
            } else {
                tvProfileUsername.setText("Username");
                tvProfileBio.setText("Tap edit profile to add a bio");
                ivProfilePic.setImageResource(android.R.drawable.ic_menu_gallery);
            }
            if (uid != null && uid.equals(currentUserUid)) {
                btnEditProfile.setVisibility(View.VISIBLE);
                btnActionFriend.setVisibility(View.GONE);
            } else {
                btnEditProfile.setVisibility(View.GONE);
                btnActionFriend.setVisibility(View.VISIBLE);
                SupabaseHelper.INSTANCE.isBefriended(currentUserUid, uid, befriended -> {
                    btnActionFriend.setText(befriended ? "Befriended" : "Befriend");
                    btnActionFriend.setEnabled(!befriended);
                    return null;
                });
            }
            return null;
        });
        
        SupabaseHelper.INSTANCE.getUserPosts(uid, posts -> {
            tvPostCount.setText(String.valueOf(posts.size()));
            userPosts.clear();
            userPosts.addAll(posts);
            filterUserPosts(profileTabs.getSelectedTabPosition());
            return null;
        });
    }

    private void loadNotifications() {
        // Migration to Supabase notifications table
    }

    private void showAddPostDialog() {
        if (map == null) return;
        LatLng center = map.getCameraPosition().target;
        uri1 = ""; uri2 = ""; uri3 = "";
        
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_post, null);
        
        EditText etTitle = view.findViewById(R.id.et_title);
        EditText etDesc = view.findViewById(R.id.et_description);
        EditText etHash = view.findViewById(R.id.et_hashtags);
        iv1 = view.findViewById(R.id.iv_preview1);
        iv2 = view.findViewById(R.id.iv_preview2);
        iv3 = view.findViewById(R.id.iv_preview3);
        MaterialSwitch cbIncludeLocation = view.findViewById(R.id.cb_include_location);
        Button btnSubmit = view.findViewById(R.id.btn_post_submit);
        Button btnCancel = view.findViewById(R.id.btn_post_cancel);
        
        iv1.setOnClickListener(v -> pickImage(1));
        iv2.setOnClickListener(v -> pickImage(2));
        iv3.setOnClickListener(v -> pickImage(3));
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String hashtags = etHash.getText().toString().trim();

            if (title.isEmpty() || desc.isEmpty() || (uri1.isEmpty() && uri2.isEmpty() && uri3.isEmpty())) {
                Toast.makeText(this, "Please add a title, description and at least one photo.", Toast.LENGTH_SHORT).show();
                return;
            }

            ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Uploading to Supabase...");
            pd.show();

            SupabaseHelper.INSTANCE.uploadPostImages(getContentResolver(), new String[]{uri1, uri2, uri3}, urls -> {
                Post newPost = new Post(currentUserUid, title, desc, hashtags, 
                        cbIncludeLocation.isChecked() ? center.getLatitude() : 0,
                        cbIncludeLocation.isChecked() ? center.getLongitude() : 0,
                        urls.get(0), urls.get(1), urls.get(2));
                
                SupabaseHelper.INSTANCE.savePost(newPost, success -> {
                    pd.dismiss();
                    if (success) {
                        loadPosts();
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "Post added!", Toast.LENGTH_SHORT).show();
                    }
                    return null;
                });
                return null;
            });
        });

        dialog.setContentView(view);
        dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        dialog.show();
    }

    private void pickImage(int imageIndex) {
        currentPickingImage = imageIndex;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    @Override
    public void onMapReady(@NonNull MapLibreMap mapLibreMap) {
        this.map = mapLibreMap;
        map.setStyle(new Style.Builder().fromUri("https://tiles.openfreemap.org/styles/liberty"), style -> {
            enableLocationComponent(style);
            displayMarkers();
        });
        
        map.addOnMapClickListener(point -> {
            for (Post p : allPosts) {
                if (p.latitude == 0 && p.longitude == 0) continue;
                float[] results = new float[1];
                android.location.Location.distanceBetween(point.getLatitude(), point.getLongitude(), p.latitude, p.longitude, results);
                if (results[0] < 50) { 
                    showPostPreview(p);
                    return true;
                }
            }
            return false;
        });
    }

    private void showPostPreview(Post post) {
        BottomSheetDialog previewDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.item_post, null);
        
        TextView tvTitle = view.findViewById(R.id.tv_post_title);
        TextView tvUser = view.findViewById(R.id.tv_username);
        ViewPager2 vp = view.findViewById(R.id.vp_images);
        
        tvTitle.setText(post.title);
        SupabaseHelper.INSTANCE.getUserProfile(post.user_uid, profile -> {
            if (profile != null) tvUser.setText(profile.getUsername());
            return null;
        });
        
        List<String> images = new ArrayList<>();
        if (post.uri1 != null && !post.uri1.isEmpty()) images.add(post.uri1);
        if (post.uri2 != null && !post.uri2.isEmpty()) images.add(post.uri2);
        if (post.uri3 != null && !post.uri3.isEmpty()) images.add(post.uri3);
        vp.setAdapter(new PostImageAdapter(images, null));
        
        previewDialog.setContentView(view);
        previewDialog.show();
    }

    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        map.getLocationComponent().activateLocationComponent(LocationComponentActivationOptions.builder(this, loadedMapStyle).build());
        map.getLocationComponent().setLocationComponentEnabled(true);
        map.getLocationComponent().setCameraMode(CameraMode.TRACKING);
        map.getLocationComponent().setRenderMode(RenderMode.COMPASS);
    }

    private void loadPosts() {
        SupabaseHelper.INSTANCE.getAllPosts(posts -> {
            allPosts.clear();
            allPosts.addAll(posts);
            postAdapter.setPosts(allPosts);
            displayMarkers();
            return null;
        });
    }

    private void displayMarkers() {
        if (map != null && map.getStyle() != null) {
            map.clear();
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_nature_pin);
            if (drawable == null) return;
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            Icon icon = IconFactory.getInstance(this).fromBitmap(bitmap);
            for (Post p : allPosts) {
                if (p.latitude == 0 && p.longitude == 0) continue;
                if (currentFilter.equals("All") || (p.hashtags != null && p.hashtags.toLowerCase().contains(currentFilter.toLowerCase()))) {
                    map.addMarker(new MarkerOptions().position(new LatLng(p.latitude, p.longitude)).title(p.title).snippet(p.description).icon(icon));
                }
            }
        }
    }

    @Override public void onStart() { super.onStart(); mapView.onStart(); }
    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause() { super.onPause(); mapView.onPause(); }
    @Override public void onStop() { super.onStop(); mapView.onStop(); }
    @Override public void onSaveInstanceState(Bundle outState) { super.onSaveInstanceState(outState); mapView.onSaveInstanceState(outState); }
    @Override public void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
}
