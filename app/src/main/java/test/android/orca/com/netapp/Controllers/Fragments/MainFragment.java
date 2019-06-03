package  test.android.orca.com.netapp.Controllers.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import test.android.orca.com.netapp.Controllers.Activities.DetailActivity;
import test.android.orca.com.netapp.Models.GithubUser;
import test.android.orca.com.netapp.R;
import test.android.orca.com.netapp.Utils.GithubStreams;
import test.android.orca.com.netapp.Utils.ItemClickSupport;
import test.android.orca.com.netapp.Views.GithubUserAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements GithubUserAdapter.Listener {

    // FOR DESIGN
    @BindView(R.id.fragment_main_recycler_view) RecyclerView recyclerView;
    @BindView(R.id.fragment_main_swipe_container) SwipeRefreshLayout swipeRefreshLayout;

    //FOR DATA
    private Disposable disposable;
    private List<GithubUser> githubUsers;
    private GithubUserAdapter adapter;

    EditText recherche ;
    Button btn ;

    public MainFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);
        recherche = (EditText) view.findViewById(R.id.research) ;
        btn = (Button) view.findViewById(R.id.btn) ;

        this.configureRecyclerView();
        this.configureSwipeRefreshLayout();
        this.configureOnClickRecyclerView();
        this.executeHttpRequestWithRetrofit();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.disposeWhenDestroy();
    }

    // -----------------
    // ACTION
    // -----------------

    private void configureOnClickRecyclerView(){
        ItemClickSupport.addTo(recyclerView, R.layout.fragment_main_item)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        GithubUser user = adapter.getUser(position);
                        startActivity(new Intent(getActivity(), DetailActivity.class).putExtra("image",user.getAvatarUrl()).putExtra("login",user.getLogin()));
                    }
                });
    }

    @Override
    public void onClickDeleteButton(int position) {
        GithubUser user = adapter.getUser(position);
        Toast.makeText(getContext(), "You are trying to delete user : "+user.getLogin(), Toast.LENGTH_SHORT).show();
    }

    // -----------------
    // CONFIGURATION
    // -----------------

    private void configureRecyclerView(){
        this.githubUsers = new ArrayList<>();
        // Create adapter passing in the sample user data
        this.adapter = new GithubUserAdapter(this.githubUsers, Glide.with(this), this);
        // Attach the adapter to the recyclerview to populate items
        this.recyclerView.setAdapter(this.adapter);
        // Set layout manager to position the items
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void configureSwipeRefreshLayout(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                executeHttpRequestWithRetrofit();
            }
        });
    }

    // -------------------
    // HTTP (RxJAVA)
    // -------------------

    private void executeHttpRequestWithRetrofit(){
        this.disposable = GithubStreams.streamFetchUserFollowing("JakeWharton").subscribeWith(new DisposableObserver<List<GithubUser>>() {
            @Override
            public void onNext(List<GithubUser> users) {
                updateUI(users);

            }

            @Override
            public void onError(Throwable e) { }

            @Override
            public void onComplete() { }
        });
    }

    private void disposeWhenDestroy(){
        if (this.disposable != null && !this.disposable.isDisposed()) this.disposable.dispose();
    }

    // -------------------
    // UPDATE UI
    // -------------------

    private void updateUI(final List<GithubUser> users){

        githubUsers.clear();
        githubUsers.addAll(users);
        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
        recherche.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = charSequence.toString() ;
                if(str.equals("")){
                    githubUsers.clear();
                    githubUsers.addAll(users);
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
                else{
                    List<GithubUser> copy_user = new ArrayList<GithubUser>();
                    Iterator<GithubUser> itr = users.iterator() ;
                    while (itr.hasNext()){
                        String login = itr.next().getLogin().toLowerCase() ;
                        if(login.contains(str.toLowerCase())){
                            copy_user.add(itr.next()) ;
                        }
                    }
                    if(copy_user!=null&&copy_user.size()!=0){
                        githubUsers.clear();
                        githubUsers.addAll(copy_user);
                        adapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                        copy_user = new ArrayList<GithubUser>() ;
                    }

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}
