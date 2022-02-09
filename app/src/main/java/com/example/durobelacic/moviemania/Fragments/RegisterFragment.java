package com.example.durobelacic.moviemania.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.durobelacic.moviemania.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterFragment extends Fragment {

    private TextInputLayout inputEmail, inputPass;
    private TextInputEditText inputEditEmail, inputEditPass;
    private Button btnRegister, btnBackToLog;
    private FirebaseAuth firebaseAuth;

    public RegisterFragment() {
        // Required empty public constructor
    }

    public static RegisterFragment newInstance() {
        RegisterFragment fragment = new RegisterFragment();
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
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        firebaseAuth = FirebaseAuth.getInstance();

        inputEmail = view.findViewById(R.id.email);
        inputPass = view.findViewById(R.id.password);
        btnRegister = view.findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String sEmail = inputEmail.getEditText().getText().toString().trim();
            String sPassword = inputPass.getEditText().getText().toString().trim();
            System.out.println(sEmail);
            System.out.println(sPassword);

            firebaseAuth.createUserWithEmailAndPassword(sEmail, sPassword).addOnSuccessListener(authResult ->
                    Toast.makeText(getActivity(), "MoiveMania Account Created", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show());

        });

        btnBackToLog = view.findViewById(R.id.btnBackToLog);
        btnBackToLog.setOnClickListener(v -> {

            LoginFragment loginFragment = new LoginFragment();
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container_view, loginFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
    }
}