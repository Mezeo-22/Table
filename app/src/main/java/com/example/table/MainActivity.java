package com.example.table;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.table.adapter.PostAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView nav_view;
    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;
    private TextView userEmail;
    private AlertDialog dialog;
    private Toolbar toolbar;
    private FloatingActionButton fb;
    private PostAdapter.OnItemClickCustom onItemClickCustom;
    private RecyclerView rcView;
    private PostAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        setOnItemClickCustom();
        rcView = findViewById(R.id.rcView);
        rcView.setLayoutManager(new LinearLayoutManager(this));

        List<NewPost> arrayPost = new ArrayList<>();

        postAdapter = new PostAdapter(arrayPost, this, onItemClickCustom);
        rcView.setAdapter(postAdapter);

        fb = findViewById(R.id.floatingActionButton2);
        fb.setOnClickListener(view -> {
            edit();
        });
        nav_view = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.drawerLayout);

        toolbar = findViewById(R.id.toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,toolbar,
                R.string.toggle_open,R.string.toggle_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        nav_view.setNavigationItemSelectedListener(this);
        userEmail = nav_view.getHeaderView(0).findViewById(R.id.tvEmail);

        mAuth = FirebaseAuth.getInstance();
    }

    private void setOnItemClickCustom() {
        onItemClickCustom = new PostAdapter.OnItemClickCustom() {
            @Override
            public void onItemSelected(int position) {
                Log.d("MyLog", "position: " +position);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        getUserData();
    }

    public void edit() {
        Intent intent = new Intent(MainActivity.this, EditActivity.class);
        startActivity(intent);
    }

    private void getUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userEmail.setText(currentUser.getEmail());
        } else {
            userEmail.setText(R.string.sign_in_or_sign_up);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.id_my_ads:
                Toast.makeText(this, "Pressed id my ads", Toast.LENGTH_SHORT).show();
                break;
            case R.id.id_cars_ads:
                Toast.makeText(this, "Pressed id cars ads", Toast.LENGTH_SHORT).show();
                break;
            case R.id.id_pc_ads:
                Toast.makeText(this, "Pressed id pc ads", Toast.LENGTH_SHORT).show();
                break;
            case R.id.id_smartphone_ads:
                Toast.makeText(this, "Pressed id smartphone ads", Toast.LENGTH_SHORT).show();
                break;
            case R.id.id_dm_ads:
                Toast.makeText(this, "Pressed id dm ads", Toast.LENGTH_SHORT).show();
                break;
            case R.id.id_sign_up:
                signupdialog(R.string.sign_up, R.string.sign_up_button, 0);
                Toast.makeText(this, "Pressed id sign up", Toast.LENGTH_SHORT).show();
                break;
            case R.id.id_sign_in:
                signupdialog(R.string.sign_in, R.string.sign_in_button, 1);
                Toast.makeText(this, "Pressed id sign in", Toast.LENGTH_SHORT).show();
                break;
            case R.id.id_sign_out:
                signOut();
                Toast.makeText(this, "Pressed id sign out", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    private void signupdialog(int title, int buttontitle, int index) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.sign_up_layout, null);
        dialogBuilder.setView(dialogView);
        TextView titleTextView = dialogView.findViewById(R.id.tvAlertTitle);
        EditText edEmail = dialogView.findViewById(R.id.edEmail);
        EditText edPassword = dialogView.findViewById(R.id.edPassword);
        titleTextView.setText(title);
        Button b = dialogView.findViewById(R.id.buttonSignUp);
        b.setText(buttontitle);
        b.setOnClickListener(v -> {
            if (index==0) {
                signUp(edEmail.getText().toString(), edPassword.getText().toString());
            } else {
                signin(edEmail.getText().toString(), edPassword.getText().toString());
            }
            dialog.dismiss();
        });
        dialog = dialogBuilder.create();
        dialog.show();
    }

    private void signUp (String email, String password) {
        if (!email.equals("") && !password.equals("")) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                getUserData();
                            } else {
                                Log.w("MyLogMain", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "Authentification failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "Email или Password пустой", Toast.LENGTH_SHORT).show();
        }
    }

    private void signin (String email, String password) {
        if (!email.equals("") && !password.equals("")) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                Log.d("MyLogMain", "signInWithEmail:success");
                                getUserData();
                            } else {
                                Log.w("MyLogmain", "signInWithEmail:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "Authorization failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "Email или Password пустой", Toast.LENGTH_SHORT).show();
        }
    }

    private void signOut () {
        mAuth.signOut();
        getUserData();
    }
}