package fsu.mobile.group1.geohashing;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.transition.Slide;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.util.Log;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//This is the activity where we will base everything game related
//We will launch the MapsActivity Fragment from here as well as any other fragments needed to support the game
public class GameActivity extends AppCompatActivity implements GameUIFragment.UiListener, ListFragment.ListListener, WaitingFragment.WaitListener, themeFragment.ThemeListener {
    private String gameName;
    private String gameType;
    private Toolbar mToolbar;
    private FragmentManager mManager;
    private FragmentTransaction fragTransaction;
    private ArrayList<String> names = new ArrayList<String>();
    private GameUIFragment myGame;
    private ListFragment myList;
    private WaitingFragment myWait;
    private ViewGroup mRoot;
    private themeFragment theme;
    private Toolbar tools;

    // Access a Cloud Firestore instance from your Activity
    private FirebaseFirestore db;

    private GoogleSignInClient mGoogleSignInClient;
    LoginManager mFBLoginManager;
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        mToolbar = (Toolbar) findViewById(R.id.action_bar);
        gameName = "Brandon is so cool wow"; //tb removed later
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mFBLoginManager = LoginManager.getInstance();
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build();
        db.setFirestoreSettings(settings);
        renderUI();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.sign_out:
                signOut();
                return true;
            case R.id.theme:
                loadThemeSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void renderUI() {
        Slide mySlide = new Slide(Gravity.RIGHT);
        myGame = new GameUIFragment();
        mySlide.setStartDelay(10000);
        myGame.setEnterTransition(mySlide);
        mManager = getSupportFragmentManager();
        fragTransaction = mManager.beginTransaction();
        fragTransaction.add(R.id.ui_fragment, myGame, "ui");
        fragTransaction.commit();
    }

    //need to add database stuff here as well
    public void onCreateGame() {

        Slide exitFade= new Slide();        //used to load fade out the current fragment
        Slide enterFade = new Slide(Gravity.LEFT);
        enterFade.setStartDelay(500);
        Bundle bundle = new Bundle();

        myWait = new WaitingFragment();
        myWait.setEnterTransition(enterFade);
        myGame.setExitTransition(exitFade);
        mManager = getSupportFragmentManager();
        fragTransaction = mManager.beginTransaction();
        fragTransaction.replace(R.id.ui_fragment, myWait, "wait");
        fragTransaction.addToBackStack("to WaitingFragment");
        fragTransaction.commit();

    }

    public void onJoinGame() {
        getGames();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("list", names);
        myList = new ListFragment();
        myList.setArguments(bundle);
        mManager = getSupportFragmentManager();
        fragTransaction = mManager.beginTransaction();
        fragTransaction.addToBackStack(myGame.toString());
        fragTransaction.replace(R.id.ui_fragment, myList, "list_frag");
        fragTransaction.commit();
    }

    //retrieves and lists the current games that are available to join
    public void getGames() {
        names = new ArrayList<String>();
        Log.i("test", "Called function getGames()");
        Map<String, Object> asdf = new HashMap<>();
        asdf.put("blah", "blasdfasdf");
        db.collection("games").document("aasdfsdf").set(asdf);
        Log.i("ASDF ACTIVITY", db.collection("games").document("aasdfsdf").getId());
        db.collection("games")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //create a list of game names
                            Log.i("testing", "Task was successful inside of getGames function");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Games Data", document.getId() + "=>" + document.getData());
                                //add to list on each iteration
                                String gameName = document.getId();
                                names.add(gameName);
                            }
                            Bundle bundle = new Bundle();
                            bundle.putStringArrayList("list", names);
                            myList = new ListFragment();
                            myList.setArguments(bundle);
                            mManager = getSupportFragmentManager();
                            fragTransaction = mManager.beginTransaction();
                            fragTransaction.addToBackStack(myGame.toString());
                            fragTransaction.replace(R.id.ui_fragment, myList, "list_frag");
                            fragTransaction.commit();
                        }
                    }
                });

    }

    public void onGameSelected(String selection) {
        // TODO: take gameName and gameType from user choice/input if we use this to join
        gameName = "GameTest";
        gameType = "BattleRoyale";
        //Add the user to the selected game document and move them to the lobby
        Bundle data = new Bundle();
        String userType = "Join";
        data.putString("gameName", selection);
        data.putString("userType", userType);
        Intent intent = new Intent(GameActivity.this, MapsActivity.class);
        intent.putExtras(data);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    public void startGame(Bundle data) {

        gameName = data.getString("gameName");
        gameType = data.getString("gameType");
        // check to see if mapsactivity runs
        // Map entry for game type (1, 2, 3)
        // Number of points to win (currently set at 5)
        // How far away each node should be (currently set at 2 km)
        // nodes table
        // Winner entry
        db.collection("games").document(gameName);
        Map<String, Object> typeData = new HashMap<>();
        typeData.put("GameType", gameType);
        db.collection("games").document(gameName).collection("GameType").document("GameType").set(typeData);
        Map<String, Object> numPoints = new HashMap<>();
        numPoints.put("numPoints", data.getString("numPoints"));
        db.collection("games").document(gameName).collection("numPoints").document("num").set(numPoints);
        Map<String, Object> setDistance = new HashMap<>();
        setDistance.put("Distance", Double.parseDouble(data.getString("Radius")));
        db.collection("games").document(gameName).collection("distance").document("distance").set(setDistance);

        Intent intent = new Intent(GameActivity.this, MapsActivity.class);

        intent.putExtras(data);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());


    }
        public void signOut() {
            FirebaseAuth.getInstance().signOut();
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent = new Intent(GameActivity.this,
                                    MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    });
            mFBLoginManager.logOut();
        }

        public void loadThemeSettings(){
            theme=new themeFragment();
            mManager = getSupportFragmentManager();
            fragTransaction = mManager.beginTransaction();
            fragTransaction.replace(R.id.ui_fragment, theme, "wait");
            fragTransaction.addToBackStack(this.toString());
            fragTransaction.commit();
        }

        public void reloadTheme(){
            mManager = getSupportFragmentManager();
            mManager.popBackStack();
        }

        public void toolbarChange(String color) {

            if (color.equals("Red")) {
                mToolbar.setBackground(new ColorDrawable(Color.parseColor("#750505")));

            }
            else if(color.equals("Dark")){
                 mToolbar.setBackground(new ColorDrawable(Color.parseColor("#262626")));

            }
            else if(color.equals("Blue")){
                 mToolbar.setBackground(new ColorDrawable(Color.parseColor("#ff33b5e5")));

            }
            else if (color.equals("Green")){
                 mToolbar.setBackground(new ColorDrawable(Color.parseColor("#206b0d")));

            }
        }

        public void home(){
        myGame= new GameUIFragment();
            mManager = getSupportFragmentManager();
            fragTransaction = mManager.beginTransaction();
            fragTransaction.remove(theme);
            fragTransaction.add(R.id.ui_fragment, myGame, "game2");
            fragTransaction.commit();
        }
}


