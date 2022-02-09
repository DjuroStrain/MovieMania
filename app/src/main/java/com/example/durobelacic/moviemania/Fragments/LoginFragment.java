package com.example.durobelacic.moviemania.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.durobelacic.moviemania.MovieManiaActivity;
import com.example.durobelacic.moviemania.R;
import com.example.durobelacic.moviemania.Utils.PageViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    private Button btnLogin, btnRegister;
    private TextInputLayout inputEmail, inputPass;
    private TextInputEditText inputEditEmail, inputEditPass;
    private FirebaseAuth firebaseAuth;
    private PageViewModel pageViewModel;

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){

        btnRegister = view.findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> {

            RegisterFragment registerFragment = new RegisterFragment();
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container_view, registerFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        firebaseAuth = FirebaseAuth.getInstance();

        inputEmail = view.findViewById(R.id.email);
        inputPass = view.findViewById(R.id.password);
        btnLogin = view.findViewById(R.id.btnLogin);

        pageViewModel = new ViewModelProvider(requireActivity()).get(PageViewModel.class);

        btnLogin.setOnClickListener(v -> {
            String sEmail = inputEmail.getEditText().getText().toString();
            String sPassword = inputPass.getEditText().getText().toString();
            firebaseAuth.signInWithEmailAndPassword(sEmail, sPassword).addOnSuccessListener(authResult -> {
                String welcomePage = String.format("Welcome "+authResult.getUser().getEmail());
                pageViewModel.setUser(authResult.getUser().getEmail());
                Toast.makeText(getActivity(), ""+welcomePage, Toast.LENGTH_SHORT).show();
                Intent MovieManiaIntent = new Intent(getActivity(), MovieManiaActivity.class);
                MovieManiaIntent.putExtra("user", authResult.getUser().getEmail());
                startActivity(MovieManiaIntent);
                getActivity().finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });
    }
}