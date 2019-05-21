package test.android.orca.com.netapp.Controllers.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.squareup.picasso.Picasso;

import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import test.android.orca.com.netapp.Models.GithubUserInfo;
import test.android.orca.com.netapp.R;
import test.android.orca.com.netapp.Utils.GithubStreams;

public class DetailActivity extends AppCompatActivity {
    RequestManager glide ;
    ImageView ProfileImage ;
    TextView name ;
    private Disposable disposable;
    TextView following_number ;
    TextView followers_number ;
    TextView posts_number ;
    ImageView return_button ;
    TextView login_txt ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ImageView ProfileImage = findViewById(R.id.profile_pic);
        name = findViewById(R.id.profile_name);
        followers_number = findViewById(R.id.followers_number);
        following_number = findViewById(R.id.following_number);
        posts_number = findViewById(R.id.posts_number);
        return_button = findViewById(R.id.return_button);
        login_txt = findViewById(R.id.login);

        String url = getIntent().getStringExtra("image");

        Picasso.with(getApplicationContext()).load(url).into(ProfileImage);



        login_txt.append("@"+getIntent().getStringExtra("login"));

        executeHttpRequestWithRetrofit();


        return_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
    private void executeHttpRequestWithRetrofit(){
       this.disposable = GithubStreams.streamFetchUserInfos(getIntent().getStringExtra("login")).subscribeWith(new DisposableObserver<GithubUserInfo>() {
            @Override
            public void onNext(GithubUserInfo userInfo) {
                //updateUI(users);
                followers_number.setText(userInfo.getFollowers());
                following_number.setText(userInfo.getFollowing().toString());
                posts_number.setText(userInfo.getPublicGists().toString());
                name.setText(userInfo.getName());
            }

            @Override
            public void onError(Throwable e) {
                Log.e("TAG", "Error occurred.");
            }

            @Override
            public void onComplete() { }
        });
    }
}
