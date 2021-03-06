/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.material.catalog.transition.hero;

import io.material.catalog.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import com.google.android.material.transition.Hold;
import com.google.android.material.transition.MaterialFadeThrough;
import com.google.android.material.transition.MaterialSharedAxis;
import io.material.catalog.transition.hero.AlbumsAdapter.AlbumAdapterListener;
import io.material.catalog.transition.hero.MusicData.Album;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A Fragment that hosts a toolbar and a child fragment with a list of music data. */
@TargetApi(VERSION_CODES.LOLLIPOP)
public class TransitionMusicLibraryDemoFragment extends Fragment
    implements AlbumAdapterListener, OnMenuItemClickListener {

  private static final int GRID_SPAN_COUNT = 2;

  private FrameLayout listContainer;

  private boolean listTypeGrid = true;
  private boolean listSorted = true;

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    // Use a Hold transition to keep this fragment visible beneath the MaterialContainerTransform
    // that transitions to the album details screen. Without a Hold, this fragment would disappear
    // as soon its container is replaced with a new Fragment.
    setExitTransition(new Hold());
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    return layoutInflater.inflate(
        R.layout.cat_transition_music_library_demo_fragment, viewGroup, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    Toolbar toolbar = view.findViewById(R.id.toolbar);
    listContainer = view.findViewById(R.id.list_container);
    toolbar.setOnMenuItemClickListener(this);
    MaterialSharedAxis sharedAxis =
        MaterialSharedAxis.create(MaterialSharedAxis.Z, true);
    setList(listTypeGrid, listSorted, sharedAxis);
  }

  @Override
  public void onAlbumClicked(View view, Album album) {
    TransitionMusicAlbumDemoFragment fragment =
        TransitionMusicAlbumDemoFragment.newInstance(album.id);

    getFragmentManager()
        .beginTransaction()
        .addSharedElement(view, ViewCompat.getTransitionName(view))
        .replace(R.id.fragment_container, fragment, TransitionMusicAlbumDemoFragment.TAG)
        .addToBackStack(TransitionMusicAlbumDemoFragment.TAG)
        .commit();
  }

  @Override
  public boolean onMenuItemClick(MenuItem menuItem) {
    int itemId = menuItem.getItemId();
    if (itemId == R.id.item_list_type) {
      // Use a fade through transition to swap between list item view types.
      MaterialFadeThrough fadeThrough = MaterialFadeThrough.create();
      setList(!listTypeGrid, listSorted, fadeThrough);
      return true;
    }

    // Use a shared axis Y transition to sort the list, showing a spacial relationship between
    // the outgoing and incoming view.
    MaterialSharedAxis sharedAxis =
        MaterialSharedAxis.create(MaterialSharedAxis.Y, true);
    setList(listTypeGrid, !listSorted, sharedAxis);
    return true;
  }

  /**
   * Add or replace the RecyclerView containing the list of albums with a new RecyclerView that is
   * either a list/grid and sorted/unsorted according to the given arguments.
   */
  private void setList(boolean listTypeGrid, boolean listSorted, Transition transition) {
    this.listTypeGrid = listTypeGrid;
    this.listSorted = listSorted;

    // Use a Transition to animate the removal and addition of the RecyclerViews.
    RecyclerView recyclerView = createRecyclerView(listTypeGrid);
    transition.addTarget(RecyclerView.class);
    TransitionManager.beginDelayedTransition(listContainer, transition);

    AlbumsAdapter adapter = new AlbumsAdapter(this, listTypeGrid);
    recyclerView.setAdapter(adapter);
    List<Album> albums = new ArrayList<>(MusicData.ALBUMS);
    if (!listSorted) {
      Collections.reverse(albums);
    }
    adapter.submitList(albums);

    listContainer.removeAllViews();
    listContainer.addView(recyclerView);
  }

  private RecyclerView createRecyclerView(boolean listTypeGrid) {
    Context context = requireContext();
    RecyclerView recyclerView = new RecyclerView(context);
    recyclerView.setLayoutParams(
        new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    int verticalPadding =
        context.getResources().getDimensionPixelSize(R.dimen.album_list_padding_vertical);
    recyclerView.setPadding(0, verticalPadding, 0, verticalPadding);
    recyclerView.setClipToPadding(false);
    if (listTypeGrid) {
      recyclerView.setLayoutManager(new GridLayoutManager(context, GRID_SPAN_COUNT));
    } else {
      recyclerView.setLayoutManager(new LinearLayoutManager(context));
      recyclerView.addItemDecoration(
          new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
    }
    return recyclerView;
  }
}
