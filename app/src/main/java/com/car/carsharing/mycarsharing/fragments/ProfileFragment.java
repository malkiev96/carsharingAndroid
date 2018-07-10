package com.car.carsharing.mycarsharing.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.car.carsharing.mycarsharing.R;
import com.car.carsharing.mycarsharing.model.Client;
import com.car.carsharing.mycarsharing.model.stat.ClientStatic;


public class ProfileFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private Client client;

    private TextView profileName;
    private TextView profileStatus;
    private TextView profilePhone;
    private TextView profileMail;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = ClientStatic.client;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        profileName = rootView.findViewById(R.id.profileName);
        profileStatus = rootView.findViewById(R.id.profileStatus);
        profilePhone = rootView.findViewById(R.id.profilePhone);
        profileMail = rootView.findViewById(R.id.profileMail);

        if (client.isEnabled() && client.isActivated()){
            String fio = client.getSecondname()+" "+client.getFirstname()+" "+client.getMiddlename();
            profileName.setText(fio);
        }

        profileStatus.setText("Подтвержден");
        profilePhone.setText(client.getTelephone());
        profileMail.setText(client.getMail());

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
